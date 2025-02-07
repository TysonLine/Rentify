package com.SEG.rentify;

import static com.SEG.rentify.DatabaseOperations.deleteAccount;
import static com.SEG.rentify.DatabaseOperations.disableUser;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SEG.rentify.accounts.Account;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AndroidPage extends AppCompatActivity {

    private Spinner userSpinner;
    private Spinner userSpinner2;
    private Button deleteUserButton;
    private Button disableAccountButton;
    private Button manageCategoriesButton;
    private DatabaseReference databaseReference;
    private List<Account> accountList;
    private List<String> usernameList;
    private List<String> userIdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize lists and Firebase reference
        accountList = new ArrayList<>();
        usernameList = new ArrayList<>();
        userIdList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Fetch accounts from Firebase
        fetchAccounts();

        // Set up layout and insets
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Spinner and Delete Button
        userSpinner = findViewById(R.id.user_spinner);
        userSpinner2 = findViewById(R.id.user_spinner2);
        deleteUserButton = findViewById(R.id.delete_account_button);
        disableAccountButton = findViewById(R.id.disable_account_button);
        manageCategoriesButton = findViewById(R.id.manage_categories_button);

        // Set delete button click listener
        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });

        disableAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {disableAccount();}
        });

        manageCategoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent admin_cat = new Intent(AndroidPage.this, AdminCategories.class);
                startActivity(admin_cat);
            }
        });
    }

    private void fetchAccounts() {
        DatabaseOperations.fetchAccount(new DatabaseOperations.AccountsCallback() {
            @Override
            public void onAccountsFetched(List<Account> accounts) {
                accountList.clear();
                usernameList.clear();
                userIdList.clear();

                for (Account account : accounts) {
                    String userId = account.getId(); // Assuming Account has a getId() method for userId
                    String username = account.getUsername(); // Assuming Account has getUsername()

                    accountList.add(account);
                    usernameList.add(username);
                    userIdList.add(userId); // Store userId for deletion
                }

                populateSpinner();
            }
        });
    }

    private void populateSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, usernameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(adapter);
        userSpinner2.setAdapter(adapter);
    }

    private void deleteUser() {
        int selectedPosition = userSpinner.getSelectedItemPosition();

        if (selectedPosition >= 0) {
            String selectedUserId = userIdList.get(selectedPosition);
            deleteAccount("id" + Integer.toString(selectedPosition));

            // Log the selectedUserId to ensure it's correct
            Toast.makeText(this, "Deleted User", Toast.LENGTH_SHORT).show();

            Log.d("DeleteUser", "Attempting to delete user with ID: " + selectedUserId);


        } else {
            Toast.makeText(this, "No user selected.", Toast.LENGTH_SHORT).show();
        }
    }
    private void disableAccount(){
        int selectedPosition = userSpinner2.getSelectedItemPosition();

        if (selectedPosition >= 0) {
            String selectedUserId = userIdList.get(selectedPosition);
            disableUser("id"+ Integer.toString(selectedPosition));

            // Log the selectedUserId to ensure it's correct
            Toast.makeText(this, "Disabled User", Toast.LENGTH_SHORT).show();

            Log.d("DisableUser", "Attempting to Disable user with ID: " + selectedUserId);


        } else {
            Toast.makeText(this, "No user selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeUserFromList(int position) {
        // Remove user from the local lists and update spinner
        accountList.remove(position);
        usernameList.remove(position);
        userIdList.remove(position);

        // Notify the spinner adapter of data changes
        populateSpinner();
    }
}
