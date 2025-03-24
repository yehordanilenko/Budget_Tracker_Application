
package org.ydanilenko.budgettracker.controller;

import javafx.collections.FXCollections;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.view.IncomeView;
import org.ydanilenko.budgettracker.view.TransactionView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class IncomeController {
    private final TransactionDAO transactionDAO;
    private final IncomeView incomeView;
    private List<Transaction> allTransactions;

    public IncomeController(TransactionDAO transactionDAO, IncomeView incomeView) {
        this.transactionDAO = transactionDAO;
        this.incomeView = incomeView;

        incomeView.getCategoryField().setItems(FXCollections.observableArrayList(transactionDAO.getAllCategories()));
        incomeView.getPaymentTypeField().setItems(FXCollections.observableArrayList(transactionDAO.getAllPaymentTypes()));

        incomeView.getAddButton().setOnAction(e -> addTransaction());
        incomeView.getFilterButton().setOnAction(e -> filterTransactionsByDateRange());
        incomeView.getResetFilterButton().setOnAction(e -> {
            incomeView.getStartDatePicker().setValue(null);
            incomeView.getEndDatePicker().setValue(null);
            updateTransactionList();
        });

        incomeView.getSwitchToExpenseButton().setOnAction(e -> {
            TransactionView expenseView = incomeView.getExpenseView();
            new TransactionController(transactionDAO, expenseView).initialize();
            expenseView.show(expenseView.getStage());
        });

    }

    public void addTransaction() {
        if (incomeView.getAmountField().getText().isEmpty() ||
                incomeView.getDateField().getValue() == null ||
                incomeView.getCategoryField().getValue() == null ||
                incomeView.getPaymentTypeField().getValue() == null) {
            incomeView.showError("All fields are required.");
            return;
        }

        try {
            double amount = Double.parseDouble(incomeView.getAmountField().getText());
            LocalDate date = incomeView.getDateField().getValue();
            if (date.isAfter(LocalDate.now())) {
                incomeView.showError("The date cannot be later than today.");
                return;
            }

            String category = incomeView.getCategoryField().getValue();
            int categoryId = transactionDAO.getCategoryIdByName(category);
            String paymentTypeName = incomeView.getPaymentTypeField().getValue();
            int paymentTypeId = transactionDAO.getPaymentTypeIdByName(paymentTypeName);
            String comment = incomeView.getCommentField().getText();
            String location = incomeView.getLocationField().getText();

            Transaction transaction = new Transaction(amount, date.toString(), categoryId, paymentTypeId, comment, location, 1); // type_id = 1 (income)
            boolean success = transactionDAO.addTransaction(transaction);

            if (success) {
                incomeView.showSuccess("Income added successfully.");
                updateTransactionList();
                incomeView.clearInputFields();
            } else {
                incomeView.showError("Failed to add income.");
            }
        } catch (NumberFormatException e) {
            incomeView.showError("Amount must be a valid number.");
        }
    }

    public void updateTransactionList() {
        allTransactions = transactionDAO.getTransactionsByType(1); // income
        incomeView.displayTransactions(allTransactions);
    }

    public void filterTransactionsByDateRange() {
        if (allTransactions == null || allTransactions.isEmpty()) {
            incomeView.showError("No transactions to filter.");
            return;
        }

        LocalDate startDate = incomeView.getStartDatePicker().getValue();
        LocalDate endDate = incomeView.getEndDatePicker().getValue();

        if (endDate != null && endDate.isAfter(LocalDate.now())) {
            incomeView.showError("End date cannot be later than today.");
            return;
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            incomeView.showError("Start date cannot be after end date.");
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

        incomeView.displayTransactions(filteredTransactions);
    }

    public void initialize() {
        updateTransactionList();
    }
}
