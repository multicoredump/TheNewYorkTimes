package com.coremantra.tutorial.thenewyorktimes.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
    @BindView(R.id.btLoadArticles)
    Button btLoadArticles;

//    @BindView(R.id.tvDetails)
//    TextView tvDetails;

    @BindView(R.id.etSearch)
    EditText etSearch;

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_articles, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickSearch(View view) {
//        tvDetails.setText("Loading articles ...");
        fetchArticles(etSearch.getText().toString());
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

                articlesAdapter.updateDate(articles);
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
