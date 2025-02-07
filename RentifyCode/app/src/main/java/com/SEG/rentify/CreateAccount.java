package com.SEG.rentify;

import static com.SEG.rentify.DatabaseOperations.postAccounts;

import android.os.Bundle;

import android.content.Intent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SEG.rentify.accounts.Account;
import com.SEG.rentify.accounts.LessorAccount;
import com.SEG.rentify.accounts.RenterAccount;

import java.util.ArrayList;
import java.util.List;


public class CreateAccount extends AppCompatActivity {
    //declares the list of accounts
    private List<Account> accountList;

    private String accountType;

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

    protected void onCreate(Bundle savedInstanceState) {

        //initializes the account list as an empty list
        accountList = new ArrayList<>();

        // Fetch accounts from Firebase
        fetchAccounts();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);

        //initialize the spinner
        Spinner accountTypeSpinner = findViewById(R.id.account_type); // Make sure the ID matches
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.account_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accountTypeSpinner.setAdapter(adapter);

        //Set up listener
        accountTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedAccountType = parentView.getItemAtPosition(position).toString();
                accountType = selectedAccountType;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                TextView error = (TextView) findViewById(R.id.creationError);
                error.setText("Please select an account type");
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    //Checks if 2 passwords match
    private boolean checkPasswordsMatch(String pw1, String pw2){

        return pw1.equals(pw2);

    }

    //check if username already exists
    private boolean checkUsername(String username){
        for(Account account: accountList){

            if (username.equals(account.getUsername())){
                return false;
            }
        }
        return true;
    }

    private void accountCreation(String username, String password){
        int lastIDNumber = 0;
        if (!accountList.isEmpty()){
            for(Account account: accountList) {
                if (Integer.parseInt(account.getId().replaceAll("[^0-9]", "")) > lastIDNumber) {
                    lastIDNumber = Integer.parseInt(account.getId().replaceAll("[^0-9]", ""));
                }
            }
        }

        lastIDNumber ++;

        //create the java object for the account and send it to the database
        if (accountType.equals("Renter account")){
            RenterAccount temp = new RenterAccount(username, password, "id" + lastIDNumber);
            postAccounts(temp, "renter");
        } else {
            LessorAccount temp = new LessorAccount(username, password, "id" + lastIDNumber);
            postAccounts(temp, "user");
        }

        //create intent and feed the values into the intent. This will get picked up by WelcomePage
        Intent intent = new Intent(CreateAccount.this, MainActivity.class);
        intent.putExtra("EXTRA_USERNAME", username);
        intent.putExtra("EXTRA_PASSWORD", password);
        startActivity(intent);
    }



    //Gets the username and password if the passwords match, and returns to main screen
    public void makeNewAccount(View view){




        TextView usernameCreateView = findViewById(R.id.username_input2);
        TextView pwView = findViewById(R.id.editTextTextPassword);
        TextView pwView2 = findViewById(R.id.editTextTextPassword2);

        String username = usernameCreateView.getText().toString();
        String password = pwView.getText().toString();
        String confirmPassword = pwView2.getText().toString();

        boolean passwordMatch = false;
        boolean nameClear = false;
        String role;
        if(accountList.isEmpty()){
            accountCreation(username, password);
        }

        if (checkPasswordsMatch(password, confirmPassword)) {
            passwordMatch = true;

        } else {
            TextView error = (TextView) findViewById(R.id.creationError);
            error.setText("Passwords Do Not Match.");
        }

        if (checkUsername(username)) {
            nameClear = true;

        } else {
            TextView error = (TextView) findViewById(R.id.creationError);
            error.setText("Username Already Exists");
        }


        if (passwordMatch && nameClear){

            accountCreation(username, password);

        }



    }

}
