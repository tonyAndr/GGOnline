package online.gameguides.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.NativeExpressAdView;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;

import online.gameguides.App;
import online.gameguides.R;
import online.gameguides.adapters.DBControllerAdapter;
import online.gameguides.adapters.RecyclerAdapter;
import online.gameguides.adapters.SpinnerAdapter;
import online.gameguides.models.GuideItem;
import online.gameguides.utils.GGApplication;
import tr.xip.errorview.ErrorView;

public class MyFragment extends Fragment {
    public MyFragment() {

    }

    public static MyFragment newInstance() {
        return new MyFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("url", url);
        outState.putString("linkNext", linkNext);
        outState.putString("currPage", currPage);
        outState.putString("filters", filters);
//        outState.putParcelableArrayList("guideItems", guideItems);
        writeListToApplication();// saves guideItems
        outState.putInt("mSpinnerPosition", mSpinnerPosition);
        outState.putInt("whichErrorWasLast", whichErrorWasLast);
        outState.putString("lastBaseUrl", baseUrl);

        if (searchView != null) {
            searchText = searchView.getQuery().toString();
        }

        outState.putString("searchText", searchText);

        if (mErrorView != null) {
            isErrorViewVisible = mErrorView.getVisibility() == View.VISIBLE;
        }
        outState.putBoolean("isErrorViewVisible", isErrorViewVisible);
        super.onSaveInstanceState(outState);
    }

    private void writeListToApplication() {
        GGApplication application = (GGApplication) getActivity().getApplication();
        application.guideItemArrayList = guideItems;
    }

    private void readListFromApplication() {
        GGApplication application = (GGApplication) getActivity().getApplication();
        guideItems = application.guideItemArrayList;

//        application.guideItemArrayList = null;
    }

    private String baseUrl;
    private String category = "&browsefilter=toprated&browsesort=toprated";

    private Spinner mSpinner;
    private int mSpinnerPosition = 1;

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private MenuItem mMenuSearch;

    private String searchText = ""; // used in Edittext field
    private String searchWord = ""; // used in url

    private boolean mFromsSavedState = false;

    private String url;  // save

    private String linkNext; // save
    private String currPage = ""; //save
//    private String pagingInfo;

    public String filters = ""; // save

    private ArrayList<GuideItem> guideItems = new ArrayList<>(); // save

    private RecyclerView rv;
    private RecyclerAdapter rvAdapter;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private boolean mAddToExisting = false;


    private Future<String> mFutureListRawData;
    private Future<String> mFutureGuideRawData;

    private ErrorView mErrorView;
    private LinearLayout webViewProgress;

    private boolean isErrorViewVisible = false;
    private int whichErrorWasLast = 0;

    private SharedPreferences mPrefs;

    private Menu mOptionsMenu;

    public void setCurrPage(String page) {
        this.currPage = page;
    }

    public void setUrl(String url) {
        this.url = url;


    }


    public void setFilters(String filters) {
        this.filters = filters;
    }

    private GetFavsTask mTaskGetFavs = new GetFavsTask();

    @Override
    public void onDestroy() {
        super.onDestroy();

//        if (mOptionsMenu != null) {
//            mOptionsMenu.clear();
//        }


        if (mTaskGetFavs.getStatus() == AsyncTask.Status.RUNNING) {
            mTaskGetFavs.cancel(true);
            mTaskGetFavs = null;
        }

        Log.d("MyFrag", "MyFrag Destroyed");
    }

    @Override
    public void onDetach() {

        if (mFutureListRawData != null && !mFutureListRawData.isDone()) {
            mFutureListRawData.cancel();
            mFutureListRawData = null;
        }
        if (mFutureGuideRawData != null && !mFutureGuideRawData.isDone()) {
            mFutureGuideRawData.cancel();
            mFutureGuideRawData = null;
        }
        super.onDetach();
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);
        setHasOptionsMenu(true);
        Log.d("MyFrag", "MyFrag Created");
        mPrefs = getActivity().getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);

        String baseId;
        baseId = getString(R.string.base_url);

        baseUrl = "http://steamcommunity.com/app/" + baseId + "/guides/";

        if (savedInstanceState != null) {
            mSpinnerPosition = savedInstanceState.getInt("mSpinnerPosition", mSpinnerPosition);
            whichErrorWasLast = savedInstanceState.getInt("whichErrorWasLast", whichErrorWasLast);
            url = savedInstanceState.getString("url");
            linkNext = savedInstanceState.getString("linkNext");
            currPage = savedInstanceState.getString("currPage");
            filters = savedInstanceState.getString("filters");
//            guideItems = savedInstanceState.getParcelableArrayList("guideItems");
            readListFromApplication(); // restores guideItems
            baseUrl = savedInstanceState.getString("lastBaseUrl");
            searchText = savedInstanceState.getString("searchText");
            isErrorViewVisible = savedInstanceState.getBoolean("isErrorViewVisible");

            mFromsSavedState = true;
        } else if (!isNetworkAvailable()) {
            mSpinnerPosition = 3;
        }

        setUrl(getFinalUrl());
    }

    public String getFinalUrl() {
        String additionalParams = "?numperpage=15&requiredtags%5B%5D=english";
        return baseUrl + additionalParams + category;
    }


    private Drawable buildMenuIcon(int position, boolean selected) {

        switch (position) {
            case 0:
                return new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_trending_up).sizeDp(20).color(getResources().getColor(selected ? R.color.colorAccent : R.color.colorAccent));
            case 1:
                return new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_stars).sizeDp(20).color(getResources().getColor(selected ? R.color.colorAccent : R.color.colorAccent));
            case 2:
                return new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_fiber_new).sizeDp(20).color(getResources().getColor(selected ? R.color.colorAccent : R.color.colorAccent));
            case 3:
                return new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_loyalty).sizeDp(20).color(getResources().getColor(selected ? R.color.colorAccent : R.color.colorAccent));
            default:
                return new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_stars).sizeDp(20).color(getResources().getColor(R.color.colorAccent));
        }
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
//        menu.clear();
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d("MyFrag", "Menu reCreated");
        menu.clear();
        inflater.inflate(R.menu.main, menu);

        mOptionsMenu = menu;

        MenuItem item = menu.findItem(R.id.spinner);

        mSpinner = (Spinner) MenuItemCompat.getActionView(item);

        mSpinner.setAdapter(new SpinnerAdapter(
                getActivity(),
                mSpinner,
                new SpinnerAdapter.SpinnerItem[]{
                        new SpinnerAdapter.SpinnerItem(getString(R.string.menu_mostpopular), buildMenuIcon(0, false)),
                        new SpinnerAdapter.SpinnerItem(getString(R.string.menu_toprated), buildMenuIcon(0, false)),
                        new SpinnerAdapter.SpinnerItem(getString(R.string.menu_mostrecent), buildMenuIcon(0, false)),
                        new SpinnerAdapter.SpinnerItem(getString(R.string.menu_favorited), buildMenuIcon(0, false))
                }));

        mSpinner.setOnItemSelectedListener(spinnerClickListener);
        mSpinner.setSelection(mSpinnerPosition, false);

//        mSpinner.setDropDownHorizontalOffset(-500);
        mSpinner.setDropDownVerticalOffset(50);
        setupSearchView(menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);
    }

    private void setupSearchView(Menu menu) {
        // Search view
        mMenuSearch = menu.findItem(R.id.menu_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        if (searchText != null && !searchText.isEmpty()) {
            if (mMenuSearch != null)
                mMenuSearch.expandActionView();
        }

        if (mMenuSearch != null) {
            searchView = (SearchView) mMenuSearch.getActionView();
            if (searchText != null && !searchText.isEmpty()) {
                searchView.setIconified(false);
                searchView.setQuery(searchText, true);
                searchView.clearFocus();
            }
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.i("onQueryTextChange", newText);

                    return true;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.i("onQueryTextSubmit", query);

                    if (!query.isEmpty())
                        searchWord = "&searchText=" + query;

                    searchText = query;

                    setFilters(searchWord);
                    setCurrPage("");
                    executeListMethods(true);


                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }

                    return true;
                }


            };

            // Get the search close button image view
            ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);

            // Set on click listener
            closeButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.d("onQueryTextClosed", "Search close button clicked");
                    collapseSearchView();

                }
            });
            searchView.setOnQueryTextListener(queryTextListener);
        }

        if (mMenuSearch != null)
            mMenuSearch.setVisible(mSpinnerPosition != 3);


    }

    private void collapseSearchView() {
        if (!filters.isEmpty())
            removeFilters();

        //Find EditText view
        EditText et = (EditText) getActivity().findViewById(R.id.search_src_text);

        //Clear the text from EditText view
        et.setText("");

        //Clear query
        searchView.setQuery("", false);
        //Collapse the action view
        searchView.onActionViewCollapsed();
        //Collapse the search widget
        mMenuSearch.collapseActionView();
    }

    private void removeFilters() {
        searchWord = "";
        searchText = "";
        setFilters("");
        setCurrPage("");
        executeListMethods(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v;
            v = inflater.inflate(R.layout.frag_list, container, false);


        webViewProgress = (LinearLayout) v.findViewById(R.id.webview_placeholder_progress);

        rv = (RecyclerView) v.findViewById(R.id.rec_view);

        mErrorView = (ErrorView) v.findViewById(R.id.error_view);

        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (isErrorViewVisible) {
            if (whichErrorWasLast == 0) {
                toggleEmptyListPlaceholder();
            } else {
                toggleNoConnectionPlaceholder();
            }
        } else {
            mErrorView.setVisibility(View.GONE);
        }

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);
        if (url != null && isNetworkAvailable()) {
            rvAdapter = new RecyclerAdapter(getActivity(), guideItems);
            rv.setAdapter(rvAdapter);
            rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) //check for scroll down
                    {
                        visibleItemCount = layoutManager.getChildCount();
                        totalItemCount = layoutManager.getItemCount();
                        pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                        {
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                if (mFutureListRawData == null || mFutureListRawData.isDone()) {
                                    if (linkNext != null && url != null && !mFromsSavedState) {
                                        currPage = linkNext;
                                        mAddToExisting = true;
                                        executeListMethods(true);
                                    }
                                }
                            }
                        }
                    }
                }
            });
            if (!mFromsSavedState) {
                executeListMethods(false);
            }
        } else {
            executeFavsMethod();
        }

        mFromsSavedState = false;


    }


    public void executeListMethods(boolean changePage) {
//        setFiltersVisibility(App.VISIBLE);
        mErrorView.setVisibility(View.GONE);
        if (guideItems.size() == 0 || changePage) {
            if (isNetworkAvailable()) {
                fadeInProgress();
                mFutureListRawData = Ion.with(getActivity())
                        .load(url + filters + currPage)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                if (e == null) {
                                    try {
                                        parseHtmlGetList(result);

                                        if (guideItems.size() == 0) {
                                            togglePlaceholder(App.METHOD_EMPTYLIST_FILTER_PLACEHOLDER);
                                        } else {
                                            attachRecycleAdapter();
                                        }
                                    } catch (NullPointerException | IOException e1) {
                                        e1.printStackTrace();
                                        togglePlaceholder(App.METHOD_NO_CONNECTION_PLACEHOLDER);
                                    }
                                    fadeOutProgress();
                                } else {
                                    if (!e.getClass().getName().equals(CancellationException.class.getName())) {

                                        if (!mAddToExisting) {
                                            togglePlaceholder(App.METHOD_NO_CONNECTION_PLACEHOLDER);
                                            fadeOutProgress();
                                        } else {
                                            Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        });
            } else {
                if (!mAddToExisting)
                    togglePlaceholder(App.METHOD_NO_CONNECTION_PLACEHOLDER);
                else
                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            attachRecycleAdapter();
        }
    }

    private void attachRecycleAdapter() {
        if (!mAddToExisting) {
            rvAdapter.updateList(guideItems, false);
        } else {

            rvAdapter.updateList(guideItems, true);
        }
        mAddToExisting = false;
    }

    public void executeFavsMethod() {
//        setFiltersVisibility(App.GONE);
        mTaskGetFavs = new GetFavsTask();
        mTaskGetFavs.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (url == null) {
            executeFavsMethod();
        }

//        if (isAdded())
//            onDestroy();

    }

    private void parseHtmlGetList(String html) throws IOException {

        Document doc = Jsoup.parse(html);

        if (!mAddToExisting) {
            guideItems = new ArrayList<>();
        }

        GuideItem gi;
        Elements elems = doc.select("a.workshopItemCollection");
        for (Element el : elems) {
            gi = new GuideItem();
            String src = el.select(".fileRating").attr("src");
            int index = src.indexOf("-star.png") - 1;
            if (index == -2) {
                gi.setRating(0);
            } else {
                gi.setRating(Integer.parseInt(String.valueOf(src.charAt(index))));
            }
            gi.setImgLink(el.select(".workshopItemPreviewImage").attr("src"));
            gi.setLink(el.attr("href"));
            gi.setHeader(el.select(".workshopItemTitle").text());
            gi.setDesc(el.select(".workshopItemShortDesc").text());
            guideItems.add(gi);
        }

        Element paging = doc.select(".workshopBrowsePagingWithBG").first();
        if (paging != null) {

            boolean pagingEmpty = !paging.select(".workshopBrowsePagingControls").hasText();

            if (!pagingEmpty) {
                Element el = paging.select(".pagebtn").last();
                linkNext = el.className().contains("disabled") ? null : el.attr("href").substring(el.attr("href").length() - 4, el.attr("href").length());
            }

        }
    }


    private void getFavsFromDB() {
        DBControllerAdapter dbControllerAdapter = DBControllerAdapter.getInstance(getActivity());
        guideItems = dbControllerAdapter.getGuides();
    }


    private class GetFavsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mErrorView.setVisibility(View.GONE);
            fadeInProgress();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getFavsFromDB();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (guideItems.size() == 0) {
                togglePlaceholder(App.METHOD_EMPTYLIST_FAV_PLACEHOLDER);
            } else {
                rvAdapter = new RecyclerAdapter(getActivity(), guideItems);
                rv.setAdapter(rvAdapter);
            }
            fadeOutProgress();
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /**
     * Methods for send intents to activity (methods names similar as activity has)
     */


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


    private void togglePlaceholder(Integer method) {
        switch (method) {
            case App.METHOD_EMPTYLIST_FAV_PLACEHOLDER:
            case App.METHOD_EMPTYLIST_FILTER_PLACEHOLDER:
                toggleEmptyListPlaceholder();
                break;
            case App.METHOD_NO_CONNECTION_PLACEHOLDER:
                toggleNoConnectionPlaceholder();
                break;

        }
    }

    private void toggleEmptyListPlaceholder() {

        mErrorView.setConfig(ErrorView.Config.create()
                .image(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? R.drawable.no_items_dark : R.drawable.no_items_light)
                .title(getString(R.string.error_oops))
                .subtitle(getString(R.string.error_message_items))
                .retryVisible(false)
//                .retryText(getString(R.string.error_retry_filters))
                .build());

        whichErrorWasLast = 0;
        mErrorView.setVisibility(View.VISIBLE);

    }

    private void toggleNoConnectionPlaceholder() {

        mErrorView.setConfig(ErrorView.Config.create()
                .image(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? R.drawable.no_internet_dark : R.drawable.no_internet_light)
                .title(getString(R.string.error_oops))
                .subtitle(getString(R.string.error_message_internet))
                .retryVisible(true)
                .retryText(getString(R.string.error_retry_retry))
                .build());

        whichErrorWasLast = 1;

        mErrorView.setVisibility(View.VISIBLE);

        mErrorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                executeListMethods(true);
            }
        });
    }


    private void updateSpinnerIcons (int selectedPosition) {
        SpinnerAdapter.SpinnerItem spinnerItem;
        SpinnerAdapter adapter = (SpinnerAdapter) mSpinner.getAdapter();
        for (int i=0; i < adapter.getCount(); i++ ) {
            spinnerItem = (SpinnerAdapter.SpinnerItem) mSpinner.getAdapter().getItem(i);
            spinnerItem.icon = buildMenuIcon(i, i == selectedPosition);
        }
        adapter.notifyDataSetChanged();
    }

    private AdapterView.OnItemSelectedListener spinnerClickListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // When the given dropdown item is selected, show its contents in the
            // container view.


            updateSpinnerIcons(position);

            if (mSpinnerPosition != position) {
                mFromsSavedState = false;
                String cat;

                if (position == 0) {
                    cat = "trend";
                    category = "&browsefilter=" + cat + "&browsesort=" + cat;
                    showFragment(getFinalUrl());
                } else if (position == 1) {
                    cat = "toprated";
                    category = "&browsefilter=" + cat + "&browsesort=" + cat;
                    showFragment(getFinalUrl());
                } else if (position == 2) {
                    cat = "mostrecent";
                    category = "&browsefilter=" + cat + "&browsesort=" + cat;
                    showFragment(getFinalUrl());
                } else if (position == 3) {
                    showFragment(null);

                }
                mSpinnerPosition = position;

                if (mMenuSearch != null)
                    mMenuSearch.setVisible(mSpinnerPosition != 3);
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };


    public void showFragment(String url) {
        if (!mFromsSavedState) {

            setUrl(url);
            setFilters("");
            setCurrPage("");
            if (url != null)
                executeListMethods(true);
            else {
                executeFavsMethod();
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }

    public boolean isSearchViewOpened() {
        return searchView != null && !searchView.isIconified();
    }

    public void closeSearchView () {
        collapseSearchView();
    }


}