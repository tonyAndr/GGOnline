package online.gameguides.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;

import online.gameguides.App;
import online.gameguides.BuildConfig;
import online.gameguides.R;
import online.gameguides.adapters.DBControllerAdapter;
import online.gameguides.models.GuideItem;
import online.gameguides.utils.DownloadIntentService;
import online.gameguides.utils.GGApplication;
import online.gameguides.utils.ReadFileIntentService;
import tr.xip.errorview.ErrorView;


public class WebActivity extends AppCompatActivity {

    private WebView mWebView;
    private SharedPreferences mPrefs;
    private LinearLayout webViewProgress;
    private ProgressBar mReadFileProgress;
    private String mUrl;
    private GuideItem mGuideItem;
    private Future<String> mFutureHtml;
    public String mHtml;
    private float mReadPosition;
    private boolean mFromSavedState = false;

    private MenuItem mFavSave;
    private MenuItem mFavRemove;
    private ErrorView mErrorView;

    private AdView mAdView;
    private AdRequest mAdRequest;
    private InterstitialAd mInterstitialAd;
    private final double AD_FULLSCREEN_CHANCE = 0.5;

    private boolean isBRregistered = false;

    private DBControllerAdapter dbController;


    private ArrayList<String> mImgUrls = new ArrayList<>();

    private BroadcastReceiver mReadFileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String filter = intent.getAction();

            if (filter.equals(ReadFileIntentService.INTENT_ACTION_READFILE)) {

                String action = intent.getStringExtra(ReadFileIntentService.INTENT_ACTION_READFILE);
                if (action.equals(ReadFileIntentService.INTENT_ACTION_READFILE_ERROR)) {
                    mReadFileProgress.setVisibility(View.GONE);
                    mHtml = null;
                    unregisterReceiver(this);
                    isBRregistered = false;
                    getPageHtml();
                }
                if (action.equals(ReadFileIntentService.INTENT_ACTION_READFILE_HTML)) {
                    mHtml = ((GGApplication) getApplication()).mHtmlBuffer;
                    mReadFileProgress.setVisibility(View.GONE);
                    unregisterReceiver(this);
                    isBRregistered = false;
                    viewHtml();
                }
                if (action.equals(ReadFileIntentService.INTENT_ACTION_READFILE_PROGRESS)) {
                    if (webViewProgress.getVisibility() == View.GONE)
                        fadeInProgress();

                    mReadFileProgress.setVisibility(View.VISIBLE);
                    mReadFileProgress.setProgress(intent.getIntExtra(ReadFileIntentService.INTENT_ACTION_READFILE_PROGRESS, 0));
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        if (isBRregistered)
            unregisterReceiver(mReadFileReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.ActivityThemeDark);
        else
            setTheme(R.style.ActivityThemeLight);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        mPrefs = getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);

        dbController = DBControllerAdapter.getInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mErrorView = (ErrorView) findViewById(R.id.error_view);

        if (savedInstanceState != null) {
//            mGuideItem = savedInstanceState.getParcelable("mGuideItem");
//            mHtml = savedInstanceState.getString("mHtml");

            GGApplication application = (GGApplication) getApplication();
            mGuideItem = application.guideItem;
            mHtml = application.HTML;
            application.guideItem = null;
            application.HTML = null;

            mReadPosition = savedInstanceState.getFloat("readPosition");
            mFromSavedState = true;
        } else {
            mGuideItem = getIntent().getParcelableExtra("guideItem");
        }
        mUrl = mGuideItem.getLink();

        setTitle(mGuideItem.getHeader());


        webViewProgress = (LinearLayout) findViewById(R.id.webview_placeholder_progress);
        mReadFileProgress = (ProgressBar) findViewById(R.id.placeholder_progressbar);
//        fadeInProgress();

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setLoadsImagesAutomatically(!mPrefs.getBoolean(App.PREFS_DATA_SAVE, true));
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.setWebViewClient(new CustomWebClient());

//        fadeInProgress();

        if (mHtml == null) {
            if (dbController.isAlreadyInFavs(mGuideItem.getLink())) {
                IntentFilter intentFilter = new IntentFilter(ReadFileIntentService.INTENT_ACTION_READFILE);
                registerReceiver(mReadFileReceiver, intentFilter);
                isBRregistered = true;
                ReadFileIntentService.startReadingService(this, mUrl);
            } else {
                getPageHtml();
            }
        } else {
            viewHtml();
        }

        setupAds(savedInstanceState == null);
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
                        handler.postDelayed(r, 10000); // inache - pokazat' s zaderjkoy
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        GGApplication application = (GGApplication) getApplication();
        application.guideItem = mGuideItem;
        application.HTML = mHtml;
        outState.putFloat("readPosition", calculateProgression(mWebView));
    }

    public void setupFavsMenuVisibility() {
        boolean exists = dbController.isAlreadyInFavs(mGuideItem.getLink());
        mFavSave.setVisible(!exists);
        mFavRemove.setVisible(exists);
    }

    public void saveToFavs() {
        GGApplication application = (GGApplication) getApplication();
        application.favImgs = mImgUrls;
        application.favGuideId = mUrl.substring(mUrl.indexOf("=") + 1);
        application.favHTML = mHtml;

        DownloadIntentService.startActionDownload(this);

        long result = dbController.insertGuide(mGuideItem);
        setupFavsMenuVisibility();
        String snackText = result > -1 ? getString(R.string.snack_save_fav) : getString(R.string.snack_exists);
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        Snackbar.make(viewGroup, snackText, Snackbar.LENGTH_SHORT).setAction("", null).show();
    }

    public void removeFav() {
        DBControllerAdapter dbControllerAdapter = DBControllerAdapter.getInstance(this);
        dbControllerAdapter.removeGuide(mGuideItem.getLink());
        setupFavsMenuVisibility();
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        Snackbar.make(viewGroup, R.string.snack_remove_fav, Snackbar.LENGTH_SHORT).setAction("", null).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web, menu);
        mFavSave = menu.findItem(R.id.menu_save);
        mFavRemove = menu.findItem(R.id.menu_delete);
        mFavRemove.setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_favorite)
                .sizeDp(20)
                .color(getResources().getColor(android.R.color.secondary_text_dark)));
        mFavSave.setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_favorite_border)
                .sizeDp(20)
                .color(getResources().getColor(android.R.color.secondary_text_dark)));
        menu.findItem(R.id.menu_share)
                .setIcon(new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_share)
                        .sizeDp(20)
                        .color(getResources().getColor(android.R.color.secondary_text_dark)));
        setupFavsMenuVisibility();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_save:
                if (isNetworkAvailable())
                    saveToFavs();
                else
                    Toast.makeText(this, getString(R.string.error_message_internet), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_delete:
                removeFav();
                return true;
            case R.id.menu_share:

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, mGuideItem.getHeader() + ", " + mGuideItem.getLink());
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, getString(R.string.share_via)));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (webGoBack())
            return;
        super.onBackPressed();
    }

    public boolean webGoBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    private class CustomWebClient extends WebViewClient {

        private CustomWebClient() {
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            String jsItem = "javascript: var el = document.getElementsByClassName('responsive_page_content'); el[0].style.paddingTop = 0;";
            view.loadUrl(jsItem);
            mErrorView.setVisibility(View.GONE);
            fadeOutProgress();

            if (mFromSavedState) {
                mFromSavedState = false;
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        float webviewsize = mWebView.getContentHeight() - mWebView.getTop();
                        float positionInWV = webviewsize * mReadPosition;
                        int positionY = Math.round(mWebView.getTop() + positionInWV);
                        mWebView.scrollTo(0, positionY);
                    }
                    // Delay the scrollTo to make it work
                }, 300);
            }
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            toggleNoConnectionPlaceholder();
            fadeOutProgress();

        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            fadeOutProgress();
        }


        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!isNetworkAvailable()) {
                Toast.makeText(WebActivity.this, getString(R.string.error_message_internet), Toast.LENGTH_SHORT).show();
                return true;
            } else if (url != null && !url.contains("images.akamai")) {
                view.getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;
            }
            return false;
        }

        @Override
        public void onLoadResource(WebView view, String url) {

        }
    }

    private void getPageHtml() {
        if (isNetworkAvailable()) {
            fadeInProgress();
            mErrorView.setVisibility(View.GONE);
            mFutureHtml = Ion.with(this)
                    .load(mUrl)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            if (e == null) {
                                try {
                                    mHtml = parseHtmlGuidePage(result);
                                    viewHtml();
                                } catch (NullPointerException e1) {
                                    e1.printStackTrace();
                                    toggleNoConnectionPlaceholder();
                                    fadeOutProgress();
                                }
                            } else {
                                if (!e.getClass().getName().equals(CancellationException.class.getName())) {
                                    toggleNoConnectionPlaceholder();
                                    fadeOutProgress();
                                }
                            }


                        }
                    });
        } else {
            toggleNoConnectionPlaceholder();
        }
    }

    private void viewHtml() {

        if (mHtml != null)
            adjustHtmlColors();

        mWebView.loadData(mHtml, "text/html; charset=UTF-8", null);
    }

    private String parseHtmlGuidePage(String html) throws NullPointerException {
        Document doc = Jsoup.parse(html);

        // Remove unnecessary stuff
        doc.select(".responsive_header").first().remove();
        doc.select(".responsive_page_content_overlay").first().remove();
        doc.select(".mainmenu").first().remove();
        doc.select(".localmenu").first().remove();
        doc.select(".responsive_local_menu_tab").first().remove();
        doc.select(".responsive_fixonscroll_ctn").first().remove();
        doc.select("#global_header").first().remove();
        doc.select("#footer_spacer").first().remove();
        doc.select("#footer_responsive_optin_spacer").first().remove();
        doc.select("#footer").first().remove();
        doc.select(".apphub_HomeHeaderContent").first().remove();
        doc.select(".breadcrumbs").first().remove();
        doc.select(".workshopItemControlsCtn").first().remove();
        doc.select("#-1").first().remove();

        if (BuildConfig.FLAVOR.equals("BLACKWAKE") || BuildConfig.FLAVOR.equals("LEGOWORLDS"))
            doc.select(".guidePreviewImage").first().remove();


        Elements videoIframes = doc.select("div.sharedFilePreviewYouTubeVideo");
        ArrayList<String> videoUrls = new ArrayList<>();
        for (Element e : videoIframes) {
            videoUrls.add(e.attr("id"));
        }
        if (!videoUrls.isEmpty()) {
            for (int i = 0; i < videoUrls.size(); i++) {
                Element el = new Element(Tag.valueOf("span"), "");
                String video_id = videoUrls.get(i);
                el.append("<a href=\"https://www.youtube.com/watch?v=" + video_id + "\">[== Watch on Youtube ==]</a>");
                doc.select("div[id=" + video_id + "]").first().replaceWith(el);
            }
        }

        Elements images = doc.select("img.sharedFilePreviewImage");
        String imgUrl = "";
        mImgUrls.clear();
        for (Element e : images) {
            imgUrl = e.attr("src");
            mImgUrls.add("http://ir0.mobify.com/jpg60/500/" + imgUrl); // for offline use
            e.attr("src", "http://ir0.mobify.com/jpg60/500/" + imgUrl);
//            e.attr("style", "display: inline; height: auto; max-width: 100%; margin-top: 1em; margin-bottom: 1em;");
            e.attr("style", "display: inline; height: auto; max-width: 100%; margin-top: 0.5em; margin-bottom: 0.5em;");
            if (e.parent().tagName().contentEquals("a")) {
                e.parent().attr("href", imgUrl);
            }
        }

        return doc.html();
    }

    private void adjustHtmlColors() {
        Document doc = Jsoup.parse(mHtml);
        String topBG = "#e5e5e5";
        String bodyBG = "#fff";
        String headerColor = "#212121";
        String textColor = "#111";
        String fontSize = "1.15em";
        String headerSize = "1.4em";
        String linkColor = "#7272cc";
        String subTitleBG = bodyBG;

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            bodyBG = String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimary)));
            topBG = String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.darkBackground)));
            textColor = String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.darkTextSecondary)));
            headerColor = String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.darkTextPrimary)));
            subTitleBG = String.format("#%06X", (0xFFFFFF & getResources().getColor(R.color.colorPrimaryDark)));
        }

        // Make colors and styles
        doc.select(".apphub_blue").first().attr("style", "background: " + bodyBG + " !important; color: " + textColor + " !important; ");
        doc.select("body").first().attr("style", "background-color: " + bodyBG + " !important; ");
        doc.select(".workshopItemTitle").attr("style", "color: " + headerColor + " !important; ");
        doc.select(".guideTopDescription").attr("style", "color: " + textColor + " !important; ");
        doc.select(".subSectionTitle").attr("style", "text-transform: uppercase: !important; color: " + headerColor + " !important; font-size: " + headerSize + ";");
        doc.select(".subSectionDesc").attr("style", "color: " + textColor + " !important; font-size: " + fontSize + " !important; color: " + textColor + " !important; ");
        doc.select(".bb_h1").attr("style", "color: " + headerColor + " !important; ");
        doc.select(".guideTop").attr("style", "background: " + topBG + " !important; ");
        doc.select(".panel").attr("style", "background: " + bodyBG + " !important; ");
        doc.select(".apphub_background").remove();
        doc.select(".apphub_HeaderBottomBG").remove();
//        doc.select(".detailBox").attr("style", "background: url('http://steamcommunity-a.akamaihd.net/public/images/groups/content_header_rule.png?v=1') 0px 0px no-repeat !important; ");
        doc.select(".detailBox").attr("style", "background: " + bodyBG + " !important; padding-left: 16px; padding-right: 16px; ");
        doc.select("a").attr("style", "color: " + linkColor + " !important; ");
        mHtml = doc.html();
    }

    public void fadeInProgress() {
        webViewProgress.setVisibility(View.VISIBLE);
        webViewProgress.animate().setDuration(200).alpha(1f);
    }

    public void fadeOutProgress() {
        webViewProgress.animate().setDuration(200).alpha(0f).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (webViewProgress.getAlpha() == 0f) {
                    webViewProgress.setVisibility(View.GONE);
                }
            }
        });
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
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

    private void toggleNoConnectionPlaceholder() {

        mErrorView.setConfig(ErrorView.Config.create()
                .image(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? R.drawable.no_internet_dark : R.drawable.no_internet_light)
                .title("Oops")
                .subtitle("No internet connection")
                .retryVisible(true)
                .retryText("Retry")
                .build());

        mErrorView.setVisibility(View.VISIBLE);

        mErrorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getPageHtml();
            }
        });
    }


    private float calculateProgression(WebView content) {
        float positionTopView = content.getTop();
        float contentHeight = content.getContentHeight();
        float currentScrollPosition = content.getScrollY();
        return (currentScrollPosition - positionTopView) / contentHeight;
    }

}
