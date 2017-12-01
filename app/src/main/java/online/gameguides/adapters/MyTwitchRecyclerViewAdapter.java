package online.gameguides.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import online.gameguides.App;
import online.gameguides.R;
import online.gameguides.fragments.TwitchFragment.OnListFragmentInteractionListener;
import online.gameguides.models.TwitchContent.TwitchItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TwitchItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyTwitchRecyclerViewAdapter extends RecyclerView.Adapter<MyTwitchRecyclerViewAdapter.ViewHolder> {

    private final List<TwitchItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public MyTwitchRecyclerViewAdapter(List<TwitchItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.twitch_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mStatus.setText(mValues.get(position).status);
        holder.mNick.setText(mValues.get(position).nick);
        holder.mViewers.setText(mValues.get(position).viewers + " viewers");
        holder.mLang.setText(mValues.get(position).lang);

        if (!holder.mView.getContext().getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE).getBoolean(App.PREFS_DATA_SAVE, true))
            Picasso.with(holder.mView.getContext()).load(mValues.get(position).previewUrl).placeholder(R.drawable.twitch_placeholder).into(holder.mPreview);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mPreview;
        public final TextView mStatus;
        public final TextView mNick;
        public final TextView mViewers;
        public final TextView mLang;
        public TwitchItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPreview = (ImageView) view.findViewById(R.id.iv_twitch_preview);
            mStatus = (TextView) view.findViewById(R.id.tv_twitch_header);
            mNick = (TextView) view.findViewById(R.id.tv_twitch_nick);
            mViewers = (TextView) view.findViewById(R.id.tv_twitch_viewers);
            mLang = (TextView) view.findViewById(R.id.tv_twitch_lang);
        }

    }
}
