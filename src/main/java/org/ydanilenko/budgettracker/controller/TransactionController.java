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
    private List<Transaction> allTransactions;

    public TransactionController(TransactionDAO transactionDAO, TransactionView transactionView) {
        this.transactionDAO = transactionDAO;
        this.transactionView = transactionView;

        transactionView.getCategoryField().setItems(FXCollections.observableArrayList(transactionDAO.getAllCategories()));

        transactionView.getAddButton().setOnAction(e -> addTransaction());
        transactionView.getFilterButton().setOnAction(e -> filterTransactionsByDateRange());
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
            LocalDate date = transactionView.getDateField().getValue();
            if (date.isAfter(LocalDate.now())) {
                transactionView.showError("The date cannot be later than today.");
                return;
            }

            String category = transactionView.getCategoryField().getValue();
            int categoryId = transactionDAO.getCategoryIdByName(category);
            String paymentTypeName = transactionView.getPaymentTypeField().getValue();
            int paymentTypeId = transactionDAO.getPaymentTypeIdByName(paymentTypeName);
            String comment = transactionView.getCommentField().getText();

            Transaction transaction = new Transaction(amount, date.toString(), categoryId, paymentTypeId, comment);
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
        allTransactions = transactionDAO.getAllTransactions();
        transactionView.displayTransactions(allTransactions);
    }

    public void filterTransactionsByDateRange() {
        if (allTransactions == null || allTransactions.isEmpty()) {
            transactionView.showError("No transactions to filter.");
            return;
        }

        LocalDate startDate = transactionView.getStartDatePicker().getValue();
        LocalDate endDate = transactionView.getEndDatePicker().getValue();

        if (endDate != null && endDate.isAfter(LocalDate.now())) {
            transactionView.showError("End date cannot be later than today.");
            return;
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            transactionView.showError("Start date cannot be after end date.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");

        List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> {
                    try {
                        LocalDate transactionDate = LocalDate.parse(t.getDate(), formatter);
                        return (startDate == null || !transactionDate.isBefore(startDate)) &&
                                (endDate == null || !transactionDate.isAfter(endDate));
                    } catch (DateTimeParseException e) {
                        System.err.println("Invalid date format in transaction: " + t.getDate());
                        return false;
                    }
                })
                .collect(Collectors.toList());

        transactionView.displayTransactions(filteredTransactions);
    }

    public void initialize() {
        updateTransactionList();
    }
}
