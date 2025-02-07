package com.SEG.rentify;

import com.SEG.rentify.accounts.Account;

public class Request {
    private String id;
    private String time;
    private String category;
    private String description;
    private String lessorId;
    private Account renterAccount; // Corrected field name
    private Product product;
    private String status;

    public Request() {
        // Default constructor required for Firebase
    }

    public Request(String id, String time, String category, String description, String lessorId, Account renterAccount, Product product, String status) {
        this.id = id;
        this.time = time;
        this.category = category;
        this.description = description;
        this.lessorId = lessorId;
        this.renterAccount = renterAccount;
        this.product = product;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLessorId() {
        return lessorId;
    }

    public void setLessorId(String lessorId) {
        this.lessorId = lessorId;
    }

    public Account getRenterAccount() {
        return renterAccount; // Field name matches Firebase
    }

    public void setRenterAccount(Account renterAccount) {
        this.renterAccount = renterAccount;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
