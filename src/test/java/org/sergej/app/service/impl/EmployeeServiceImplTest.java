package org.sergej.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sergej.app.exception.EmployeeNotFoundException;
import org.sergej.app.model.Employee;
import org.sergej.app.service.EmployeeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceImplTest {

    private EmployeeService service;
    private List<Employee> employees;

    @BeforeEach
    void setUp() {
        service = new EmployeeServiceImpl();

        employees = Arrays.asList(
                new Employee(1, "Василиса", "Петровна", 50000),
                new Employee(2, "Василий", "Николаевич", 75000),
                new Employee(3, "Вадим", "Кузнецов", 60000),
                new Employee(4, "Диана", "Сергеевна", 90000)
        );
    }

    @Test
    @DisplayName("getEmployeeById: должен вернуть сотрудника по существующему ID")
    void getEmployeeById_ExistingId_ReturnsEmployee() {
        Employee result = service.getEmployeeById(2, employees);

        assertEquals(2, result.getId());
        assertEquals("Василий", result.getFirstName());
        assertEquals(75000, result.getSalary());
    }

    @Test
    @DisplayName("getEmployeeById: должен бросить исключение при несуществующем ID")
    void getEmployeeById_NonExistingId_ThrowsException() {
        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> service.getEmployeeById(999, employees)
        );

        assertEquals("Employee with id 999 not found!", exception.getMessage());
    }

    @Test
    @DisplayName("getEmployeeById: должен бросить исключение при пустом списке")
    void getEmployeeById_EmptyList_ThrowsException() {
        List<Employee> emptyList = Collections.emptyList();

        EmployeeNotFoundException exception = assertThrows(
                EmployeeNotFoundException.class,
                () -> service.getEmployeeById(1, emptyList)
        );

        assertEquals("Employee with id 1 not found!", exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "70000, 2",
            "60000, 2",
            "90000, 0",
            "50000, 3"
    })
    @DisplayName("getEmployeesBySalaryGreaterThan: фильтрация по зарплате")
    void getEmployeesBySalaryGreaterThan_ReturnsCorrectCount(
            int targetSalary, int expectedCount) {

        List<Employee> result = service.getEmployeesBySalaryGreaterThan(targetSalary, employees);

        assertEquals(expectedCount, result.size());
    }

    @Test
    @DisplayName("getEmployeesBySalaryGreaterThan: возвращает пустой список при отсутствии подходящих")
    void getEmployeesBySalaryGreaterThan_NoMatches_ReturnsEmptyList() {
        List<Employee> result = service.getEmployeesBySalaryGreaterThan(100000, employees);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getEmployeesBySalaryGreaterThan: возвращает всех при низком пороге")
    void getEmployeesBySalaryGreaterThan_LowThreshold_ReturnsAll() {
        List<Employee> result = service.getEmployeesBySalaryGreaterThan(0, employees);
        assertEquals(employees.size(), result.size());
    }

    @Test
    @DisplayName("getEmployeesBySalaryGreaterThan: не изменяет исходный список")
    void getEmployeesBySalaryGreaterThan_DoesNotModifyOriginalList() {
        List<Employee> original = new ArrayList<>(employees);
        service.getEmployeesBySalaryGreaterThan(60000, employees);

        assertEquals(original, employees);
    }

    @Test
    @DisplayName("getEmployeeMap: создаёт корректную карту по ID")
    void getEmployeeMap_CreatesCorrectMap() {
        Map<String, Employee> map = service.getEmployeeMap(employees);

        assertEquals(4, map.size());
        assertTrue(map.containsKey("id1"));
        assertTrue(map.containsKey("id2"));
        assertEquals("Василиса", map.get("id1").getFirstName());
        assertEquals(90000, map.get("id4").getSalary());
    }

    @Test
    @DisplayName("getEmployeeMap: пустой список → пустая карта")
    void getEmployeeMap_EmptyList_ReturnsEmptyMap() {
        Map<String, Employee> map = service.getEmployeeMap(Collections.emptyList());
        assertTrue(map.isEmpty());
    }

    @Test
    @DisplayName("getEmployeeMap: карту нельзя изменить")
    void getEmployeeMap_ReturnsMutableMap() {
        Map<String, Employee> map = service.getEmployeeMap(employees);
        assertThrows(UnsupportedOperationException.class,
                () -> map.put("id5", new Employee(5, "Илья", "Иванов", 80000)));
        assertEquals(4, map.size());
    }

}