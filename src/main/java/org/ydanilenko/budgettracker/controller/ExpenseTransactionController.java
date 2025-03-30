package org.ydanilenko.budgettracker.controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.view.IncomeTransactionView;
import org.ydanilenko.budgettracker.view.PaymentTypeManager;
import org.ydanilenko.budgettracker.view.TransactionForm;
import org.ydanilenko.budgettracker.view.ExpenseTransactionView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ContextMenu;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpenseTransactionController {
    private final TransactionDAO transactionDAO;
    private final ExpenseTransactionView expenseTransactionView;
    private List<Transaction> allTransactions;
    private List<Transaction> visibleTransactions;
    private Transaction copiedTransaction = null;

    public ExpenseTransactionController(TransactionDAO transactionDAO, ExpenseTransactionView expenseTransactionView) {
        this.transactionDAO = transactionDAO;
        this.expenseTransactionView = expenseTransactionView;

        expenseTransactionView.getAddButton().setOnAction(e -> {
            TransactionForm form = new TransactionForm(transactionDAO, 0);
            form.show(expenseTransactionView.getStage(), this::updateTransactionList);
        });

        expenseTransactionView.getResetFilterButton().setOnAction(e -> {
            expenseTransactionView.getStartDatePicker().setValue(null);
            expenseTransactionView.getEndDatePicker().setValue(null);
            updateTransactionList();
        });

        expenseTransactionView.getSwitchToIncomeButton().setOnAction(e -> {
            IncomeTransactionView incomeView = new IncomeTransactionView(expenseTransactionView.getStage());
            incomeView.setExpenseView(expenseTransactionView);
            IncomeTransactionController incomeController = new IncomeTransactionController(transactionDAO, incomeView);
            incomeController.initialize();
            incomeView.show();
        });

        expenseTransactionView.getShowCategoryChartButton().setOnAction(e ->
                showPieChart("Spending by Category", groupByCategory(visibleTransactions))
        );

        expenseTransactionView.getShowPaymentChartButton().setOnAction(e ->
                showPieChart("Spending by Payment Type", groupByPaymentType(visibleTransactions))
        );

        expenseTransactionView.getManagePaymentTypesButton().setOnAction(e -> {
            new PaymentTypeManager(
                    expenseTransactionView.getStage(),
                    transactionDAO,
                    expenseTransactionView,
                    null
            ).show();
        });

        expenseTransactionView.getFilterButton().setOnAction(e -> filterTransactionsByDateRange());

        setupContextMenu();
    }

    public void updateTransactionList() {
        allTransactions = transactionDAO.getTransactionsByType(0);
        LocalDate now = LocalDate.now();
        visibleTransactions = allTransactions.stream()
                .filter(t -> {
                    LocalDate date = LocalDate.parse(t.getDate());
                    return date.getMonth() == now.getMonth() && date.getYear() == now.getYear();
                })
                .collect(Collectors.toList());
        expenseTransactionView.displayTransactions(visibleTransactions);
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
        popup.initOwner(expenseTransactionView.getStage());
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
            expenseTransactionView.showError("No transactions to filter.");
            return;
        }

        LocalDate startDate = expenseTransactionView.getStartDatePicker().getValue();
        LocalDate endDate = expenseTransactionView.getEndDatePicker().getValue();

        if (endDate != null && endDate.isAfter(LocalDate.now())) {
            expenseTransactionView.showError("End date cannot be later than today.");
            return;
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            expenseTransactionView.showError("Start date cannot be after end date.");
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
        expenseTransactionView.displayTransactions(visibleTransactions);
    }

    private void setupContextMenu() {
        TableView<Transaction> table = expenseTransactionView.getTable();

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> {
            Transaction selected = expenseTransactionView.getSelectedTransaction();
            if (selected != null) {
                TransactionForm editForm = new TransactionForm(
                        expenseTransactionView.getStage(),
                        transactionDAO,
                        this::updateTransactionList,
                        selected
                );
            }
        });

        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> {
            Transaction selected = expenseTransactionView.getSelectedTransaction();
            if (selected != null) {
                new TransactionForm(
                        expenseTransactionView.getStage(),
                        transactionDAO,
                        0,
                        selected,
                        this::updateTransactionList
                );
            }
        });

        ContextMenu contextMenu = new ContextMenu(editItem, copyItem);
        table.setContextMenu(contextMenu);
    }


    public void initialize() {
        updateTransactionList();
    }
}
