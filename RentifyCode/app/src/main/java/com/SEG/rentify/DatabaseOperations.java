package com.SEG.rentify;




import android.content.Context;

import androidx.annotation.NonNull;

import com.SEG.rentify.accounts.Account;
import com.SEG.rentify.accounts.DisabledAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

import com.SEG.rentify.accounts.AdminAccount;
import com.SEG.rentify.accounts.LessorAccount;
import com.SEG.rentify.accounts.RenterAccount;
import com.SEG.rentify.Request;

import java.util.Objects;

public class DatabaseOperations {

    private final Context context;

    boolean addProductCheck;

    public DatabaseOperations(Context context) {
        this.context = context.getApplicationContext();
    }
    private static DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();


    //defines an interface so the asynchronous fetch code executes once data is ready
    public interface AccountsCallback {
        void onAccountsFetched(List<com.SEG.rentify.accounts.Account> accounts);
    }


    //fetches the accounts from the database and parses them into accounts

    //uses callback because if we directly return, we will et a blank list
    //this is because we did not give the data time to be fetched
    //doing it this way allows the data to be available only when fetched
    protected static void fetchAccount(final AccountsCallback callback){
        //creates the arrayList of accounts. Currently it is empty
        final List<com.SEG.rentify.accounts.Account> fetchedAccounts = new ArrayList<com.SEG.rentify.accounts.Account>();

        //set up the listener
        ValueEventListener accountsListener = new ValueEventListener() {
            @Override
            //called once when you fetch the data and again if data changes
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //parses through the json and creates instances of accounts
                for (DataSnapshot userSnapshot : snapshot.getChildren()){

                    //checks type of account
                    if(Objects.equals(userSnapshot.child("role").getValue(String.class), "user")){
                        String username = userSnapshot.child("username").getValue(String.class);
                        String password = userSnapshot.child("password").getValue(String.class);
                        String id = userSnapshot.getKey();
                        Boolean disabled = userSnapshot.child("disabled").getValue(Boolean.class);

                        com.SEG.rentify.accounts.LessorAccount tempUser = new com.SEG.rentify.accounts.LessorAccount(username, password, id);
                        if (!fetchedAccounts.contains(tempUser)){
                            fetchedAccounts.add(tempUser);
                        }

                    } else if (Objects.equals(userSnapshot.child("role").getValue(String.class), "renter")){
                        String username = userSnapshot.child("username").getValue(String.class);
                        String password = userSnapshot.child("password").getValue(String.class);
                        String id = userSnapshot.getKey();
                        Boolean disabled = userSnapshot.child("disabled").getValue(Boolean.class);

                        com.SEG.rentify.accounts.RenterAccount tempUser = new com.SEG.rentify.accounts.RenterAccount(username, password, id);
                        if (!fetchedAccounts.contains(tempUser)){
                            fetchedAccounts.add(tempUser);
                        }

                    } else if (Objects.equals(userSnapshot.child("role").getValue(String.class), "admin")){
                        com.SEG.rentify.accounts.AdminAccount adminAccount = new AdminAccount();
                        if (!fetchedAccounts.contains(adminAccount)){
                            fetchedAccounts.add(adminAccount);
                        }

                    } else {
                        System.out.println("for internal only: unrecognized account detected on the cloud");
                    }
                }

                //this basically returns the data via callback
                callback.onAccountsFetched(fetchedAccounts);
            }
            //returns error if there is an error
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("DatabaseError: " + error.getMessage());
            }
        };


        //all the previous code is just making the listener, ths line assigns the listener to the databse
        mDatabase.child("accounts").addValueEventListener(accountsListener);
    }

    protected static void postAccounts(com.SEG.rentify.accounts.Account account, String role) {
        mDatabase.child("accounts").child(account.getId()).child("username").setValue(account.getUsername());
        mDatabase.child("accounts").child(account.getId()).child("password").setValue(account.getPassword());
        mDatabase.child("accounts").child(account.getId()).child("role").setValue(role);


    }

    public static void deleteAccount(String userID) {
        mDatabase.child("accounts").child(userID).removeValue();
    }

    public static void disableUser(String userID) {
        //todo change from boolean to make new account called disable account (transfer data) and remove current account.
        DatabaseReference userRef = mDatabase.child("accounts").child(userID);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String username = task.getResult().child("username").getValue(String.class);
                String password = task.getResult().child("password").getValue(String.class);
                String id = task.getResult().child("id").getValue(String.class);
                String role = task.getResult().child("role").getValue(String.class);


                DisabledAccount disabledAccount = new DisabledAccount(username, password, id, role);

                mDatabase.child("disabled_accounts").child(userID).setValue(disabledAccount)
                        .addOnCompleteListener(disableTask -> {
                            if (disableTask.isSuccessful()) {
                                // Remove the original account data
                                userRef.removeValue().addOnCompleteListener(removeTask -> {
                                    if (removeTask.isSuccessful()) {
                                        System.out.println("User account disabled and moved to disabled_accounts.");
                                    } else {
                                        System.out.println("Failed to delete original account data.");
                                    }
                                });
                            } else {
                                System.out.println("Failed to save disabled account data.");
                            }
                        });
            } else {
                System.out.println("User not found or failed to retrieve user data.");
            }
        });
    }

    public static void enableUser(String userID) {
        DatabaseReference disabledUserRef = mDatabase.child("disabled_accounts").child(userID);
        DatabaseReference userRef = mDatabase.child("accounts").child(userID);


        disabledUserRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {

                String username = task.getResult().child("username").getValue(String.class);
                String password = task.getResult().child("password").getValue(String.class);
                String id = task.getResult().child("id").getValue(String.class);
                String role = task.getResult().child("role").getValue(String.class);
                Account enabledAccount;


                switch (role) {
                    case "user":
                        enabledAccount = new LessorAccount(username, password, id);
                        break;
                    case "renter":
                        enabledAccount = new RenterAccount(username, password, id);
                        break;
                    case "admin":
                        enabledAccount = new AdminAccount();
                        break;
                    default:
                        System.out.println("Unrecognized role, defaulting to regular user.");
                        enabledAccount = new LessorAccount(username, password, id);
                        break;
                }

                userRef.setValue(enabledAccount).addOnCompleteListener(enableTask -> {
                    if (enableTask.isSuccessful()) {
                        // Remove the disabled account data after successful transfer
                        disabledUserRef.removeValue().addOnCompleteListener(removeTask -> {
                            if (removeTask.isSuccessful()) {
                                System.out.println("User account enabled and moved back to accounts.");
                            } else {
                                System.out.println("Failed to delete disabled account data.");
                            }
                        });
                    } else {
                        System.out.println("Failed to restore user account data.");
                    }
                });
            } else {
                System.out.println("Disabled user not found or failed to retrieve data.");
            }
        });
    }


    public static void createCategory(Category category){
        mDatabase.child("categories").child(category.getId()).child("name").setValue(category.getName());
        mDatabase.child("categories").child(category.getId()).child("description").setValue(category.getDescription());
    }

    public static void editCategoryName(String categoryID, String newCategoryName){
        mDatabase.child("categories").child(categoryID).child("name").setValue(newCategoryName);
    }

    public static void editCategoryDescription(String categoryID, String newCategoryDescription){
        mDatabase.child("categories").child(categoryID).child("description").setValue(newCategoryDescription);
    }
    //hi

    public static void deleteCategory(String categoryID){
        mDatabase.child("categories").child(categoryID).removeValue();

        mDatabase.child("categories").child(categoryID).get()
                .addOnSuccessListener(categorySnapshot -> {
                    if (categorySnapshot.exists()) {
                        String categoryName = categorySnapshot.child("name").getValue(String.class);

                        //delete products with the same category
                        mDatabase.child("products").get()
                                .addOnSuccessListener(productsSnapshot -> {
                                    for (DataSnapshot productSnapshot : productsSnapshot.getChildren()) {
                                        String productCategory = productSnapshot.child("category").getValue(String.class);
                                        if (categoryName != null && categoryName.equals(productCategory)) {
                                            productSnapshot.getRef().removeValue();
                                        }
                                    }
                                });
                    }
                });


    };


    public interface ProductsCallback {
        void onProductsFetched(List<Product> products);
    }

    //todo: get this to work with products
    protected static void fetchProducts(final ProductsCallback callback){
        //creates the arrayList of products.

        //logic outside to make sure the category is correct
        final List<Product> fetchedProducts = new ArrayList<Product>();

        //set up the listener
        ValueEventListener accountsListener = new ValueEventListener() {
            @Override
            //called once when you fetch the data and again if data changes
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fetchedProducts.clear();

                //parses through the json and creates instances of products
                for (DataSnapshot productSnapshot : snapshot.getChildren()){
                    String id = productSnapshot.child("id").getValue(String.class);
                    String name = productSnapshot.child("name").getValue(String.class);
                    Double price = productSnapshot.child("price").getValue(Double.class);
                    String time = productSnapshot.child("time").getValue(String.class);
                    String category = productSnapshot.child("category").getValue(String.class);
                    String description = productSnapshot.child("description").getValue(String.class);
                    String accountID = productSnapshot.child("account").getValue(String.class);
                    Product product = new Product(id, name, price, time, category, description, accountID);
                    fetchedProducts.add(product);
                }

                //this basically returns the data via callback
                callback.onProductsFetched(fetchedProducts);
            }
            //returns error if there is an error
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.err.println("DatabaseError: " + error.getMessage());
            }
        };
        //all the previous code is just making the listener, ths line assigns the listener to the databse
        mDatabase.child("products").addValueEventListener(accountsListener);
    }




    public static void updateProduct(String id, String name, double price, String time, String category, String description, Account account) {

        Product product = new Product(id, name, price, time, category, description, account.getId());
        mDatabase.child("products").child(id).get().addOnSuccessListener(dataSnapshot -> {
            Product existingProduct = dataSnapshot.getValue(Product.class);


            if (existingProduct != null &&
                    (account.getUsername().equals("admin") && account.getPassword().equals("XPI76SZUqyCjVxgnUjm0")) ||
                    account.getId().equals(existingProduct.getAccount())) {
                Product updatedProduct = new Product(id, name, price, time, category, description, account.getId());
                mDatabase.child("products").child(id).setValue(updatedProduct);
                System.out.println("Product updated successfully.");
            } else {
                System.out.println("Update failed: Creators do not match or product not found.");
            }

        }).addOnFailureListener(e -> {
            System.out.println("Database error: " + e.getMessage());
        });
    }

    public static void deleteProduct(String id, Account account) {
        DatabaseReference productRef = mDatabase.child("products").child(id);

        // Retrieve the product to check if it exists and if the creator matches
        productRef.get().addOnSuccessListener(dataSnapshot -> {
            Product existingProduct = dataSnapshot.getValue(Product.class);

            if (existingProduct != null &&
                    (account.getUsername().equals("admin") && account.getPassword().equals("XPI76SZUqyCjVxgnUjm0")) ||
                    account.getId().equals(existingProduct.getAccount())) {
                // Product exists, and creator matches, so delete it
                productRef.removeValue();
                System.out.println("Product deleted successfully.");
            } else {
                System.out.println("Delete failed: Product not found or creator does not match.");
            }
        }).addOnFailureListener(e -> {
            System.out.println("Database error: " + e.getMessage());
        });
    }

    public static void addProduct(String name, double price, String time, String category, String description, String accountID) {
//        String name = editText.getText().toString().trim();
//        double price = Double.parseDouble(String.valueOf(editPrice.getText().toString()));
        String id = mDatabase.child("products").push().getKey();
        Product product = new Product(id, name, price, time,description,category, accountID);
        mDatabase.child("products").child(id).setValue(product);
    }

    public static void acceptRequest(Request request) {
        // Validate the request
        if (request == null || request.getProduct() == null) {
            System.out.println("Invalid request or product.");
            return;
        }

        // Extract details from the request
        String requestId = request.getId();
        Product product = request.getProduct();
        String productId = product.getId();

        // Reference to the database
        DatabaseReference productRef = mDatabase.child("products").child(productId);
        DatabaseReference rentedItemsRef = mDatabase.child("rented_items").child(productId);
        DatabaseReference requestRef = mDatabase.child("requests").child(requestId);

        // Move product to rented_items
        productRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                // Convert the product into a RentedItems object
                RentedItems rentedItem = new RentedItems(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        request.getTime(), // Use the time from the request
                        request.getCategory(), // Use the category from the request
                        product.getDescription(),
                        product.getAccount() // Use the account ID of the lessor
                );

                // Save the rented item to the rented_items node
                rentedItemsRef.setValue(rentedItem).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Remove the original product from the products node
                        productRef.removeValue().addOnCompleteListener(removeTask -> {
                            if (removeTask.isSuccessful()) {
                                // Remove the request
                                requestRef.removeValue().addOnCompleteListener(requestRemoveTask -> {
                                    if (requestRemoveTask.isSuccessful()) {
                                        System.out.println("Request accepted: Product moved to rented_items, and request removed.");
                                    } else {
                                        System.out.println("Failed to remove the request after accepting.");
                                    }
                                });
                            } else {
                                System.out.println("Failed to remove the original product from products.");
                            }
                        });
                    } else {
                        System.out.println("Failed to save the rented item to rented_items.");
                    }
                });
            } else {
                System.out.println("Product not found in the database.");
            }
        }).addOnFailureListener(e -> {
            System.out.println("Database error: " + e.getMessage());
        });
    }



    public static void denyRequest(Request request) {
        // Validate the request
        if (request == null || request.getId() == null) {
            System.out.println("Invalid request.");
            return;
        }

        // Reference to the specific request in the database
        String requestId = request.getId();
        DatabaseReference requestRef = mDatabase.child("requests").child(requestId);

        // Remove the request from the database
        requestRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                System.out.println("Request successfully denied and removed from the database.");
            } else {
                System.out.println("Failed to remove the request.");
            }
        }).addOnFailureListener(e -> {
            System.out.println("Database error while removing request: " + e.getMessage());
        });
    }


    public static void stopRenting(String rentedItemId) {
        // Validate input
        if (rentedItemId == null || rentedItemId.isEmpty()) {
            System.out.println("Invalid rented item ID.");
            return;
        }

        // Reference to the database nodes
        DatabaseReference rentedItemRef = mDatabase.child("rented_items").child(rentedItemId);
        DatabaseReference productsRef = mDatabase.child("products").child(rentedItemId);

        // Fetch the rented item details
        rentedItemRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                // Retrieve rented item data
                RentedItems rentedItem = dataSnapshot.getValue(RentedItems.class);

                if (rentedItem != null) {
                    // Convert the rented item back to a product
                    Product product = new Product(
                            rentedItem.getId(),
                            rentedItem.getName(),
                            rentedItem.getPrice(),
                            rentedItem.getTime(), // Retain the same time or update as needed
                            rentedItem.getCategory(),
                            rentedItem.getDescription(),
                            rentedItem.getAccount() // Retain the same account ID
                    );

                    // Save the product back to the products node
                    productsRef.setValue(product).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Remove the rented item from the rented_items node
                            rentedItemRef.removeValue().addOnCompleteListener(removeTask -> {
                                if (removeTask.isSuccessful()) {
                                    System.out.println("Rented item successfully moved back to products.");
                                } else {
                                    System.out.println("Failed to remove the rented item after moving it back to products.");
                                }
                            });
                        } else {
                            System.out.println("Failed to save the product back to products.");
                        }
                    });
                } else {
                    System.out.println("Failed to retrieve rented item details.");
                }
            } else {
                System.out.println("Rented item not found in the database.");
            }
        }).addOnFailureListener(e -> {
            System.out.println("Database error: " + e.getMessage());
        });
    }
}
