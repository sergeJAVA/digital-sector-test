package org.sergej.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sergej.app.exception.FileLoadException;
import org.sergej.app.model.Employee;
import org.sergej.app.service.FileService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceImplTest {

    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl();
    }

    @Test
    @DisplayName("saveEmployeesToFile: сохраняет сотрудников в файл с .txt")
    void saveEmployeesToFile_AppendsTxtAndSavesCorrectly() throws IOException {
        String filename = "employees";
        Path filePath = tempDir.resolve(filename + ".txt");

        List<Employee> employees = List.of(
                new Employee(1, "Диана", "Сергеевна", 50000),
                new Employee(2, "Василий", "Николаевич", 75000)
        );

        String path = pathInTempDir(filename);

        fileService.saveEmployeesToFile(employees, path);

        assertTrue(Files.exists(filePath));

        List<String> lines = Files.readAllLines(filePath);
        assertEquals(2, lines.size());
        assertEquals("1,Диана,Сергеевна,50000", lines.get(0));
        assertEquals("2,Василий,Николаевич,75000", lines.get(1));
    }

    @Test
    @DisplayName("saveEmployeesToFile: не добавляет .txt, если уже есть")
    void saveEmployeesToFile_DoesNotDuplicateTxt() throws IOException {
        List<Employee> employees = List.of(new Employee(1, "A", "B", 100));

        String filename = "data.txt";
        Path filePath = tempDir.resolve(filename);

        fileService.saveEmployeesToFile(employees, filePath.toAbsolutePath().toString());

        assertTrue(Files.exists(filePath));
        assertEquals("1,A,B,100", Files.readString(filePath).trim());
    }

    @Test
    @DisplayName("saveEmployeesToFile: бросает IllegalArgumentException при null")
    void saveEmployeesToFile_NullFilename_ThrowsIllegalArgumentException() {
        List<Employee> employees = List.of(new Employee(1, "X", "Y", 100));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fileService.saveEmployeesToFile(employees, null)
        );

        assertEquals("The specified file name is invalid!", ex.getMessage());
    }

    @Test
    @DisplayName("saveEmployeesToFile: бросает IllegalArgumentException при пустой строке")
    void saveEmployeesToFile_EmptyFilename_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.saveEmployeesToFile(List.of(), ""));
    }

    @Test
    @DisplayName("saveEmployeesToFile: бросает RuntimeException при ошибке записи")
    void saveEmployeesToFile_WriteError_ThrowsRuntimeException() {
        List<Employee> employees = List.of(new Employee(1, "X", "Y", 100));
        String filename = "/invalid/path/employees.txt";

        assertThrows(RuntimeException.class,
                () -> fileService.saveEmployeesToFile(employees, filename));
    }

    @Test
    @DisplayName("loadEmployeesFromFile: загружает корректный файл")
    void loadEmployeesFromFile_ValidFile_ReturnsEmployeeList() throws IOException {
        Path filePath = tempDir.resolve("data.txt");
        Files.write(filePath, List.of(
                "1,Диана,Сергеевна,50000",
                "2,Василий,Николаевич,75000"
        ));

        String filename = filePath.toAbsolutePath().toString();
        List<Employee> result = fileService.loadEmployeesFromFile(filename);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Диана", result.get(0).getFirstName());
        assertEquals(75000, result.get(1).getSalary());
    }

    @Test
    @DisplayName("loadEmployeesFromFile: добавляет .txt, если отсутствует")
    void loadEmployeesFromFile_AppendsTxtIfMissing() throws IOException {
        Path filePath = tempDir.resolve("test.txt");
        Files.writeString(filePath, "1,Джо,Форд,60000");

        String filename = pathInTempDir("test");
        List<Employee> result = fileService.loadEmployeesFromFile(filename);

        assertEquals(1, result.size());
        assertEquals("Джо", result.getFirst().getFirstName());
    }

    @Test
    @DisplayName("loadEmployeesFromFile: бросает FileLoadException, если файл не существует")
    void loadEmployeesFromFile_NonExistingFile_ThrowsFileLoadException() {
        FileLoadException ex = assertThrows(
                FileLoadException.class,
                () -> fileService.loadEmployeesFromFile("nonexistent.txt")
        );

        assertTrue(ex.getMessage().contains("File not found"));
    }

    @Test
    @DisplayName("loadEmployeesFromFile: пропускает некорректные строки и логирует")
    void loadEmployeesFromFile_SkipsInvalidLines_AndLogsToErr() throws IOException {
        Path filePath = tempDir.resolve("mixed.txt");
        Files.write(filePath, List.of(
                "1,Василиса,Николаевна,50000",
                "invalid,line,too,many,fields",
                "2,Сергей,,70000",  // пустой lastName
                "3,Вадим,Курочкин,abc",  // не число в зарплате
                "4,Алла,Иванова,-100"  // отрицательная зарплата
        ));

        PrintStream originalErr = System.err;
        ByteArrayOutputStream errCapture = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errCapture));

        try {
            List<Employee> result = fileService.loadEmployeesFromFile(filePath.toAbsolutePath().toString());

            assertEquals(1, result.size());
            assertEquals("Василиса", result.getFirst().getFirstName());

            String errOutput = errCapture.toString();
            assertTrue(errOutput.contains("Error parsing line 2"));
            assertTrue(errOutput.contains("Expected 4 fields"));
            assertTrue(errOutput.contains("Error parsing line 3"));
            assertTrue(errOutput.contains("First name or last name is empty"));
            assertTrue(errOutput.contains("Error parsing line 4"));
            assertTrue(errOutput.contains("Invalid number format"));
            assertTrue(errOutput.contains("Error parsing line 5"));
            assertTrue(errOutput.contains("Salary cannot be negative"));

        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    @DisplayName("loadEmployeesFromFile: пропускает пустые строки")
    void loadEmployeesFromFile_SkipsEmptyLines() throws IOException {
        Path filePath = tempDir.resolve("with-empty.txt");
        Files.write(filePath, List.of(
                "1,Сергей,Николаевич,50000",
                "",
                "  ",
                "2,Вадим,Александрович,75000"
        ));

        List<Employee> result = fileService.loadEmployeesFromFile(filePath.toAbsolutePath().toString());

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("loadEmployeesFromFile: корректно парсит поля с пробелами")
    void loadEmployeesFromFile_TrimsWhitespace() throws IOException {
        String filename = tempDir.resolve("spaces.txt").toAbsolutePath().toString();
        Files.writeString(Path.of(filename), "  1 , Диана  ,  Сергеевна  , 50000  ");

        List<Employee> result = fileService.loadEmployeesFromFile(filename);

        assertEquals(1, result.size());
        assertEquals("Диана", result.getFirst().getFirstName());
        assertEquals("Сергеевна", result.getFirst().getLastName());
    }

    @Test
    @DisplayName("loadEmployeesFromFile: бросает IllegalArgumentException при null имени")
    void loadEmployeesFromFile_NullFilename_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> fileService.loadEmployeesFromFile(null));
    }

    /**
     * Вспомогательный метод для получения пути без расширения .txt
     */
    private String pathInTempDir(String filename) {
        String name = filename.endsWith(".txt") ? filename.substring(0, filename.length() - 4) : filename;
        String path = tempDir.resolve(name + ".txt").toAbsolutePath().toString();
        return path.substring(0, path.length() - 4);
    }

}