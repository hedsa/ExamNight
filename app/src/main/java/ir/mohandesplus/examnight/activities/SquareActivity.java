package ir.mohandesplus.examnight.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dynamixsoftware.ErrorAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ir.mohandesplus.examnight.BuildConfig;
import ir.mohandesplus.examnight.R;
import ir.mohandesplus.examnight.adapters.PackageAdapter;
import ir.mohandesplus.examnight.app.AppController;
import ir.mohandesplus.examnight.modules.Package;
import ir.mohandesplus.examnight.modules.SaveMode;
import ir.mohandesplus.examnight.utils.PreferenceUtils;
import ir.mohandesplus.examnight.utils.TimeUtils;
import ir.mohandesplus.examnight.utils.WebUtils;
import ir.mohandesplus.examnight.views.AutofitRecyclerView;
import ir.mohandesplus.examnight.views.SpaceItemDecoration;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SquareActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String SQUARE_TITLE = "SquareTitle",
            PACKAGE_IDS = "PackageIds";

    Button retryButton;
    ActionBar actionBar;
    View[] clickableViews;
    TextView noConnectionText;
    FloatingActionButton addToCart;
    MaterialProgressBar progressBar;
    AutofitRecyclerView recyclerView;
    View noConnectionLayout, contentLayout;

    List<Package> packages;
    PackageAdapter adapter;
    boolean isShowingProgressBar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_square);

        initializeViews();
        setUpToolbar();
        showProgressBar();
        initializeRecyclerView();
        addToCart.setVisibility(View.GONE); // Todo: For Now

    }

    private void initializeViews() {
        contentLayout = findViewById(R.id.square_content);
        noConnectionLayout = findViewById(R.id.no_connection);
        addToCart = (FloatingActionButton) findViewById(R.id.fab);
        noConnectionText = (TextView) findViewById(R.id.no_connection_text);
        retryButton = (Button) findViewById(R.id.no_connection_button);
        progressBar = (MaterialProgressBar) findViewById(R.id.progress_bar);
        recyclerView = (AutofitRecyclerView) findViewById(R.id.square_recycler);
        clickableViews = new View[]{retryButton, addToCart};
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getIntent().getStringExtra(SQUARE_TITLE));
        }
    }

    private void organizeViews() {
        handleClicks();
        loadPackages();
    }

    private void initializeRecyclerView() {

        SpaceItemDecoration spaceDecoration = new SpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_margin_side),
                getResources().getDimensionPixelSize(R.dimen.grid_margin_side)
        );

        GridLayoutManager manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);
        adapter = new PackageAdapter(this, new ArrayList<Package>());
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(spaceDecoration);

        organizeViews();

    }

    private void handleClicks() {
        for (View view : clickableViews) view.setOnClickListener(this);
    }

    int result = 1;
    Package pack;
    private void loadPackages() {

        packages = new ArrayList<>();
        String[] packageIds = getIntent().getStringArrayExtra(PACKAGE_IDS);

        HashMap<String, String> params = new HashMap<>();
        params.put("Request", "Get");

        // Todo: Cancel All When One Corrupted! (Volley Tags)

        for (String packageId : packageIds) {

            params.put("Id", packageId);
            final String cacheKey = WebUtils.generateCacheKeyWithParam(BuildConfig.URL_PACKAGE, params);

            Cache cache = AppController.getInstance().getRequestQueue().getCache();
            Cache.Entry entry = cache.get(cacheKey);

            if (entry != null && TimeUtils.getMinuteDifference(entry.serverDate, System.currentTimeMillis()) <= 30) {
                try {
                    if (result == 0) break;
                    String content = new String(entry.data, "UTF-8");
                    JSONObject packageModel = new JSONObject(content);
                    pack = new Package();
                    pack.setId(Long.valueOf(packageModel.getInt("Id")));
                    pack.price = packageModel.getInt("Price");
                    pack.title = packageModel.getString("Title");
                    pack.questions = packageModel.getString("Questions");
                    pack.description = packageModel.getString("Description");
                    packages.add(pack);
                    adapter.insertAtEnd(pack);
                    if (isShowingProgressBar) showContent();
                } catch (JSONException e) {
                    showNoConnectionLayout();
                    noConnectionText.setText(e.toString());
                    result = 0;
                    e.printStackTrace();
                    ErrorAgent.reportError(e, "Error while parsing json from cache");
                } catch (UnsupportedEncodingException e) {
                    showNoConnectionLayout();
                    noConnectionText.setText(e.toString());
                    result = 0;
                    e.printStackTrace();
                    ErrorAgent.reportError(e, "Error while parsing string from cache");
                }
            } else {
                if (entry != null) cache.invalidate(cacheKey, true);
                final JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.GET,
                        WebUtils.generateUrlWithGetParams(BuildConfig.URL_PACKAGE, params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject packageModel) {
                                try {
                                    if (result == 0) return;
                                    pack = new Package();
                                    pack.setId(Long.valueOf(packageModel.getInt("Id")));
                                    pack.price = packageModel.getInt("Price");
                                    pack.title = packageModel.getString("Title");
                                    pack.questions = packageModel.getString("Questions");
                                    pack.description = packageModel.getString("Description");
                                    packages.add(pack);
                                    adapter.insertAtEnd(pack);
                                    if (isShowingProgressBar) showContent();
                                } catch (JSONException e) {
                                    showNoConnectionLayout();
                                    noConnectionText.setText(e.toString());
                                    result = 0;
                                    e.printStackTrace();
                                    ErrorAgent.reportError(e, "Error while parsing json from web response");
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                showNoConnectionLayout();
                                noConnectionText.setText(error.toString());
                                showNoConnectionLayout();
                                error.printStackTrace();
                                ErrorAgent.reportError(error, "Error while parsing json from web response");
                                result = 0;
                            }
                        }
                ) {
                    @Override
                    public String getCacheKey() {
                        return cacheKey;
                    }
                };
                AppController.getInstance().addToRequestQueue(request);
            }

            if (result == 0) break;

        }

    }

    private void showProgressBar() {
        isShowingProgressBar = true;
        addToCart.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        noConnectionLayout.setVisibility(View.GONE);
    }

    private void showContent() {
        isShowingProgressBar = false;
        progressBar.setVisibility(View.GONE);
        addToCart.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.VISIBLE);
        noConnectionLayout.setVisibility(View.GONE);
    }

    private void showNoConnectionLayout() {
        isShowingProgressBar = false;
        addToCart.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.square, menu);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.no_connection_button: loadPackages(); break;
            case R.id.fab: {
                for (Package pack : packages) {
                    pack.setSaveMode(SaveMode.CART);
                    pack.save();
                }
                Toast.makeText(SquareActivity.this, "Added To Cart!", Toast.LENGTH_LONG).show();
                break;
            }
            default: break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed(); return true;
            case R.id.square_action_shopping_cart: {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(MainActivity.LAUNCH_MODE, MainActivity.SHOPPING_CART);
                startActivity(intent);
                return true;
            }
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        if (!PreferenceUtils.hasLoggedIn(this))
            startActivity(new Intent(this, LoginActivity.class));
        super.onResume();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
