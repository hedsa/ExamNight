package ir.mohandesplus.examnight.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;

import ir.mohandesplus.examnight.utils.WebUtils;

public class CustomMathView extends WebView {

    public CustomMathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        requestFocusFromTouch();
        setBackgroundColor(Color.TRANSPARENT);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setAppCacheEnabled(false);
        setLongClickable(true);
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        setLongClickable(false);
        setHapticFeedbackEnabled(false);
    }

    public void setHtmlText(String htmlText) {
        this.clearCache(true);
        this.loadDataWithBaseURL(WebUtils.generateHtmlFromLaTeXCode(htmlText),
                WebUtils.generateHtmlFromLaTeXCode(htmlText), "text/html", "utf-8", "about:blank");
    }

}
