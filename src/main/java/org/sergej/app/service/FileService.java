package org.sergej.app.service;

import org.sergej.app.model.Employee;

import java.util.List;

public interface FileService {

    void saveEmployeesToFile(List<Employee> employees, String filename);

    List<Employee> loadEmployeesFromFile(String filename);

}
