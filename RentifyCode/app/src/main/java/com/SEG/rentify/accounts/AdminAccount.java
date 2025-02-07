package com.SEG.rentify.accounts;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminAccount extends Account {
    private String role;
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    //constructor
    public AdminAccount(){
        super("admin", "XPI76SZUqyCjVxgnUjm0", "admin");//assigns pre-determined value for username and password
        this.role = "admin";
    }

    //getter
    public String getRole(){
        return this.role;
    }

    //todo: eventually implement methods for admin
    public void removeUser(String userId, OnCompleteListener<Void> onCompleteListener){
        db.child("user").child(userId).removeValue().addOnCompleteListener(onCompleteListener);
    }

    //public void disableUser(String userId, OnCompleteListener<Void> onCompleteListener){

    //}

    /*
    public void enableUser(String userId, OnCompleteListener<Void> onCompleteListener){
        db.child("user").child(userId).setValue(setDisable(false));
    }
    */


}
