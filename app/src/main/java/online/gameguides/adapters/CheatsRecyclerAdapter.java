package online.gameguides.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import online.gameguides.R;
import online.gameguides.activities.CheatsActivity;
import online.gameguides.fragments.CheatsFragment;
import online.gameguides.models.CheatsContent;

public class CheatsRecyclerAdapter extends RecyclerView.Adapter<CheatsRecyclerAdapter.ViewHolder> {

    private final List<CheatsContent.CheatItem> mValues;
    private final CheatsFragment.OnCheatsFragmentInteractionListener mListener;

    public CheatsRecyclerAdapter(List<CheatsContent.CheatItem> items, CheatsFragment.OnCheatsFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cheat_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mPlatform.setText(mValues.get(position).platform);

        holder.mView.setOnClickListener(cheatClickListener);
        holder.mView.setTag(holder.mItem);

    }

    public View.OnClickListener cheatClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheatsContent.CheatItem item;
            if (v.getTag() instanceof CheatsContent.CheatItem) {
                item = (CheatsContent.CheatItem) v.getTag();
                Intent intent = new Intent();
                intent.putExtra("platform", item.platform);
                intent.putExtra("link", item.link);
                intent.setClass(v.getContext(), CheatsActivity.class);
                v.getContext().startActivity(intent);
            }
            Log.d("CheatFrag", "Item clicked");
        }
    };

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mPlatform;
        public CheatsContent.CheatItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPlatform = (TextView) view.findViewById(R.id.cheat_item_platform);
        }

    }
}
