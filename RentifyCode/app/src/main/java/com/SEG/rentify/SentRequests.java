package com.SEG.rentify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SEG.rentify.accounts.Account;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SentRequests extends AppCompatActivity {

    private Button returnButton;
    private ListView pendingRequestsListView, acceptedRequestsListView;

    Account account;

    private List<String> pendingRequests = new ArrayList<>();
    private List<String> acceptedRequests = new ArrayList<>();

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requested_items);

        // Retrieve the account from the Intent
        Intent intent = getIntent();
        account = intent.getSerializableExtra("EXTRA_ACCOUNT", Account.class);

        if (account == null) {
            Toast.makeText(this, "Account information is missing. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI elements
        pendingRequestsListView = findViewById(R.id.pending_items);
        acceptedRequestsListView = findViewById(R.id.accepted_items);
        returnButton = findViewById(R.id.return_button2);

        // Set up return button functionality
        returnButton.setOnClickListener(view -> {
            Intent returnIntent = new Intent(SentRequests.this, LeaseItems.class);
            returnIntent.putExtra("EXTRA_ACCOUNT", account);
            startActivity(returnIntent);
        });

        // Fetch pending and accepted requests from Firebase
        fetchRequests();
    }

    private void fetchRequests() {
        databaseReference = FirebaseDatabase.getInstance().getReference("rentalRequests");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pendingRequests.clear();
                acceptedRequests.clear();

                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    Request request = requestSnapshot.getValue(Request.class);

                    if (request != null) {
                        // Debug log to show the request data
                        android.util.Log.d("FetchRequests", "Request: " + requestSnapshot.getValue());

                        if (request.getRenterAccount() != null && request.getRenterAccount().getId() != null && account.getId() != null) {
                            if (request.getRenterAccount().getId().equals(account.getId())) {
                                // Filter based on the status
                                if ("Pending".equals(request.getStatus())) {
                                    pendingRequests.add("Product: " + request.getProduct().getName());
                                } else if ("Accepted".equals(request.getStatus())) {
                                    acceptedRequests.add("Product: " + request.getProduct().getName());
                                }
                            }
                        } else {
                            // Log incomplete requests for debugging
                            android.util.Log.w("FetchRequests", "Incomplete request data: " + requestSnapshot.getValue());
                        }
                    }
                }

                // Update ListViews
                updateListView(pendingRequestsListView, pendingRequests);
                updateListView(acceptedRequestsListView, acceptedRequests);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(SentRequests.this, "Failed to load requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateListView(ListView listView, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
    }
}
