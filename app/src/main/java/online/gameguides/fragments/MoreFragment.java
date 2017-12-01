package online.gameguides.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import online.gameguides.App;
import online.gameguides.R;
import online.gameguides.activities.MainActivity;
import online.gameguides.activities.StuffActivity;

public class MoreFragment extends Fragment {

    private OnLinkMoreInteractionListener mLinkListener;

    //    private ImageButton mLinkSteam;
//    private ImageButton mLinkReddit;
//    private ImageButton mLinkTwitch;
//    private ImageButton mLinkWiki;
//    private ImageButton mLinkWallpapers;
    private View mGetMore;

    private ImageView mIconFaq;
    private ImageView mIconAbout;
    private ImageView mIconGetMore;

    private LinearLayout mBtnFaq;
    private LinearLayout mBtnAbout;
//    private CardView mUsefulLinks;

    private SharedPreferences mPrefs;
    //    private Switch mNightModeSwitch;
    private Switch mNotificationsSwitch;
    private Switch mImagesSwitch;


    public MoreFragment() {
        // Required empty public constructor
    }


    public static MoreFragment newInstance() {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
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
        View v;
//        if (!mPrefs.getBoolean(App.KEY_NIGHTMODE, false))
        v = inflater.inflate(R.layout.fragment_more, container, false);
//        else
//            v = inflater.inflate(R.layout.fragment_more_dark, container, false);

//        mLinkSteam = (ImageButton) v.findViewById(R.id.ib_more_steam);
//        mLinkReddit = (ImageButton) v.findViewById(R.id.ib_more_reddit);
//        mLinkTwitch = (ImageButton) v.findViewById(R.id.ib_more_twitch);
//        mLinkWiki = (ImageButton) v.findViewById(R.id.ib_more_wiki);
//        mLinkWallpapers = (ImageButton) v.findViewById(R.id.ib_more_wallpapers);

        setupLinks();

        mGetMore = v.findViewById(R.id.ib_more_get);

        mIconFaq = (ImageView) v.findViewById(R.id.ic_more_faq);
        mIconAbout = (ImageView) v.findViewById(R.id.ic_more_about);
        mIconGetMore = (ImageView) v.findViewById(R.id.ic_more_get);

        mIconFaq.setImageDrawable(buildMenuIcon(GoogleMaterial.Icon.gmd_question_answer));
        mIconAbout.setImageDrawable(buildMenuIcon(GoogleMaterial.Icon.gmd_info_outline));
        if (mIconGetMore != null)
            mIconGetMore.setImageDrawable(buildMenuIcon(GoogleMaterial.Icon.gmd_file_download));

        mBtnFaq = (LinearLayout) v.findViewById(R.id.btn_more_faq);
        mBtnAbout = (LinearLayout) v.findViewById(R.id.btn_more_about);

//        mUsefulLinks = (CardView) v.findViewById(R.id.more_card_links);

//        mNightModeSwitch = (Switch) v.findViewById(R.id.switch_nightmode);
        mNotificationsSwitch = (Switch) v.findViewById(R.id.switch_notifications);
        mImagesSwitch = (Switch) v.findViewById(R.id.switch_images);

//        mNightModeSwitch.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        mNotificationsSwitch.setChecked(mPrefs.getBoolean(App.PREFS_NOTIFICATIONS_ENABLED, true));
        mImagesSwitch.setChecked(mPrefs.getBoolean(App.PREFS_DATA_SAVE, true));


//        mNightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                mPrefs.edit().putBoolean(App.KEY_NIGHTMODE, b).apply();
//                AppCompatDelegate
//                        .setDefaultNightMode(b ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
//                ((MainActivity)getActivity()).restartActivity();
//            }
//        });

        mNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mPrefs.edit().putBoolean(App.PREFS_NOTIFICATIONS_ENABLED, b).apply();
            }
        });

        mImagesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mPrefs.edit().putBoolean(App.PREFS_DATA_SAVE, b).apply();
                ((MainActivity) getActivity()).showDatasavingWarning(b);
                if (!b)
                    ((MainActivity) getActivity()).restartMainActvity();
            }
        });


        setupButtons();
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnLinkMoreInteractionListener) {
            mLinkListener = (OnLinkMoreInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLinkMoreInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("AppCycle", "MoreFrag Destroyed");
    }

    private Drawable buildMenuIcon(CommunityMaterial.Icon iconName) {
        int color = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? getResources().getColor(R.color.colorAccent) : getResources().getColor(R.color.colorSecondaryText);
        return new IconicsDrawable(getContext()).icon(iconName).sizeDp(32).color(color);
    }

    private Drawable buildMenuIcon(GoogleMaterial.Icon iconName) {
        int color = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? getResources().getColor(R.color.colorAccent) : getResources().getColor(R.color.colorPrimary);
        return new IconicsDrawable(getContext()).icon(iconName).sizeDp(20).color(color);
    }

    private void setupButtons() {
//        mLinkSteam.setImageDrawable(buildMenuIcon(CommunityMaterial.Icon.cmd_steam));
//        mLinkReddit.setImageDrawable(buildMenuIcon(CommunityMaterial.Icon.cmd_reddit));
//        mLinkWiki.setImageDrawable(buildMenuIcon(CommunityMaterial.Icon.cmd_wikipedia));
//        mLinkWallpapers.setImageDrawable(buildMenuIcon(CommunityMaterial.Icon.cmd_image_filter_hdr));
//        mLinkTwitch.setImageDrawable(buildMenuIcon(CommunityMaterial.Icon.cmd_twitch));

        mBtnFaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), StuffActivity.class);
                intent.putExtra("layoutId", R.layout.frag_faq);
                startActivity(intent);
            }
        });
        mBtnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), StuffActivity.class);
                intent.putExtra("layoutId", R.layout.frag_about);
                startActivity(intent);
            }
        });

        mGetMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.account_url))));
            }
        });
    }

    private void setupLinks() {
        String redditUrl = getString(R.string.link_reddit);
        String steamUrl = getString(R.string.link_steam) + getString(R.string.base_url);
        String wikiaUrl = getString(R.string.link_wikia);
        String wallhavenUrl = getString(R.string.link_wallhaven);
        String twitchUrl = getString(R.string.link_twitch) + getString(R.string.game_name);

//        if (steamUrl.isEmpty()) {
//            mLinkSteam.setVisibility(View.GONE);
//        } else {
//            mLinkSteam.setTag(steamUrl);
//        }
//        if (redditUrl.isEmpty()) {
//            mLinkReddit.setVisibility(View.GONE);
//        } else {
//            mLinkReddit.setTag(redditUrl);
//        }
//        if (wikiaUrl.isEmpty()) {
//            mLinkWiki.setVisibility(View.GONE);
//        } else {
//            mLinkWiki.setTag(wikiaUrl);
//        }
//        if (wallhavenUrl.isEmpty()) {
//            mLinkWallpapers.setVisibility(View.GONE);
//        } else {
//            mLinkWallpapers.setTag(wallhavenUrl);
//        }
//        if (twitchUrl.isEmpty()) {
//            mLinkTwitch.setVisibility(View.GONE);
//        } else {
//            mLinkTwitch.setTag(twitchUrl);
//        }
//
//        mLinkSteam.setOnClickListener(onLinkClickListener);
//        mLinkReddit.setOnClickListener(onLinkClickListener);
//        mLinkTwitch.setOnClickListener(onLinkClickListener);
//        mLinkWiki.setOnClickListener(onLinkClickListener);
//        mLinkWallpapers.setOnClickListener(onLinkClickListener);
//
//        if (redditUrl.isEmpty() && steamUrl.isEmpty() && wikiaUrl.isEmpty() && wallhavenUrl.isEmpty() && twitchUrl.isEmpty()) {
//            mUsefulLinks.setVisibility(View.GONE);
//        }

    }

    public interface OnLinkMoreInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(String url);
    }

    private View.OnClickListener onLinkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mLinkListener.onListFragmentInteraction(view.getTag().toString());
        }
    };
}
