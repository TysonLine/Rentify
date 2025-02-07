package com.SEG.rentify;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SEG.rentify.accounts.Account;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class LeaseItems extends AppCompatActivity {
    //initialize variables
    List<Product> productList = new ArrayList<>();
    Account account;

    EditText searchEditText;
    ListView resultsListView;
    Button sendRequests;
    Product selectedProduct = null;


    //initialize callback for products
    private void fetchProducts(Account account) {
        DatabaseOperations.fetchProducts(new DatabaseOperations.ProductsCallback() {
            @Override
            public void onProductsFetched(List<Product> products) {
                // Clear the existing list to avoid old products being displayed
                productList.clear();


                for (Product product : products) {
                    // Only add products that belong to the current account or are accessible by admin
                    //if ((Objects.equals(product.getAccount(), account.getId())) || account.getUsername().equals("admin")) {
                    productList.add(product);
                    //}
                }
                //productAdapter.notifyDataSetChanged();
            }
        });
    }

    //todo change to string of product so it displays name and stuff

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lease_items);

        // Initialize UI elements
        searchEditText = findViewById(R.id.searchEditText);
        resultsListView = findViewById(R.id.resultsListView);
        sendRequests = findViewById(R.id.sendRequestButton);

        // Validate account
        account = (Account) getIntent().getSerializableExtra("EXTRA_ACCOUNT");
        if (account == null) {
            Toast.makeText(this, "Account information is missing. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch products and initialize lists
        fetchProducts(account);

        // Set up ListView and search functionality
        List<Product> filteredList = new ArrayList<>(productList);
        ArrayAdapter<Product> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredList);
        resultsListView.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchTerm = s.toString().toLowerCase().trim();
                filteredList.clear();

                if (searchTerm.isEmpty()) {
                    filteredList.addAll(productList);
                } else {
                    for (Product product : productList) {
                        if (product.getName().toLowerCase().contains(searchTerm.toLowerCase()) || product.getCategory().toLowerCase().contains(searchTerm.toLowerCase()) ) {
                            filteredList.add(product);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        resultsListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedProduct = filteredList.get(position);
            Toast.makeText(this, "Selected: " + selectedProduct.getName(), Toast.LENGTH_SHORT).show();
        });

        sendRequests.setOnClickListener(v -> {
            if (selectedProduct == null) {
                Toast.makeText(this, "Please select a product first.", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("rentalRequests");
            String requestId = databaseReference.push().getKey();
            if (requestId == null) {
                Toast.makeText(this, "Error generating request ID.", Toast.LENGTH_SHORT).show();
                return;
            }

            Request newRequest = new Request(
                    requestId,
                    selectedProduct.getTime(),
                    selectedProduct.category,
                    "Please let me rent " + selectedProduct.getName(),
                    selectedProduct.getAccount(),
                    account,
                    selectedProduct,
                    "Pending"
            );

            databaseReference.child(requestId).setValue(newRequest)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Request sent successfully!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        Button home = findViewById(R.id.return_button2);
        home.setOnClickListener(view -> {
            Intent return1 = new Intent(LeaseItems.this, WelcomePage.class);
            return1.putExtra("EXTRA_ACCOUNT", account);
            return1.putExtra("ROLE", "Renter");
            startActivity(return1);
        });

        Button viewRequests = findViewById(R.id.viewRequestsButton);
        viewRequests.setOnClickListener(view -> {
            Intent return2 = new Intent(LeaseItems.this, SentRequests.class);
            return2.putExtra("EXTRA_ACCOUNT", account);
            startActivity(return2);
        });
    }
}
//read this before implementing the UI for search
//after the field, you will need a TextWatcher for the editText. you can make one with the "new" keyword
//this takes 3 parameters, which are all methods: