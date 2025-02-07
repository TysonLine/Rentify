package com.SEG.rentify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

public class ManageRentRequests extends AppCompatActivity {

    private Spinner requestsSpinner;
    private Button returnButton, acceptButton, rejectButton;
    private DatabaseReference databaseReference;

    Account account;

    private List<Request> requestList = new ArrayList<>(); // To store all the requests

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_requests);

        Intent intent = getIntent();
        account = intent.getSerializableExtra("EXTRA_ACCOUNT", Account.class);

        // Initialize UI elements
        requestsSpinner = findViewById(R.id.requests_spinner);
        returnButton = findViewById(R.id.return_button2);
        acceptButton = findViewById(R.id.accept_request_button);
        rejectButton = findViewById(R.id.reject_request_button);

        // Set return button functionality
        returnButton.setOnClickListener(view -> {
                Intent return1 = new Intent(ManageRentRequests.this, WelcomePage.class);
                return1.putExtra("EXTRA_ACCOUNT", account);
                startActivity(return1);
        });

        // Load requests into the Spinner
        loadRequestsIntoSpinner();

        // Set accept/reject button click listeners
        acceptButton.setOnClickListener(v -> handleRequestAction("Accepted"));
        rejectButton.setOnClickListener(v -> handleRequestAction("Rejected"));
    }

    private void loadRequestsIntoSpinner() {
        databaseReference = FirebaseDatabase.getInstance().getReference("rentalRequests");

        List<String> requestDescriptions = new ArrayList<>();
        requestDescriptions.add("Loading requests...");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, requestDescriptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        requestsSpinner.setAdapter(adapter);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                requestList.clear();
                requestDescriptions.clear();

                for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                    Request request = requestSnapshot.getValue(Request.class);

                    if (request != null && isRequestForCurrentLessor(request)) {
                        requestList.add(request);

                        requestDescriptions.add("Product: " + request.getProduct().getName() +
                                "\n| Renter: " + request.getRenterAccount().getUsername());
                    }
                }

                if (requestDescriptions.isEmpty()) {
                    requestDescriptions.add("No requests found.");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ManageRentRequests.this, "Failed to load requests: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleRequestAction(String action) {
        int position = requestsSpinner.getSelectedItemPosition() - 1; // Adjust for "Loading requests..." placeholder
        if (position >= 0 && position < requestList.size()) {
            Request selectedRequest = requestList.get(position);

            if (selectedRequest != null) {
                // Update the status in Firebase
                databaseReference.child(selectedRequest.getId()).child("status").setValue(action)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Request " + action + " successfully!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } else {
            Toast.makeText(this, "Please select a valid request.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isRequestForCurrentLessor(Request request) {
        if (request == null || request.getProduct() == null || request.getProduct().getAccount() == null || account == null || account.getId() == null) {
            return false;
        }
        return request.getProduct().getAccount().equals(account.getId());
    }
}
