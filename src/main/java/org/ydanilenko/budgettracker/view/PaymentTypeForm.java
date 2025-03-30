package org.ydanilenko.budgettracker.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ydanilenko.budgettracker.model.PaymentType;
import org.ydanilenko.budgettracker.model.TransactionDAO;

public class PaymentTypeForm {
    private final TransactionDAO dao;
    private final PaymentType paymentType; // can be null for new
    private final Runnable onFinish;
    private final Stage stage;

    public PaymentTypeForm(Stage parentStage, TransactionDAO dao, PaymentType paymentType, Runnable onFinish) {
        this.dao = dao;
        this.paymentType = paymentType;
        this.onFinish = onFinish;
        this.stage = new Stage();

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(parentStage);
        popup.setTitle(paymentType == null ? "Add Payment Type" : "Edit Payment Type");

        TextField nameField = new TextField();
        TextField bankField = new TextField();
        TextField issuerField = new TextField();
        TextField issueDateField = new TextField();
        TextField expirationDateField = new TextField();

        if (paymentType != null) {
            nameField.setText(paymentType.getName());
            bankField.setText(paymentType.getBank());
            issuerField.setText(paymentType.getIssuer());
            issueDateField.setText(paymentType.getIssueDate());
            expirationDateField.setText(paymentType.getExpirationDate());
        }

        Button saveBtn = new Button(paymentType == null ? "Add" : "Update");
        saveBtn.setOnAction(e -> {
            String name = nameField.getText();
            if (name.isEmpty()) {
                showError("Name is required.");
                return;
            }

            PaymentType pt = new PaymentType(
                    paymentType != null ? paymentType.getId() : 0,
                    name,
                    emptyIfNull(bankField.getText()),
                    emptyIfNull(issuerField.getText()),
                    emptyIfNull(issueDateField.getText()),
                    emptyIfNull(expirationDateField.getText())
            );

            boolean success = paymentType == null ? dao.addPaymentType(pt) : dao.updatePaymentType(pt);
            if (success) {
                onFinish.run();
                popup.close();
            } else {
                showError("Failed to save payment type.");
            }
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Bank:"), 0, 1); grid.add(bankField, 1, 1);
        grid.add(new Label("Issuer:"), 0, 2); grid.add(issuerField, 1, 2);
        grid.add(new Label("Issue Date:"), 0, 3); grid.add(issueDateField, 1, 3);
        grid.add(new Label("Expiration Date:"), 0, 4); grid.add(expirationDateField, 1, 4);
        grid.add(saveBtn, 1, 5);

        Scene scene = new Scene(grid, 400, 300);
        popup.setScene(scene);
        popup.showAndWait();
    }

    public void show() {
        stage.showAndWait();
    }


    private String emptyIfNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
