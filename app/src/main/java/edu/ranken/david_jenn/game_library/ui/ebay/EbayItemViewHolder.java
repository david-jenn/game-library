package edu.ranken.david_jenn.game_library.ui.ebay;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class EbayItemViewHolder extends RecyclerView.ViewHolder {

    public TextView summaryTitle;
    public TextView price;
    public TextView priceCurrency;
    public TextView shippingCost;
    public TextView shippingCostCurrency;
    public ImageView image;
    public TextView condition;
    public TextView seller;
    public TextView location;



    public EbayItemViewHolder(View view) {
        super(view);
    }
}
