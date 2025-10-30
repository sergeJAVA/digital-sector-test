package org.sergej.app;

import org.sergej.app.exception.EmployeeNotFoundException;
import org.sergej.app.model.Employee;
import org.sergej.app.service.EmployeeService;
import org.sergej.app.service.FileService;
import org.sergej.app.service.impl.EmployeeServiceImpl;
import org.sergej.app.service.impl.FileServiceImpl;

import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee(1, "Иван", "Петров", 50000),
                new Employee(2, "Илья", "Иванов", 75000),
                new Employee(3, "Анна", "Ивановна", 35000),
                new Employee(4, "Ангелина", "Николаевна", 51000),
                new Employee(5, "Василий", "Сергеевич", 21000),
                new Employee(5, "Cергей", "Николаевич", 40000)
        );

        EmployeeService employeeService = new EmployeeServiceImpl();
        FileService fileService = new FileServiceImpl();

        Employee employee4 = employeeService.getEmployeeById(4, employees);
        System.out.println('\n' + "Найден employee по id 4: " + employee4);
        System.out.println("---------------------------------------" + '\n');

        List<Employee> filteredEmployees = employeeService.getEmployeesBySalaryGreaterThan(40000, employees);
        System.out.println("Список работников, у которых зарплата больше 40к:");
        printEmployees(filteredEmployees);
        System.out.println("---------------------------------------" + '\n');

        fileService.saveEmployeesToFile(filteredEmployees, "salaryGreater40k");
        List<Employee> loadedEmployees = fileService.loadEmployeesFromFile("salaryGreater40k");
        System.out.println("Список работников, полученных из файла:");
        printEmployees(loadedEmployees);
        System.out.println("---------------------------------------" + '\n');

        System.out.println("Список загруженных работников в виде ключ-значение:");
        printEmployeesMap(employeeService.getEmployeeMap(loadedEmployees));
        System.out.println("---------------------------------------" + '\n');

        System.out.println("Поиск несуществующего работника приводит к исключению: ");
        try {
            employeeService.getEmployeeById(999, employees);
        }catch (EmployeeNotFoundException e) {
            System.out.println("Exception message: " + e.getMessage());
        }
        System.out.println("---------------------------------------");

    }

    private static void printEmployees(List<Employee> employees) {
        for (Employee employee : employees) {
            System.out.println(employee);
        }
    }

    private static void printEmployeesMap(Map<String, Employee> employees) {
        for (Map.Entry<String, Employee> entry : employees.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }

}
