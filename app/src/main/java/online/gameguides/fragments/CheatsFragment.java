package online.gameguides.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import online.gameguides.App;
import online.gameguides.R;
import online.gameguides.activities.MainActivity;
import online.gameguides.adapters.CheatsRecyclerAdapter;
import online.gameguides.models.CheatsContent;
import tr.xip.errorview.ErrorView;

public class CheatsFragment extends Fragment {

    private OnCheatsFragmentInteractionListener mListener;
    private CheatsRecyclerAdapter mCheatsAdapter;
    private RecyclerView mCheatsRecycler;

    private SharedPreferences mPrefs;

    private LinearLayout mProgressView;

    private String mUrl;
    private Future<String> mFutureHtml;

    public boolean noCheats = false;
    private boolean fromSavedState = false;

    private ErrorView mErrorView;

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean("noCheats", noCheats);

        super.onSaveInstanceState(outState);
    }


    public CheatsFragment() {
    }

    @SuppressWarnings("unused")
    public static CheatsFragment newInstance() {
        CheatsFragment fragment = new CheatsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CheatFrag", "CHEATS FRAG Created");
        setHasOptionsMenu(false);

        mPrefs = getActivity().getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;

        view = inflater.inflate(R.layout.fragment_cheats, container, false);

        mCheatsRecycler = view.findViewById(R.id.cheats_recycle_list);
        mCheatsRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));

        mProgressView = view.findViewById(R.id.webview_placeholder_progress);

        mErrorView = view.findViewById(R.id.error_view);
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCheatsFragmentInteractionListener) {
            mListener = (OnCheatsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mUrl = "https://www.gamefaqs.com/search?game=" + getString(R.string.game_name);

        mCheatsAdapter = new CheatsRecyclerAdapter(CheatsContent.ITEMS, mListener);
        mCheatsRecycler.setAdapter(mCheatsAdapter);

        showCheats();
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
                showCheats();
            }
        });

        mProgressView.setVisibility(View.GONE);
    }

    private void hideCheatsFragment() {
        noCheats = true;

//        final MainActivity activity = (MainActivity) getActivity();
//        if (activity != null)
//            activity.hideEmptyFragment();


    }

    private void showCheats() {
        mProgressView.setVisibility(View.GONE);
        mCheatsAdapter.notifyDataSetChanged();

        if (CheatsContent.ITEMS == null || CheatsContent.ITEMS.isEmpty()) {
            showErrorView();
        }
        else {
            mErrorView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        if (mFutureHtml != null && !mFutureHtml.isDone()) {
            mFutureHtml.cancel(true);
            mFutureHtml = null;
        }
        super.onDestroy();
        Log.d("AppCycle", "CheatsFrag Destroyed");
    }

    public interface OnCheatsFragmentInteractionListener {
        void OnCheatsFragmentInteractionListener();
    }




}
