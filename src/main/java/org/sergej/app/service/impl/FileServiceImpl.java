package org.sergej.app.service.impl;

import org.sergej.app.exception.FileLoadException;
import org.sergej.app.model.Employee;
import org.sergej.app.service.FileService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileServiceImpl implements FileService {

    @Override
    public void saveEmployeesToFile(List<Employee> employees, String filename) {
        if (!isFilenameValid(filename)) {
            throw new IllegalArgumentException("The specified file name is invalid!");
        }

        if (!filename.endsWith(".txt")) {
            filename += ".txt";
        }

        try (FileWriter writer = new FileWriter(filename)) {
            writeEmployees(writer, employees);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Employee> loadEmployeesFromFile(String filename) {
        if (!isFilenameValid(filename)) {
            throw new IllegalArgumentException("The specified file name is invalid!");
        }

        filename = filename.trim();
        if (!filename.endsWith(".txt")) {
            filename += ".txt";
        }

        List<Employee> employees = new ArrayList<>();
        File file = new File(filename);

        if (!file.exists()) {
            throw new FileLoadException("File not found: " + filename);
        }

        try {
            loadEmployeesFromFile(employees, file);
        } catch (IOException e) {
            throw new FileLoadException("Failed to read file: " + filename, e);
        }

        return employees;
    }

    private void writeEmployees(FileWriter writer, List<Employee> employees) throws IOException {
        for (Employee employee : employees) {
            String employeeString = employee.getId() + "," +
                    employee.getFirstName() + "," +
                    employee.getLastName() + "," +
                    employee.getSalary() + "\n";
            writer.write(employeeString);
        }
    }

    private void loadEmployeesFromFile(List<Employee> employees, File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                try {
                    Employee employee = parseEmployee(line);
                    employees.add(employee);
                } catch (Exception ex) {
                    System.err.printf("Error parsing line %d: '%s' — %s. Skipping.%n",
                            lineNumber, line, ex.getMessage());
                }
            }
        }
    }

    private boolean isFilenameValid(String filename) {
        return filename != null && !filename.isEmpty();
    }

    private Employee parseEmployee(String line) {
        String[] parts = line.split(",", -1);

        if (parts.length != 4) {
            throw new IllegalArgumentException("Expected 4 fields, but got " + parts.length);
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String firstName = parts[1].trim();
            String lastName = parts[2].trim();
            int salary = Integer.parseInt(parts[3].trim());

            if (firstName.isEmpty() || lastName.isEmpty()) {
                throw new IllegalArgumentException("First name or last name is empty");
            }

            if (salary < 0) {
                throw new IllegalArgumentException("Salary cannot be negative");
            }

            return new Employee(id, firstName, lastName, salary);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + e.getMessage());
        }
    }

}
