package org.ydanilenko.budgettracker.model;

public class Transaction {
    private int id; // Auto-incremented by the database
    private double amount;
    private String date;
    private int categoryId; // Store the category ID for database operations
    private String categoryName; // Use for displaying the category name in the UI
    private String paymentType;
    private String comment;

    // Constructor for retrieving transactions (includes id and categoryName)
    public Transaction(int id, double amount, String date, String categoryName, String paymentType, String comment) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.categoryName = categoryName; // Name from the database join
        this.paymentType = paymentType;
        this.comment = comment;
    }

    // Constructor for adding new transactions (uses categoryId)
    public Transaction(double amount, String date, int categoryId, String paymentType, String comment) {
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId; // Use ID for database insertion
        this.paymentType = paymentType;
        this.comment = comment;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
