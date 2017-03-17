package com.coremantra.tutorial.thenewyorktimes.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ShareActionProvider;
import android.widget.Toast;
import android.widget.Toolbar;

import com.coremantra.tutorial.thenewyorktimes.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DisplayArticleActivity extends Activity {

    String url;

    @BindView(R.id.wvArticle)
    WebView webView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // To display progress in activity title bar
        // Reference: https://developer.android.com/reference/android/webkit/WebView.html
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_display_article);
        ButterKnife.bind(this);
        setActionBar(toolbar);
        getActionBar().setDisplayHomeAsUpEnabled(true);

//        progressDialog  = ProgressDialog.show(this, "", "Loading...",true);

        url = getIntent().getStringExtra("url");

        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        // Configure the client to use when opening URLs
        webView.setWebViewClient(webViewClient);
        // Load the initial URL
        webView.loadUrl(url);;
    }

    final Activity activity = this;
    WebViewClient webViewClient = new WebViewClient() {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }

        public void onProgressChanged(WebView view, int progress) {
            // Activities and WebViews measure progress with different scales.
            // The progress meter will automatically disappear when we reach 100%
            activity.setProgress(progress * 1000);
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_share, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();


        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        // pass in the URL currently being used by the WebView
        shareIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());

        mShareActionProvider.setShareIntent(shareIntent);

        // Return true to display menu
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }




}
