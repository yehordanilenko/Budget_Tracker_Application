package database;

import org.junit.jupiter.api.Test;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.util.DatabaseConnection;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionDAOTest {
    Connection connection = DatabaseConnection.getConnection();
    private final TransactionDAO dao = new TransactionDAO(connection);

    @Test
    public void testInsertAndFetchTransaction() throws Exception {
        Transaction tx = new Transaction(
                123.45,
                "2025-04-21",
                1, 1, "Test insert from JUnit",
                1, 1, 1
        );

        boolean inserted = dao.addTransaction(tx);
        assertTrue(inserted, "Insert should succeed.");

        List<Transaction> allTransactions = dao.getAllTransactions();

        boolean found = allTransactions.stream().anyMatch(t ->
                Math.abs(t.getAmount() - 123.45) < 0.01 &&
                        "Test insert from JUnit".equals(t.getComment())
        );

        assertTrue(found, "Inserted transaction should be found in the database.");

        connection.createStatement().executeUpdate(
                "DELETE FROM Transactions WHERE comment = 'Test insert from JUnit'"
        );
    }

    @Test
    public void testDeleteTransaction() throws Exception {
        Transaction tx = new Transaction(99.99, "2025-04-22", 1, 1, "Delete Test", 1, 1, 1);
        boolean inserted = dao.addTransaction(tx);
        assertTrue(inserted, "Insert should succeed");

        List<Transaction> all = dao.getAllTransactions();
        Transaction toDelete = all.stream()
                .filter(t -> "Delete Test".equals(t.getComment()))
                .reduce((first, second) -> second)
                .orElseThrow();

        int id = toDelete.getId();

        boolean deleted = dao.deleteTransaction(id);
        assertTrue(deleted, "Transaction should be deleted");

        List<Transaction> afterDelete = dao.getAllTransactions();
        boolean stillExists = afterDelete.stream().anyMatch(t -> t.getId() == id);
        assertFalse(stillExists, "Transaction should no longer exist in DB");
    }

    @Test
    public void testInsertInvalidForeignKeyFails() {
        Transaction tx = new Transaction(
                12.00, "2025-04-30", 999, 999, "Invalid FK", 999, 999, 1
        );

        boolean inserted = dao.addTransaction(tx);
        assertFalse(inserted, "Insert should fail with invalid foreign keys.");
    }

    @Test
    public void testGetTransactionsByType() throws Exception {
        Transaction tx = new Transaction(200.00, "2025-04-30", 1, 1, "Type Test", 1, 1, 1);
        dao.addTransaction(tx);

        List<Transaction> incomes = dao.getTransactionsByType(1);
        boolean found = incomes.stream().anyMatch(t -> "Type Test".equals(t.getComment()));
        assertTrue(found, "Income transaction should be returned by type filter.");

        connection.createStatement().executeUpdate(
                "DELETE FROM transactions WHERE comment = 'Type Test'"
        );
    }

}

