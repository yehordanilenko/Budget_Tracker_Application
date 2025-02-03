package org.ydanilenko.budgettracker.model;

public class Transaction {
    private int id;
    private double amount;
    private String date;
    private int categoryId;
    private String categoryName;
    private String paymentType;
    private String comment;

    public Transaction(int id, double amount, String date, String categoryName, String paymentType, String comment) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.categoryName = categoryName;
        this.paymentType = paymentType;
        this.comment = comment;
    }

    public Transaction(double amount, String date, int categoryId, String paymentType, String comment) {
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.paymentType = paymentType;
        this.comment = comment;
    }

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
