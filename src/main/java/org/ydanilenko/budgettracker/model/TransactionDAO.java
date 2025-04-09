package org.ydanilenko.budgettracker.model;

import org.ydanilenko.budgettracker.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private final Connection connection;

    public TransactionDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO Transactions (amount, date, category_id, payment_type_id, comment, place_id, beneficiary_id, type_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, transaction.getAmount());
            ps.setString(2, transaction.getDate());
            ps.setInt(3, transaction.getCategoryId());
            ps.setInt(4, transaction.getPaymentTypeId());
            ps.setString(5, transaction.getComment());
            ps.setInt(6, transaction.getPlaceId());
            ps.setInt(7, transaction.getBeneficiaryId());
            ps.setInt(8, transaction.getTypeId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTransaction(Transaction transaction) {
        String query = "UPDATE transactions SET amount = ?, date = ?, category_id = ?, payment_type_id = ?, comment = ?, place_id = ?, beneficiary_id = ?, type_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDouble(1, transaction.getAmount());
            stmt.setString(2, transaction.getDate());
            stmt.setInt(3, transaction.getCategoryId());
            stmt.setInt(4, transaction.getPaymentTypeId());
            stmt.setString(5, transaction.getComment());
            stmt.setInt(6, transaction.getPlaceId());
            stmt.setInt(7, transaction.getBeneficiaryId());
            stmt.setInt(8, transaction.getTypeId());
            stmt.setInt(9, transaction.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.id, t.amount, t.date, c.name AS categoryName,\n" +
                "       p.name AS paymentType, t.comment,\n" +
                "       pl.name AS placeName, b.name AS beneficiaryName\n" +
                "FROM Transactions t\n" +
                "JOIN Categories c ON t.category_id = c.id\n" +
                "JOIN PaymentTypes p ON t.payment_type_id = p.id\n" +
                "LEFT JOIN Places pl ON t.place_id = pl.id\n" +
                "LEFT JOIN Beneficiaries b ON t.beneficiary_id = b.id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                double amount = rs.getDouble("amount");
                String date = rs.getString("date");
                String categoryName = rs.getString("categoryName");
                String paymentType = rs.getString("paymentType");
                String comment = rs.getString("comment");
                String placeName = rs.getString("placeName");
                String beneficiaryName = rs.getString("beneficiaryName");

                transactions.add(new Transaction(id, amount, date, categoryName, paymentType, comment, placeName, beneficiaryName));


            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public List<Transaction> getTransactionsByType(int typeId) {
        List<Transaction> transactions = new ArrayList<>();

        String sql = "SELECT t.id, t.amount, t.date, c.name AS categoryName, " +
                "p.name AS paymentType, t.comment, " +
                "pl.name AS placeName, b.name AS beneficiaryName " +
                "FROM Transactions t " +
                "JOIN Categories c ON t.category_id = c.id " +
                "JOIN PaymentTypes p ON t.payment_type_id = p.id " +
                "LEFT JOIN Places pl ON t.place_id = pl.id " +
                "LEFT JOIN Beneficiaries b ON t.beneficiary_id = b.id " +
                "WHERE t.type_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                double amount = rs.getDouble("amount");
                String date = rs.getString("date");
                String categoryName = rs.getString("categoryName");
                String paymentType = rs.getString("paymentType");
                String comment = rs.getString("comment");
                String placeName = rs.getString("placeName");
                String beneficiaryName = rs.getString("beneficiaryName");

                Transaction transaction = new Transaction(
                        id, amount, date, categoryName, paymentType, comment, placeName, beneficiaryName
                );
                transaction.setTypeId(typeId); // if needed
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

    public boolean addPlace(String name) {
        String sql = "INSERT INTO Places (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addBeneficiary(String name) {
        String sql = "INSERT INTO Beneficiaries (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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

    public int getPlaceIdByName(String placeName) {
        String sql = "SELECT id FROM Places WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, placeName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getBeneficiaryIdByName(String beneficiaryName) {
        String sql = "SELECT id FROM Beneficiaries WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, beneficiaryName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<String> getAllPlaces() {
        List<String> places = new ArrayList<>();
        String sql = "SELECT name FROM Places";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) places.add(rs.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return places;
    }

    public List<String> getAllBeneficiaries() {
        List<String> beneficiaries = new ArrayList<>();
        String sql = "SELECT name FROM Beneficiaries";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) beneficiaries.add(rs.getString("name"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return beneficiaries;
    }


    public List<PaymentType> getAllPaymentTypeObjects() {
        List<PaymentType> paymentTypes = new ArrayList<>();
        String sql = "SELECT id, name, bank, issuer, issue_date, expiration_date FROM PaymentTypes";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                paymentTypes.add(new PaymentType(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("bank"),
                        rs.getString("issuer"),
                        rs.getString("issue_date"),
                        rs.getString("expiration_date")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return paymentTypes;
    }

    public boolean addPaymentType(PaymentType pt) {
        String sql = "INSERT INTO PaymentTypes (name, bank, issuer, issue_date, expiration_date) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pt.getName());
            stmt.setString(2, pt.getBank());
            stmt.setString(3, pt.getIssuer());
            stmt.setString(4, pt.getIssueDate());
            stmt.setString(5, pt.getExpirationDate());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePaymentType(PaymentType pt) {
        String sql = "UPDATE PaymentTypes SET name = ?, bank = ?, issuer = ?, issue_date = ?, expiration_date = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pt.getName());
            stmt.setString(2, pt.getBank());
            stmt.setString(3, pt.getIssuer());
            stmt.setString(4, pt.getIssueDate());
            stmt.setString(5, pt.getExpirationDate());
            stmt.setInt(6, pt.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePaymentType(int id) {
        String sql = "DELETE FROM PaymentTypes WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
