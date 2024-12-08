package org.ydanilenko.budgettracker.controller;

import javafx.collections.FXCollections;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.view.TransactionView;

import java.util.List;
import java.time.LocalDate;

public class TransactionController {
    private final TransactionDAO transactionDAO;
    private final TransactionView transactionView;

    public TransactionController(TransactionDAO transactionDAO, TransactionView transactionView) {
        this.transactionDAO = transactionDAO;
        this.transactionView = transactionView;

        // Populate categories in ComboBox
        transactionView.getCategoryField().setItems(FXCollections.observableArrayList(transactionDAO.getAllCategories()));

        // Set up event handling
        transactionView.getAddButton().setOnAction(e -> addTransaction());
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
        transactionView.displayTransactions(transactionDAO.getAllTransactions());
    }

    public void initialize() {
        updateTransactionList();
    }
}
