import entities.NhanVien;
import service.EmployeeService;
import service.impl.EmployeeServiceImpl;

import java.util.List;

public class Runner {
    public static void main(String[] args) {
        try {
            System.out.println("--- Testing Pharmacy Backend Connection ---");
            
            EmployeeService employeeService = new EmployeeServiceImpl();
            List<NhanVien> nhanViens = employeeService.getAll();
            
            System.out.println("Connected successfully!");
            System.out.println("Total Employees in database: " + nhanViens.size());
            
            if (!nhanViens.isEmpty()) {
                System.out.println("Sample Employee: " + nhanViens.get(0).getHoTen());
            } else {
                System.out.println("No employees found. Please run SampleData to seed the database.");
            }
            
            System.out.println("--- Test Finished ---");
        } catch (Exception e) {
            System.err.println("Error during execution:");
            e.printStackTrace();
        }
    }
}
