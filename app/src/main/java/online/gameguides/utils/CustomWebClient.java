package online.gameguides.utils;

import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Tony on 17-Nov-15.
 */
public class CustomWebClient extends WebViewClient {

    public CustomWebClient() {
//        this.myFragment = myFragment;
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        String jsItem = "javascript: var el = document.getElementsByClassName('responsive_page_content'); el[0].style.paddingTop = 0;";
        view.loadUrl(jsItem);
//        myFragment.fadeOutProgress();
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url != null && !url.contains("images.akamai")) {
            view.getContext().startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        } else {
            return false;
        }
    }

}
