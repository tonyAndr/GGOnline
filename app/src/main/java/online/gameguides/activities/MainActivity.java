package online.gameguides.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;
import hotchemi.android.rate.StoreType;
import online.gameguides.App;
import online.gameguides.BuildConfig;
import online.gameguides.R;
import online.gameguides.models.CheatsContent;
import online.gameguides.models.TwitchContent;
import online.gameguides.fragments.CheatsFragment;
import online.gameguides.fragments.MoreFragment;
import online.gameguides.fragments.MyFragment;
import online.gameguides.fragments.TwitchFragment;

public class MainActivity extends AppCompatActivity
        implements TwitchFragment.OnListFragmentInteractionListener, MoreFragment.OnLinkMoreInteractionListener, CheatsFragment.OnCheatsFragmentInteractionListener {


    private final double AD_FULLSCREEN_CHANCE = 0.5;

    private ViewPager vpPager;

    TabLayout mTabLayout;
    private MyPagerAdapter adapterViewPager;
    private FragmentManager mFragmentManager;

    private MyFragment mGuidesFragment;
    private TwitchFragment mTwitchFragment;
    private MoreFragment mMoreFragment;
    private CheatsFragment mCheatsFragment;

    private List<Fragment> mFragmentList = new ArrayList<>();
    private List<String> mTabsList = new ArrayList<>();

    private InterstitialAd mInterstitialAd;

    private SharedPreferences mPrefs;
    private AdView mAdView;
    private AdRequest mAdRequest;

    private LinearLayout mDatasavingWarning;

    private String mCheatsUrl;
    private Future<String> mFutureHtml;
    private boolean noCheats = false;
    private boolean savedState = false;

    private LinearLayout mProgressView;

    private Menu mOptionsMenu;



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("noCheats", noCheats);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            setTheme(R.style.ActivityThemeDark);
        else
            setTheme(R.style.ActivityThemeLight);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getString(R.string.app_name));
        // Main setup
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);

        mPrefs = getSharedPreferences(App.PREFS_NAME, MODE_PRIVATE);
        mPrefs.edit().putBoolean(App.PREFS_FIRST_LAUNCH, false).apply();

        mProgressView = findViewById(R.id.webview_placeholder_progress);
        mProgressView.setVisibility(View.VISIBLE);

        vpPager = (ViewPager) findViewById(R.id.vpPager);
        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null)
                checkCheats(); // this will check if it's necessary to show cheatfragment. and then it will call loading of all fragments
        else {
            noCheats = savedInstanceState.getBoolean("noCheats");
            reCreateFragments();
        }


        if (!BuildConfig.DEBUG) {
            setupAdvertisments();
        }

        showDatasavingWarning(mPrefs.getBoolean(App.PREFS_DATA_SAVE, false));
        setupRateAppDialog();

    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void setupRateAppDialog() {
        AppRate.with(this)
                .setStoreType(StoreType.GOOGLEPLAY)
                .setInstallDays(3) // default 10, 0 means install day.
                .setLaunchTimes(6) // default 10
                .setRemindInterval(3) // default 1
                .setShowLaterButton(false) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(MainActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);
    }

    public void showDatasavingWarning(boolean enabled) {
        mDatasavingWarning = findViewById(R.id.warning_datasaving_layout);
        mDatasavingWarning.findViewById(R.id.warning_datasaving_turnoff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPrefs.edit().putBoolean(App.PREFS_DATA_SAVE, false).apply();
                mDatasavingWarning.setVisibility(View.GONE);
                restartMainActvity();
            }
        });
        if (enabled) {
            mDatasavingWarning.setVisibility(View.VISIBLE);
            if (mAdView != null)
                mAdView.setVisibility(View.GONE);
        } else {
            mDatasavingWarning.setVisibility(View.GONE);
            if (mAdView != null)
                mAdView.setVisibility(View.VISIBLE);
        }
    }

    public void restartMainActvity() { // on datasaving mode change
        Toast.makeText(MainActivity.this, "Restarting...", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.restartActivity();
            }
        }, 500);
    }

    @SuppressLint("RestrictedApi")
    private void showDisclamer() { //old disclamer which was replaced
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.ActivityThemeDark));

        View dialogView = getLayoutInflater().inflate(R.layout.startup_dialog, null);

        alertDialog.setView(dialogView);
        final AlertDialog ad = alertDialog.create();

        final Button alertOK = (Button) dialogView.findViewById(R.id.alert_btn_ok);
        Button alertQUIT = (Button) dialogView.findViewById(R.id.alert_btn_quit);

        alertOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPrefs.edit().putBoolean(App.PREFS_FIRST_LAUNCH, false).apply();
                ad.dismiss();
            }
        });

        alertOK.setEnabled(false);
        alertQUIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
                MainActivity.this.finish();
            }
        });

        CheckBox cb = (CheckBox) dialogView.findViewById(R.id.startup_dialog_checkbox);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mPrefs.edit().putBoolean(App.PREFS_DATA_SAVE, !b).apply();
                alertOK.setEnabled(b);
            }
        });

        ad.setCancelable(false);

        ad.show();

    }

    private void setupPortraitViewPager(FragmentManager fragmentManager) {
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mTabLayout.setupWithViewPager(vpPager);
        adapterViewPager = new MyPagerAdapter(fragmentManager);
        vpPager.setOffscreenPageLimit(5);
        vpPager.setAdapter(adapterViewPager);


        vpPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vpPager.setCurrentItem(tab.getPosition());

                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void reCreateFragments() {
        mFragmentList.clear();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        if (savedState) {
            for (Fragment fr : mFragmentManager.getFragments()) {
                if (fr.getClass().getSimpleName().equals(MyFragment.class.getSimpleName())) {
                    mGuidesFragment = (MyFragment) fr;
                }
                if (fr.getClass().getSimpleName().equals(MoreFragment.class.getSimpleName())) {
                    mMoreFragment = (MoreFragment) fr;
                }
                if (fr.getClass().getSimpleName().equals(TwitchFragment.class.getSimpleName())) {
                    mTwitchFragment = (TwitchFragment) fr;
                }
                if (fr.getClass().getSimpleName().equals(CheatsFragment.class.getSimpleName())) {
                    mCheatsFragment = (CheatsFragment) fr;
                }
                fragmentTransaction.remove(fr);
            }

            fragmentTransaction.commitNow();

            mGuidesFragment = (MyFragment) fetchOldState(mGuidesFragment);
            mTwitchFragment = (TwitchFragment) fetchOldState(mTwitchFragment);
            mMoreFragment = (MoreFragment) fetchOldState(mMoreFragment);
            mCheatsFragment = (CheatsFragment) fetchOldState(mCheatsFragment);

        } else {
            mGuidesFragment = MyFragment.newInstance();
            mTwitchFragment = TwitchFragment.newInstance();
            mMoreFragment = MoreFragment.newInstance();
            mCheatsFragment = CheatsFragment.newInstance();
        }

        mFragmentList.add(mGuidesFragment);
        if (!noCheats)
            mFragmentList.add(mCheatsFragment);
        mFragmentList.add(mTwitchFragment);
        mFragmentList.add(mMoreFragment);

        commitTransaction(); // insert fragments into layouts
    }

    private void commitTransaction() {  // to complete transaction when needed
        // Portrait mode (tablet + phone)
        if (vpPager != null) {
//            tr.commit();
//            mFragmentManager.executePendingTransactions();

            setupPortraitViewPager(mFragmentManager);

        }
        // Landscape mode
        else {
            setupLandscapeView();
        }

//        if (isNetworkAvailable())
//            checkCheats(); // dobavlyaem fragment tolko esli est' cheats

        mProgressView.setVisibility(View.GONE);
    }

    private Fragment fetchOldState(Fragment f) //to prevent fragment recreation and crashes on rotation
    {
        try {
            Fragment.SavedState oldState = mFragmentManager.saveFragmentInstanceState(f);

            Fragment newInstance = f.getClass().newInstance();
            newInstance.setInitialSavedState(oldState);

            return newInstance;
        } catch (Exception e) // InstantiationException, IllegalAccessException
        {
            return null;
        }
    }

    private void setupLandscapeView() {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_tablet_list, mGuidesFragment);
        fragmentTransaction.replace(R.id.main_tablet_more, mMoreFragment);
        fragmentTransaction.replace(R.id.main_tablet_streams, mTwitchFragment);
        if (!noCheats) {
            fragmentTransaction.replace(R.id.main_tablet_cheats, mCheatsFragment);
            findViewById(R.id.main_tablet_content_container_middle).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.main_tablet_content_container_middle).setVisibility(View.GONE);
        }

        fragmentTransaction.commitNow();
//        mFragmentManager.executePendingTransactions();
    }

    private void setupAdvertisments() {

        if (!BuildConfig.DEBUG) {
            mAdView = (AdView) findViewById(R.id.adView);
            mAdRequest = new AdRequest.Builder()
                    .addTestDevice("FB5370BA8832184D91D40924747776DD").build();
            mAdView.loadAd(mAdRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    if (!mPrefs.getBoolean(App.PREFS_DATA_SAVE, false)) // esli v datasave mode to ne pokazivat' reklamu
                        mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    mAdView.setVisibility(View.GONE);
                }

            });

        }

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_fullscreen_banner));
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("FB5370BA8832184D91D40924747776DD")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }


    @Override
    public void onBackPressed() {

//        if (vpPager != null) {
//            if (adapterViewPager.mPagerMyFrag != null && adapterViewPager.mPagerMyFrag.isSearchViewOpened()) {
//                adapterViewPager.mPagerMyFrag.closeSearchView();
//                return;
//            }
//        } else {
        if (mGuidesFragment.isSearchViewOpened()) {
            mGuidesFragment.closeSearchView();
            return;
        }
//        }


        super.onBackPressed();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!BuildConfig.DEBUG) {
            requestNewInterstitial();
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        menu.clear();
        return super.onPrepareOptionsMenu(menu);
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

    // Twitch fragment

    @Override
    public void onListFragmentInteraction(TwitchContent.TwitchItem item) {
        final String streamUrl = item.url;
        if (!BuildConfig.DEBUG) {
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(streamUrl)));
                }
            });
            if (mInterstitialAd.isLoaded() && Math.random() < AD_FULLSCREEN_CHANCE) {
                mInterstitialAd.show();
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(streamUrl)));
            }
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(streamUrl)));
        }
    }

    // More Fragment - useful links

    @Override
    public void onListFragmentInteraction(final String url) {
        if (!BuildConfig.DEBUG) {
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            });
            if (mInterstitialAd.isLoaded() && Math.random() < AD_FULLSCREEN_CHANCE) {
                mInterstitialAd.show();
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

//    public void hideEmptyFragment() {
//        if (vpPager != null) {
//            adapterViewPager.removeTabPage();
//        } else {
//
//        }
//
//    }

    @Override
    public void OnCheatsFragmentInteractionListener() {

    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fragmentManager) {

            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        //        public void removeTabPage() {
//            mFragmentManager.executePendingTransactions();
//            if (!mFragmentList.isEmpty()) {
//                Fragment fragToDelete = null;
//                for (Fragment f : mFragmentList) {
//                    if (f instanceof CheatsFragment) {
//                        fragToDelete = f;
//                        break;
//                    }
//                }
//                int pos = mFragmentList.indexOf(fragToDelete);
//                mFragmentList.remove(fragToDelete);
//                mTabsList.remove(pos);
//                notifyDataSetChanged();
//            }
//        }
        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {

            return mFragmentList.get(position);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            if (getItem(position) instanceof MyFragment)
                return getString(R.string.tab_guides);
            if (getItem(position) instanceof CheatsFragment)
                return getString(R.string.tab_cheats);
            if (getItem(position) instanceof TwitchFragment)
                return getString(R.string.tab_streams);
            if (getItem(position) instanceof MoreFragment)
                return getString(R.string.tab_more);
            return "";
        }


        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            super.restoreState(state, loader);
        }

//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            super.destroyItem(container, position, object);
//            if (position >= getCount())
//                mFragmentManager.beginTransaction().remove((Fragment) object).commit();
//        }
//
//        @Override
//        public int getItemPosition(Object object) {
//            if (mFragmentList.contains(object)) return mFragmentList.indexOf(object);
//            else return POSITION_NONE;
//        }


    }

    public void restartActivity() {
        Intent i = new Intent(this, MainActivity.class);
        finish();
        startActivity(i);
    }

    private void checkCheats() {
        if (isNetworkAvailable()) {
            mCheatsUrl = "https://www.gamefaqs.com/search?game=" + getString(R.string.game_name);
            mFutureHtml = Ion.with(this)
                    .load(mCheatsUrl)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            if (result == null || !parseHtmlCheck(result)) {
                                noCheats = true;
                            }
                            reCreateFragments();
                        }
                    });
        } else {
            reCreateFragments();
        }

    }

//    private void addCheatsFragmentToView() {
//        if (!mTabsList.contains(getString(R.string.tab_cheats))) {
//            mTabsList.add(1, getString(R.string.tab_cheats));
//        }
//        if (!mFragmentList.contains(mCheatsFragment)) {
//            mFragmentList.add(1, mCheatsFragment);
//        }
//
//        if (vpPager != null) {
//            adapterViewPager.notifyDataSetChanged();
//        } else {
//            mFragmentManager.beginTransaction().replace(R.id.main_tablet_cheats, mCheatsFragment).commit();
//            findViewById(R.id.main_tablet_content_container_middle).setVisibility(View.VISIBLE);
//        }
//    }

    private void hideCheatsFragment() {
        if (vpPager == null)
            findViewById(R.id.main_tablet_content_container_middle).setVisibility(View.GONE);
    }

    private boolean parseHtmlCheck(String html) throws NullPointerException {
        Document doc = Jsoup.parse(html);

        Elements searchResults = doc.select(".search_result");
        // CHECK 1: esli nichego ne naideno vashe
        if (searchResults == null || searchResults.isEmpty()) {
            return false;
        }

        String gameName = "";
        int searchIndex = 0;
        int foundIndex = 999;
        for (Element e : searchResults) {
            gameName = e.select(".sr_name").first().select("a").first().text();
            if (gameName.equals(Uri.decode(getString(R.string.game_name)))) {
                foundIndex = searchIndex;
                break;
            }
            searchIndex++;
        }

        // CHECK 2: Esli net sootvetstviya po nazvaniyu
        if (foundIndex == 999) {
            return false;
        }

        Elements platforms = searchResults.get(foundIndex).select(".sr_product");

        String platformName;
        String link;

        Element a;

        CheatsContent.clearItems();

        int index = 0;
        for (Element e : platforms) {
            platformName = e.select(".sr_product_name").first().select("a").first().text();
            a = e.select(".sr_links").first().select("a[href*=cheats]").first();
            if (a != null) {
                link = "https://www.gamefaqs.com" + a.attr("href");
//                platform_cheat_Links.put(platformName, link);
                CheatsContent.addItem(new CheatsContent.CheatItem(index, platformName, link));
                index++;
            }
        }

        // CHECK 3: Esli net chitov
        if (CheatsContent.ITEMS.isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        if (mFutureHtml != null && !mFutureHtml.isDone()) {
            mFutureHtml.cancel(true);
            mFutureHtml = null;
        }
        super.onDestroy();
    }
}
