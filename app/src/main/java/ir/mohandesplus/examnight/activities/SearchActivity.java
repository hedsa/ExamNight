package ir.mohandesplus.examnight.activities;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.dynamixsoftware.ErrorAgent;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;

import org.json.JSONArray;
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
import ir.mohandesplus.examnight.utils.TimeUtils;
import ir.mohandesplus.examnight.utils.WebUtils;
import ir.mohandesplus.examnight.views.AutofitRecyclerView;
import ir.mohandesplus.examnight.views.SpaceItemDecoration;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SearchActivity extends AppCompatActivity implements
        View.OnClickListener,
        UltimateRecyclerView.OnLoadMoreListener {

    public final static String SEARCH_QUERY = "SearchQuery";

    int loadedCount=0;
    String searchQuery;
    boolean shouldLoadMore = false;

    Button retryButton;
    ActionBar actionBar;
    AppBarLayout appbar;
    View[] clicableViews;
    TextView noConnectionText;
    MaterialProgressBar progressBar;
    AutofitRecyclerView recyclerView;
    View noConnectionLayout, searchFirstLayout, nothingFoundLayout;

    UltimateViewAdapter adapter;
    List<Long> ids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        this.searchQuery = getIntent().getStringExtra(SEARCH_QUERY);
        initializeToolbar();
        initializeViews();
        addDecorations();
        organizeViews();
        ids = new ArrayList<>();

    }

    private void initializeToolbar() {
        appbar = (AppBarLayout) findViewById(R.id.appbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.search));
        }
    }

    private void initializeViews() {
        searchFirstLayout = findViewById(R.id.search_first);
        nothingFoundLayout = findViewById(R.id.nothing_found);
        noConnectionLayout = findViewById(R.id.no_connection);
        progressBar = (MaterialProgressBar) findViewById(R.id.progress_bar);
        retryButton = (Button) noConnectionLayout.findViewById(R.id.no_connection_button);
        noConnectionText = (TextView) noConnectionLayout.findViewById(R.id.no_connection_text);
        recyclerView = (AutofitRecyclerView) findViewById(R.id.search_recycler);
        clicableViews = new View[]{retryButton};
    }

    private void addDecorations() {
        SpaceItemDecoration spaceDecoration = new SpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_margin_side),
                getResources().getDimensionPixelSize(R.dimen.grid_margin_side)
        );
        recyclerView.addItemDecoration(spaceDecoration);
    }

    private void organizeViews() {
        showProgressBar();
        for (View view : clicableViews) view.setOnClickListener(this);
        if (TextUtils.isEmpty(searchQuery)) {
            actionBar.setTitle(R.string.search);
            showSearchFirstLayout();
        } else {
            actionBar.setTitle(searchQuery);
            loadedCount = 0;
            adapter = null;
            loadContent();
        }
    }

    private void loadContent() {

        if (ids == null) ids = new ArrayList<>();
        HashMap<String, String> params = new HashMap<>();
        params.put("Request", "Search");
        params.put("SearchQuery", searchQuery.trim());
        params.put("FirstRow", String.valueOf(loadedCount));
        params.put("LastRow", String.valueOf(loadedCount+10));

        final String cacheKey = WebUtils.generateCacheKeyWithParam(BuildConfig.URL_PACKAGE, params);
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(cacheKey);

        if (entry!=null && TimeUtils.
                getMinuteDifference(entry.serverDate, System.currentTimeMillis())<=30) {
            try {
                List<Package> packages = new ArrayList<>();
                Package pack;
                JSONObject packageModel;
                String content = new String(entry.data, "UTF-8");
                JSONArray packagesModel = new JSONArray(content);
                for (int i=0; i<packagesModel.length(); i++) {

                    packageModel = packagesModel.getJSONObject(i);
                    if (TextUtils.isEmpty(packageModel.getString("PackageDetails"))) continue;
                    String[] packs = packageModel.getString("PackageDetails").split(",");

                    for (String pac : packs) {
                        if (TextUtils.isEmpty(pac)) continue;
                        String[] details = pac.split("\\|");
                        pack = new Package();
                        pack.setId(Long.valueOf(details[0]));
                        if (!ids.contains(pack.getId())) {
                            ids.add(pack.getId());
                            pack.title = details[1];
                            pack.price = Integer.valueOf(details[2]);
                            packages.add(pack);
                        }
                    }

                }
                enableLoadMore();
                if (packagesModel.length() < 10) disableLoadMore();
                feedPackagesData(packages);
                loadedCount += packagesModel.length();
            } catch (JSONException e) {
                noConnectionText.setText(e.toString());
                e.printStackTrace();
                showNoConnectionLayout();
                ErrorAgent.reportError(e, "Error while parsing json from cache");
            } catch (UnsupportedEncodingException e) {
                noConnectionText.setText(e.toString());
                e.printStackTrace();
                showNoConnectionLayout();
                ErrorAgent.reportError(e, "Error while parsing string from cache");
            }
        } else {
            if (entry != null) cache.invalidate(cacheKey, true);
            JsonArrayRequest request = new JsonArrayRequest(
                    Request.Method.GET,
                    WebUtils.generateUrlWithGetParams(BuildConfig.URL_PACKAGE, params),
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray packagesModel) {
                            List<Package> packages = new ArrayList<>();
                            Package pack;
                            JSONObject packageModel;
                            try {
                                for (int i=0; i<packagesModel.length(); i++) {

                                    packageModel = packagesModel.getJSONObject(i);
                                    if (TextUtils.isEmpty(packageModel.getString("PackageDetails"))) continue;
                                    String[] packs = packageModel.getString("PackageDetails").split(",");

                                    for (String pac : packs) {
                                        if (TextUtils.isEmpty(pac)) continue;
                                        String[] details = pac.split("\\|");
                                        pack = new Package();
                                        pack.setId(Long.valueOf(details[0]));
                                        if (!ids.contains(pack.getId())) {
                                            ids.add(pack.getId());
                                            pack.title = details[1];
                                            pack.price = Integer.valueOf(details[2]);
                                            packages.add(pack);
                                        }
                                    }

                                }
                                enableLoadMore();
                                if (packagesModel.length() < 10) disableLoadMore();
                                feedPackagesData(packages);
                                loadedCount += packagesModel.length();
                            } catch (JSONException e) {
                                noConnectionText.setText(e.toString());
                                e.printStackTrace();
                                showNoConnectionLayout();
                                ErrorAgent.reportError(e, "Error while parsing json from web response");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            noConnectionText.setText(error.toString());
                            error.printStackTrace();
                            showNoConnectionLayout();
                            ErrorAgent.reportError(error, "Error while parsing json from web response");
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

    }

    private void feedPackagesData(List<Package> packages) {

        if (adapter == null) {
            if (packages.size() == 0) {
                showNothingFoundLayout();
                return;
            }
            GridLayoutManager manager = new GridLayoutManager(this, 2);
            recyclerView.setHasFixedSize(false);
            recyclerView.setLayoutManager(manager);
            adapter = new PackageAdapter(this, packages);
            recyclerView.setAdapter(adapter);
            recyclerView.setOnLoadMoreListener(this);
            showContentLayout();
        } else ((PackageAdapter)adapter).insertDataAtEnd(packages);

    }

    private void showProgressBar() {
        if (ids != null) ids.clear();
        adapter = null;
        appbar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        searchFirstLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.GONE);
        nothingFoundLayout.setVisibility(View.GONE);
    }

    private void showContentLayout() {
        appbar.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        searchFirstLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.GONE);
        nothingFoundLayout.setVisibility(View.GONE);
    }

    private void showNoConnectionLayout() {
        if (ids != null) ids.clear();
        adapter = null;
        appbar.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        searchFirstLayout.setVisibility(View.GONE);
        nothingFoundLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.VISIBLE);
    }

    private void showSearchFirstLayout() {
        if (ids != null) ids.clear();
        adapter = null;
        appbar.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        nothingFoundLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.GONE);
        searchFirstLayout.setVisibility(View.VISIBLE);
    }

    private void showNothingFoundLayout() {
        if (ids != null) ids.clear();
        adapter = null;
        appbar.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        searchFirstLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.GONE);
        nothingFoundLayout.setVisibility(View.VISIBLE);
    }

    private void enableLoadMore() {
        this.shouldLoadMore = true;
        if (recyclerView != null) recyclerView.enableLoadmore();
    }

    private void disableLoadMore() {
        this.shouldLoadMore = false;
        if (recyclerView!=null && adapter!=null && recyclerView.isLoadMoreEnabled())
            recyclerView.disableLoadmore();
    }

    @Override
    public void loadMore(int itemsCount, int maxLastVisiblePosition) {
        if (shouldLoadMore) loadContent();
        else disableLoadMore();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.no_connection_button: loadContent(); break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQuery = query;
                organizeViews();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
