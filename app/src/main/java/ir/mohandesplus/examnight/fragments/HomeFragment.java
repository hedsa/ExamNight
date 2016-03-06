package ir.mohandesplus.examnight.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.text.TextUtilsCompatJellybeanMr1;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.dynamixsoftware.ErrorAgent;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ir.mohandesplus.examnight.BuildConfig;
import ir.mohandesplus.examnight.R;
import ir.mohandesplus.examnight.activities.SquareActivity;
import ir.mohandesplus.examnight.adapters.PackageAdapter;
import ir.mohandesplus.examnight.app.AppController;
import ir.mohandesplus.examnight.modules.*;
import ir.mohandesplus.examnight.modules.Package;
import ir.mohandesplus.examnight.utils.TimeUtils;
import ir.mohandesplus.examnight.utils.WebUtils;
import ir.mohandesplus.examnight.views.SpaceItemDecoration;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class HomeFragment extends Fragment implements View.OnClickListener {

    MaterialProgressBar progressBar;
    LinearLayout contentLayout;
    View noConnectionLayout;
    View clickableViews[];
    Button retryButton;
    View mainLayout;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainLayout = inflater.inflate(R.layout.fragment_home, container, false);
        if (mainLayout!=null && isAdded()) showContent();
        return mainLayout;
    }

    private void showContent() {
        initializeViews();
        organizeViews();
    }

    private void initializeViews() {

        Context context = getActivity();
        if (context == null) return;

        noConnectionLayout = mainLayout.findViewById(R.id.no_connection);
        progressBar = (MaterialProgressBar) mainLayout.findViewById(R.id.progress_bar);
        contentLayout = (LinearLayout) mainLayout.findViewById(R.id.fragment_home_content);
        retryButton = (Button) noConnectionLayout.findViewById(R.id.no_connection_button);

        clickableViews = new View[]{retryButton};

    }

    private void organizeViews() {
        showProgressBar();
        handleClicks();
        loadContent();
    }

    private void loadContent() {

        HashMap<String, String> params = new HashMap<>();
        params.put("Request", "HomeContent");

        final String cacheKey = WebUtils.generateCacheKeyWithParam(BuildConfig.URL_MAIN, params);
        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(cacheKey);

        if (entry != null && TimeUtils.
                getMinuteDifference(entry.serverDate, System.currentTimeMillis()) <= 30) {
            try {
                List<Package> packages = new ArrayList<>();
                List<Square> squares = new ArrayList<>();
                JSONArray array;
                JSONObject item;
                Square square;
                Package pack;
                String title;
                String content = new String(entry.data, "UTF-8");
                JSONArray response = new JSONArray(content);
                for (int i=0; i<response.length(); i++) {
                    item = (JSONObject) response.get(i);
                    /*
                     * 1: GridSquares
                     * 2: HorizontalList
                     */
                    if (item.getInt("type") == 2) {
                        item = item.getJSONObject("content");
                        title = item.getString("title");
                        array = item.getJSONArray("packages");
                        packages.clear();
                        for (int j=0; j<array.length(); j++) {
                            item = array.getJSONObject(j);
                            pack = new Package();
                            Long id = Long.valueOf(item.getInt("Id"));
                            Log.d("test", "Setting pack's id to : " + id);
                            pack.setId(id);
                            pack.price = item.getInt("Price");
                            pack.title = item.getString("Title");
                            pack.questions = item.getString("Questions");
                            pack.description = item.getString("Description");
                            packages.add(pack);
                        }
                        addHorizontalList(packages, title);
                    } else {
                        item = item.getJSONObject("content");
                        array = item.getJSONArray("squares");
                        squares.clear();
                        for (int j=0; j<array.length(); j++) {
                            item = array.getJSONObject(j);
                            square = new Square();
                            square.title = item.getString("title");
                            square.imageUrl = item.getString("imageUrl");
                            square.packages = item.getString("packages");
                            squares.add(square);
                        }
                        addGridSquares(squares);
                    }
                }
                showContentLayout();
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
                    WebUtils.generateUrlWithGetParams(BuildConfig.URL_MAIN, params),
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            List<Package> packages = new ArrayList<>();
                            List<Square> squares = new ArrayList<>();
                            JSONArray array;
                            JSONObject item;
                            Square square;
                            Package pack;
                            String title;
                            try {
                                for (int i=0; i<response.length(); i++) {
                                    item = (JSONObject) response.get(i);
                                    /*
                                     * 1: GridSquares
                                     * 2: HorizontalList
                                     */
                                    if (item.getInt("type") == 2) {
                                        item = item.getJSONObject("content");
                                        title = item.getString("title");
                                        array = item.getJSONArray("packages");
                                        packages.clear();
                                        for (int j=0; j<array.length(); j++) {
                                            item = array.getJSONObject(j);
                                            pack = new Package();
                                            Long id = Long.valueOf(item.getInt("Id"));
                                            Log.d("test", "Setting pack's id to : " + id);
                                            pack.setId(id);pack.price = item.getInt("Price");
                                            pack.title = item.getString("Title");
                                            pack.questions = item.getString("Questions");
                                            pack.description = item.getString("Description");
                                            packages.add(pack);
                                        }
                                        addHorizontalList(packages, title);
                                    } else {
                                        item = item.getJSONObject("content");
                                        array = item.getJSONArray("squares");
                                        squares.clear();
                                        for (int j=0; j<array.length(); j++) {
                                            item = array.getJSONObject(j);
                                            square = new Square();
                                            square.title = item.getString("title");
                                            square.imageUrl = item.getString("imageUrl");
                                            square.packages = item.getString("packages");
                                            squares.add(square);
                                        }
                                        addGridSquares(squares);
                                    }
                                }
                                showContentLayout();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                showNoConnectionLayout();
                                ErrorAgent.reportError(e, "Error parsing json from web response");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            showNoConnectionLayout();
                            showNoConnectionLayout();
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

    private void addGridSquares(List<Square> squares) {
        final Context context = getActivity();
        if (context == null) return;
        View mainItem;
        Square square1, square2;
        TextView title1, title2;
        ImageView image1, image2;
        for (int i=0; i<squares.size(); i+=2) {
            mainItem = LayoutInflater.from(context).inflate(R.layout.square_grid, contentLayout, false);
            title1 = (TextView) mainItem.findViewById(R.id.square_text_1);
            title2 = (TextView) mainItem.findViewById(R.id.square_text_2);
            image1 = (ImageView) mainItem.findViewById(R.id.square_image_1);
            image2 = (ImageView) mainItem.findViewById(R.id.square_image_2);
            square1 = squares.get(i);
            square2 = squares.get(i+1);
            title1.setText(square1.title);
            title2.setText(square2.title);
//            Picasso.with(context)
//                    .load(square1.imageUrl)
//                    .fit()
//                    .error(R.drawable.ic_action_no_signal)
//                    .into(image1);
//            Picasso.with(context)
//                    .load(square2.imageUrl)
//                    .fit()
//                    .error(R.drawable.ic_action_no_signal)
//                    .into(image2);
            contentLayout.addView(mainItem);
            image1.setTag(R.id.square_title, square1.title);
            title1.setTag(R.id.square_title, square1.title);
            image2.setTag(R.id.square_title, square2.title);
            title2.setTag(R.id.square_title, square2.title);
            image1.setTag(R.id.package_ids, square1.getPackageIds());
            title1.setTag(R.id.package_ids, square1.getPackageIds());
            image2.setTag(R.id.package_ids, square2.getPackageIds());
            title2.setTag(R.id.package_ids, square2.getPackageIds());
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, SquareActivity.class);
                    intent.putExtra(SquareActivity.SQUARE_TITLE, (String) view.getTag(R.id.square_title));
                    intent.putExtra(SquareActivity.PACKAGE_IDS, (String[]) view.getTag(R.id.package_ids));
                    startActivity(intent);
                }
            };
            image1.setOnClickListener(listener);
            image2.setOnClickListener(listener);
            title1.setOnClickListener(listener);
            title2.setOnClickListener(listener);
            // Todo: Images
        }
    }

    private void addHorizontalList(List<Package> packages, String title) {
        Context context = getActivity();
        if (context == null) return;
        SpaceItemDecoration spaceDecoration = new SpaceItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_margin_side), 0
        );
        View mainCategory;
        TextView titleText;
        UltimateRecyclerView recyclerView;
        mainCategory = LayoutInflater.from(context).inflate(R.layout.horizontal_card_list, contentLayout, false);
        titleText = (TextView) mainCategory.findViewById(R.id.horizontal_list_title);
        recyclerView = (UltimateRecyclerView) mainCategory.findViewById(R.id.horizontal_list_self);
        titleText.setText(title);
        LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new PackageAdapter(context, packages));
        recyclerView.addItemDecoration(spaceDecoration);
        contentLayout.addView(mainCategory);
    }

    private void handleClicks() {
        for (View view : clickableViews) view.setOnClickListener(this);
    }

    private void showProgressBar() {
        contentLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        noConnectionLayout.setVisibility(View.GONE);
    }

    private void showContentLayout() {
        progressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.VISIBLE);
        noConnectionLayout.setVisibility(View.GONE);
    }

    private void showNoConnectionLayout() {
        progressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.no_connection_button: organizeViews(); break;
            default: break;
        }
    }

}
