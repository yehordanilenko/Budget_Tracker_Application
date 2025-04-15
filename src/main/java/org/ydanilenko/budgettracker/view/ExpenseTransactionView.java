package org.ydanilenko.budgettracker.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.beans.binding.Bindings;

import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.util.DatabaseConnection;
import org.ydanilenko.budgettracker.view.PaymentTypeManager;
import org.ydanilenko.budgettracker.view.StatisticsView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExpenseTransactionView {
    private final TableView<Transaction> table;
    private final PieChart pieChart;
    private final PieChart paymentTypePieChart;
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button filterButton;
    private final Button addButton;
    private Stage stage;
    private final Button resetFilterButton = new Button("\uD83D\uDD04 Reset Filter");
    private final Button switchToIncomeButton = new Button("\uD83D\uDCE5 Income Page");
    private final Button showCategoryChartButton = new Button("\uD83D\uDDC2 Spending by Category");
    private final Button showPaymentChartButton = new Button("\uD83D\uDCB3 Spending by Payment Type");
    private final Button managePaymentTypesButton = new Button("\uD83C\uDFE6 Manage Payment Types");
    private final Label totalLabel = new Label("Total: 0.00");
    private final Button showHistogramButton = new Button("\uD83D\uDCCA Income vs Expense Chart");
    private Button statsButton = new Button("\uD83D\uDCCB Show Statistics");

    public ExpenseTransactionView(Stage stage) {
        this.stage = stage;
        this.table = new TableView<>();
        this.pieChart = new PieChart();
        this.paymentTypePieChart = new PieChart();
        this.startDatePicker = new DatePicker();
        this.endDatePicker = new DatePicker();
        this.filterButton = new Button("\uD83D\uDD0D Filter");
        this.addButton = new Button("➕ Add Expense");

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

        if (transactions.isEmpty()) {
            table.setPlaceholder(new Label("No expenses to show in this range."));
        } else {
            table.setPlaceholder(new Label(""));
        }

        double total = transactions.stream().mapToDouble(Transaction::getAmount).sum();
        totalLabel.setText(String.format("💸 Total Expenses: %.2f", total));
        totalLabel.setStyle("""
            -fx-background-color: #ffe5e5;
            -fx-text-fill: red;
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-padding: 8px 12px;
            -fx-background-radius: 8px;
        """);
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

        HBox chartButtonBox = new HBox(10, showCategoryChartButton, showPaymentChartButton, showHistogramButton);

        HBox filterRow = new HBox(10, filterButton, resetFilterButton);

        Label startLabel = new Label("Start Date:");
        startLabel.setAlignment(Pos.CENTER_LEFT);
        startLabel.setMinHeight(Control.USE_PREF_SIZE);

        Label endLabel = new Label("End Date:");
        endLabel.setAlignment(Pos.CENTER_LEFT);
        endLabel.setMinHeight(Control.USE_PREF_SIZE);

        HBox dateRow = new HBox(10, startLabel, startDatePicker, endLabel, endDatePicker);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        VBox pieAndFilterBox = new VBox(20, chartButtonBox, dateRow, filterRow);

        VBox mainCenter = new VBox(10, table, pieAndFilterBox);
        layout.setCenter(mainCenter);

        startDatePicker.getEditor().setDisable(true);
        startDatePicker.getEditor().setOpacity(1);

        endDatePicker.getEditor().setDisable(true);
        endDatePicker.getEditor().setOpacity(1);

        managePaymentTypesButton.setOnAction(e -> {
            new PaymentTypeManager(stage, new TransactionDAO(DatabaseConnection.getConnection()), this, null).show();
        });
        showHistogramButton.setOnAction(e -> showIncomeExpenseHistogram());

        statsButton.setOnAction(e -> {
            StatisticsView view = new StatisticsView(new TransactionDAO(DatabaseConnection.getConnection()));
            view.show();
        });

        Region spacer_for_total = new Region();
        HBox.setHgrow(spacer_for_total, Priority.ALWAYS);

        HBox leftControls = new HBox(10, addButton, managePaymentTypesButton, statsButton);
        HBox rightTotal = new HBox(totalLabel);
        rightTotal.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(rightTotal, Priority.ALWAYS);

        BorderPane bottomPane = new BorderPane();
        bottomPane.setLeft(leftControls);
        bottomPane.setRight(rightTotal);

        layout.setBottom(bottomPane);

        Scene scene = new Scene(layout, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
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

    private void showIncomeExpenseHistogram() {
        TransactionDAO dao = new TransactionDAO(DatabaseConnection.getConnection());

        List<Transaction> expenses = dao.getTransactionsByType(0);
        List<Transaction> incomes = dao.getTransactionsByType(1);

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
