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
import java.util.stream.Collectors;

public class TransactionView {
    private final TableView<Transaction> table;
    private final TextField amountField;
    private final DatePicker dateField;
    private final ComboBox<String> categoryField; // Dropdown for category
    private final ComboBox<String> paymentTypeField; // Dropdown for payment type
    private final TextField commentField;
    private final Button addButton;
    private final PieChart pieChart; // PieChart for category-wise visualization
    private final DatePicker startDatePicker; // Start date for filter
    private final DatePicker endDatePicker; // End date for filter
    private final Button filterButton; // Button to apply date range filter

    public TransactionView() {
        this.table = new TableView<>();
        this.amountField = new TextField();
        this.dateField = new DatePicker();
        this.categoryField = new ComboBox<>();
        this.paymentTypeField = new ComboBox<>();
        this.commentField = new TextField();
        this.addButton = new Button("Add Transaction");
        this.pieChart = new PieChart(); // Initialize the PieChart
        this.startDatePicker = new DatePicker(); // Initialize start date picker
        this.endDatePicker = new DatePicker(); // Initialize end date picker
        this.filterButton = new Button("Filter"); // Initialize filter button

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
            if (entry.getValue() > 0) { // Include only non-zero totals
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }

        pieChart.setData(pieChartData);
    }

    // Display transactions in the table
    public void displayTransactions(List<Transaction> transactions) {
        ObservableList<Transaction> data = FXCollections.observableArrayList(transactions);
        table.setItems(data);
        updatePieChart(transactions); // Update PieChart when transactions are displayed
    }

    // Apply date range filter and update PieChart
    public void filterByDateRange(List<Transaction> allTransactions) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        // Filter transactions by date range
        List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> {
                    LocalDate transactionDate = LocalDate.parse(t.getDate());
                    return (startDate == null || !transactionDate.isBefore(startDate)) &&
                            (endDate == null || !transactionDate.isAfter(endDate));
                })
                .collect(Collectors.toList());

        // Update the table and PieChart with filtered data
        displayTransactions(filteredTransactions);
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
        form.add(paymentTypeField, 1, 3);

        form.add(new Label("Comment:"), 0, 4);
        form.add(commentField, 1, 4);

        HBox buttonBox = new HBox(10, addButton);
        form.add(buttonBox, 1, 5);

        return form;
    }

    // Create the date range filter layout
    private VBox createDateRangeFilter() {
        VBox dateFilterBox = new VBox(10); // Vertical spacing of 10
        dateFilterBox.setPadding(new Insets(10));

        // Create the Filter button
        Button filterButton = this.filterButton;

        // Start Date and End Date controls
        HBox datePickers = new HBox(10); // Horizontal spacing of 10
        datePickers.getChildren().addAll(
                new Label("Start Date:"), startDatePicker,
                new Label("End Date:"), endDatePicker
        );

        // Add the Filter button first, then the DatePickers
        dateFilterBox.getChildren().addAll(filterButton, datePickers);

        return dateFilterBox;
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

        // Center: TableView and PieChart with date range filter
        HBox centerBox = new HBox(20, pieChart, createDateRangeFilter());
        VBox mainCenter = new VBox(10, table, centerBox);
        layout.setCenter(mainCenter);

        // Form at the bottom
        layout.setBottom(createForm());

        // Scene and stage setup
        Scene scene = new Scene(layout, 1000, 700); // Adjusted width for PieChart and date filter
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

    public DatePicker getStartDatePicker() {
        return startDatePicker;
    }

    public DatePicker getEndDatePicker() {
        return endDatePicker;
    }

    public Button getFilterButton() {
        return filterButton;
    }
}
