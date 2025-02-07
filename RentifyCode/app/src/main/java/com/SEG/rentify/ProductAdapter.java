package com.SEG.rentify;

import android.widget.ArrayAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {
    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        super(context, 0, productList); // Passing 0 as the resource, we are inflating custom view
        this.context = context;
        this.productList = productList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the product for this position
        Product product = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_product_list, parent, false);
        }

        // Find the TextView and set the product name
        TextView productNameTextView = convertView.findViewById(R.id.textViewName);
        productNameTextView.setText(product.getName());

        // Return the view
        return convertView;
    }
}
