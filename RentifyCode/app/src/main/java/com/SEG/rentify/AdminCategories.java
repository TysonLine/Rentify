package com.SEG.rentify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminCategories extends AppCompatActivity {

    private Button returnButton, addButton, editButton, deleteButton, itemsButton;
    private Spinner editItems, deleteItems;
    private DatabaseReference databaseReference;
    private List<String> categoryList;
    private List<String> categoryIds;
    private EditText categoryNameInput, categoryDescriptionInput, editCategoryNameInput, editCategoryDescriptionInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_categories);

        returnButton = findViewById(R.id.return_button2);
        addButton = findViewById(R.id.admin_add_button);
        editButton = findViewById(R.id.admin_edit_button);
        deleteButton = findViewById(R.id.admin_delete_button);
        itemsButton = findViewById(R.id.go_to_item_button);
        editItems = findViewById(R.id.admin_spinner);
        deleteItems = findViewById(R.id.admin_spinner2);
        categoryNameInput = findViewById(R.id.new_category_name);
        categoryDescriptionInput = findViewById(R.id.new_category_description);


        editCategoryNameInput = findViewById(R.id.edit_category_name); // Create these fields in your XML
        editCategoryDescriptionInput = findViewById(R.id.edit_category_description); // Create these fields in your XML

        databaseReference = FirebaseDatabase.getInstance().getReference("categories");
        categoryList = new ArrayList<>();
        categoryIds = new ArrayList<>();

        fetchCategories();


        returnButton.setOnClickListener(v -> startActivity(new Intent(AdminCategories.this, AndroidPage.class)));
        itemsButton.setOnClickListener(v -> startActivity(new Intent(AdminCategories.this, RentItems.class)));


        addButton.setOnClickListener(v -> {
            String name = categoryNameInput.getText().toString();
            String description = categoryDescriptionInput.getText().toString();
            addCategory(name, description);

            categoryNameInput.setText("");
            categoryDescriptionInput.setText("");
        });


        editButton.setOnClickListener(v -> {

            if (editCategoryNameInput.getVisibility() == View.VISIBLE && editCategoryDescriptionInput.getVisibility() == View.VISIBLE) {
                int selectedPosition = editItems.getSelectedItemPosition();
                if (selectedPosition >= 0) {
                    String categoryID = categoryIds.get(selectedPosition);
                    String newName = editCategoryNameInput.getText().toString();
                    String newDescription = editCategoryDescriptionInput.getText().toString();


                    editCategory(categoryID, newName, newDescription);


                    editCategoryNameInput.setText("");
                    editCategoryDescriptionInput.setText("");


                    editCategoryNameInput.setVisibility(View.GONE);
                    editCategoryDescriptionInput.setVisibility(View.GONE);
                }
            } else {

                editCategoryNameInput.setVisibility(View.VISIBLE);
                editCategoryDescriptionInput.setVisibility(View.VISIBLE);
            }
        });

        deleteButton.setOnClickListener(v -> {
            int selectedPosition = deleteItems.getSelectedItemPosition();
            if (selectedPosition >= 0) {
                String categoryID = categoryIds.get(selectedPosition);
                deleteCategory(categoryID);
            }
        });
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editItems.setAdapter(adapter);
        deleteItems.setAdapter(adapter);
    }

    private void addCategory(String name, String description) {
        String id = databaseReference.push().getKey();
        Category category = new Category(name, description, id);
        DatabaseOperations.createCategory(category);
        fetchCategories();
    }

    private void editCategory(String categoryID, String newName, String newDescription) {
        DatabaseOperations.editCategoryName(categoryID, newName);
        DatabaseOperations.editCategoryDescription(categoryID, newDescription);
        fetchCategories();
    }

    private void deleteCategory(String categoryID) {
        DatabaseOperations.deleteCategory(categoryID);
        fetchCategories();
    }
}
