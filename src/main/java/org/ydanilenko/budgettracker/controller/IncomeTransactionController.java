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
import org.ydanilenko.budgettracker.view.IncomeTransactionView;
import org.ydanilenko.budgettracker.view.ExpenseTransactionView;
import org.ydanilenko.budgettracker.view.TransactionForm;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IncomeTransactionController {
    private final TransactionDAO transactionDAO;
    private final IncomeTransactionView incomeView;
    private List<Transaction> allTransactions;
    private List<Transaction> visibleTransactions;

    public IncomeTransactionController(TransactionDAO transactionDAO, IncomeTransactionView incomeView) {
        this.transactionDAO = transactionDAO;
        this.incomeView = incomeView;

        incomeView.getCategoryField().setItems(FXCollections.observableArrayList(transactionDAO.getAllCategories()));
        incomeView.getPaymentTypeField().setItems(FXCollections.observableArrayList(transactionDAO.getAllPaymentTypes()));

        incomeView.getAddButton().setOnAction(e -> {
            TransactionForm transactionForm = new TransactionForm(transactionDAO, 1);
            transactionForm.show(incomeView.getStage(), this::updateTransactionList);
        });
        incomeView.getFilterButton().setOnAction(e -> filterTransactionsByDateRange());
        incomeView.getResetFilterButton().setOnAction(e -> {
            incomeView.getStartDatePicker().setValue(null);
            incomeView.getEndDatePicker().setValue(null);
            updateTransactionList();
        });

        incomeView.getSwitchToExpenseButton().setOnAction(e -> {
            ExpenseTransactionView expenseView = incomeView.getExpenseView();
            new ExpenseTransactionController(transactionDAO, expenseView).initialize();
            expenseView.show(expenseView.getStage());
        });

        incomeView.getShowCategoryChartButton().setOnAction(e ->
                showPieChart("Income by Category", groupByCategory(visibleTransactions))
        );

        incomeView.getShowPaymentChartButton().setOnAction(e ->
                showPieChart("Income by Payment Type", groupByPaymentType(visibleTransactions))
        );
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

            Transaction transaction = new Transaction(amount, date.toString(), categoryId, paymentTypeId, comment, location, 1);
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
        allTransactions = transactionDAO.getTransactionsByType(1);
        visibleTransactions = allTransactions;
        incomeView.displayTransactions(visibleTransactions);
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

        visibleTransactions = filteredTransactions;
        incomeView.displayTransactions(visibleTransactions);
    }

    private void showPieChart(String title, Map<String, Double> dataMap) {
        Stage popup = new Stage();
        popup.setTitle(title);
        popup.initOwner(incomeView.getStage());
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

    public void initialize() {
        updateTransactionList();
    }
}
