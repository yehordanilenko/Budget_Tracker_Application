package org.ydanilenko.budgettracker.view;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.util.DatabaseConnection;
import org.ydanilenko.budgettracker.view.PaymentTypeManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncomeTransactionView {
    private final TableView<Transaction> table = new TableView<>();
    private final PieChart categoryChart = new PieChart();
    private final PieChart paymentTypeChart = new PieChart();
    private final TextField amountField = new TextField();
    private final DatePicker dateField = new DatePicker();
    private final ComboBox<String> categoryField = new ComboBox<>();
    private final ComboBox<String> paymentTypeField = new ComboBox<>();
    private final TextField commentField = new TextField();
    private final TextField locationField = new TextField();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();
    private final Button filterButton = new Button("Filter");
    private final Button resetFilterButton = new Button("Reset Filter");
    private final Button addButton = new Button("Add Income");
    private final Button switchToExpenseButton = new Button("← Expense Page");
    private final Stage stage;
    private ExpenseTransactionView expenseView;
    Button showCategoryChartButton = new Button("Income by Category");
    Button showPaymentChartButton = new Button("Income by Payment Type");
    private final Button managePaymentTypesButton = new Button("Manage Payment Types");

    public IncomeTransactionView(Stage stage) {
        this.stage = stage;
        setupTable();
        setupCharts();
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
                dateColumn,
                locationColumn,
                categoryColumn,
                commentColumn,
                paymentTypeColumn,
                amountColumn
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupCharts() {
        categoryChart.setTitle("Income by Category");
        categoryChart.setLegendVisible(true);
        paymentTypeChart.setTitle("Income by Payment Type");
        paymentTypeChart.setLegendVisible(true);
    }

    public void displayTransactions(List<Transaction> transactions) {
        ObservableList<Transaction> data = FXCollections.observableArrayList(transactions);
        table.setItems(data);
        updateCharts(transactions);
    }

    private void updateCharts(List<Transaction> transactions) {
        double total = transactions.stream().mapToDouble(Transaction::getAmount).sum();
        Map<String, Double> categoryTotals = new HashMap<>();
        Map<String, Double> paymentTypeTotals = new HashMap<>();

        for (Transaction t : transactions) {
            categoryTotals.put(t.getCategoryName(), categoryTotals.getOrDefault(t.getCategoryName(), 0.0) + t.getAmount());
            paymentTypeTotals.put(t.getPaymentType(), paymentTypeTotals.getOrDefault(t.getPaymentType(), 0.0) + t.getAmount());
        }

        ObservableList<PieChart.Data> categoryData = FXCollections.observableArrayList();
        ObservableList<PieChart.Data> paymentData = FXCollections.observableArrayList();

        categoryTotals.forEach((name, amount) -> {
            double percent = (amount / total) * 100;
            PieChart.Data slice = new PieChart.Data(name, amount);
            slice.nameProperty().bind(Bindings.concat(name, " (", String.format("%.2f", percent), "%)"));
            categoryData.add(slice);
        });

        paymentTypeTotals.forEach((name, amount) -> {
            double percent = (amount / total) * 100;
            PieChart.Data slice = new PieChart.Data(name, amount);
            slice.nameProperty().bind(Bindings.concat(name, " (", String.format("%.2f", percent), "%)"));
            paymentData.add(slice);
        });

        categoryChart.setData(categoryData);
        paymentTypeChart.setData(paymentData);
    }

    public void show() {

        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        Label title = new Label("Income Tracker");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox header = new HBox();
        header.setPadding(new Insets(10));
        header.setSpacing(10);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, switchToExpenseButton);
        layout.setTop(header);

        HBox chartButtonBox = new HBox(10, showCategoryChartButton, showPaymentChartButton);

        HBox filterRow = new HBox(10, filterButton, resetFilterButton);
        HBox dateRow = new HBox(10,
                new Label("Start Date:"), startDatePicker,
                new Label("End Date:"), endDatePicker
        );

        managePaymentTypesButton.setOnAction(e -> {
            new PaymentTypeManager(stage, new TransactionDAO(DatabaseConnection.getConnection()), null, this).show();
        });

        HBox bottomControls = new HBox(10, addButton, managePaymentTypesButton);

        VBox pieAndFilterBox = new VBox(20, chartButtonBox, filterRow, dateRow);
        VBox mainCenter = new VBox(10, table, pieAndFilterBox);
        layout.setCenter(mainCenter);

        layout.setBottom(bottomControls);

        Scene scene = new Scene(layout, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Budget Tracker");
        stage.show();
    }

    public Transaction getSelectedTransaction() {
        return table.getSelectionModel().getSelectedItem();
    }

    public TableView<Transaction> getTable() {
        return table;
    }

    public TextField getAmountField() { return amountField; }
    public DatePicker getDateField() { return dateField; }
    public ComboBox<String> getCategoryField() { return categoryField; }
    public ComboBox<String> getPaymentTypeField() { return paymentTypeField; }
    public TextField getCommentField() { return commentField; }
    public TextField getLocationField() { return locationField; }
    public Button getAddButton() { return addButton; }
    public Button getFilterButton() { return filterButton; }
    public Button getResetFilterButton() { return resetFilterButton; }
    public Button getSwitchToExpenseButton() { return switchToExpenseButton; }
    public DatePicker getStartDatePicker() { return startDatePicker; }
    public DatePicker getEndDatePicker() { return endDatePicker; }

    public void setExpenseView(ExpenseTransactionView view) {
        this.expenseView = view;
    }

    public ExpenseTransactionView getExpenseView() {
        return expenseView;
    }

    public Button getShowCategoryChartButton() {
        return showCategoryChartButton;
    }

    public Button getShowPaymentChartButton() {
        return showPaymentChartButton;
    }

    public void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    public Button getManagePaymentTypesButton() {
        return managePaymentTypesButton;
    }
    public void clearInputFields() {
        amountField.clear();
        dateField.setValue(null);
        categoryField.getSelectionModel().clearSelection();
        paymentTypeField.getSelectionModel().clearSelection();
        commentField.clear();
        locationField.clear();
    }

    public Stage getStage() {
        return stage;
    }
}
