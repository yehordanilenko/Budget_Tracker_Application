package org.ydanilenko.budgettracker.view;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.ydanilenko.budgettracker.model.TransactionDAO;

public class StatisticsView {
    private final TransactionDAO dao;

    public StatisticsView(TransactionDAO dao) {
        this.dao = dao;
    }

    public void show() {
        Stage statsStage = new Stage();
        statsStage.setTitle("Financial Statistics");
        statsStage.initModality(Modality.APPLICATION_MODAL);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(20);

        Label totalIncomeLabel = new Label("Total Income: " + dao.getTotalIncome());
        Label totalExpenseLabel = new Label("Total Expense: " + dao.getTotalExpense());
        Label txCountLabel = new Label("Total Transactions: " + dao.getTotalTransactions());
        Label maxTxLabel = new Label("Max Transaction: " + dao.getMaxTransactionAmount());
        Label topCategoryLabel = new Label("Top Category: " + dao.getMostUsedCategory());
        Label topBeneficiaryLabel = new Label("Top Beneficiary: " + dao.getTopBeneficiary());

        grid.add(totalIncomeLabel, 0, 0);
        grid.add(totalExpenseLabel, 0, 1);
        grid.add(txCountLabel, 0, 2);
        grid.add(maxTxLabel, 0, 3);
        grid.add(topCategoryLabel, 0, 4);
        grid.add(topBeneficiaryLabel, 0, 5);

        Scene scene = new Scene(grid);
        statsStage.setScene(scene);
        statsStage.showAndWait();
    }
}
