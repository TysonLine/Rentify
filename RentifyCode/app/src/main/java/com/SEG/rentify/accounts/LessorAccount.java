package com.SEG.rentify.accounts;

public class LessorAccount extends Account {
    private String role;
    //todo: make listings list

    //constructor
    public LessorAccount(String username, String password, String id){
        super(username, password,id);
        this.role = "user";
    }

    //getter
    public String getRole(){
        return this.role;
    }

}
