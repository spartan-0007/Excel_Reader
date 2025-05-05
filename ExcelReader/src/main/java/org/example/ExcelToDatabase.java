import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;

public class ExcelToDatabase {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/sfms_intranet_db"; // Change to your actual database
    private static final String USER = "root"; // Change to your actual MySQL username
    private static final String PASSWORD = "root"; // Change to your actual MySQL password

    public static void main(String[] args) {
        String excelFilePath = "D:\\FiletoRead\\ESCALATION_MATRIX_03042025.xlsx"; // Update with the path to your Excel file

        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis);
             Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            // Skip the header row
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    // Read data from the Excel sheet
                    String bankId = row.getCell(0).getStringCellValue();
                    String applicantName = row.getCell(1).getStringCellValue();
                    String rtgsGroupMailId = row.getCell(2).getStringCellValue();
                    String neftGroupMailId = row.getCell(3).getStringCellValue();
                    String otherMailId = row.getCell(4).getStringCellValue();
                    String contactNumber = row.getCell(5).getStringCellValue();
                    String phoneNum = row.getCell(6).getStringCellValue();
                    String designation = row.getCell(7).getStringCellValue();
                    String flag = row.getCell(8).getStringCellValue(); // Assuming FLAG is the 9th column

                    // Get the first four characters of the bank_id
                    String bankShortName = bankId.length() >= 4 ? bankId.substring(0, 4) : "";

                    // Query to get the corresponding bank_id from the i_banks table
                    String bankIdFromDB = getBankIdFromDatabase(connection, bankShortName);

                    // Determine the contact email and feature
                    String contactEmail;
                    String feature;

                    if (rtgsGroupMailId != null && !rtgsGroupMailId.trim().isEmpty()) {
                        contactEmail = rtgsGroupMailId;
                        feature = "RTGS & NEFT";
                    } else if (neftGroupMailId != null && !neftGroupMailId.trim().isEmpty()) {
                        contactEmail = neftGroupMailId;
                        feature = "NEFT";
                    } else {
                        contactEmail = ""; // Or handle it accordingly
                        feature = ""; // Adjust as needed
                    }

                    // Determine the level ID based on the FLAG value
                    long levelId = mapFlagToLevelId(flag);

                    // Prepare and execute the insert statement
                    String insertSQL = "INSERT INTO i_escalation_contacts (bank_id, contact_email, contact_name, contact_number, feature, level_id, designation) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
                        if (bankIdFromDB != null) {
                            preparedStatement.setString(1, bankIdFromDB); // Set the retrieved bank_id
                        } else {
                            preparedStatement.setNull(1, java.sql.Types.VARCHAR); // Insert null if no matching bank ID is found
                        }
                        preparedStatement.setString(2, contactEmail);  // Set determined contact email
                        preparedStatement.setString(3, applicantName);
                        preparedStatement.setString(4, contactNumber);
                        preparedStatement.setString(5, feature); // Set determined feature
                        preparedStatement.setLong(6, levelId); // Set level ID based on FLAG
                        preparedStatement.setString(7, designation); // Include designation in insertion
                        preparedStatement.executeUpdate();
                    }
                }
            }
            System.out.println("Data inserted successfully!");

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static long mapFlagToLevelId(String flag) {
        switch (flag.toUpperCase()) {
            case "L1":
                return 1;
            case "L2":
                return 2;
            case "L3":
                return 3;
            default:
                return 0; // Handle unexpected FLAG values appropriately
        }
    }

    private static String getBankIdFromDatabase(Connection connection, String bankShortName) throws SQLException {
        String bankId = null;
        String query = "SELECT id FROM i_banks WHERE bank_short_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, bankShortName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                bankId = resultSet.getString("id");
            }
        }
        return bankId; // Return the found bank_id or null if not found
    }
}