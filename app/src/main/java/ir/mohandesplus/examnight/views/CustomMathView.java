package ir.mohandesplus.examnight.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ir.mohandesplus.examnight.utils.WebUtils;

public class CustomMathView extends WebView {

    public CustomMathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        requestFocusFromTouch();
        setBackgroundColor(Color.TRANSPARENT);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setAppCacheEnabled(false);
    }

    public void setHtmlText(String htmlText) {
        this.clearCache(true);
        this.loadDataWithBaseURL(WebUtils.generateHtmlFromLaTeXCode(htmlText),
                WebUtils.generateHtmlFromLaTeXCode(htmlText), "text/html", "utf-8", "about:blank");
    }

}
