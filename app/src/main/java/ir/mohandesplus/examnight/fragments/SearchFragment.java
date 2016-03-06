package ir.mohandesplus.examnight.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import ir.mohandesplus.examnight.adapters.QuestionAdapter;
import ir.mohandesplus.examnight.app.AppController;
import ir.mohandesplus.examnight.modules.Package;
import ir.mohandesplus.examnight.modules.Question;
import ir.mohandesplus.examnight.utils.TimeUtils;
import ir.mohandesplus.examnight.utils.WebUtils;
import ir.mohandesplus.examnight.views.AutofitRecyclerView;
import ir.mohandesplus.examnight.views.SpaceItemDecoration;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class SearchFragment extends Fragment implements
        View.OnClickListener,
        UltimateRecyclerView.OnLoadMoreListener {

    public final static String SEARCH_CONTENT = "SearchContent",
            SEARCH_QUERY = "SearchQuery";
    public final static int CONTENT_PACKAGE = 1,
            CONTENT_QUESTION = 2;

    String searchQuery;
    int contentCode, loadedCount=0;
    boolean shouldLoadMore = false;

    View mainLayout;
    Button retryButton;
    View[] clicableViews;
    MaterialProgressBar progressBar;
    AutofitRecyclerView recyclerView;
    View noConnectionLayout, searchFirstLayout, nothingFoundLayout;

    UltimateViewAdapter adapter;

    public SearchFragment() {}

    public static SearchFragment newInstance(int contentCode, String searchQuery) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putInt(SEARCH_CONTENT, contentCode);
        args.putString(SEARCH_QUERY, searchQuery);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.contentCode = getArguments().getInt(SEARCH_CONTENT);
            this.searchQuery = getArguments().getString(SEARCH_QUERY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainLayout = inflater.inflate(R.layout.fragment_search, container, false);
        if (mainLayout!=null && isAdded()) showContent();
        return mainLayout;
    }

    private void showContent() {
        initializeViews();
        showProgressBar();
        loadContent();
    }

    private void initializeViews() {
        searchFirstLayout = mainLayout.findViewById(R.id.search_first);
        nothingFoundLayout = mainLayout.findViewById(R.id.nothing_found);
        noConnectionLayout = mainLayout.findViewById(R.id.no_connection);
        progressBar = (MaterialProgressBar) mainLayout.findViewById(R.id.progress_bar);
        retryButton = (Button) noConnectionLayout.findViewById(R.id.no_connection_button);
        recyclerView = (AutofitRecyclerView) mainLayout.findViewById(R.id.search_recycler);
        clicableViews = new View[]{retryButton};
    }

    private void loadContent() {
        if (TextUtils.isEmpty(searchQuery)) showSearchFirstLayout();
        else switch (contentCode) {
            case CONTENT_PACKAGE: searchPackages(); break;
            case CONTENT_QUESTION: searchQuestions(); break;
        }
    }

    private void searchPackages() {

        HashMap<String, String> params = new HashMap<>();
        params.put("Request", "Search");
        params.put("SearchQuery", searchQuery);
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
                    pack = new Package();
                    pack.setId(Long.valueOf(packageModel.getInt("Id")));
                    pack.price = packageModel.getInt("Price");
                    pack.title = packageModel.getString("Title");
                    pack.questions = packageModel.getString("Questions");
                    pack.description = packageModel.getString("Description");
                    packages.add(pack);
                }
                enableLoadMore();
                if (packagesModel.length() < 10) disableLoadMore();
                feedPackagesData(packages);
                loadedCount += packagesModel.length();
            } catch (JSONException e) {
                e.printStackTrace();
                showNoConnectionLayout();
                ErrorAgent.reportError(e, "Error while parsing json from cache");
            } catch (UnsupportedEncodingException e) {
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
                                    pack = new Package();
                                    pack.setId(Long.valueOf(packageModel.getInt("Id")));
                                    pack.price = packageModel.getInt("Price");
                                    pack.title = packageModel.getString("Title");
                                    pack.questions = packageModel.getString("Questions");
                                    pack.description = packageModel.getString("Description");
                                    packages.add(pack);
                                }
                                enableLoadMore();
                                if (packagesModel.length() < 10) disableLoadMore();
                                feedPackagesData(packages);
                                loadedCount += packagesModel.length();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                showNoConnectionLayout();
                                ErrorAgent.reportError(e, "Error while parsing json from web response");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
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

    private void searchQuestions() {

        HashMap<String, String> params = new HashMap<>();
        params.put("Request", "Search");
        params.put("Method", "ByTitle");
        params.put("SearchQuery", searchQuery);
        params.put("FirstRow", String.valueOf(loadedCount));
        params.put("LastRow", String.valueOf(loadedCount+10));

        final String cacheKey = WebUtils.generateCacheKeyWithParam(BuildConfig.URL_QUESTION, params);
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(cacheKey);

        if (entry!=null && TimeUtils.
                getMinuteDifference(entry.serverDate, System.currentTimeMillis())<=30) {
            try {
                List<Question> questions = new ArrayList<>();
                Question question;
                JSONObject questionModel;
                String content = new String(entry.data, "UTF-8");
                JSONArray questionsModel = new JSONArray(content);
                for (int i=0; i<questionsModel.length(); i++) {
                    questionModel = questionsModel .getJSONObject(i);
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
                }
                enableLoadMore();
                if (questionsModel.length() < 10) disableLoadMore();
                feedQuestionsData(questions);
                loadedCount += questionsModel.length();
            } catch (JSONException e) {
                e.printStackTrace();
                showNoConnectionLayout();
                ErrorAgent.reportError(e, "Error while parsing json from cache");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                showNoConnectionLayout();
                ErrorAgent.reportError(e, "Error while parsing string from cache");
            }
        } else {
            if (entry != null) cache.invalidate(cacheKey, true);
            JsonArrayRequest request = new JsonArrayRequest(
                    Request.Method.GET,
                    WebUtils.generateUrlWithGetParams(BuildConfig.URL_QUESTION, params),
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray questionsModel) {
                            List<Question> questions = new ArrayList<>();
                            Question question;
                            JSONObject questionModel;
                            try {
                                for (int i=0; i<questionsModel .length(); i++) {
                                    questionModel = questionsModel .getJSONObject(i);
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
                                }
                                enableLoadMore();
                                if (questionsModel.length() < 10) disableLoadMore();
                                feedQuestionsData(questions);
                                loadedCount += questionsModel.length();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                showNoConnectionLayout();
                                ErrorAgent.reportError(e, "Error while parsing json from web response");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
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

        if (getActivity() == null) return;
        if (adapter == null) {
            if (packages.size() == 0) {
                showNothingFoundLayout();
                return;
            }
            SpaceItemDecoration spaceDecoration = new SpaceItemDecoration(
                    getResources().getDimensionPixelSize(R.dimen.grid_margin_side),
                    getResources().getDimensionPixelSize(R.dimen.grid_margin_side)
            );
            GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setHasFixedSize(false);
            recyclerView.setLayoutManager(manager);
            adapter = new PackageAdapter(getActivity(), packages);
            recyclerView.setAdapter(adapter);
            recyclerView.setOnLoadMoreListener(this);
            recyclerView.addItemDecoration(spaceDecoration);
            showContentLayout();
        } else ((PackageAdapter)adapter).insertDataAtEnd(packages);

    }

    private void feedQuestionsData(final List<Question> questions) {

        if (getActivity() == null) return;
        if (adapter == null) {
            if (questions.size() == 0) {
                showNothingFoundLayout();
                return;
            }
            SpaceItemDecoration spaceDecoration = new SpaceItemDecoration(
                    getResources().getDimensionPixelSize(R.dimen.grid_margin_side),
                    getResources().getDimensionPixelSize(R.dimen.grid_margin_side)
            );
            GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
            recyclerView.setHasFixedSize(false);
            recyclerView.setLayoutManager(manager);
            adapter = new QuestionAdapter(getActivity(), questions);
            recyclerView.setAdapter(adapter);
            recyclerView.setOnLoadMoreListener(this);
            recyclerView.addItemDecoration(spaceDecoration);
            showContentLayout();
        } else ((QuestionAdapter)adapter).insertDataAtEnd(questions);

    }

    private void showProgressBar() {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        searchFirstLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.GONE);
    }

    private void showContentLayout() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        searchFirstLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.GONE);
        nothingFoundLayout.setVisibility(View.GONE);
    }

    private void showNoConnectionLayout() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        searchFirstLayout.setVisibility(View.GONE);
        nothingFoundLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.VISIBLE);
    }

    private void showSearchFirstLayout() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        nothingFoundLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.GONE);
        searchFirstLayout.setVisibility(View.VISIBLE);
    }

    private void showNothingFoundLayout() {
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
        if (recyclerView!=null && adapter!=null &&
                recyclerView.isLoadMoreEnabled()) recyclerView.disableLoadmore();
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
}
