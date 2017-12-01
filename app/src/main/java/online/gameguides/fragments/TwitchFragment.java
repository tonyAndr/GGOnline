package online.gameguides.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import online.gameguides.App;
import online.gameguides.R;
import online.gameguides.adapters.MyTwitchRecyclerViewAdapter;
import online.gameguides.models.TwitchContent;
import online.gameguides.models.TwitchContent.TwitchItem;
import tr.xip.errorview.ErrorView;

public class TwitchFragment extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private Future<JsonObject> mFutureTwitchJson;
    private MyTwitchRecyclerViewAdapter myTwitchRecyclerViewAdapter;

    private final String TWITCH_KEY = "xc1pe35q5qcrou4539wxw74cnn1way";

    private LinearLayout webViewProgress;
    private SharedPreferences mPrefs;
    private ErrorView mErrorView;

    public TwitchFragment() {
    }

    @SuppressWarnings("unused")
    public static TwitchFragment newInstance() {
        TwitchFragment fragment = new TwitchFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        mPrefs = getActivity().getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;

            view = inflater.inflate(R.layout.fragment_twitch_list, container, false);



        myTwitchRecyclerViewAdapter = new MyTwitchRecyclerViewAdapter(TwitchContent.ITEMS, mListener);
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.twitch_recycle_list);

        boolean tabletSize = getResources().getBoolean(R.bool.is_tablet);

        if (getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || tabletSize) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        }


        recyclerView.setAdapter(myTwitchRecyclerViewAdapter);

        mErrorView = (ErrorView) view.findViewById(R.id.error_view);
        webViewProgress = (LinearLayout) view.findViewById(R.id.webview_placeholder_progress);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getStreams();
    }

    private void getStreams() {
        if (isNetworkAvailable()) {
            fadeInProgress();
            String url = "https://api.twitch.tv/kraken/streams?client_id=" + TWITCH_KEY + "&amp;limit=20&amp;game=" + getString(R.string.game_name);
            mFutureTwitchJson = Ion.with(this)
                    .load(url)
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {

                            if (result == null || result.get("_total").getAsInt() == 0) {
                                showErrorPlaceholder(0);
                            } else {
                                JsonArray streams = result.getAsJsonArray("streams");
                                parseJsonArray(streams);
                                myTwitchRecyclerViewAdapter.notifyDataSetChanged();
                                mErrorView.setVisibility(View.GONE);
                            }
                            fadeOutProgress();
                        }
                    });

        } else {
            showErrorPlaceholder(1);
        }
    }

    private void showErrorPlaceholder(int mode) {
        fadeOutProgress();
        boolean nightmode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        mErrorView.setConfig(ErrorView.Config.create()
                .image(mode == 1 ? nightmode ? R.drawable.no_internet_dark : R.drawable.no_internet_light : nightmode ? R.drawable.no_items_dark : R.drawable.no_items_light)
                .title(getString(R.string.error_oops))
                .subtitle(mode == 1 ? getString(R.string.error_message_internet) : getString(R.string.error_message_nostreams))
                .retryVisible(true)
                .retryText(getString(R.string.error_retry_retry))
                .build());

        mErrorView.setOnRetryListener(new ErrorView.RetryListener() {
            @Override
            public void onRetry() {
                getStreams();
            }
        });

        mErrorView.setVisibility(View.VISIBLE);
    }

    private void parseJsonArray(JsonArray array) {
        TwitchContent.clearItems();
        TwitchItem twitchItem;
        int size = array.size();
        for (int i = 0; i < size; i++) {
            twitchItem = new TwitchItem(String.valueOf(i),
                    array.get(i).getAsJsonObject().get("preview").getAsJsonObject().get("medium").getAsString(),
                    array.get(i).getAsJsonObject().get("channel").getAsJsonObject().get("display_name").getAsString(),
                    array.get(i).getAsJsonObject().get("channel").getAsJsonObject().get("status").getAsString(),
                    array.get(i).getAsJsonObject().get("viewers").getAsString(),
                    array.get(i).getAsJsonObject().get("channel").getAsJsonObject().get("language").getAsString(),
                    array.get(i).getAsJsonObject().get("channel").getAsJsonObject().get("url").getAsString());
            TwitchContent.addItem(twitchItem);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AppCycle", "TwitchFrag Destroyed");
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(TwitchItem item);
    }


    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
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

}
