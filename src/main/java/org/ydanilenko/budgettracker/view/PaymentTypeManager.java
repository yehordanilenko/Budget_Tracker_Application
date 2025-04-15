package org.ydanilenko.budgettracker.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ydanilenko.budgettracker.model.PaymentType;
import org.ydanilenko.budgettracker.model.TransactionDAO;

import java.time.LocalDate;

public class PaymentTypeManager {
    private final TransactionDAO dao;
    private final Stage window;
    private final TableView<PaymentType> table;

    public PaymentTypeManager(Stage ownerStage, TransactionDAO dao, ExpenseTransactionView expenseView, IncomeTransactionView incomeView) {
        this.dao = dao;
        this.window = new Stage();
        this.window.initOwner(ownerStage);
        this.window.initModality(Modality.APPLICATION_MODAL);
        this.window.setTitle("Manage Payment Types");
        Image icon = new Image(getClass().getResourceAsStream("/images/bank_icon.png"));
        this.window.getIcons().add(icon);
        this.table = new TableView<>();
        setupTable();

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> showForm(null));


        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            PaymentType selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && confirmDelete()) {
                boolean success = dao.deletePaymentType(selected.getId());
                if (success) {
                    loadData();
                } else {
                    showError("This payment method is associated with existing transactions and cannot be deleted.");
                }
            }
        });

        HBox buttonBox = new HBox(10, addButton, deleteButton);
        buttonBox.setPadding(new Insets(10));

        VBox layout = new VBox(10, table, buttonBox);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 600, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        setupContextMenu();
        window.setScene(scene);

        loadData();
    }

    private void setupTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PaymentType, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<PaymentType, String> bankCol = new TableColumn<>("Bank");
        bankCol.setCellValueFactory(new PropertyValueFactory<>("bank"));

        TableColumn<PaymentType, String> issuerCol = new TableColumn<>("Issuer");
        issuerCol.setCellValueFactory(new PropertyValueFactory<>("issuer"));

        TableColumn<PaymentType, String> issueDateCol = new TableColumn<>("Issue Date");
        issueDateCol.setCellValueFactory(new PropertyValueFactory<>("issueDate"));

        TableColumn<PaymentType, String> expirationDateCol = new TableColumn<>("Expiration Date");
        expirationDateCol.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));

        table.getColumns().addAll(nameCol, bankCol, issuerCol, issueDateCol, expirationDateCol);
    }

    private void loadData() {
        ObservableList<PaymentType> types = FXCollections.observableArrayList(dao.getAllPaymentTypeObjects());
        table.setItems(types);
    }

    private void showForm(PaymentType pt) {
        Stage formStage = new Stage();
        formStage.initOwner(window);
        formStage.initModality(Modality.APPLICATION_MODAL);
        formStage.setTitle(pt == null ? "Add Payment Type" : "Edit Payment Type");
        formStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/bank_icon.png")));
        TextField nameField = new TextField(pt != null ? pt.getName() : "");
        TextField bankField = new TextField(pt != null ? pt.getBank() : "");
        TextField issuerField = new TextField(pt != null ? pt.getIssuer() : "");
        DatePicker issueDateField = new DatePicker();
        DatePicker expDateField = new DatePicker();
        if (pt != null) {
            if (pt.getIssueDate() != null && !pt.getIssueDate().isEmpty()) {
                issueDateField.setValue(LocalDate.parse(pt.getIssueDate()));
            }
            if (pt.getExpirationDate() != null && !pt.getExpirationDate().isEmpty()) {
                expDateField.setValue(LocalDate.parse(pt.getExpirationDate()));
            }
        }


        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Name is required.");
                return;
            }

            PaymentType updated = new PaymentType(
                    pt != null ? pt.getId() : 0,
                    name,
                    bankField.getText().trim(),
                    issuerField.getText().trim(),
                    issueDateField.getValue() != null ? issueDateField.getValue().toString() : null,
                    expDateField.getValue() != null ? expDateField.getValue().toString() : null
            );

            boolean isNew = pt == null || pt.getId() == 0;
            boolean success = isNew ? dao.addPaymentType(updated) : dao.updatePaymentType(updated);

            if (success) {
                loadData();
                formStage.close();
            } else {
                showError("Failed to save payment type.");
            }
        });

        Button clearIssueDate = new Button("X");
        clearIssueDate.setOnAction(e -> issueDateField.setValue(null));

        Button clearExpDate = new Button("X");
        clearExpDate.setOnAction(e -> expDateField.setValue(null));

        Button clearButton = new Button("Clear Fields");
        clearButton.setOnAction(e -> {
            nameField.clear();
            bankField.clear();
            issuerField.clear();
            issueDateField.setValue(null);
            expDateField.setValue(null);
        });


        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("Bank:"), 0, 1);
        form.add(bankField, 1, 1);
        form.add(new Label("Issuer:"), 0, 2);
        form.add(issuerField, 1, 2);
        form.add(new Label("Issue Date:"), 0, 3);
        form.add(issueDateField, 1, 3);
        form.add(new Label("Expiration Date:"), 0, 4);
        form.add(expDateField, 1, 4);
        HBox buttons = new HBox(10, saveButton, clearButton);
        form.add(buttons, 1, 5);
        HBox issueRow = new HBox(5, issueDateField, clearIssueDate);
        HBox expRow = new HBox(5, expDateField, clearExpDate);
        form.add(issueRow, 1, 3);
        form.add(expRow, 1, 4);

        Scene scene = new Scene(form);scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        formStage.setWidth(400);
        formStage.setHeight(330);
        formStage.setScene(scene);
        formStage.showAndWait();
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> {
            PaymentType selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showForm(selected);
            }
        });

        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> {
            PaymentType selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                PaymentType copied = new PaymentType(
                        0,
                        selected.getName(),
                        selected.getBank(),
                        selected.getIssuer(),
                        selected.getIssueDate(),
                        selected.getExpirationDate()
                );
                showForm(copied);
            }
        });

        contextMenu.getItems().addAll(editItem, copyItem);

        table.setRowFactory(tv -> {
            TableRow<PaymentType> row = new TableRow<>();
            row.setOnContextMenuRequested(e -> {
                if (!row.isEmpty()) {
                    table.getSelectionModel().select(row.getItem());
                    contextMenu.show(row, e.getScreenX(), e.getScreenY());
                }
            });
            return row;
        });
    }



    private boolean confirmDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete this payment type?");
        alert.setContentText("This action cannot be undone.");

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-confirm");
        return alert.showAndWait().filter(btn -> btn == ButtonType.OK).isPresent();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error");
        alert.setContentText(msg);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        dialogPane.getStyleClass().add("custom-alert");

        alert.showAndWait();
    }

    public void show() {
        window.showAndWait();
    }
}
