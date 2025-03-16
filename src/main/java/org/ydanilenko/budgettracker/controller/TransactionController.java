package org.ydanilenko.budgettracker.controller;

import javafx.collections.FXCollections;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.view.TransactionForm;
import org.ydanilenko.budgettracker.view.TransactionView;

import java.util.List;

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
        // Filtering logic remains unchanged
    }

    public void initialize() {
        updateTransactionList();
    }
}
