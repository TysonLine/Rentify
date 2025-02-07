package com.SEG.rentify.accounts;

import java.io.Serializable;

public class Account implements Serializable {
    private String username;
    private String password;
    private String id;

    Account(){}
    Account(String username, String password, String id){
        this.username = username;
        this.password = password;
        this.id = id;
    }
    public String getUsername() { return username; }

    public String getPassword() {
        return password;
    }

    public String getId() { return id; }

}
