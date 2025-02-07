package com.SEG.rentify;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SEG.rentify.accounts.Account;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import android.widget.EditText;
import android.widget.ListView;


import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

public class WelcomePage extends AppCompatActivity {
    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;

    List<Product> products;
    DatabaseReference databaseProducts;

    String role;
    String username;
    String password;
    Account account;


    //todo:write some sort of json file and parses them to create instances of various accounts.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-Edge UI setup
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Retrieve intent extras
        Intent intent = getIntent();
        account = intent.getSerializableExtra("EXTRA_ACCOUNT", Account.class);
        this.username = account.getUsername();
        this.password = account.getPassword();
        role = intent.getStringExtra("ROLE");

        // Display username and role
        TextView tv1 = findViewById(R.id.welcome_role);
        tv1.append(username);

        TextView tv2 = findViewById(R.id.user_role_welcome);
        tv2.append(role);

        // Set up the LessorButton click listener
        Button rentItemsButton = findViewById(R.id.LessorButton);
        rentItemsButton.setOnClickListener(view -> {
            if ("Lessor".equals(role) || "Admin".equals(role)) {
                Intent rentItemsIntent = new Intent(WelcomePage.this, RentItems.class);
                rentItemsIntent.putExtra("EXTRA_ACCOUNT", account);
                startActivity(rentItemsIntent);
//            } else if ("Renter".equals(role)) {
//                Intent rentItemsIntent = new Intent(WelcomePage.this, LeaseItems.class);
//                startActivity(rentItemsIntent);
            } else {
                Toast.makeText(WelcomePage.this, "Access denied. You do not have permission to access this page.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the rentButton click listener
        Button renterPage = findViewById(R.id.rentButton);
        renterPage.setOnClickListener(view -> {
            if ("Renter".equals(role) || "Admin".equals(role)) {
                Intent rentPage = new Intent(WelcomePage.this, LeaseItems.class);
                rentPage.putExtra("EXTRA_ACCOUNT", account);
                startActivity(rentPage);
            }
            else{
                Toast.makeText(WelcomePage.this, "Access denied. You do not have permission to access this page.", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the manageRequests click listener
        Button manageRequests = findViewById(R.id.manageRequestButton);
        manageRequests.setOnClickListener(view -> {
            if ("Lessor".equals(role) || "Admin".equals(role)) {
                Intent rentPage = new Intent(WelcomePage.this, ManageRentRequests.class);
                rentPage.putExtra("EXTRA_ACCOUNT", account);
                startActivity(rentPage);
            } else {
                Toast.makeText(WelcomePage.this, "Access denied. You do not have permission to access this page.", Toast.LENGTH_SHORT).show();
            }

        });

    }

}
