package org.ydanilenko.budgettracker.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PaymentType {
    private int id;
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty bank = new SimpleStringProperty();
    private final StringProperty issuer = new SimpleStringProperty();
    private final StringProperty issueDate = new SimpleStringProperty();
    private final StringProperty expirationDate = new SimpleStringProperty();

    public PaymentType(int id, String name, String bank, String issuer, String issueDate, String expirationDate) {
        this.id = id;
        this.name.set(name);
        this.bank.set(bank);
        this.issuer.set(issuer);
        this.issueDate.set(issueDate);
        this.expirationDate.set(expirationDate);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Properties
    public StringProperty nameProperty() { return name; }
    public StringProperty bankProperty() { return bank; }
    public StringProperty issuerProperty() { return issuer; }
    public StringProperty issueDateProperty() { return issueDate; }
    public StringProperty expirationDateProperty() { return expirationDate; }

    // Getters
    public String getName() { return name.get(); }
    public String getBank() { return bank.get(); }
    public String getIssuer() { return issuer.get(); }
    public String getIssueDate() { return issueDate.get(); }
    public String getExpirationDate() { return expirationDate.get(); }

    // Setters
    public void setName(String value) { name.set(value); }
    public void setBank(String value) { bank.set(value); }
    public void setIssuer(String value) { issuer.set(value); }
    public void setIssueDate(String value) { issueDate.set(value); }
    public void setExpirationDate(String value) { expirationDate.set(value); }
}
