package client.ui;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

public class BarcodeScannerDialog extends Stage {
    private FrameGrabber grabber;
    private ImageView imageView = new ImageView();
    private AtomicBoolean isScanning = new AtomicBoolean(true);
    private Consumer<String> onCodeScanned;
    private Thread scannerThread;

    public BarcodeScannerDialog(Consumer<String> onCodeScanned) {
        this.onCodeScanned = onCodeScanned;
        setTitle("Quét mã Barcode / QR Sản Phẩm");
        initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new javafx.geometry.Insets(20));

        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);

        Label lblStatus = new Label("Đang khởi động camera...");
        javafx.scene.control.TextField txtManualCode = new javafx.scene.control.TextField();
        txtManualCode.setPromptText("Nhập mã sản phẩm nếu camera lỗi");
        txtManualCode.setMaxWidth(280);
        Button btnAddByCode = new Button("Thêm theo mã");
        btnAddByCode.setOnAction(e -> {
            String code = txtManualCode.getText() == null ? "" : txtManualCode.getText().trim();
            if (!code.isEmpty()) {
                onCodeScanned.accept(code);
                close();
            }
        });

        javafx.scene.layout.HBox manualBox = new javafx.scene.layout.HBox(8, txtManualCode, btnAddByCode);
        manualBox.setAlignment(Pos.CENTER);
        Button btnDecodeFile = new Button("Chọn ảnh QR");
        btnDecodeFile.setOnAction(e -> decodeFromImageFile());

        Button btnDecodeClipboard = new Button("Dán ảnh từ Clipboard");
        btnDecodeClipboard.setOnAction(e -> decodeFromClipboardImage());

        javafx.scene.layout.HBox quickActions = new javafx.scene.layout.HBox(8, btnDecodeFile, btnDecodeClipboard);
        quickActions.setAlignment(Pos.CENTER);

        Button btnCancel = new Button("Hủy bỏ");
        btnCancel.setOnAction(e -> close());

        root.getChildren().addAll(
                new Label("Đưa mã barcode sản phẩm vào trước camera"),
                imageView,
                lblStatus,
                quickActions,
                manualBox,
                btnCancel
        );

        Scene scene = new Scene(root, 500, 560);
        setScene(scene);

        setOnCloseRequest(e -> stopCamera());

        startCamera(lblStatus);
    }

    private void startCamera(Label lblStatus) {
        scannerThread = new Thread(() -> {
            Java2DFrameConverter converter = new Java2DFrameConverter();
            try {
                CountDownLatch initLatch = new CountDownLatch(1);
                AtomicReference<Throwable> initError = new AtomicReference<>();
                Platform.runLater(() -> {
                    try {
                        String os = System.getProperty("os.name", "").toLowerCase();
                        if (os.contains("mac")) {
                            Throwable last = null;
                            String[] devices = new String[] {"0:none", "default:none", "0"};
                            for (String device : devices) {
                                FFmpegFrameGrabber macGrabber = new FFmpegFrameGrabber(device);
                                macGrabber.setFormat("avfoundation");
                                macGrabber.setImageWidth(640);
                                macGrabber.setImageHeight(480);
                                macGrabber.setFrameRate(30.0);
                                macGrabber.setOption("framerate", "30");
                                try {
                                    macGrabber.start();
                                    grabber = macGrabber;
                                    last = null;
                                    break;
                                } catch (Throwable ex) {
                                    last = ex;
                                    try {
                                        macGrabber.release();
                                    } catch (Exception ignored) {}
                                }
                            }
                            if (grabber == null) {
                                throw last != null ? last : new RuntimeException("Không mở được camera AVFoundation.");
                            }
                        } else {
                            OpenCVFrameGrabber cvGrabber = new OpenCVFrameGrabber(0);
                            cvGrabber.setImageWidth(640);
                            cvGrabber.setImageHeight(480);
                            grabber = cvGrabber;
                            grabber.start();
                        }
                    } catch (Throwable e) {
                        initError.set(e);
                    } finally {
                        initLatch.countDown();
                    }
                });
                initLatch.await();
                if (initError.get() != null) {
                    throw initError.get();
                }

                Platform.runLater(() -> lblStatus.setText("Đang quét..."));

                while (isScanning.get()) {
                    Frame frame = grabber.grab();
                    if (frame == null) {
                        Thread.sleep(80);
                        continue;
                    }

                    BufferedImage image = converter.convert(frame);
                    if (image == null) {
                        Thread.sleep(80);
                        continue;
                    }

                    WritableImage fxImage = SwingFXUtils.toFXImage(image, null);
                    Platform.runLater(() -> imageView.setImage(fxImage));

                    try {
                        LuminanceSource source = new BufferedImageLuminanceSource(image);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                        Result result = new MultiFormatReader().decode(bitmap);
                        if (result != null && result.getText() != null && !result.getText().isBlank()) {
                            String code = result.getText().trim();
                            isScanning.set(false);
                            Platform.runLater(() -> {
                                onCodeScanned.accept(code);
                                close();
                            });
                            break;
                        }
                    } catch (NotFoundException ignore) {
                        // Keep scanning
                    }

                    Thread.sleep(80);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                Platform.runLater(() -> lblStatus.setText("Lỗi camera: " + msg + ". Dùng nhập tay/ảnh QR bên dưới."));
            } finally {
                converter.close();
                stopCamera();
            }
        });
        scannerThread.setDaemon(true);
        scannerThread.start();
    }

    private void stopCamera() {
        isScanning.set(false);
        if (grabber != null) {
            try {
                grabber.stop();
            } catch (Exception ignored) {}
            try {
                grabber.release();
            } catch (Exception ignored) {}
            grabber = null;
        }
    }

    private void decodeFromImageFile() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Chọn ảnh QR/Barcode");
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
            );
            File file = chooser.showOpenDialog(this);
            if (file == null) return;

            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                showWarn("Không đọc được ảnh.");
                return;
            }
            String code = tryDecode(image);
            if (code == null || code.isBlank()) {
                showWarn("Không tìm thấy QR/Barcode trong ảnh.");
                return;
            }
            onCodeScanned.accept(code.trim());
            close();
        } catch (Exception ex) {
            showWarn("Lỗi đọc ảnh: " + ex.getMessage());
        }
    }

    private void decodeFromClipboardImage() {
        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (!clipboard.hasImage()) {
                showWarn("Clipboard chưa có ảnh.");
                return;
            }
            BufferedImage image = SwingFXUtils.fromFXImage(clipboard.getImage(), null);
            if (image == null) {
                showWarn("Không đọc được ảnh từ clipboard.");
                return;
            }
            String code = tryDecode(image);
            if (code == null || code.isBlank()) {
                showWarn("Không tìm thấy QR/Barcode trong ảnh clipboard.");
                return;
            }
            onCodeScanned.accept(code.trim());
            close();
        } catch (Exception ex) {
            showWarn("Lỗi clipboard: " + ex.getMessage());
        }
    }

    private String tryDecode(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            Result result = new MultiFormatReader().decode(bitmap, hints);
            return result == null ? null : result.getText();
        } catch (Exception ignore) {
            return null;
        }
    }

    private void showWarn(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setTitle("Quét mã");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
