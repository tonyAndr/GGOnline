package online.gameguides.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import online.gameguides.App;
import online.gameguides.BuildConfig;
import online.gameguides.R;
import tr.xip.errorview.ErrorView;

public class CheatsActivity extends AppCompatActivity {

    private WebView mWebView;
    private SharedPreferences mPrefs;
    private LinearLayout webViewProgress;
    private String mPlatformName;
    private String mLink;

    private AdView mAdView;
    private AdRequest mAdRequest;
    private InterstitialAd mInterstitialAd;
    private final double AD_FULLSCREEN_CHANCE = 0.5;
    private ErrorView mErrorView;
    private Future<String> mFutureHtml;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.ActivityThemeDark);
        else
            setTheme(R.style.ActivityThemeLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheats);

        mPrefs = getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mErrorView = (ErrorView) findViewById(R.id.error_view);
        webViewProgress = (LinearLayout) findViewById(R.id.webview_placeholder_progress);

        setupWebView();
        setupAds(savedInstanceState == null);

        mPlatformName = getIntent().getStringExtra("platform");
        mLink = getIntent().getStringExtra("link");


        setTitle("Cheats for "+mPlatformName);

            if (isNetworkAvailable()) {
                getCheatsFromLink();
            } else {
                showErrorView();
            }

    }

    private void setupWebView () {
        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void setupAds(final boolean wait) {
        if (!BuildConfig.DEBUG) {

            mAdRequest = new AdRequest.Builder()
                    .addTestDevice("FB5370BA8832184D91D40924747776DD")
                    .build();

            final double adChanceRand = Math.random();
            final boolean adChance = adChanceRand < AD_FULLSCREEN_CHANCE;
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.ad_fullscreen_banner));
            mInterstitialAd.loadAd(mAdRequest);
            if (adChance) {
                final Handler handler = new Handler();
                final Runnable r = new Runnable() {
                    public void run() {
                        if (mInterstitialAd.isLoaded())
                            mInterstitialAd.show();
                    }
                };
                handler.postDelayed(r, 800); // inache - pokazat' s zaderjkoy

            }


            mAdView = (AdView) findViewById(R.id.adView);
            mAdView.loadAd(mAdRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {

                    final Handler handler = new Handler();

                    final Runnable r = new Runnable() {
                        public void run() {
                            mAdView.setVisibility(View.VISIBLE);
                        }
                    };
                    if (wait)
                        handler.postDelayed(r, 6000); // inache - pokazat' s zaderjkoy
                    else
                        mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    mAdView.setVisibility(View.GONE);
                }

                @Override
                public void onAdOpened() {
                    super.onAdOpened();
                }
            });


        }
    }

    private void getCheatsFromLink() {
        webViewProgress.setVisibility(View.VISIBLE);
        mFutureHtml = Ion.with(this)
                .load(mLink)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        if (e == null) {
                            String html = parseCheatsHtml(result);
                            if(html == null) {
                                showErrorView();
                            }
                            else {
                                showCheats(html);
                            }
                        } else {
                            showErrorView();
                        }

                    }
                });
    }

    private String parseCheatsHtml(String result) {
        Document doc = Jsoup.parse(result);

        Elements parts = doc.select("div.span8").first().select("div.pod:not(.contrib)");

        if (parts != null && !parts.isEmpty()) {
            int blocks = parts.size();

            Element lastBlock = parts.get(blocks-1);

            cleanFromBr(lastBlock);

            parts.append("<style>html{color:#B6B6B6;background:#000}h1,h2,th{color:#ffea00}h3,h4,h5{color:#ccc}.cheat_inforate{display:none}ul.cheats{list-style-type:none;padding:0}th{text-transform:uppercase}td,th{border:1px solid #B6B6B6;padding:.2em}</style>");

            return parts.html();
        } else {
            return null;
        }
    }

    private Element cleanFromBr (Element element) {

        if (element.children().last().tagName().equals("br")) {
            element.children().last().remove();
            cleanFromBr(element);
        }

        return element;
    }

    private void showCheats(String html) {
        mWebView.loadData(html, "text/html; charset=UTF-8", null);
        webViewProgress.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showErrorView() {

        mErrorView.setConfig(ErrorView.Config.create()
                .image(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? R.drawable.no_internet_dark : R.drawable.no_internet_light)
                .title("Oops")
                .subtitle("No internet connection or cheats weren't found")
                .retryVisible(true)
                .retryText("Retry")
                .build());

        mErrorView.setVisibility(View.VISIBLE);

        mErrorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getCheatsFromLink();
            }
        });

        webViewProgress.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        if (mFutureHtml != null && !mFutureHtml.isDone()) {
            mFutureHtml.cancel(true);
            mFutureHtml = null;
        }
//        unregisterReceiver(mReadFileReceiver);
        super.onDestroy();
    }

}
