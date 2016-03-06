package ir.mohandesplus.examnight.app;

import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.orm.SugarApp;

import ir.mohandesplus.examnight.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class AppController extends SugarApp {

    public static final String TAG = AppController.class.getSimpleName();
    public static final long WATCH_ID = 12324; // TODO
    private static AppController appControllerInstance;
    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig calligraphyConfig = new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/IranianSansLight.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build();
        CalligraphyConfig.initDefault(calligraphyConfig);
        appControllerInstance = this;
    }

    public static synchronized AppController getInstance() {
        return appControllerInstance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request, String tag) {
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(request);
    }

    public <T> void addToRequestQueue(Request<T> request) {
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public void cancelPendingRequests(Object tag) {
        if (requestQueue != null) requestQueue.cancelAll(tag);
    }

}
