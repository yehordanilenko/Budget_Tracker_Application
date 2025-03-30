package org.ydanilenko.budgettracker.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.ydanilenko.budgettracker.model.PaymentType;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;

import java.time.LocalDate;

public class TransactionForm {
    private final TransactionDAO transactionDAO;
    private final TextField locationField = new TextField();
    private final int typeId;
    private Transaction editingTransaction;

    public TransactionForm(TransactionDAO dao, int typeId) {
        this.transactionDAO = dao;
        this.typeId = typeId;
    }

    public TransactionForm(Stage ownerStage, TransactionDAO dao, Runnable onFinish, Transaction editingTransaction) {
        this.transactionDAO = dao;
        this.typeId = editingTransaction.getTypeId();
        this.editingTransaction = editingTransaction;

        Stage popupStage = new Stage();
        popupStage.initOwner(ownerStage);
        popupStage.initModality(Modality.APPLICATION_MODAL);

        TextField amountField = new TextField(String.valueOf(editingTransaction.getAmount()));
        DatePicker dateField = new DatePicker(LocalDate.parse(editingTransaction.getDate()));
        ComboBox<String> categoryField = new ComboBox<>();

        ComboBox<PaymentType> paymentTypeField = new ComboBox<>();
        paymentTypeField.setItems(FXCollections.observableArrayList(dao.getAllPaymentTypeObjects()));
        paymentTypeField.setConverter(new StringConverter<>() {
            @Override
            public String toString(PaymentType pt) {
                return pt == null ? "" : pt.getName();
            }

            @Override
            public PaymentType fromString(String string) {
                return paymentTypeField.getItems().stream()
                        .filter(pt -> pt.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });
        paymentTypeField.setValue(dao.getAllPaymentTypeObjects().stream()
                .filter(pt -> pt.getName().equals(editingTransaction.getPaymentType()))
                .findFirst().orElse(null));


        TextField commentField = new TextField(editingTransaction.getComment());
        TextField locationField = new TextField(editingTransaction.getLocation());

        categoryField.setItems(FXCollections.observableArrayList(dao.getAllCategories()));
        categoryField.setValue(editingTransaction.getCategoryName());

        Button saveButton = new Button("Update Transaction");
        saveButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String date = dateField.getValue().toString();
                int categoryId = dao.getCategoryIdByName(categoryField.getValue());
                int paymentTypeId = paymentTypeField.getValue() != null ? paymentTypeField.getValue().getId() : -1;
                String comment = commentField.getText();
                String location = locationField.getText();

                Transaction updatedTransaction = new Transaction(
                        editingTransaction.getId(), amount, date, categoryId, paymentTypeId, comment, location, typeId
                );

                boolean success = dao.updateTransaction(updatedTransaction);
                if (success) {
                    onFinish.run();
                    popupStage.close();
                } else {
                    showError("Failed to update transaction.");
                }
            } catch (NumberFormatException ex) {
                showError("Amount must be a valid number.");
            }
        });

        Button deleteButton = new Button("Delete Transaction");
        deleteButton.setOnAction(e -> {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Deletion");
            confirmation.setHeaderText("Are you sure you want to delete this transaction?");
            confirmation.setContentText("This action cannot be undone.");

            confirmation.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    boolean success = transactionDAO.deleteTransaction(editingTransaction.getId());
                    if (success) {
                        onFinish.run();
                        popupStage.close();
                    } else {
                        showError("Failed to delete transaction.");
                    }
                }
            });
        });

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
        form.add(new Label("Location:"), 0, 5);
        form.add(locationField, 1, 5);
        form.add(saveButton, 1, 6);
        form.add(deleteButton, 1, 7);

        Scene scene = new Scene(form);
        popupStage.setScene(scene);
        popupStage.setTitle("Edit Transaction");
        popupStage.show();
    }

    public TransactionForm(Stage parentStage, TransactionDAO dao, int typeId, Transaction copiedTransaction, Runnable onTransactionAdded) {
        this.transactionDAO = dao;
        this.typeId = typeId;

        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(parentStage);
        popupStage.setTitle("Add Transaction (Copied)");

        TextField amountField = new TextField(String.valueOf(copiedTransaction.getAmount()));
        DatePicker dateField = new DatePicker(LocalDate.parse(copiedTransaction.getDate()));
        ComboBox<String> categoryField = new ComboBox<>(FXCollections.observableArrayList(dao.getAllCategories()));
        ComboBox<PaymentType> paymentTypeField = new ComboBox<>(FXCollections.observableArrayList(dao.getAllPaymentTypeObjects()));
        paymentTypeField.setConverter(new StringConverter<>() {
            @Override
            public String toString(PaymentType pt) {
                return pt == null ? "" : pt.getName();
            }

            @Override
            public PaymentType fromString(String string) {
                return paymentTypeField.getItems().stream()
                        .filter(pt -> pt.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });
        TextField commentField = new TextField(copiedTransaction.getComment());
        TextField locationField = new TextField(copiedTransaction.getLocation());

        categoryField.setValue(copiedTransaction.getCategoryName());
        paymentTypeField.setValue(
                dao.getAllPaymentTypeObjects().stream()
                        .filter(pt -> pt.getName().equals(copiedTransaction.getPaymentType()))
                        .findFirst()
                        .orElse(null)
        );


        Button saveButton = new Button("Add Transaction");
        saveButton.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                String date = dateField.getValue().toString();
                int categoryId = dao.getCategoryIdByName(categoryField.getValue());
                int paymentTypeId = paymentTypeField.getValue() != null ? paymentTypeField.getValue().getId() : -1;
                String comment = commentField.getText();
                String location = locationField.getText();

                boolean success = dao.addTransaction(new Transaction(amount, date, categoryId, paymentTypeId, comment, location, typeId));
                if (success) {
                    onTransactionAdded.run(); // Refresh list
                    popupStage.close();
                } else {
                    showError("Failed to add transaction.");
                }
            } catch (NumberFormatException ex) {
                showError("Amount must be a valid number.");
            }
        });

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
        form.add(new Label("Location:"), 0, 5);
        form.add(locationField, 1, 5);
        form.add(saveButton, 1, 6);

        Scene scene = new Scene(form, 350, 300);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    public void show(Stage parentStage, Runnable onTransactionAdded) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(parentStage);
        popupStage.setTitle("Add Transaction");

        TextField amountField = new TextField();
        DatePicker dateField = new DatePicker(LocalDate.now());
        ComboBox<String> categoryField = new ComboBox<>(FXCollections.observableArrayList(transactionDAO.getAllCategories()));
        ComboBox<PaymentType> paymentTypeField = new ComboBox<>(FXCollections.observableArrayList(transactionDAO.getAllPaymentTypeObjects()));
        paymentTypeField.setConverter(new StringConverter<>() {
            @Override
            public String toString(PaymentType pt) {
                return pt == null ? "" : pt.getName();
            }

            @Override
            public PaymentType fromString(String string) {
                return paymentTypeField.getItems().stream()
                        .filter(pt -> pt.getName().equals(string))
                        .findFirst().orElse(null);
            }
        });

        TextField commentField = new TextField();
        TextField locationField = new TextField();

        Button clearButton = new Button("Clear Fields");
        clearButton.setOnAction(e -> {
            amountField.clear();
            dateField.setValue(LocalDate.now());
            categoryField.getSelectionModel().clearSelection();
            paymentTypeField.getSelectionModel().clearSelection();
            commentField.clear();
            locationField.clear();
        });

        Button saveButton = new Button("Add Transaction");
        saveButton.setOnAction(e -> {
            if (amountField.getText().isEmpty() || dateField.getValue() == null ||
                    categoryField.getValue() == null || paymentTypeField.getValue() == null) {
                showError("All fields are required.");
                return;
            }

            try {
                double amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    showError("Amount must be greater than zero.");
                    return;
                }

                String category = categoryField.getValue();
                int categoryId = transactionDAO.getCategoryIdByName(category);
                String paymentType = paymentTypeField.getValue() != null ? paymentTypeField.getValue().getName() : null;
                int paymentTypeId = transactionDAO.getPaymentTypeIdByName(paymentType);
                String comment = commentField.getText();
                String location = locationField.getText();

                boolean success = transactionDAO.addTransaction(new Transaction(amount, dateField.getValue().toString(), categoryId, paymentTypeId, comment, location, typeId));

                if (success) {
                    onTransactionAdded.run();
                    popupStage.close();
                } else {
                    showError("Failed to add transaction.");
                }
            } catch (NumberFormatException ex) {
                showError("Amount must be a valid number.");
            }
        });

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
        form.add(new Label("Location:"), 0, 5);
        form.add(locationField, 1, 5);
        HBox buttonBox = new HBox(10, saveButton, clearButton);
        form.add(buttonBox, 1, 6);

        Scene scene = new Scene(form, 350, 300);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
