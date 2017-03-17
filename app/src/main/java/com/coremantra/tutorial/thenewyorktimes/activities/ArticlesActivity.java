package com.coremantra.tutorial.thenewyorktimes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.coremantra.tutorial.thenewyorktimes.R;
import com.coremantra.tutorial.thenewyorktimes.adapters.ArticlesAdapter;
import com.coremantra.tutorial.thenewyorktimes.api.NYTimesAPI;
import com.coremantra.tutorial.thenewyorktimes.models.Doc;
import com.coremantra.tutorial.thenewyorktimes.models.ResponseWrapper;
import com.coremantra.tutorial.thenewyorktimes.utils.EndlessRecyclerViewScrollListener;
import com.coremantra.tutorial.thenewyorktimes.utils.ItemClickSupport;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ArticlesActivity extends AppCompatActivity {

    private static final String TAG = ArticlesActivity.class.getName();
//    @BindView(R.id.tvDetails)
//    TextView tvDetails;

    @BindView(R.id.rvArticles)
    RecyclerView rvArticles;

    @BindView(R.id.toolbar) Toolbar toolbar;

    Gson gson;
    Retrofit retrofit;
    NYTimesAPI nyTimesAPI;

    List<Doc> articles = new ArrayList<>();
    ArticlesAdapter articlesAdapter;

    EndlessRecyclerViewScrollListener scrollListener;

    // Query params
    String sort = null;
    String searchQuery = null;
    String filters = null;

    ItemClickSupport.OnItemClickListener itemClickListener = new ItemClickSupport.OnItemClickListener() {
        @Override
        public void onItemClicked(RecyclerView recyclerView, int position, View v) {

            Doc article = articlesAdapter.getItem(position);
            if (article != null) {
                Intent displayArticleIntent = new Intent(getApplicationContext(), DisplayArticleActivity.class);
                displayArticleIntent.putExtra("url", article.getWebUrl());
                startActivity(displayArticleIntent);
            } else {
                Snackbar.make(v, "position: " + position + " received NULL object", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(NYTimesAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        nyTimesAPI = retrofit.create(NYTimesAPI.class);

        fetchArticles(null);

        // UI Code
        articlesAdapter = new ArticlesAdapter(articles);
        rvArticles.setAdapter(articlesAdapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, 1);
        rvArticles.setLayoutManager(layoutManager);

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.d(TAG, " ---------------- Inside onLoadMore: " + page);
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvArticles.addOnScrollListener(scrollListener);

        ItemClickSupport.addTo(rvArticles).setOnItemClickListener(itemClickListener);
    }


    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    // Send an API request to retrieve appropriate paginated data
    public void loadNextDataFromApi(int page) {
        Call<ResponseWrapper> responseWrapperCall = nyTimesAPI.getArticles(nyTimesAPI.API_KEY, page, sort,
                searchQuery, filters); // getArticles(nyTimesAPI.API_KEY, 0,"news_desk:(\"Education\"%20\"Health\")");
        Log.d(TAG, responseWrapperCall.request().url().toString());
        responseWrapperCall.enqueue(nextDataResponseCallback);
    }

    private void fetchArticles(String query) {
        searchQuery = query;
        Call<ResponseWrapper> responseWrapperCall = nyTimesAPI.getArticles(nyTimesAPI.API_KEY, 0, sort,
                searchQuery, filters); // getArticles(nyTimesAPI.API_KEY, 0,"news_desk:(\"Education\"%20\"Health\")");
        Log.d(TAG, responseWrapperCall.request().url().toString());

        responseWrapperCall.enqueue(newQueryResponseCallback);
    }

    Callback<ResponseWrapper> newQueryResponseCallback = new Callback<ResponseWrapper>() {
        @Override
        public void onResponse(Call<ResponseWrapper> call, Response<ResponseWrapper> response) {
            if (response.isSuccessful()) {
                Log.d(TAG, "newQueryResponseCallback: Articles response is successful");
                List<Doc> articles = response.body().getResponse().getDocs();

                StringBuilder builder = new StringBuilder();
                for (Doc article : articles) {
                    Log.i(TAG, article.getHeadline().getMain());
                    builder.append(article.getHeadline().getMain() + "\n\n");
                }

                // Since this is a new search query, we replace the data in adapter.
                articlesAdapter.replaceData(articles);
//                    tvDetails.setText(builder.toString());

            } else {
                Log.e(TAG, "newQueryResponseCallback: Something went wrong " + response.code());
                Log.e(TAG, response.errorBody().toString());
            }
        }

        @Override
        public void onFailure(Call<ResponseWrapper> call, Throwable t) {
            Log.e(TAG, "newQueryResponseCallback: Articles: Something went terrible", t);
        }
    };

    Callback<ResponseWrapper> nextDataResponseCallback = new Callback<ResponseWrapper>() {
        @Override
        public void onResponse(Call<ResponseWrapper> call, Response<ResponseWrapper> response) {
            if (response.isSuccessful()) {
                Log.d(TAG, "nextDataResponseCallback: Articles response is successful");
                List<Doc> articles = response.body().getResponse().getDocs();

                StringBuilder builder = new StringBuilder();
                for (Doc article : articles) {
                    Log.i(TAG, article.getHeadline().getMain());
                    builder.append(article.getHeadline().getMain() + "\n\n");
                }

                // Since this is a incremental data, we append it
                articlesAdapter.appendData(articles);

            } else {
                Log.e(TAG, "nextDataResponseCallback: Something went wrong " + response.code());
            }
        }

        @Override
        public void onFailure(Call<ResponseWrapper> call, Throwable t) {
            Log.e(TAG, "nextDataResponseCallback: Articles: Something went terrible", t);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_articles, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        int searchEditId = android.support.v7.appcompat.R.id.search_src_text;
        EditText et = (EditText) searchView.findViewById(searchEditId);
        et.setHint(getResources().getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                scrollListener.resetState();
                fetchArticles(query);

                Log.d(TAG, "=========== Inside onQueryTextSubmit");
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "------- BACK Pressed onMenuItemActionCollapse-----");
                //DO SOMETHING WHEN THE SEARCHVIEW IS CLOSING
                scrollListener.resetState();
                fetchArticles(null);

                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                item.expandActionView();
                searchView.requestFocus();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }
}
