package com.coremantra.tutorial.thenewyorktimes.activities;

import android.os.Bundle;
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

import com.coremantra.tutorial.thenewyorktimes.R;
import com.coremantra.tutorial.thenewyorktimes.adapters.ArticlesAdapter;
import com.coremantra.tutorial.thenewyorktimes.api.NYTimesAPI;
import com.coremantra.tutorial.thenewyorktimes.models.Doc;
import com.coremantra.tutorial.thenewyorktimes.models.ResponseWrapper;
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

    Gson gson;
    Retrofit retrofit;
    NYTimesAPI nyTimesAPI;

    List<Doc> articles = new ArrayList<>();
    ArticlesAdapter articlesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

    }

    private void fetchArticles(String query) {
        Call<ResponseWrapper> responseWrapperCall = nyTimesAPI.getArticles(nyTimesAPI.API_KEY, 1, null,
                query, null); // getArticles(nyTimesAPI.API_KEY, 0,"news_desk:(\"Education\"%20\"Health\")");
        Log.d(TAG, responseWrapperCall.request().url().toString());

        responseWrapperCall.enqueue(responseWrapperCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_articles, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
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

    Callback<ResponseWrapper> responseWrapperCallback = new Callback<ResponseWrapper>() {
        @Override
        public void onResponse(Call<ResponseWrapper> call, Response<ResponseWrapper> response) {
            if (response.isSuccessful()) {
                Log.d(TAG, "Articles response is successful");
                List<Doc> articles = response.body().getResponse().getDocs();

                StringBuilder builder = new StringBuilder();

                for (Doc article : articles) {
                    Log.i(TAG, article.getHeadline().getMain());
                    builder.append(article.getHeadline().getMain() + "\n\n");
                }

                articlesAdapter.updateData(articles);
//                    tvDetails.setText(builder.toString());

            } else {
                Log.e(TAG, "Something went wrong");
            }
        }

        @Override
        public void onFailure(Call<ResponseWrapper> call, Throwable t) {
            Log.e(TAG, "Articles: Something went terrible", t);
        }
    };
}
