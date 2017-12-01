package online.gameguides.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.ads.NativeExpressAdView;

/**
 * Created by Tony on 09-Nov-15.
 */
public class GuideItem implements Parcelable{
    private String link;
    private String header;
    private String desc;
    private String imgLink;
    private String release;
    private String id;
    private int rating;
    private NativeExpressAdView adView;
    private int isAd; // 1 = true 0 = false

    public void setAdView(NativeExpressAdView adView) {
        this.adView = adView;
        isAd = 1;
        link = "";
        header = "";
        desc = "";
        imgLink = "";
        release = "";
        id = "";
        rating = 0;
    }

    public NativeExpressAdView getAdView () {
        return adView;
    }

    public boolean isAd() {
        return isAd == 1;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public void setLink (String link) {
        this.link = link;
    }

    public void setHeader (String header) {
        this.header = header;
    }

    public void setDesc (String  desc) {
        this.desc = desc;
    }

    public void setImgLink (String imgLink) {
        this.imgLink = imgLink;
    }

    public void setRating (int rating) {
        this.rating = rating;
    }


    public String getLink () {
        return link;
    }

    public String getHeader ()  {
        return header;
    }

    public String getDesc () {
        return desc;
    }

    public String getImgLink () {
        return imgLink;
    }

    public int getRating () {
        return rating;
    }

    public int describeContents() {
        return 0;
    }

    /** save object in parcel */
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(rating);
        out.writeInt(isAd);
        out.writeStringArray(new String[] {link, imgLink, header, desc, id, release});
    }

    public static final Parcelable.Creator<GuideItem> CREATOR
            = new Parcelable.Creator<GuideItem>() {
        public GuideItem createFromParcel(Parcel in) {
            return new GuideItem(in);
        }

        public GuideItem[] newArray(int size) {
            return new GuideItem[size];
        }
    };

    public GuideItem () {

    }

    /** recreate object from parcel */
    private GuideItem(Parcel in) {
        rating = in.readInt();
        isAd = in.readInt();
        String[] st = new String[6];
        in.readStringArray(st);
        link = st[0];
        imgLink = st[1];
        header = st[2];
        desc = st[3];
        id = st[4];
        release = st[5];
    }
}
