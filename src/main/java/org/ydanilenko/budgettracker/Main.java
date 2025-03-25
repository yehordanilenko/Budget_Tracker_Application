package org.ydanilenko.budgettracker;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.ydanilenko.budgettracker.controller.ExpenseTransactionController;
import org.ydanilenko.budgettracker.util.DatabaseConnection;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.view.ExpenseTransactionView;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        var connection = DatabaseConnection.getConnection();

        TransactionDAO transactionDAO = new TransactionDAO(connection);
        ExpenseTransactionView transactionView = new ExpenseTransactionView(primaryStage);
        ExpenseTransactionController transactionController = new ExpenseTransactionController(transactionDAO, transactionView);

        Image icon = new Image(getClass().getResourceAsStream("/images/app_icon.png"));
        primaryStage.getIcons().add(icon);

        transactionController.initialize();

        transactionView.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
