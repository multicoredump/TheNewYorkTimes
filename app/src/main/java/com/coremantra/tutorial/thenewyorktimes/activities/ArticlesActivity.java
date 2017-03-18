package com.coremantra.tutorial.thenewyorktimes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
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
import com.coremantra.tutorial.thenewyorktimes.fragments.SearchFilterFragment;
import com.coremantra.tutorial.thenewyorktimes.models.Doc;
import com.coremantra.tutorial.thenewyorktimes.models.ResponseWrapper;
import com.coremantra.tutorial.thenewyorktimes.models.SearchFilters;
import com.coremantra.tutorial.thenewyorktimes.utils.EndlessRecyclerViewScrollListener;
import com.coremantra.tutorial.thenewyorktimes.utils.ItemClickSupport;
import com.coremantra.tutorial.thenewyorktimes.utils.NetworkUtils;
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

public class ArticlesActivity extends AppCompatActivity implements SearchFilterFragment.OnFragmentInteractionListener {

    private static final String TAG = "NY: " + ArticlesActivity.class.getName();


    @BindView(R.id.rvArticles)
    RecyclerView rvArticles;

    @BindView(R.id.toolbar) Toolbar toolbar;

    Gson gson;
    Retrofit retrofit;
    NYTimesAPI nyTimesAPI;

    List<Doc> articles = new ArrayList<>();
    ArticlesAdapter articlesAdapter;

    EndlessRecyclerViewScrollListener scrollListener;

    SearchFilters searchFilters;

    Snackbar snackbar;

    ItemClickSupport.OnItemClickListener itemClickListener = new ItemClickSupport.OnItemClickListener() {
        @Override
        public void onItemClicked(RecyclerView recyclerView, int position, View v) {

            Doc article = articlesAdapter.getItem(position);
            if (article != null) {
                Intent displayArticleIntent = new Intent(getApplicationContext(), DisplayArticleActivity.class);
                displayArticleIntent.putExtra("url", article.getWebUrl());
                startActivity(displayArticleIntent);
            } else {
                Snackbar.make(v, "Internal error. Please try again", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles);
        ButterKnife.bind(this);

        Log.d(TAG, "---------- Inside ON CREATE +++++++++++++ -");

        setSupportActionBar(toolbar);

        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(NYTimesAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        nyTimesAPI = retrofit.create(NYTimesAPI.class);

        searchFilters = new SearchFilters();

        // UI Code
        articlesAdapter = new ArticlesAdapter(articles);
        rvArticles.setAdapter(articlesAdapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, 1);
        rvArticles.setLayoutManager(layoutManager);

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page, searchFilters);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvArticles.addOnScrollListener(scrollListener);

        ItemClickSupport.addTo(rvArticles).setOnItemClickListener(itemClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "---------- Inside ONRESUME -------------");

        if (NetworkUtils.isNetworkAvailable(this) || NetworkUtils.isOnline()) {
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
            }
            searchArticles(searchFilters);
        } else {
            handleRequestError();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "---------- Inside ON PAUSE ********************");
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.d(TAG, "---------- Inside ON onSaveInstanceState ********************");

    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    // Send an API request to retrieve appropriate paginated data
    public void loadNextDataFromApi(int page, SearchFilters filters) {

        Log.d(TAG, "loadNextDataFromApi Filter: "+ filters.toString());

        Call<ResponseWrapper> responseWrapperCall = nyTimesAPI.getArticles(nyTimesAPI.API_KEY, page, filters.getSortOrder(),
                filters.getQuery(), filters.getNewsDesk());
        Log.d(TAG, "loadNextDataFromApi Request: "+ responseWrapperCall.request().url().toString());
        responseWrapperCall.enqueue(nextDataResponseCallback);
    }

    private void searchArticles(SearchFilters filters) {

        Log.d(TAG, "searchArticles Filter: "+ filters.toString());

        Call<ResponseWrapper> responseWrapperCall = nyTimesAPI.getArticles(nyTimesAPI.API_KEY, 0, filters.getSortOrder(),
                filters.getQuery(), filters.getNewsDesk());
        Log.d(TAG, "searchArticles Request: "+ responseWrapperCall.request().url().toString());

        responseWrapperCall.enqueue(newQueryResponseCallback);
    }

    Callback<ResponseWrapper> newQueryResponseCallback = new Callback<ResponseWrapper>() {
        @Override
        public void onResponse(Call<ResponseWrapper> call, Response<ResponseWrapper> response) {
            if (response.isSuccessful()) {
                Log.d(TAG, "newQueryResponseCallback: Articles response is successful");

                // Since this is a new search query, we replace the data in adapter.
                articles.clear();
                articles = getArticles(response.body().getResponse());
                articlesAdapter.replaceData(articles);

            } else {
                Log.e(TAG, "newQueryResponseCallback: Something went wrong " + response.code());
                Log.e(TAG, response.errorBody().toString());
            }
        }

        @Override
        public void onFailure(Call<ResponseWrapper> call, Throwable t) {
            Log.e(TAG, "newQueryResponseCallback: Articles: Something went terrible", t);
            handleRequestError();
        }
    };

    Callback<ResponseWrapper> nextDataResponseCallback = new Callback<ResponseWrapper>() {
        @Override
        public void onResponse(Call<ResponseWrapper> call, Response<ResponseWrapper> response) {
            if (response.isSuccessful()) {
                Log.d(TAG, "nextDataResponseCallback: Articles response is successful");

                // Since this is a incremental data, we append it
                articles.addAll(getArticles(response.body().getResponse()));
                articlesAdapter.appendData(articles);

            } else {
                Log.e(TAG, "nextDataResponseCallback: Something went wrong " + response.code());
            }
        }

        @Override
        public void onFailure(Call<ResponseWrapper> call, Throwable t) {
            Log.e(TAG, "nextDataResponseCallback: Articles: Something went terrible", t);
            handleRequestError();

        }
    };

    private void handleRequestError() {

        // The request was not successful hence first check if network is connected
        if (!NetworkUtils.isNetworkAvailable(this) || !NetworkUtils.isOnline()) {
            snackbar = Snackbar.make(rvArticles, "Network Error. Please connect to Internet and try again", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Wi-Fi Settings", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    });
            snackbar.show();
        }
    }


    public List<Doc> getArticles(com.coremantra.tutorial.thenewyorktimes.models.Response response) {

        return Doc.filterDocsByDocumentType(Doc.DOCUMENT_TYPE_ARTICLE, response.getDocs());
    }

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
                searchFilters.setQuery(query);
                searchArticles(searchFilters);
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

                searchFilters.resetQuery();
                searchArticles(searchFilters);

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

            case R.id.action_filter:
                Log.d(TAG, "Action filter clicked - Launch a dialog fragment");
                showFilterFragment();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFilterFragment() {
        FragmentManager fm = getSupportFragmentManager();
        SearchFilterFragment filterDialogFragment = SearchFilterFragment.newInstance(searchFilters);
        filterDialogFragment.show(fm, "fragment_filter");
    }

    @Override
    public void onFinishDialog(SearchFilters filters) {
        // update search filters;
        searchFilters = filters;

        // search articles using updated filters;
        searchArticles(searchFilters);
    }
}
