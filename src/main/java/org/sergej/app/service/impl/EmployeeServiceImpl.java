package org.sergej.app.service.impl;

import org.sergej.app.exception.EmployeeNotFoundException;
import org.sergej.app.model.Employee;
import org.sergej.app.service.EmployeeService;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeServiceImpl implements EmployeeService {

    @Override
    public Employee getEmployeeById(int id, List<Employee> employees) {
        for (Employee employee : employees) {
            if (employee.getId() == id) {
                return employee;
            }
        }
        throw new EmployeeNotFoundException("Employee with id " + id + " not found!");
    }

    @Override
    public List<Employee> getEmployeesBySalaryGreaterThan(int targetSalary,
                                                          List<Employee> employees) {
        return  employees.stream()
                .filter(e -> e.getSalary() > targetSalary)
                .toList();
    }

    @Override
    public Map<String, Employee> getEmployeeMap(List<Employee> employees) {
        Map<String, Employee> employeeMap = new HashMap<>();
        for (Employee employee : employees) {
            String key = "id" + employee.getId();
            employeeMap.put(key, employee);
        }
        return Collections.unmodifiableMap(employeeMap);
    }

}
