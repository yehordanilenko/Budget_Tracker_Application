package org.ydanilenko.budgettracker.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ydanilenko.budgettracker.model.Transaction;
import org.ydanilenko.budgettracker.model.TransactionDAO;

import java.time.LocalDate;

public class TransactionForm {
    private final TransactionDAO transactionDAO;
    private final TextField locationField = new TextField();
    private final int typeId;

    public TransactionForm(TransactionDAO dao, int typeId) {
        this.transactionDAO = dao;
        this.typeId = typeId;
    }

    public void show(Stage parentStage, Runnable onTransactionAdded) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(parentStage);
        popupStage.setTitle("Add Transaction");

        TextField amountField = new TextField();
        DatePicker dateField = new DatePicker(LocalDate.now());
        ComboBox<String> categoryField = new ComboBox<>(FXCollections.observableArrayList(transactionDAO.getAllCategories()));
        ComboBox<String> paymentTypeField = new ComboBox<>(FXCollections.observableArrayList("Cash", "Card", "Bank Transfer", "Other"));
        TextField commentField = new TextField();
        TextField locationField = new TextField();

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
                String paymentType = paymentTypeField.getValue();
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
        form.add(saveButton, 1, 6);

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
