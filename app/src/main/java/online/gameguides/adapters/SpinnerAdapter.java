package online.gameguides.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import online.gameguides.R;

/**
 * Created by Tony on 28-Dec-15.
 */
public class SpinnerAdapter extends ArrayAdapter<SpinnerAdapter.SpinnerItem> implements ThemedSpinnerAdapter {
    private final ThemedSpinnerAdapter.Helper mDropDownHelper;
    private Spinner mSpinner;

    public SpinnerAdapter(Context context, Spinner spinner, SpinnerItem[] objects) {
        super(context, R.layout.spinner_item, R.id.toolbar_spinner_text, objects);
        mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        mSpinner = spinner;

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
//        v.setVisibility(View.GONE);

//        if (convertView == null) {
//            LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
//            v = inflater.inflate(R.layout.spinner_item, parent, false);
//        }
        Drawable icon = getItem(position).icon;
//        icon.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
//        icon.setColorFilter(new
//                PorterDuffColorFilter(v.getContext().getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN));


        ((ImageView) v.findViewById(R.id.toolbar_spinner_icon)).setImageDrawable(icon);

        return v;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = null;

        if (convertView == null) {
            // Inflate the drop down using the helper's LayoutInflater
            LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
            view = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);

            holder = new ViewHolder();

            holder.icon = (ImageView) view.findViewById(R.id.toolbar_spinner_icon_drop);
            holder.text = (TextView) view.findViewById(R.id.toolbar_spinner_text);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }


        if (holder.icon != null) {




            holder.icon.setImageDrawable(getItem(position).icon);
        }


        if (holder.text != null)
            holder.text.setText(getItem(position).text);


        return view;
    }


    @Override
    public Resources.Theme getDropDownViewTheme() {
        return mDropDownHelper.getDropDownViewTheme();
    }

    @Override
    public void setDropDownViewTheme(Resources.Theme theme) {
        mDropDownHelper.setDropDownViewTheme(theme);
    }

    static class ViewHolder {
        ImageView icon;
        TextView text;
    }

    public static class SpinnerItem {
        public String text;
        public Drawable icon;


        public SpinnerItem(String text, Drawable icon) {
            this.text = text;
            this.icon = icon;
        }

    }
}
