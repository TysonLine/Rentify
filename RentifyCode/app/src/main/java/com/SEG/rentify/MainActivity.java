package com.SEG.rentify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SEG.rentify.accounts.Account;
import com.SEG.rentify.accounts.AdminAccount;
import com.SEG.rentify.accounts.LessorAccount;
import com.SEG.rentify.accounts.RenterAccount;

import java.util.ArrayList;
import java.util.List;




public class MainActivity extends AppCompatActivity {

    //declares the list of accounts
    private List<Account> accountList;
    private String role;
    private Account account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //initializes the account list as an empty list
        accountList = new ArrayList<>();

        // Fetch accounts from Firebase
        fetchAccounts();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //fetches it using the method in the utility class DatabaseOperations
    private void fetchAccounts() {
        DatabaseOperations.fetchAccount(new DatabaseOperations.AccountsCallback() {
            @Override
            public void onAccountsFetched(List<Account> accounts) {
                // Update the accountList with the fetched data
                accountList.clear(); // Clear any existing data
                accountList.addAll(accounts); // Add the fetched accounts to the list

            }
        });
    }

    private boolean checkUserCredentials(String username, String password) {
        boolean isLoginSuccessful = false;

        for (Account account : accountList) {
            // Check if the username matches
            if (account.getUsername().equals(username)) {
                // Username found, now check the password
                if (account.getPassword().equals(password)) {
                    role = account.getClass().toString();
                    isLoginSuccessful = true; // Login successful
                    this.account = account;
                    break; // Exit loop since we found the correct account
                }
                // If password is incorrect, set isLoginSuccessful to false
                break; // Exit loop since the user exists but password is wrong
            }
        }
        return isLoginSuccessful; //todo: also return the account in like a tuple so that we can pass the object into the intent
    }

    //onClick function for the sign in button
    public void login(View view) {

        //notTodo: go though the file and find something that matches with the username and password. if it doesnt match then alert the user
        //^ done, now it checks firebase and sees if its right. new todo: create dummy data on firebase
        //todo: detect null value to prevent crash


        //gets the 2 input boxes from the main activity and then store the value inside
        TextView usernameView = findViewById(R.id.username_input);
        TextView passwordView = findViewById(R.id.password_input);

        //assign the values from the input boxes to the variables
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        if((username.equals("admin"))&&(password.equals("XPI76SZUqyCjVxgnUjm0"))){
            Intent intent_admin = new Intent(MainActivity.this, AndroidPage.class);
            startActivity(intent_admin);

        }

        else if (checkUserCredentials(username, password)) {
            //create intent and feed the values into the intent. This will get picked up by WelcomePage
            Intent intent = new Intent(MainActivity.this, WelcomePage.class);
            intent.putExtra("EXTRA_ACCOUNT", account);

            if (role.equals(RenterAccount.class.toString())) {
                role = "Renter"; // Set role as "Renter" before passing it
            } else if (role.equals(LessorAccount.class.toString())) {
                role = "Lessor";
            }

            intent.putExtra("ROLE", role); // Pass role to WelcomePage
            startActivity(intent); // Redirect to WelcomePage
        } else {
            TextView loginError = findViewById(R.id.login_error);
            loginError.setText("Incorrect Login Information");
        }

    }


        //onClick for createAccount button
        public void createAccount (View view) {
            Intent intent = new Intent(MainActivity.this, CreateAccount.class);
            startActivity(intent);
        }

}