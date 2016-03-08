package ir.mohandesplus.examnight.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.dynamixsoftware.ErrorAgent;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ir.mohandesplus.examnight.BuildConfig;
import ir.mohandesplus.examnight.R;
import ir.mohandesplus.examnight.adapters.ExpandablePackageAdapter;
import ir.mohandesplus.examnight.app.AppController;
import ir.mohandesplus.examnight.modules.Package;
import ir.mohandesplus.examnight.modules.Question;
import ir.mohandesplus.examnight.modules.SaveMode;
import ir.mohandesplus.examnight.utils.PreferenceUtils;
import ir.mohandesplus.examnight.utils.TimeUtils;
import ir.mohandesplus.examnight.utils.WebUtils;
import ir.mohandesplus.examnight.views.SpaceItemDecoration;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PackageActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PACKAGE_ID = "PackageId",
            PACKAGE_TITLE = "PackageTitle",
            PACKAGE_PRICE = "PackagePrice",
            PACKAGE_IMAGES_URL = BuildConfig.URL_IMAGES + "packages/";

    Toolbar toolbar;
    Button retryButton;
    ActionBar actionBar;
    ImageView packImage;
    View[] clickableViews;
    View noConnectionLayout;
    FloatingActionButton fab;
    AppBarLayout appBarLayout;
    RecyclerView recyclerView;
    MaterialProgressBar progressBar;
    TextView subtitleText, noConnectionText;
    CollapsingToolbarLayout collapsingToolbarLayout;

    Package pack;
    List<Question> questions;
    ExpandablePackageAdapter adapter;
    boolean isShowingProgressBar = false;

    Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);
        this.savedInstanceState = savedInstanceState;

        if (!PreferenceUtils.hasLoggedIn(this))
            startActivity(new Intent(this, LoginActivity.class));

        initializeViews();
        showProgressBar();
        loadPackage();

    }

    private void initializeViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        noConnectionLayout = findViewById(R.id.no_connection);
        packImage = (ImageView) findViewById(R.id.package_image);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        subtitleText = (TextView) findViewById(R.id.package_subtitle);
        retryButton = (Button) findViewById(R.id.no_connection_button);
        noConnectionText = (TextView) findViewById(R.id.no_connection_text);
        progressBar = (MaterialProgressBar) findViewById(R.id.progress_bar);
        recyclerView = (RecyclerView) findViewById(R.id.package_recycler);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        clickableViews = new View[]{retryButton, fab};
    }

    private void setUpDecoration() {
        setUpToolbar();
        fillCollapsingToolbarLayout();
        organizeFab();
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        toolbar.setTitle(pack.title);
    }

    private void organizeFab() {
        if (pack.isSaved() && pack.getSaveMode()==SaveMode.ARCHIVE) {
            fab.setImageResource(R.drawable.ic_action_archive);
        } else fab.setImageResource(R.drawable.ic_action_add_shopping_cart);
    }

    private void fillCollapsingToolbarLayout() {
        Picasso.with(this)
                .load(PACKAGE_IMAGES_URL + pack.getId() + "/1.jpg")
                .fit()
                .placeholder(R.drawable.ic_action_no_signal)
                .into(packImage);
        collapsingToolbarLayout.setTitle(pack.title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        subtitleText.setText(pack.description);
    }

    private void initializePackage() {

        questions = new ArrayList<>();
        setUpDecoration();
        SpaceItemDecoration spaceDecoration = new SpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_margin_side),
                getResources().getDimensionPixelSize(R.dimen.grid_margin_side)
        );

        LinearLayoutManager manager = new LinearLayoutManager(this);
        adapter = new ExpandablePackageAdapter(this, questions);
        adapter.onRestoreInstanceState(savedInstanceState);
        adapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
            @Override
            public void onListItemExpanded(int i) {
                adapter.collapseAllParents();
                adapter.expandParent(i);
                appBarLayout.setExpanded(false, true);
            }

            @Override
            public void onListItemCollapsed(int i) {
                appBarLayout.setExpanded(true, true);
            }
        });

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(spaceDecoration);

        for (View view : clickableViews) view.setOnClickListener(this);

    }

    private void loadPackage() {
        pack = new Package();
        pack.setId(getIntent().getLongExtra(PACKAGE_ID, -1));
        pack.title = getIntent().getStringExtra(PACKAGE_TITLE);
        pack.price = getIntent().getIntExtra(PACKAGE_PRICE, -1);
        if (pack.isSaved()) {
            pack = pack.getSavedPack();
            initializePackage();
            for (int id : pack.getQuestionIds()) {
                adapter.insertAtEnd(Question.findById(Question.class, id));
                if (isShowingProgressBar) showContent();
            }
        } else downLoadPackage();
    }

    private void downLoadPackage() {

        HashMap<String, String> params = new HashMap<>();
        params.put("Request", "Get");
        params.put("Id", String.valueOf(pack.getId()));
        final String cacheKey = WebUtils.generateCacheKeyWithParam(BuildConfig.URL_PACKAGE, params);

        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(cacheKey);

        if (entry != null && TimeUtils.
                getMinuteDifference(entry.serverDate, System.currentTimeMillis()) <= 30) {
            try {
                String content = new String(entry.data, "UTF-8");
                JSONObject packModel = new JSONObject(content);
                pack.description = packModel.getString("Description");
                pack.questions = packModel.getString("Questions");
                initializePackage();
                loadQuestions();
            } catch (JSONException e) {
                noConnectionText.setText(e.toString());
                showNoConnectionLayout();
                e.printStackTrace();
                ErrorAgent.reportError(e, "Error while parsing json from cache");
            } catch (UnsupportedEncodingException e) {
                noConnectionText.setText(e.toString());
                showNoConnectionLayout();
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
                        public void onResponse(JSONObject packModel) {
                            try {
                                pack.description = packModel.getString("Description");
                                pack.questions = packModel.getString("Questions");
                                initializePackage();
                                loadQuestions();
                            } catch (JSONException e) {
                                noConnectionText.setText(e.toString());
                                showNoConnectionLayout();
                                e.printStackTrace();
                                ErrorAgent.reportError(e, "Error while parsing json from web response");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            noConnectionText.setText(error.toString());
                            showNoConnectionLayout();
                            error.printStackTrace();
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

    int result = 1;
    Question question;
    private void loadQuestions() {

        questions = new ArrayList<>();

        final int[] questionIds = pack.getQuestionIds();
        HashMap<String, String> params = new HashMap<>();
        params.put("Request", "Get");

        for (int questionId : questionIds) {

            params.put("Id", String.valueOf(questionId));
            final String cacheKey = WebUtils.generateCacheKeyWithParam(BuildConfig.URL_QUESTION, params);

            Cache cache = AppController.getInstance().getRequestQueue().getCache();
            Cache.Entry entry = cache.get(cacheKey);

            if (entry != null && TimeUtils.
                    getMinuteDifference(entry.serverDate, System.currentTimeMillis()) <= 30) {
                try {
                    if (result == 0) break;
                    String content = new String(entry.data, "UTF-8");
                    JSONObject questionModel = new JSONObject(content);
                    question = new Question();
                    question.setId(Long.valueOf(questionModel.getInt("Id")));
                    question.field = questionModel.getInt("Field");
                    question.content = questionModel.getString("Content");
                    question.shortAnswer = questionModel.getString("SAnswer");
                    question.longAnswer = questionModel.getString("QAnswer");
                    question.code = questionModel.getInt("Code");
                    question.dateInformation = questionModel.getString("DateInformation");
                    question.type = questionModel.getInt("Type");
                    question.universityCode = questionModel.getInt("UniversityCode");
                    questions.add(question);
                    adapter.insertAtEnd(question);
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
                        WebUtils.generateUrlWithGetParams(BuildConfig.URL_QUESTION, params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject questionModel) {
                                try {
                                    if (result == 0) return;
                                    question = new Question();
                                    question.setId(Long.valueOf(questionModel.getInt("Id")));
                                    question.field = questionModel.getInt("Field");
                                    question.content = questionModel.getString("Content");
                                    question.shortAnswer = questionModel.getString("SAnswer");
                                    question.longAnswer = questionModel.getString("QAnswer");
                                    question.code = questionModel.getInt("Code");
                                    question.dateInformation = questionModel.getString("DateInformation");
                                    question.type = questionModel.getInt("Type");
                                    question.universityCode = questionModel.getInt("UniversityCode");
                                    questions.add(question);
                                    adapter.insertAtEnd(question);
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

    private void hideFab() {
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        p.setAnchorId(View.NO_ID);
        fab.setLayoutParams(p);
        fab.setVisibility(View.GONE);
    }

    private void showFab() {
        CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        p.setAnchorId(R.id.app_bar);
        fab.setLayoutParams(p);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(this);
    }

    private void showProgressBar() {
        isShowingProgressBar = true;
        appBarLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        noConnectionLayout.setVisibility(View.GONE);
        hideFab();
    }

    private void showContent() {
        isShowingProgressBar = false;
        progressBar.setVisibility(View.GONE);
        appBarLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        noConnectionLayout.setVisibility(View.GONE);
        showFab();
    }

    private void showNoConnectionLayout() {
        isShowingProgressBar = false;
        progressBar.setVisibility(View.GONE);
        appBarLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.VISIBLE);
        hideFab();
    }

    private void addPackageToCart() {

        for (Question question : questions) {
            if (!question.isSaved()) question.save();
        }

        if (!pack.isSaved()) {
            pack.setSaveMode(SaveMode.CART);
            pack.save();
            Toast.makeText(PackageActivity.this, "Added To Cart!", Toast.LENGTH_LONG).show();
        } else {
            if (pack.getSaveMode() == SaveMode.CART) {
                Toast.makeText(PackageActivity.this, "Was Already Added To Cart!", Toast.LENGTH_LONG).show();
            } else if (pack.getSaveMode() == SaveMode.ARCHIVE) {
                // Todo: Navigate to Archives...
                Toast.makeText(this,"Archived!!!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this,"WTF!? SaveMode = " + pack.getSaveMode(), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.no_connection_button: loadPackage(); break;
            case R.id.fab: addPackageToCart(); break;
            default: break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed(); return true;
            case R.id.fab: addPackageToCart(); return true;
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

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        adapter.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        adapter.onRestoreInstanceState(savedInstanceState);
    }

}
