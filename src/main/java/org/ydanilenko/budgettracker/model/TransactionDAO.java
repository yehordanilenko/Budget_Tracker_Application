package org.ydanilenko.budgettracker.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private final Connection connection;

    public TransactionDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO Transactions (amount, date, category, paymentType, comment) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, transaction.getAmount());
            pstmt.setString(2, transaction.getDate());
            pstmt.setInt(3, transaction.getCategoryId());
            pstmt.setInt(4, transaction.getPaymentTypeId());
            pstmt.setString(5, transaction.getComment());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.id, t.amount, t.date, c.name AS categoryName, p.name AS paymentType, t.comment " +
                "FROM Transactions t " +
                "JOIN Categories c ON t.category = c.id " +
                "JOIN PaymentTypes p ON t.paymentType = p.id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                double amount = rs.getDouble("amount");
                String date = rs.getString("date");
                String categoryName = rs.getString("categoryName");
                String paymentType = rs.getString("paymentType");
                String comment = rs.getString("comment");

                System.out.println("Transaction: " + id + ", " + amount + ", " + date + ", " + categoryName + ", " + paymentType + ", " + comment);

                transactions.add(new Transaction(id, amount, date, categoryName, paymentType, comment));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM Categories";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public int getCategoryIdByName(String categoryName) {
        String sql = "SELECT id FROM Categories WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getPaymentTypeIdByName(String paymentTypeName) {
        String sql = "SELECT id FROM PaymentTypes WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, paymentTypeName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
