package org.ydanilenko.budgettracker.controller;

import javafx.collections.FXCollections;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.view.TransactionForm;
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

        transactionView.getAddButton().setOnAction(e -> {
            TransactionForm transactionForm = new TransactionForm(transactionDAO);
            transactionForm.show(transactionView.getStage(), this::updateTransactionList);
        });

        transactionView.getFilterButton().setOnAction(e -> filterTransactionsByDateRange());
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
