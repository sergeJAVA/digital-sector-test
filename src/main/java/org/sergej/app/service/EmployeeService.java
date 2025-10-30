package org.sergej.app.service;

import org.sergej.app.model.Employee;

import java.util.List;
import java.util.Map;

public interface EmployeeService {

    Employee getEmployeeById(int id, List<Employee> employees);

    List<Employee> getEmployeesBySalaryGreaterThan(int targetSalary, List<Employee> employees);

    Map<String, Employee> getEmployeeMap(List<Employee> employees);

}
