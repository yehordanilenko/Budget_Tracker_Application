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
        String sql = "INSERT INTO Transactions (amount, date, category_id, payment_type_id, comment, location, type_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, transaction.getAmount());
            ps.setString(2, transaction.getDate());
            ps.setInt(3, transaction.getCategoryId());
            ps.setInt(4, transaction.getPaymentTypeId());
            ps.setString(5, transaction.getComment());
            ps.setString(6, transaction.getLocation());
            ps.setInt(7, 0);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.id, t.amount, t.date, c.name AS categoryName, p.name AS paymentType, t.comment, t.location " +
                "FROM Transactions t " +
                "JOIN Categories c ON t.category_id = c.id " +
                "JOIN PaymentTypes p ON t.payment_type_id = p.id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                double amount = rs.getDouble("amount");
                String date = rs.getString("date");
                String categoryName = rs.getString("categoryName");
                String paymentType = rs.getString("paymentType");
                String comment = rs.getString("comment");
                String location = rs.getString("location");

                System.out.println("Transaction: " + id + ", " + amount + ", " + date + ", " + categoryName + ", " + paymentType + ", " + comment);

                transactions.add(new Transaction(id, amount, date, categoryName, paymentType, comment, location));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public List<Transaction> getTransactionsByType(int typeId) {
        List<Transaction> transactions = new ArrayList<>();

        String sql = "SELECT t.id, t.amount, t.date, c.name AS categoryName, p.name AS paymentType, " +
                "t.comment, t.location, t.type_id " +
                "FROM Transactions t " +
                "JOIN Categories c ON t.category_id = c.id " +
                "JOIN PaymentTypes p ON t.payment_type_id = p.id " +
                "WHERE t.type_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Transaction transaction = new Transaction(
                        rs.getDouble("amount"),
                        rs.getString("date"),
                        -1,
                        -1,
                        rs.getString("comment"),
                        rs.getString("location"),
                        rs.getInt("type_id")
                );
                transaction.setId(rs.getInt("id"));
                transaction.setCategoryName(rs.getString("categoryName"));
                transaction.setPaymentType(rs.getString("paymentType"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public List<String> getAllPaymentTypes() {
        List<String> types = new ArrayList<>();
        String sql = "SELECT name FROM PaymentTypes";

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                types.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return types;
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
