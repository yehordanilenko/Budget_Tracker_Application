package org.ydanilenko.budgettracker.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.view.IncomeView;
import org.ydanilenko.budgettracker.view.TransactionForm;
import org.ydanilenko.budgettracker.view.TransactionView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionController {
    private final TransactionDAO transactionDAO;
    private final TransactionView transactionView;
    private List<Transaction> allTransactions;
    private List<Transaction> visibleTransactions;

    public TransactionController(TransactionDAO transactionDAO, TransactionView transactionView) {
        this.transactionDAO = transactionDAO;
        this.transactionView = transactionView;

        transactionView.getAddButton().setOnAction(e -> {
            TransactionForm transactionForm = new TransactionForm(transactionDAO);
            transactionForm.show(transactionView.getStage(), this::updateTransactionList);
        });

        transactionView.getResetFilterButton().setOnAction(e -> {
            transactionView.getStartDatePicker().setValue(null);
            transactionView.getEndDatePicker().setValue(null);
            updateTransactionList();
        });

        transactionView.getSwitchToIncomeButton().setOnAction(e -> {
            IncomeView incomeView = new IncomeView(transactionView.getStage());
            incomeView.setExpenseView(transactionView);
            IncomeController incomeController = new IncomeController(transactionDAO, incomeView);
            incomeController.initialize();
            incomeView.show();
        });

        transactionView.getShowCategoryChartButton().setOnAction(e ->
                showPieChart("Spending by Category", groupByCategory(visibleTransactions))
        );

        transactionView.getShowPaymentChartButton().setOnAction(e ->
                showPieChart("Spending by Payment Type", groupByPaymentType(visibleTransactions))
        );

        transactionView.getFilterButton().setOnAction(e -> filterTransactionsByDateRange());
    }

    public void updateTransactionList() {
        allTransactions = transactionDAO.getTransactionsByType(0);
        visibleTransactions = allTransactions;
        transactionView.displayTransactions(visibleTransactions);
    }

    private Map<String, Double> groupByCategory(List<Transaction> transactions) {
        return transactions.stream().collect(Collectors.groupingBy(
                Transaction::getCategoryName,
                Collectors.summingDouble(Transaction::getAmount)
        ));
    }

    private Map<String, Double> groupByPaymentType(List<Transaction> transactions) {
        return transactions.stream().collect(Collectors.groupingBy(
                Transaction::getPaymentType,
                Collectors.summingDouble(Transaction::getAmount)
        ));
    }

    private void showPieChart(String title, Map<String, Double> dataMap) {
        Stage popup = new Stage();
        popup.setTitle(title);
        popup.initOwner(transactionView.getStage());
        popup.initModality(Modality.WINDOW_MODAL);

        double total = dataMap.values().stream().mapToDouble(Double::doubleValue).sum();
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();

        for (Map.Entry<String, Double> entry : dataMap.entrySet()) {
            double percentage = (entry.getValue() / total) * 100;
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            slice.nameProperty().bind(Bindings.concat(entry.getKey(), " (", String.format("%.2f", percentage), "%)"));
            data.add(slice);
        }

        PieChart chart = new PieChart(data);
        chart.setLegendVisible(true);
        chart.setTitle(title);

        VBox layout = new VBox(chart);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 500, 400);

        popup.setScene(scene);
        popup.showAndWait();
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
        visibleTransactions = filteredTransactions;
        transactionView.displayTransactions(visibleTransactions);
    }

    public void initialize() {
        updateTransactionList();
    }
}
