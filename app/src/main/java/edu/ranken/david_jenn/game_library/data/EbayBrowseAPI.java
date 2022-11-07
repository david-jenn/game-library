package edu.ranken.david_jenn.game_library.data;

import java.util.ArrayList;
import retrofit2.Call;

import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class EbayBrowseAPI {
    private final String baseUrl;
    private final Retrofit retrofit;
    private final BrowseService service;

    public EbayBrowseAPI(AuthEnvironment env) {
        this(env.baseUrl);
    }

    public EbayBrowseAPI(String baseUrl) {
        this.baseUrl = baseUrl;

        this.retrofit =
            new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.service = retrofit.create(BrowseService.class);
    }

    public BrowseService getService() { return service; }

    public Call<SearchResponse> searchAsync(
        String authToken,
        String query,
        int limit,
        String category,
        String sort,
        String filter,
        Callback<SearchResponse> callback) {

        Call<SearchResponse> call = service.search("Bearer " + authToken, query, limit, category, sort, filter);
        call.enqueue(callback);
        return call;
    }

    public interface BrowseService {
        @GET("/buy/browse/v1/item_summary/search")
        @Headers({"Accept: application/json", "User-Agent: retrofit"})
        Call<SearchResponse> search(
            @Header("Authorization") String authorization,
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("category") String category,
            @Query("sort") String sort,
            @Query("filter") String filter

        );

        @GET("/buy/browse/v1/item/{itemId}")
        @Headers({"Accept: application/json", "User-Agent: retrofit"})
        Call<Item> getItem(
            @Header("Authorization") String authorization,
            @Path("itemId") String itemId
        );

    }

    public static class SearchResponse {
        public String href;  // the request url
        public String next;  // the request url, for the next page
        public int total;    // the total number of listings found
        public int limit;    // the number if items per page
        public int offset;   // the number of items skipped
        public ArrayList<ItemSummary> itemSummaries;  // one page of listings
    }
    public static class ItemSummary {
        public String itemId;      // the listing's unique id
        public String title;       // the title of the listing
        public ItemImage image;    // the primary image for the listing
        public ArrayList<ItemImage> thumbnailImages;  // images to use in search results
        public String condition;   // new/used
        public ItemSeller seller;  // the seller's name and ratings
        public ItemPrice price;    // the price and currency
        public ArrayList<ShippingOptions> shippingOptions; // shipping information
        public ItemLocation itemLocation; //location of summary
        public String itemWebUrl; //link to webpage of summary

    }
    public static class Item extends ItemSummary {
        // only fields that are not in ItemSummary ...
    }
    public static class ItemImage {
        public String imageUrl;
    }
    public static class ItemSeller {
        public String username;
    }
    public static class ItemPrice {
        public String value;
        public String currency;
    }
    public static class ShippingOptions {
        public String shippingCostType;
        public ShippingCost shippingCost;
    }
    public static class ShippingCost {
        public String value;
        public String currency;
    }
    public static class ItemLocation {
        public String postalCode;
        public String country;
    }

}
