package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class UniversityFilterToDB {
    private static final String DB_URL = "jdbc:postgresql://localhost:5000/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "mq1strM69!";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите код специальности (например, 001): ");
        String specialtyCode = scanner.nextLine().trim();

        if (!specialtyCode.matches("\\d{3}")) {
            System.out.println("Неверный формат. Введите три цифры.");
            return;
        }

        String specialtyMarker = "B" + specialtyCode + " - ";
        String pdfFilePath = "C:\\Users\\Malika\\Desktop\\PdfREADER2\\src\\main\\resources\\file.pdf";
        Map<String, List<String>> universityToStudents = new LinkedHashMap<>();

        boolean inSpecialtyBlock = false;

        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String fullText = stripper.getText(document);
            String[] lines = fullText.split("\\r?\\n");

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                if (trimmed.startsWith("B") && trimmed.contains(" - ")) {
                    inSpecialtyBlock = trimmed.startsWith(specialtyMarker);
                    continue;
                }
                if (!inSpecialtyBlock) continue;
                if (!trimmed.matches("^\\d+.*")) continue;

                String[] columns = trimmed.split("\\s+");
                if (columns.length < 5) continue;

                String universityCode = columns[columns.length - 1].trim();
                if (!universityCode.matches("\\d+")) continue;

                universityToStudents.computeIfAbsent(universityCode, k -> new ArrayList<>()).add(trimmed);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении PDF: " + e.getMessage());
            return;
        } finally {
            scanner.close();
        }

        if (universityToStudents.isEmpty()) {
            System.out.println("Не найдено ни одной записи для специальности B" + specialtyCode);
            return;
        }

        System.out.println("=== Сохранение данных в базу ===");

        String tableName = "students_" + specialtyCode;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);

            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "student_id TEXT PRIMARY KEY, "
                    + "full_name TEXT NOT NULL, "
                    + "total_score INT NOT NULL, "
                    + "university_code TEXT NOT NULL"
                    + ")";

            String clearTableQuery = "DELETE FROM " + tableName;
            String insertStudentQuery = "INSERT INTO " + tableName + " (student_id, full_name, total_score, university_code) "
                    + "VALUES (?, ?, ?, ?) ON CONFLICT (student_id) DO NOTHING";

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(createTableQuery);
                stmt.executeUpdate(clearTableQuery);
            }

            try (PreparedStatement studentStmt = conn.prepareStatement(insertStudentQuery)) {
                for (Map.Entry<String, List<String>> entry : universityToStudents.entrySet()) {
                    String uniCode = entry.getKey();

                    for (String studentLine : entry.getValue()) {
                        String[] parts = studentLine.split("\\s+");

                        if (parts.length < 4) continue;

                        String studentId = parts[1];

                        try {
                            int totalScore = Integer.parseInt(parts[parts.length - 2]);

                            String fullName = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length - 2));

                            studentStmt.setString(1, studentId);
                            studentStmt.setString(2, fullName);
                            studentStmt.setInt(3, totalScore);
                            studentStmt.setString(4, uniCode);
                            studentStmt.executeUpdate();
                        } catch (NumberFormatException e) {
                            System.out.println("Ошибка обработки строки: " + studentLine);
                        }
                    }
                }

                conn.commit();
                System.out.println("Данные успешно сохранены в таблицу " + tableName);
            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Ошибка при сохранении данных: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
    }
}
