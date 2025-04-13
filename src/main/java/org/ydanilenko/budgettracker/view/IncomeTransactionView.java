package org.ydanilenko.budgettracker.view;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.util.DatabaseConnection;
import org.ydanilenko.budgettracker.view.PaymentTypeManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class IncomeTransactionView {
    private final TableView<Transaction> table = new TableView<>();
    private final PieChart categoryChart = new PieChart();
    private final PieChart paymentTypeChart = new PieChart();
    private final TextField amountField = new TextField();
    private final DatePicker dateField = new DatePicker();
    private final ComboBox<String> categoryField = new ComboBox<>();
    private final ComboBox<String> paymentTypeField = new ComboBox<>();
    private final TextField commentField = new TextField();
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
    private final ComboBox<String> placeField = new ComboBox<>();
    private final ComboBox<String> beneficiaryField = new ComboBox<>();
    private final Label totalLabel = new Label("Total: 0.00");
    private final Button showHistogramButton = new Button("Income vs Expense Chart");

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

        TableColumn<Transaction, String> placeColumn = new TableColumn<>("Place");
        placeColumn.setCellValueFactory(new PropertyValueFactory<>("placeName"));

        TableColumn<Transaction, String> beneficiaryColumn = new TableColumn<>("Beneficiary");
        beneficiaryColumn.setCellValueFactory(new PropertyValueFactory<>("beneficiaryName"));


        table.getColumns().addAll(
                dateColumn,
                placeColumn,
                beneficiaryColumn,
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

        double total = transactions.stream().mapToDouble(Transaction::getAmount).sum();
        totalLabel.setText(String.format("💰 Total Income: %.2f", total));
        totalLabel.setStyle("""
    -fx-background-color: #e6ffe6;
    -fx-text-fill: green;
    -fx-font-size: 16px;
    -fx-font-weight: bold;
    -fx-padding: 8px 12px;
    -fx-background-radius: 8px;
""");

        if (transactions.isEmpty()) {
            table.setPlaceholder(new Label("No income to show in this range."));
        } else {
            table.setPlaceholder(new Label(""));
        }

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
        showHistogramButton.setOnAction(e -> showIncomeExpenseHistogram());

        HBox chartButtonBox = new HBox(10, showCategoryChartButton, showPaymentChartButton, showHistogramButton);

        HBox filterRow = new HBox(10, filterButton, resetFilterButton);
        HBox dateRow = new HBox(10,
                new Label("Start Date:"), startDatePicker,
                new Label("End Date:"), endDatePicker
        );

        managePaymentTypesButton.setOnAction(e -> {
            new PaymentTypeManager(stage, new TransactionDAO(DatabaseConnection.getConnection()), null, this).show();
        });

        Region spacer_for_total = new Region();
        HBox.setHgrow(spacer_for_total, Priority.ALWAYS);

        HBox bottomControls = new HBox(10, addButton, managePaymentTypesButton, spacer_for_total, totalLabel);
        bottomControls.setPadding(new Insets(10));
        HBox leftControls = new HBox(10, addButton, managePaymentTypesButton);
        HBox rightTotal = new HBox(totalLabel);
        rightTotal.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightTotal, Priority.ALWAYS);

        BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(leftControls);
        bottomPane.setRight(rightTotal);
        bottomPane.setPadding(new Insets(10));

        layout.setBottom(bottomPane);


        VBox pieAndFilterBox = new VBox(20, chartButtonBox, filterRow, dateRow);
        VBox mainCenter = new VBox(10, table, pieAndFilterBox);
        layout.setCenter(mainCenter);



        Scene scene = new Scene(layout, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
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
    private void showIncomeExpenseHistogram() {
        TransactionDAO dao = new TransactionDAO(DatabaseConnection.getConnection());

        List<Transaction> expenses = dao.getTransactionsByType(0); // 0 = Expense
        List<Transaction> incomes = dao.getTransactionsByType(1);  // 1 = Income

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start != null && end != null) {
            expenses = expenses.stream()
                    .filter(t -> !LocalDate.parse(t.getDate()).isBefore(start) && !LocalDate.parse(t.getDate()).isAfter(end))
                    .toList();
            incomes = incomes.stream()
                    .filter(t -> !LocalDate.parse(t.getDate()).isBefore(start) && !LocalDate.parse(t.getDate()).isAfter(end))
                    .toList();
        }

        Map<String, Double> incomeMap = new TreeMap<>();
        Map<String, Double> expenseMap = new TreeMap<>();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Transaction t : incomes) {
            String month = LocalDate.parse(t.getDate()).format(monthFormatter);
            incomeMap.merge(month, t.getAmount(), Double::sum);
        }

        for (Transaction t : expenses) {
            String month = LocalDate.parse(t.getDate()).format(monthFormatter);
            expenseMap.merge(month, t.getAmount(), Double::sum);
        }
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Income vs Expense");
        xAxis.setLabel("Date");
        yAxis.setLabel("Amount");

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Income");

        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Expense");

        Set<String> allDates = new TreeSet<>();
        allDates.addAll(incomeMap.keySet());
        allDates.addAll(expenseMap.keySet());

        for (String date : allDates) {
            incomeSeries.getData().add(new XYChart.Data<>(date, incomeMap.getOrDefault(date, 0.0)));
            expenseSeries.getData().add(new XYChart.Data<>(date, expenseMap.getOrDefault(date, 0.0)));
        }

        barChart.getData().addAll(incomeSeries, expenseSeries);

        Stage chartStage = new Stage();
        chartStage.initModality(Modality.APPLICATION_MODAL);
        chartStage.initOwner(stage);
        chartStage.setTitle("Income vs Expense Histogram");
        chartStage.setScene(new Scene(barChart, 800, 600));
        chartStage.showAndWait();

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
        placeField.getSelectionModel().clearSelection();
        beneficiaryField.getSelectionModel().clearSelection();
    }
    public ComboBox<String> getPlaceField() {
        return placeField;
    }

    public ComboBox<String> getBeneficiaryField() {
        return beneficiaryField;
    }

    public Stage getStage() {
        return stage;
    }
}
