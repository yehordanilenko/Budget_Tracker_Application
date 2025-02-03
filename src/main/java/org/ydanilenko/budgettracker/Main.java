package org.ydanilenko.budgettracker;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.ydanilenko.budgettracker.controller.TransactionController;
import org.ydanilenko.budgettracker.util.DatabaseConnection;
import org.ydanilenko.budgettracker.model.TransactionDAO;
import org.ydanilenko.budgettracker.view.TransactionView;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        var connection = DatabaseConnection.getConnection();

        TransactionDAO transactionDAO = new TransactionDAO(connection);
        TransactionView transactionView = new TransactionView();
        TransactionController transactionController = new TransactionController(transactionDAO, transactionView);

        Image icon = new Image(getClass().getResourceAsStream("/images/app_icon.png"));
        primaryStage.getIcons().add(icon);

        transactionController.initialize();

        transactionView.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
