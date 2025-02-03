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
    private final ComboBox<String> categoryField;
    private final ComboBox<String> paymentTypeField;
    private final TextField commentField;
    private final Button addButton;
    private final PieChart pieChart;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button filterButton;

    public TransactionView() {
        this.table = new TableView<>();
        this.amountField = new TextField();
        this.dateField = new DatePicker();
        this.categoryField = new ComboBox<>();
        this.paymentTypeField = new ComboBox<>();
        this.commentField = new TextField();
        this.addButton = new Button("Add Transaction");
        this.pieChart = new PieChart();
        this.startDatePicker = new DatePicker();
        this.endDatePicker = new DatePicker();
        this.filterButton = new Button("Filter");

        setupTable();
        setupPaymentTypeOptions();
        restrictFutureDates();
        setupPieChart();
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

        table.getColumns().addAll(amountColumn, dateColumn, categoryColumn, paymentTypeColumn, commentColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupPaymentTypeOptions() {
        ObservableList<String> paymentTypes = FXCollections.observableArrayList("Cash", "Card", "Bank Transfer", "Other");
        paymentTypeField.setItems(paymentTypes);
    }

    private void restrictFutureDates() {
        dateField.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #cccccc;");
                }
            }
        });
    }

    private void setupPieChart() {
        pieChart.setTitle("Spending by Category");
        pieChart.setLegendVisible(true);
    }

    public void updatePieChart(List<Transaction> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Transaction transaction : transactions) {
            String category = transaction.getCategoryName();
            double amount = transaction.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }

        pieChart.setData(pieChartData);
    }

    public void displayTransactions(List<Transaction> transactions) {
        ObservableList<Transaction> data = FXCollections.observableArrayList(transactions);
        table.setItems(data);
        updatePieChart(transactions);
    }

    public void filterByDateRange(List<Transaction> allTransactions) {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> {
                    LocalDate transactionDate = LocalDate.parse(t.getDate());
                    return (startDate == null || !transactionDate.isBefore(startDate)) &&
                            (endDate == null || !transactionDate.isAfter(endDate));
                })
                .collect(Collectors.toList());

        displayTransactions(filteredTransactions);
    }

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

    private VBox createDateRangeFilter() {
        VBox dateFilterBox = new VBox(10);
        dateFilterBox.setPadding(new Insets(10));

        Button filterButton = this.filterButton;

        HBox datePickers = new HBox(10);
        datePickers.getChildren().addAll(
                new Label("Start Date:"), startDatePicker,
                new Label("End Date:"), endDatePicker
        );

        dateFilterBox.getChildren().addAll(filterButton, datePickers);

        return dateFilterBox;
    }

    public void show(Stage stage) {
        BorderPane layout = new BorderPane();
        layout.setPadding(new Insets(10));

        Label title = new Label("Budget Tracker");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        layout.setTop(title);
        BorderPane.setMargin(title, new Insets(10));

        HBox centerBox = new HBox(20, pieChart, createDateRangeFilter());
        VBox mainCenter = new VBox(10, table, centerBox);
        layout.setCenter(mainCenter);

        layout.setBottom(createForm());

        Scene scene = new Scene(layout, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Budget Tracker");
        stage.show();
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
