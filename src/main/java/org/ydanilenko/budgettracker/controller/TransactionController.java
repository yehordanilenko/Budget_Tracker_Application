package org.ydanilenko.budgettracker.controller;

import javafx.collections.FXCollections;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.view.TransactionView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionController {
    private final TransactionDAO transactionDAO;
    private final TransactionView transactionView;
    private List<Transaction> allTransactions; // Store all transactions in memory

    public TransactionController(TransactionDAO transactionDAO, TransactionView transactionView) {
        this.transactionDAO = transactionDAO;
        this.transactionView = transactionView;

        // Populate categories in ComboBox
        transactionView.getCategoryField().setItems(FXCollections.observableArrayList(transactionDAO.getAllCategories()));

        // Set up event handling
        transactionView.getAddButton().setOnAction(e -> addTransaction());
        transactionView.getFilterButton().setOnAction(e -> filterTransactionsByDateRange()); // Add filter handling
    }

    public void addTransaction() {
        if (transactionView.getAmountField().getText().isEmpty() ||
                transactionView.getDateField().getValue() == null ||
                transactionView.getCategoryField().getValue() == null ||
                transactionView.getPaymentTypeField().getValue() == null) {
            transactionView.showError("All fields are required.");
            return;
        }

        try {
            double amount = Double.parseDouble(transactionView.getAmountField().getText());
            LocalDate date = transactionView.getDateField().getValue(); // Get selected date
            if (date.isAfter(LocalDate.now())) { // Check if the date is in the future
                transactionView.showError("The date cannot be later than today.");
                return;
            }

            String category = transactionView.getCategoryField().getValue();
            int categoryId = transactionDAO.getCategoryIdByName(category);
            String paymentType = transactionView.getPaymentTypeField().getValue();
            String comment = transactionView.getCommentField().getText();

            Transaction transaction = new Transaction(amount, date.toString(), categoryId, paymentType, comment);
            boolean success = transactionDAO.addTransaction(transaction);

            if (success) {
                transactionView.showSuccess("Transaction added successfully.");
                updateTransactionList();
                transactionView.getAmountField().clear();
                transactionView.getDateField().setValue(null);
                transactionView.getCategoryField().getSelectionModel().clearSelection();
                transactionView.getPaymentTypeField().getSelectionModel().clearSelection();
                transactionView.getCommentField().clear();
            } else {
                transactionView.showError("Failed to add transaction.");
            }
        } catch (NumberFormatException e) {
            transactionView.showError("Amount must be a valid number.");
        }
    }

    public void updateTransactionList() {
        allTransactions = transactionDAO.getAllTransactions(); // Fetch all transactions
        transactionView.displayTransactions(allTransactions); // Display all transactions
    }

    public void filterTransactionsByDateRange() {
        if (allTransactions == null || allTransactions.isEmpty()) {
            transactionView.showError("No transactions to filter.");
            return;
        }

        // Get date range from the view
        LocalDate startDate = transactionView.getStartDatePicker().getValue();
        LocalDate endDate = transactionView.getEndDatePicker().getValue();

        // Validate end date
        if (endDate != null && endDate.isAfter(LocalDate.now())) {
            transactionView.showError("End date cannot be later than today.");
            return;
        }

        // Validate start date vs end date
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            transactionView.showError("Start date cannot be after end date.");
            return;
        }

        // Define a formatter to parse the dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");

        // Filter transactions by date range
        List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> {
                    try {
                        LocalDate transactionDate = LocalDate.parse(t.getDate(), formatter);
                        return (startDate == null || !transactionDate.isBefore(startDate)) &&
                                (endDate == null || !transactionDate.isAfter(endDate));
                    } catch (DateTimeParseException e) {
                        System.err.println("Invalid date format in transaction: " + t.getDate());
                        return false; // Exclude transactions with invalid dates
                    }
                })
                .collect(Collectors.toList());

        // Update the view with the filtered transactions
        transactionView.displayTransactions(filteredTransactions);
    }


    public void initialize() {
        updateTransactionList();
    }
}
