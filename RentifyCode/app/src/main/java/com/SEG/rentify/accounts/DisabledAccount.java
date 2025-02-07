package com.SEG.rentify.accounts;

public class DisabledAccount extends Account{

    String role;
    public DisabledAccount(String username, String password, String id, String role) {
        super(username, password, id);
        this.role = role;
    }
}
