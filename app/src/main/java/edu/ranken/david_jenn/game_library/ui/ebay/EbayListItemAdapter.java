package edu.ranken.david_jenn.game_library.ui.ebay;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.R;
import edu.ranken.david_jenn.game_library.data.EbayBrowseAPI;

public class EbayListItemAdapter extends RecyclerView.Adapter<EbayItemViewHolder> {

    private static final String LOG_TAG = EbayListItemAdapter.class.getSimpleName();

    private final AppCompatActivity context;
    private List<EbayBrowseAPI.ItemSummary> itemSummaries;
    private final EbayBrowseViewModel model;
    private final Picasso picasso;
    private final LayoutInflater layoutInflater;


    public EbayListItemAdapter(AppCompatActivity context, EbayBrowseViewModel model) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.model = model;
        this.itemSummaries = null;
        this.picasso = Picasso.get();

    }

    public void setItemSummaries(List<EbayBrowseAPI.ItemSummary> itemSummaries) {
        this.itemSummaries = itemSummaries;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return itemSummaries != null ? itemSummaries.size() : 0;
    }

    @NonNull
    @Override
    public EbayItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.ebay_item, parent, false);
        EbayItemViewHolder vh = new EbayItemViewHolder(itemView);
        vh.summaryTitle = itemView.findViewById(R.id.summaryTitle);
        vh.price = itemView.findViewById(R.id.summaryPrice);
        vh.shippingCost = itemView.findViewById(R.id.summaryShippingCost);
        vh.image = itemView.findViewById(R.id.summaryImage);
        vh.condition = itemView.findViewById(R.id.summaryCondition);
        vh.seller = itemView.findViewById(R.id.summarySeller);
        vh.location = itemView.findViewById(R.id.summaryLocation);

        vh.itemView.setOnClickListener((view) -> {
            EbayBrowseAPI.ItemSummary summary = itemSummaries.get(vh.getAdapterPosition());
            Uri webpage = Uri.parse(summary.itemWebUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            context.startActivity(intent);
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull EbayItemViewHolder vh, int position) {
        EbayBrowseAPI.ItemSummary summary = itemSummaries.get(position);

        if (summary.title != null) {
            vh.summaryTitle.setText(summary.title);
        }

        if (summary.price != null) {
            String priceFormatted;
            if (Objects.equals(summary.price.currency, "USD")) {
                priceFormatted = "$" + summary.price.value;
            } else {
                priceFormatted = summary.price.value + " " + summary.price.currency;
            }
            vh.price.setText(priceFormatted);

        } else {
            vh.price.setText(null);
        }
        List<EbayBrowseAPI.ShippingOptions> options = summary.shippingOptions;
        if (options != null && options.get(0) != null && options.get(0).shippingCost != null) {
            String shippingCostFormatted;

            if (Objects.equals(options.get(0).shippingCost.currency, "USD")) {
                shippingCostFormatted = "$" + options.get(0).shippingCost.value;
            } else {
                shippingCostFormatted = options.get(0).shippingCost.value + " " + options.get(0).shippingCost.currency;
            }
            vh.shippingCost.setText(shippingCostFormatted);
        } else {
            vh.shippingCost.setText(R.string.ebayShippingCostError);
        }
        if (summary.seller != null && summary.seller.username != null) {
            vh.seller.setText(summary.seller.username);
        } else {
            vh.seller.setText(R.string.ebaySellerError);
        }
        if (summary.condition != null) {
            vh.condition.setText(summary.condition);
        } else {
            vh.condition.setText(R.string.ebayConditionError);
        }
        if (summary.itemLocation != null && summary.itemLocation.country != null) {
            vh.location.setText(summary.itemLocation.country);
        } else {
            vh.location.setText(R.string.ebayLocationError);
        }
        if (summary.image != null && summary.image.imageUrl != null) {
            this.picasso
                .load(summary.image.imageUrl)
                .resizeDimen(R.dimen.ebayPictureResize, R.dimen.ebayPictureResize)
                .centerCrop()
                .into(vh.image);
        }
    }
}
