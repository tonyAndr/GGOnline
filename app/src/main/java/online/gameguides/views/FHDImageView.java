package online.gameguides.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Tony on 05-Jan-16.
 */
public class FHDImageView extends ImageView {

    public FHDImageView(Context context) {
        super(context);
    }

    public FHDImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FHDImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = Math.round(width / 1.7777f);
        setMeasuredDimension(width, height);
    }

}