package com.coremantra.tutorial.thenewyorktimes.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.coremantra.tutorial.thenewyorktimes.clients.NYTimesClient;
import com.coremantra.tutorial.thenewyorktimes.fragments.SearchFilterFragment;
import com.coremantra.tutorial.thenewyorktimes.models.Doc;
import com.coremantra.tutorial.thenewyorktimes.models.ResponseWrapper;
import com.coremantra.tutorial.thenewyorktimes.models.SearchFilters;
import com.coremantra.tutorial.thenewyorktimes.utils.EndlessRecyclerViewScrollListener;
import com.coremantra.tutorial.thenewyorktimes.utils.ItemClickSupport;
import com.coremantra.tutorial.thenewyorktimes.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ArticlesActivity extends AppCompatActivity implements SearchFilterFragment.OnFragmentInteractionListener {

    private static final String TAG = "NY: " + ArticlesActivity.class.getName();

    @BindView(R.id.swipe_refresh_container)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.rvArticles)
    RecyclerView rvArticles;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    NYTimesClient nyTimesClient;

    List<Doc> articles = new ArrayList<>();
    ArticlesAdapter articlesAdapter;

    EndlessRecyclerViewScrollListener scrollListener;

    SearchFilters searchFilters;

    private CompositeSubscription compositeSubscription;

    Snackbar snackbar;

    ItemClickSupport.OnItemClickListener articleClickListener = new ItemClickSupport.OnItemClickListener() {
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

        setSupportActionBar(toolbar);

        nyTimesClient = NYTimesClient.getInstance();

        compositeSubscription = new CompositeSubscription();

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
                loadNextPage(searchFilters, page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvArticles.addOnScrollListener(scrollListener);

        ItemClickSupport.addTo(rvArticles).setOnItemClickListener(articleClickListener);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.white,
                R.color.colorPrimary,
                android.R.color.white);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                searchArticles(searchFilters);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

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
                // SearchView is closing
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

    @Override protected void onDestroy() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public void onFinishDialog(SearchFilters filters) {
        // update search filters;
        searchFilters = filters;

        // search articles using updated filters;
        searchArticles(searchFilters);
    }

    private void showFilterFragment() {
        FragmentManager fm = getSupportFragmentManager();
        SearchFilterFragment filterDialogFragment = SearchFilterFragment.newInstance(searchFilters);
        filterDialogFragment.show(fm, "fragment_filter");
    }

    private List<Doc> getArticles(com.coremantra.tutorial.thenewyorktimes.models.Response response) {
        return Doc.filterDocsByDocumentType(Doc.DOCUMENT_TYPE_ARTICLE, response.getDocs());
    }

    private void searchArticles(SearchFilters filters) {

        swipeRefreshLayout.setRefreshing(true);

        String beginDate = filters.isIgnoreBeginDate() ? null : filters.getBeginDateString();
        Log.d(TAG, "searchArticles: " + beginDate + " filters.isIgnoreBeginDate() " + filters.isIgnoreBeginDate());

        Call<ResponseWrapper> responseWrapperCall = nyTimesClient.getArticles(0, filters.getSortOrder(),
                               filters.getQuery(), beginDate, filters.getNewsDesk());
               Log.d(TAG, "searchArticles Request: "+ responseWrapperCall.request().url().toString());

        compositeSubscription.add(NYTimesClient.getInstance()
                .getRxArticles(0, filters.getSortOrder(),
                        filters.getQuery(), beginDate, filters.getNewsDesk())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseWrapper>() {
                    @Override public void onCompleted() {

                        Log.d(TAG, "Rx: In onCompleted()");
                        swipeRefreshLayout.setRefreshing(false);
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Rx: In onError()");
                        swipeRefreshLayout.setRefreshing(false);
                        handleRequestError();
                    }

                    @Override public void onNext(ResponseWrapper responseWrapper) {
                        Log.d(TAG, "Rx: In onNext(): Articles response is successful");
                        // Since this is a new search query, we replace the data in adapter.
                        articles.clear();
                        //ToDo: Move this filter operation upStream to Obeservable using an operator
                        articles = getArticles(responseWrapper.getResponse());
                        articlesAdapter.replaceData(articles);
                    }
                })
        );

    }

    private void loadNextPage(SearchFilters filters, int page) {

        String beginDate = filters.isIgnoreBeginDate()? null : filters.getBeginDateString();
        Log.d(TAG, "loadNextPage: " + beginDate);

        compositeSubscription.add(NYTimesClient.getInstance()
                .getRxArticles(page, filters.getSortOrder(),
                        filters.getQuery(), beginDate, filters.getNewsDesk())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseWrapper>() {
                    @Override public void onCompleted() {
                        Log.d(TAG, "Rx: In onCompleted()");
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "Rx: In onError()");
                        handleRequestError();
                    }

                    @Override public void onNext(ResponseWrapper responseWrapper) {
                        Log.d(TAG, "Rx: In onNext(): Articles response is successful");
                        // Since this is a incremental data, we append it
                        //ToDo: Move this filter operation upStream to Obeservable using an operator
                        articles.addAll(getArticles(responseWrapper.getResponse()));
                        articlesAdapter.appendData(articles);
                    }
                })
        );
    }

    private void handleRequestError() {

        // The request was not successful hence first check if network is connected
        if (!NetworkUtils.isNetworkAvailable(this) || !NetworkUtils.isOnline()) {
            snackbar = Snackbar.make(rvArticles, "Network Error. Please connect to Internet and try again", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Wi-Fi Settings", v -> {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    });
            snackbar.show();
        }
    }
}
