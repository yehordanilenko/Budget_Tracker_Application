package org.ydanilenko.budgettracker.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.beans.binding.Bindings;

import javafx.stage.Stage;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.util.DatabaseConnection;
import org.ydanilenko.budgettracker.view.PaymentTypeManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseTransactionView {
    private final TableView<Transaction> table;
    private final PieChart pieChart;
    private final PieChart paymentTypePieChart;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button filterButton;
    private final Button addButton;
    private Stage stage;
    private final Button resetFilterButton = new Button("Reset Filter");
    private final Button switchToIncomeButton = new Button("→ Income Page");
    private final Button showCategoryChartButton = new Button("Spending by Category");
    private final Button showPaymentChartButton = new Button("Spending by Payment Type");
    private final Button managePaymentTypesButton = new Button("Manage Payment Types");

    public ExpenseTransactionView(Stage stage) {
        this.stage = stage;
        this.table = new TableView<>();
        this.pieChart = new PieChart();
        this.paymentTypePieChart = new PieChart();
        this.startDatePicker = new DatePicker();
        this.endDatePicker = new DatePicker();
        this.filterButton = new Button("Filter");
        this.addButton = new Button("Add Expense");

        setupTable();
        setupPieChart();
        setupPaymentTypePieChart();
    }
    public Button getResetFilterButton() {
        return resetFilterButton;
    }

    private void setupTable() {
        TableColumn<Transaction, Double> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Transaction, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<Transaction, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryName"));

        TableColumn<Transaction, String> paymentTypeColumn = new TableColumn<>("Payment Type");
        paymentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));

        TableColumn<Transaction, String> commentColumn = new TableColumn<>("Comment");
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        TableColumn<Transaction, String> locationColumn = new TableColumn<>("Location");
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        table.getColumns().addAll(
                amountColumn,
                dateColumn,
                categoryColumn,
                paymentTypeColumn,
                commentColumn,
                locationColumn
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupPieChart() {
        pieChart.setTitle("Spending by Category");
        pieChart.setLegendVisible(true);
    }

    private void setupPaymentTypePieChart() {
        paymentTypePieChart.setTitle("Spending by Payment Type");
        paymentTypePieChart.setLegendVisible(true);
    }

    public void updatePieChart(List<Transaction> transactions) {
        double totalAmount = transactions.stream().mapToDouble(Transaction::getAmount).sum();

        Map<String, Double> categoryTotals = new HashMap<>();
        for (Transaction transaction : transactions) {
            String category = transaction.getCategoryName().trim();
            double amount = transaction.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }

        Map<String, Double> paymentTypeTotals = new HashMap<>();
        for (Transaction transaction : transactions) {
            String paymentType = transaction.getPaymentType().trim();
            double amount = transaction.getAmount();
            paymentTypeTotals.put(paymentType, paymentTypeTotals.getOrDefault(paymentType, 0.0) + amount);
        }

        ObservableList<PieChart.Data> categoryPieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0) {
                PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
                double percentage = (entry.getValue() / totalAmount) * 100;
                slice.nameProperty().bind(Bindings.concat(entry.getKey(), " (", String.format("%.2f", percentage), "%)"));
                categoryPieChartData.add(slice);
            }
        }

        ObservableList<PieChart.Data> paymentTypePieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : paymentTypeTotals.entrySet()) {
            if (entry.getValue() > 0) {
                PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
                double percentage = (entry.getValue() / totalAmount) * 100;
                slice.nameProperty().bind(Bindings.concat(entry.getKey(), " (", String.format("%.2f", percentage), "%)"));
                paymentTypePieChartData.add(slice);
            }
        }

        pieChart.setData(categoryPieChartData);
        paymentTypePieChart.setData(paymentTypePieChartData);
    }

    public void displayTransactions(List<Transaction> transactions) {
        ObservableList<Transaction> data = FXCollections.observableArrayList(transactions);
        table.setItems(data);
        updatePieChart(transactions);
    }

    public VBox createDateRangeFilter() {
        VBox dateFilterBox = new VBox(10);
        dateFilterBox.setPadding(new Insets(10));

        HBox buttonBox = new HBox(10, filterButton, resetFilterButton);

        HBox datePickers = new HBox(10);
        datePickers.getChildren().addAll(
                new Label("Start Date:"), startDatePicker,
                new Label("End Date:"), endDatePicker
        );

        dateFilterBox.getChildren().addAll(buttonBox, datePickers);

        return dateFilterBox;
    }


    public void show(Stage stage) {
        this.stage = stage;

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        Label title = new Label("Expense Tracker");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.setSpacing(10);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, switchToIncomeButton);
        layout.setTop(header);

        HBox chartButtonBox = new HBox(10, showCategoryChartButton, showPaymentChartButton);

        HBox filterRow = new HBox(10, filterButton, resetFilterButton);

        HBox dateRow = new HBox(10,
                new Label("Start Date:"), startDatePicker,
                new Label("End Date:"), endDatePicker
        );

        VBox pieAndFilterBox = new VBox(20, chartButtonBox, filterRow, dateRow);

        VBox mainCenter = new VBox(10, table, pieAndFilterBox);
        layout.setCenter(mainCenter);

       // HBox bottomControls = new HBox(10, addButton);


        managePaymentTypesButton.setOnAction(e -> {
            new PaymentTypeManager(stage, new TransactionDAO(DatabaseConnection.getConnection()), this, null).show();
        });

        HBox bottomControls = new HBox(10, addButton, managePaymentTypesButton);
        layout.setBottom(bottomControls);


        Scene scene = new Scene(layout, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Budget Tracker");
        stage.show();
    }


    public Button getShowCategoryChartButton() {
        return showCategoryChartButton;
    }

    public Button getShowPaymentChartButton() {
        return showPaymentChartButton;
    }


    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Action Completed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Transaction getSelectedTransaction() {
        return table.getSelectionModel().getSelectedItem();
    }

    public TableView<Transaction> getTable() {
        return table;
    }

    public Button getAddButton() {
        return addButton;
    }
    public Button getManagePaymentTypesButton() {
        return managePaymentTypesButton;
    }

    public Button getSwitchToIncomeButton() {
        return switchToIncomeButton;
    }

    public DatePicker getStartDatePicker() {
        return startDatePicker;
    }

    public DatePicker getEndDatePicker() {
        return endDatePicker;
    }

    public Button getFilterButton() {
        return filterButton;
    }

    public Stage getStage() {
        return stage;
    }
}
