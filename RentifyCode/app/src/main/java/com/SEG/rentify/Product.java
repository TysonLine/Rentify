package com.SEG.rentify;

import com.SEG.rentify.accounts.Account;

public class Product {
    private String id;
    private String name;
    private double price;
    String time;
    String category;
    String description;
    String accountId;

    public Product(String id, String name, double price, String time, String category, String description, String accountId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.time = time; //need some sort of date format;
        this.category = category;
        this.description = description;
        this.accountId = accountId;
    }

    public Product(){

    }
    //getters and setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public String getAccount(){
        return this.accountId;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public void setAccount(String account){
        this.accountId = account;
    }

    @Override
    public String toString(){
        return "name: " + name + " Price: $" + price;
    }
}

