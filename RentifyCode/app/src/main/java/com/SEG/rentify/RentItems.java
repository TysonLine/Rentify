package com.SEG.rentify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SEG.rentify.accounts.Account;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class RentItems extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private ArrayAdapter<Product> productAdapter; // Declare the adapter at the class level
    EditText editTextName;
    EditText editTextCategory;
    EditText editTextPrice;
    EditText editTextTime;
    Button buttonAddProduct;
    EditText description;
    private List<String> categoryList;
    private List<String> categoryIds;
    ListView listViewProducts;
    Spinner spinner;
    List<Product> productList = new ArrayList<>();
    ImageView targetImage;
    String username;
    Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("RentItems", "Inside onCreate");
        Intent intent = getIntent();
        account = intent.getSerializableExtra("EXTRA_ACCOUNT", Account.class);
        this.username = account.getUsername();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.rent_items);

        listViewProducts = findViewById(R.id.ListViewProducts); // Find the ListView after setContentView
        productAdapter = new ProductAdapter(RentItems.this, productList);  // Initialize the adapter

        listViewProducts.setAdapter(productAdapter); // Set the adapter for ListView
        fetchProducts(account);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.EditTextCategory), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("categories");

        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        listViewProducts = findViewById(R.id.ListViewProducts);
        buttonAddProduct = findViewById(R.id.addButton);
        editTextTime = findViewById(R.id.editTextTime);
        description = findViewById(R.id.Description);
        Button buttonLoadImage = findViewById(R.id.loadImage);
        targetImage = findViewById(R.id.targetimage);
        spinner = findViewById(R.id.editTextCategory);
        categoryList = new ArrayList<>();
        categoryIds = new ArrayList<>();

        fetchCategories();

        buttonAddProduct.setOnClickListener(view -> {
            if (isInputValid()) {
                String selectedCategory = spinner.getSelectedItem().toString();
                double price = Double.parseDouble(editTextPrice.getText().toString().trim());

                DatabaseOperations.addProduct(
                        editTextName.getText().toString().trim(),
                        price,
                        editTextTime.getText().toString().trim(),
                        description.getText().toString().trim(),
                        selectedCategory,
                        account.getId()
                );

                clearInputFields();
                Toast.makeText(RentItems.this, "Product added successfully!", Toast.LENGTH_SHORT).show();

                // Clear and fetch updated products
                productList.clear();
                productAdapter.notifyDataSetChanged();
                fetchProducts(account);
            }
        });


        buttonLoadImage.setOnClickListener(arg0 -> {
            Intent intent1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent1, 0);
        });



        listViewProducts.setOnItemClickListener((parent, view, position, id) -> {

            if (position < 0 || position >= productList.size()) {
                Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show();
                return;
            }

            Product selectedProduct = productList.get(position);

            // Pre-fill the form with selected product details
            editTextName.setText(selectedProduct.getName());
            editTextPrice.setText(String.valueOf(selectedProduct.getPrice()));
            editTextTime.setText(selectedProduct.getTime());
            description.setText(selectedProduct.getDescription());

            // Set category spinner to the selected product's category
            String selectedCategory = selectedProduct.getCategory();
            int spinnerPosition = ((ArrayAdapter<String>) spinner.getAdapter()).getPosition(selectedCategory);
            spinner.setSelection(spinnerPosition);

            // Update button logic to handle editing
            buttonAddProduct.setText("Update Product");

            buttonAddProduct.setOnClickListener(view1 -> {
                if (isInputValid()) {
                    String selectedCategoryForUpdate = spinner.getSelectedItem().toString();
                    double price = Double.parseDouble(editTextPrice.getText().toString().trim());

                    // Update product logic
                    DatabaseOperations.updateProduct(
                            selectedProduct.getId(),
                            editTextName.getText().toString().trim(),
                            price,
                            editTextTime.getText().toString().trim(),
                            selectedCategoryForUpdate,
                            description.getText().toString().trim(),
                            account
                    );

                    clearInputFields();
                    Toast.makeText(RentItems.this, "Product updated successfully!", Toast.LENGTH_SHORT).show();

                    // Reset the button text after updating
                    buttonAddProduct.setText("Add Product");

                    // Clear productList before fetching new data
                    productList.clear();
                    productAdapter.notifyDataSetChanged();
                    // Fetch updated products
                    fetchProducts(account);

                    // Ensure adapter is updated after fetching new data
                    productAdapter.notifyDataSetChanged();
                }
            });
        });



        listViewProducts.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= productList.size()) {
                Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show();
                return false;
            }

            Product selectedProduct = productList.get(position);

            new android.app.AlertDialog.Builder(this)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete this product?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        productList.remove(selectedProduct);  // Remove the item from the list

                        // Update the adapter after removing the product
                        productAdapter.notifyDataSetChanged();

                        // Delete the product from Firebase
                        DatabaseOperations.deleteProduct(selectedProduct.getId(), account);




                        Toast.makeText(this, "Product deleted successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();

            return true; // Mark the event as handled
        });




    }

    private boolean isInputValid() {
        if (isEditTextEmpty(editTextName) || isEditTextEmpty(editTextPrice) || isEditTextEmpty(editTextTime) || isEditTextEmpty(description)) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if ("Select category".equals(spinner.getSelectedItem().toString())) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Double.parseDouble(editTextPrice.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price. Please enter a valid number.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isEditTextEmpty(EditText editText) {
        return Objects.equals(editText.getText().toString().trim(), "");
    }

    private void clearInputFields() {
        editTextName.setText("");
        editTextPrice.setText("");
        editTextTime.setText("");
        description.setText("");
        spinner.setSelection(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri targetUri = data.getData();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                targetImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchCategories() {
        databaseReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categoryList.clear();
                categoryIds.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String categoryName = snapshot.child("name").getValue(String.class);
                    String categoryId = snapshot.getKey();
                    categoryList.add(categoryName);
                    categoryIds.add(categoryId);
                }
                populateSpinners();
            }
        });
    }

    private void populateSpinners() {
        categoryList.add(0, "Select category");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
    }

    private void fetchProducts(Account account) {
        DatabaseOperations.fetchProducts(new DatabaseOperations.ProductsCallback() {
            @Override
            public void onProductsFetched(List<Product> products) {
                // Clear the existing list to avoid old products being displayed
                productList.clear();


                for (Product product : products) {
                    // Only add products that belong to the current account or are accessible by admin
                    if ((Objects.equals(product.getAccount(), account.getId())) || account.getUsername().equals("admin")) {
                        productList.add(product);
                    }
                }
                productAdapter.notifyDataSetChanged();
            }
        });
    }



    //read this before implementing the UI for search
    //after the field, you will need a TextWatcher for the editText. you can make one with the "new" keyword
    //this takes 3 parameters, which are all methods:






}

