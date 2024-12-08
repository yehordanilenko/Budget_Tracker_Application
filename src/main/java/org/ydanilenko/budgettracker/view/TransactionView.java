package org.ydanilenko.budgettracker.view;

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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionView {
    private final TableView<Transaction> table;
    private final TextField amountField;
    private final DatePicker dateField;
    private final ComboBox<String> categoryField; // Dropdown for category
    private final ComboBox<String> paymentTypeField; // Dropdown for payment type
    private final TextField commentField;
    private final Button addButton;
    private final PieChart pieChart; // PieChart for category-wise visualization

    public TransactionView() {
        this.table = new TableView<>();
        this.amountField = new TextField();
        this.dateField = new DatePicker();
        this.categoryField = new ComboBox<>();
        this.paymentTypeField = new ComboBox<>();
        this.commentField = new TextField();
        this.addButton = new Button("Add Transaction");
        this.pieChart = new PieChart(); // Initialize the PieChart

        setupTable();
        setupPaymentTypeOptions();
        restrictFutureDates(); // Restrict future dates in the DatePicker
        setupPieChart(); // Configure the PieChart layout
    }

    // Setup the table columns (without ID)
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

        table.getColumns().addAll(amountColumn, dateColumn, categoryColumn, paymentTypeColumn, commentColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Auto-resize columns
    }

    // Populate payment type options
    private void setupPaymentTypeOptions() {
        ObservableList<String> paymentTypes = FXCollections.observableArrayList("Cash", "Card", "Bank Transfer", "Other");
        paymentTypeField.setItems(paymentTypes);
    }

    // Restrict future dates in the DatePicker
    private void restrictFutureDates() {
        dateField.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Disable future dates
                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #cccccc;"); // Optional: Grey out future dates
                }
            }
        });
    }

    // Setup the PieChart layout
    private void setupPieChart() {
        pieChart.setTitle("Spending by Category");
        pieChart.setLegendVisible(true); // Show legend
    }

    // Update the PieChart with transaction data
    public void updatePieChart(List<Transaction> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();

        // Calculate total amounts per category
        for (Transaction transaction : transactions) {
            String category = transaction.getCategoryName();
            double amount = transaction.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }

        // Populate the PieChart with category data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        pieChart.setData(pieChartData);
    }

    // Display transactions in the table
    public void displayTransactions(List<Transaction> transactions) {
        ObservableList<Transaction> data = FXCollections.observableArrayList(transactions);
        table.setItems(data);
        updatePieChart(transactions); // Update PieChart when transactions are displayed
    }

    // Create the form for adding new transactions
    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);

        form.add(new Label("Amount:"), 0, 0);
        form.add(amountField, 1, 0);

        form.add(new Label("Date:"), 0, 1);
        form.add(dateField, 1, 1);

        form.add(new Label("Category:"), 0, 2);
        form.add(categoryField, 1, 2);

        form.add(new Label("Payment Type:"), 0, 3);
        form.add(paymentTypeField, 1, 3); // Use ComboBox for Payment Type

        form.add(new Label("Comment:"), 0, 4);
        form.add(commentField, 1, 4);

        HBox buttonBox = new HBox(10, addButton);
        form.add(buttonBox, 1, 5);

        return form;
    }

    // Show the main view
    public void show(Stage stage) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        // Title
        Label title = new Label("Budget Tracker");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        layout.setTop(title);
        BorderPane.setMargin(title, new Insets(10));

        // TableView and PieChart
        VBox centerBox = new VBox(10, table, pieChart); // Add PieChart below TableView
        layout.setCenter(centerBox);

        // Form at the bottom
        layout.setBottom(createForm());

        // Scene and stage setup
        Scene scene = new Scene(layout, 900, 700); // Adjust dimensions for the PieChart
        stage.setScene(scene);
        stage.setTitle("Budget Tracker");
        stage.show();
    }

    // Show an error message
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Show a success message
    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Action Completed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters for the input fields and button
    public TextField getAmountField() {
        return amountField;
    }

    public DatePicker getDateField() {
        return dateField;
    }

    public ComboBox<String> getCategoryField() {
        return categoryField;
    }

    public ComboBox<String> getPaymentTypeField() {
        return paymentTypeField;
    }

    public TextField getCommentField() {
        return commentField;
    }

    public Button getAddButton() {
        return addButton;
    }
}

