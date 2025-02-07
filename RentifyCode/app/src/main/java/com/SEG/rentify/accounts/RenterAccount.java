package com.SEG.rentify.accounts;

public class RenterAccount extends Account {
    private String role;

    //constructor
    public RenterAccount(String username, String password, String id){
        super(username, password, id);
        this.role = "renter";
    }

    //getter
    public String getRole(){
        return this.role;
    }
}
