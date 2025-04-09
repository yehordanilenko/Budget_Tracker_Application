package org.ydanilenko.budgettracker.model;

public class Transaction {
    private int id;
    private double amount;
    private String date;
    private int categoryId;
    private String categoryName;
    private String paymentType;
    private int paymentTypeId;
    private String comment;
    private int typeId;
    private int placeId;
    private int beneficiaryId;
    private String placeName;
    private String beneficiaryName;

    public Transaction(double amount, String date, int categoryId, int paymentTypeId, String comment, int placeId, int beneficiaryId, int typeId)
    {
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.paymentTypeId = paymentTypeId;
        this.comment = comment;
        this.typeId = typeId;
        this.placeId = placeId;
        this.beneficiaryId = beneficiaryId;

    }

    public Transaction(int id, double amount, String date, int categoryId, int paymentTypeId, String comment, int typeId) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.paymentTypeId = paymentTypeId;
        this.comment = comment;
        this.typeId = typeId;
    }

    public Transaction(int id, double amount, String date, String categoryName, String paymentType, String comment, String placeName, String beneficiaryName) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.categoryName = categoryName;
        this.paymentType = paymentType;
        this.comment = comment;
        this.placeName = placeName;
        this.beneficiaryName = beneficiaryName;
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

    public int getPaymentTypeId() {
        return paymentTypeId;
    }

    public void setPaymentTypeId(int paymentType) {
        this.paymentTypeId = paymentTypeId;
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

    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }

    public int getPlaceId() { return placeId; }
    public void setPlaceId(int placeId) { this.placeId = placeId; }

    public int getBeneficiaryId() { return beneficiaryId; }
    public void setBeneficiaryId(int beneficiaryId) { this.beneficiaryId = beneficiaryId; }

    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }

    public String getBeneficiaryName() { return beneficiaryName; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }
}
