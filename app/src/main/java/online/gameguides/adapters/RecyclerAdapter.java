package online.gameguides.adapters;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import online.gameguides.App;
import online.gameguides.BuildConfig;
import online.gameguides.activities.MainActivity;
import online.gameguides.fragments.MyFragment;
import online.gameguides.models.GuideItem;

import online.gameguides.R;

import online.gameguides.activities.WebActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tony on 09-Nov-15.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private SharedPreferences mPrefs;

    private final int NORMAL_VIEW = 0;
//    private final int ADMOB_VIEW = 1;
//    private final int POSITION_1 = 5;
//    private final int POSITION_2 = 25;
//    private final int POSITION_3 = 50;

    private Integer AdSizeWidth = 0; // save size to prevent multi-call of .post func
    private Integer AdSizeHeight = 132;

    private List<GuideItem> guideItems;
//    private Context mContext;

    private RecyclerView mRecyclerView;
//    private String AdID;

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
                super.onAttachedToRecyclerView(recyclerView);
        this.mRecyclerView = recyclerView;
    }

    public RecyclerAdapter(Context context, List<GuideItem> guideItems) {
        this.guideItems = guideItems;
//        this.mContext = context;

//        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
//            AdID = context.getString(R.string.ad_native_dark);
//        } else {
//            AdID = context.getString(R.string.ad_native_light);
//
//        }

//        if (!BuildConfig.DEBUG && guideItems.size() > 0)
//            reInsertAdsToList();
        mPrefs = context.getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void updateList(List<GuideItem> guideItems, boolean toEnd) {
        this.guideItems = guideItems;
//        if (!BuildConfig.DEBUG)
//            reInsertAdsToList();
        notifyDataSetChanged();
    }

//    private void reInsertAdsToList() {
//        insertAdToList(POSITION_1);
//        insertAdToList(POSITION_2);
//        insertAdToList(POSITION_3);
//
//        if (AdSizeWidth > 0) {
//            AdSize size = new AdSize(AdSizeHeight, AdSizeHeight);
//            setAdViewSize(size);
//        } else if (mRecyclerView != null ) {
//            mRecyclerView.post(new Runnable() {
//                @Override
//                public void run() {
//                    float density = mRecyclerView.getContext().getResources().getDisplayMetrics().density;
//                    AdSizeWidth = (int)(mRecyclerView.getWidth()/density - 8);
//                    AdSize size = new AdSize(AdSizeWidth, AdSizeHeight);
//                    setAdViewSize(size);
//                }
//            });
//        } else {
//            AdSize size = new AdSize((int)(320), AdSizeHeight);
//            setAdViewSize(size);
//        }
//    }

//    private void insertAdToList(int pos) {
//        if (guideItems.size() > pos) {
//            if (!guideItems.get(pos).isAd() || guideItems.get(pos).getAdView() == null) {
//                NativeExpressAdView nativeExpressAdView;
//                GuideItem guideItem;
//
//                nativeExpressAdView = createNativeAdForRecycler();
//                guideItem = new GuideItem();
//                guideItem.setAdView(nativeExpressAdView);
//                guideItems.add(pos, guideItem);
//            }
//        }
//    }

//    private void setAdViewSize (AdSize size) {
//        GuideItem g;
//        for (int i = 5; i < guideItems.size(); i+=5) {
//            if (i == POSITION_1 || i == POSITION_2 || i == POSITION_3) {
//                 g = guideItems.get(i);
//                    if (g.isAd() && g.getAdView().getAdSize() == null) {
//                        g.getAdView().setAdSize(size);
//                        g.getAdView().loadAd(new AdRequest.Builder().build());
//
//                    }
//
//            }
//        }
//
//    }

//    private NativeExpressAdView createNativeAdForRecycler() {
//        final NativeExpressAdView adView = new NativeExpressAdView(mRecyclerView.getContext());
//        adView.setAdUnitId(AdID);
//        adView.setAdListener(new AdListener(){
//            @Override
//            public void onAdFailedToLoad(int errorCode) {
//                // Code to be executed when an ad request fails.
//                adView.setTag(false);
//            }
//
//            @Override
//            public void onAdLoaded() {
//                adView.setTag(true);
//            }
//        });
//        return adView;
//    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {

        View view;
        switch (type) {
//            case ADMOB_VIEW:
//                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.admob_native, viewGroup, false);
//                return new AdmobViewHolder(view);
            case NORMAL_VIEW:
            default:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
                return new CustomViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {

//          рабочий кусок кода для нативной рекламы, отключено 13.11.17 (денег не приносит)
//        if (!BuildConfig.DEBUG)
//            if (position == POSITION_1 || position == POSITION_2 || position == POSITION_3) {
//                return ADMOB_VIEW;
//            }
        return NORMAL_VIEW;

    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        GuideItem guideItem = guideItems.get(i);

        switch (getItemViewType(i)) {
//            case ADMOB_VIEW:
//                AdmobViewHolder admobViewHolder = (AdmobViewHolder) holder;
//                NativeExpressAdView nativeExpressAdView = guideItem.getAdView();
//
//                ViewGroup adCardView = (ViewGroup) admobViewHolder.itemView;
//
//                if (nativeExpressAdView.getParent() != null) {
//                    ((ViewGroup) nativeExpressAdView.getParent()).removeView(nativeExpressAdView);
//                }
//
//                if (nativeExpressAdView.getTag() != null && nativeExpressAdView.getTag() instanceof Boolean && (Boolean) nativeExpressAdView.getTag()) {
//                    adCardView.addView(nativeExpressAdView);
//                }
//                admobViewHolder.itemView.setOnClickListener(clickListener);
//                admobViewHolder.itemView.setTag(holder);
//                break;
            case NORMAL_VIEW:
            default:
                CustomViewHolder normalHolder = (CustomViewHolder) holder;

                Context context = normalHolder.imageView.getContext();
                //Download image using picasso library
                if (context.getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE).getBoolean(App.PREFS_DATA_SAVE, true)
                        || guideItem.getLink() == null
                        || normalHolder.itemView == null ) {
                    if (i % 2 == 0)
                        normalHolder.imageView.setBackgroundColor(context.getResources().getColor(R.color.ph1));
                    if (i % 3 == 0)
                        normalHolder.imageView.setBackgroundColor(context.getResources().getColor(R.color.ph2));
                    if (i % 4 == 0)
                        normalHolder.imageView.setBackgroundColor(context.getResources().getColor(R.color.ph3));
                    if (i % 5 == 0)
                        normalHolder.imageView.setBackgroundColor(context.getResources().getColor(R.color.ph4));
                    if (i % 6 == 0)
                        normalHolder.imageView.setBackgroundColor(context.getResources().getColor(R.color.ph5));
                } else {
                    Picasso.with(context).load(guideItem.getImgLink()).into(normalHolder.imageView);
                }
                //Setting text view title
                normalHolder.textViewHeader.setText(guideItem.getHeader());
                normalHolder.textViewDesc.setText(guideItem.getDesc());

                String stars = "";
                if (guideItem.getRating() > 0)
                    for (int j = 0; j < guideItem.getRating(); j++) {
                        stars = stars + "\u2605";
                    }
                normalHolder.textViewRating.setText(stars);

                normalHolder.itemView.setOnClickListener(clickListener);
                normalHolder.itemView.setTag(holder);
                break;
        }
    }


    @Override
    public int getItemCount() {
        return guideItems == null ? 0 : guideItems.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected final ImageView imageView;
        protected final TextView textViewHeader;
        protected final TextView textViewDesc;
        protected final TextView textViewRating;

        public CustomViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.iv_icon);
            this.textViewHeader = (TextView) view.findViewById(R.id.tv_header);
            this.textViewDesc = (TextView) view.findViewById(R.id.tv_desc);
            this.textViewRating = (TextView) view.findViewById(R.id.tv_item_rating);
        }
    }

    public class AdmobViewHolder extends RecyclerView.ViewHolder {
        AdmobViewHolder(View view) {
            super(view);
        }
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view.getTag() instanceof CustomViewHolder) {
                CustomViewHolder holder = (CustomViewHolder) view.getTag();
                int position = holder.getAdapterPosition();
                Context context = holder.imageView.getContext();
                GuideItem guideItem = guideItems.get(position);

                Intent intent = new Intent(context, WebActivity.class);
                intent.putExtra("guideItem", guideItem);

                context.startActivity(intent);
            } else { // esli reclama ne zagruzilas', to pokazana zaglushka
                rateApp();
            }
        }
    };

    public void rateApp()
    {
        try
        {
            Intent rateIntent = rateIntentForUrl("market://details");
            mRecyclerView.getContext().startActivity(rateIntent);
        }
        catch (ActivityNotFoundException e)
        {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            mRecyclerView.getContext().startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, mRecyclerView.getContext().getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21)
        {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        }
        else
        {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }
}
