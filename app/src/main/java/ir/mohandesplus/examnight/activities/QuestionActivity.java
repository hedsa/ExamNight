package ir.mohandesplus.examnight.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.HashMap;

import ir.mohandesplus.examnight.BuildConfig;
import ir.mohandesplus.examnight.R;
import ir.mohandesplus.examnight.app.AppController;
import ir.mohandesplus.examnight.modules.Question;
import ir.mohandesplus.examnight.modules.SaveMode;
import ir.mohandesplus.examnight.utils.TimeUtils;
import ir.mohandesplus.examnight.utils.WebUtils;
import ir.mohandesplus.examnight.views.CustomMathView;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class QuestionActivity extends AppCompatActivity implements View.OnClickListener {

    public final static String QUESTION_ID = "QuestionId";

    Button retryButton;
    ActionBar actionBar;
    View[] clickableViews;
    FloatingActionButton addToCart;
    MaterialProgressBar progressBar;
    View noConnectionLayout, contentLayout;
    TextView buyQuestion, buyShortAnswer, buyLongAnswer;
    CustomMathView questionView, shortAnswerView, longAnswerView;

    Question question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        initializeViews();
        setUpToolbar();
        loadQuestion();

    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.app_name);
        }
    }

    private void initializeViews() {
        contentLayout = findViewById(R.id.question_content);
        noConnectionLayout = findViewById(R.id.no_connection);
        buyQuestion = (TextView) findViewById(R.id.question_buy);
        buyShortAnswer = (TextView) findViewById(R.id.short_buy);
        addToCart = (FloatingActionButton) findViewById(R.id.fab);
        buyLongAnswer = (TextView) findViewById(R.id.complete_buy);
        retryButton = (Button) findViewById(R.id.no_connection_button);
        progressBar = (MaterialProgressBar) findViewById(R.id.progress_bar);
        questionView = (CustomMathView) findViewById(R.id.question_problem);
        shortAnswerView = (CustomMathView) findViewById(R.id.question_short_answer);
        longAnswerView = (CustomMathView) findViewById(R.id.question_complete_answer);
        clickableViews = new View[]{retryButton, addToCart};
    }

    private void organizeViews() {
        handleClicks();
        setUpFab();
        feedQuestionData();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showContent();
            }
        }, 2000);
    }

    private void setUpFab() {
//        if (question.sumPrice == 0) addToCart.setImageResource(R.drawable.ic_action_archive);
    }

    private void feedQuestionData() {
//        if (question.qPrice > 0) {
//            questionView.setVisibility(View.GONE);
//            buyQuestion.setVisibility(View.VISIBLE);
//        } else {
//            buyQuestion.setVisibility(View.GONE);
//            questionView.setVisibility(View.VISIBLE);
//            questionView.setHtmlText(question.content);
//        }
//        if (question.sPrice > 0) {
//            shortAnswerView.setVisibility(View.GONE);
//            buyShortAnswer.setVisibility(View.VISIBLE);
//        } else {
//            buyShortAnswer.setVisibility(View.GONE);
//            shortAnswerView.setVisibility(View.VISIBLE);
//            shortAnswerView.setHtmlText(question.shortAnswer);
//        }
//        if (question.lPrice > 0) {
//            longAnswerView.setVisibility(View.GONE);
//            buyLongAnswer.setVisibility(View.VISIBLE);
//        } else {
//            buyLongAnswer.setVisibility(View.GONE);
//            longAnswerView.setVisibility(View.VISIBLE);
//            longAnswerView.setHtmlText(question.longAnswer);
//        }
    }

    private void handleClicks() {
        for (View view : clickableViews) view.setOnClickListener(this);
    }

    private void loadQuestion() {

        final String questionId = getIntent().getStringExtra(QUESTION_ID);
        showProgressBar();

        HashMap<String, String> params = new HashMap<>();
        params.put("Request", "Get");
        params.put("Id", questionId);

        params.put("Id", String.valueOf(questionId));
        final String cacheKey = WebUtils.generateCacheKeyWithParam(BuildConfig.URL_QUESTION, params);

        Cache cache = AppController.getInstance().getRequestQueue().getCache();
        Cache.Entry entry = cache.get(cacheKey);

        if (entry != null && TimeUtils.getMinuteDifference(entry.serverDate, System.currentTimeMillis()) <= 30) {
            try {
                String content = new String(entry.data, "UTF-8");
                JSONObject questionModel = new JSONObject(content);
                question = new Question();
                question.setId(Long.valueOf(questionModel.getInt("Id")));
                question.field = questionModel.getInt("Field");
                question.content = questionModel.getString("Content");
                question.shortAnswer = questionModel.getString("SAnswer");
                question.longAnswer = questionModel.getString("QAnswer");
                question.code = questionModel.getInt("Code");
//                question.qPrice = questionModel.getInt("QPrice");
//                question.sPrice = questionModel.getInt("SPrice");
//                question.lPrice = questionModel.getInt("LPrice");
//                question.sumPrice = questionModel.getInt("SumPrice");
                question.dateInformation = questionModel.getString("DateInformation");
                question.type = questionModel.getInt("Type");
                question.universityCode = questionModel.getInt("UniversityCode");
                organizeViews();
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
            final JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    WebUtils.generateUrlWithGetParams(BuildConfig.URL_QUESTION, params),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject questionModel) {
                            try {
                                question = new Question();
                                question.setId(Long.valueOf(questionModel.getInt("Id")));
                                question.field = questionModel.getInt("Field");
                                question.content = questionModel.getString("Content");
                                question.shortAnswer = questionModel.getString("SAnswer");
                                question.longAnswer = questionModel.getString("QAnswer");
                                question.code = questionModel.getInt("Code");
//                                question.qPrice = questionModel.getInt("QPrice");
//                                question.sPrice = questionModel.getInt("SPrice");
//                                question.lPrice = questionModel.getInt("LPrice");
//                                question.sumPrice = questionModel.getInt("SumPrice");
                                question.dateInformation = questionModel.getString("DateInformation");
                                question.type = questionModel.getInt("Type");
                                question.universityCode = questionModel.getInt("UniversityCode");
                                organizeViews();
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

    private void showProgressBar() {
        addToCart.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        noConnectionLayout.setVisibility(View.GONE);
    }

    private void showContent() {
        progressBar.setVisibility(View.GONE);
        addToCart.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.VISIBLE);
        noConnectionLayout.setVisibility(View.GONE);
    }

    private void showNoConnectionLayout() {
        addToCart.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.no_connection_button: loadQuestion(); break;
            case R.id.fab: {
//                if (question.sumPrice > 0) {
//                    if (Question.findById(Question.class, question.id) == null) {
//                        if (question.qPrice==0 && question.sPrice==0) {
//                            question.setSaveMode(SaveMode.CART);
//                            question.setPurchaseMode(QuestionPurchaseMode.LONG_ANSWER);
//                            question.save();
//                            Toast.makeText(QuestionActivity.this, "Added To Cart!", Toast.LENGTH_LONG).show();
//                        } else {
//                            String[] items;
//                            int checkedItem;
//                            if (question.qPrice==0) {
//                                checkedItem = 1;
//                                items = new String[]{"پاسخ کوتاه", "پاسخ بلند"};
//                            } else {
//                                checkedItem = 2;
//                                items = new String[]
//                                        {"فقط سوال", "سوال‌ و پاسخ‌کوتاه", "سوال‌ و پاسخ‌ کامل"};
//                            }
//                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                            builder.setTitle("خرید سوال")
//                                    .setSingleChoiceItems(items, checkedItem,null)
//                                    .setPositiveButton("تایید", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.dismiss();
//                                            addQuestionToCard(((AlertDialog)dialog)
//                                                            .getListView().getCheckedItemPosition(),
//                                                    ((AlertDialog)dialog).getListView().getChildCount());
//                                        }
//                                    })
//                                    .show();
//                        }
//
//                    } else {
//                        // Todo: Question Was Saved Already
//                    }
//                } else {
//                    // Todo: Add To Archives
//                }
                break;
            }
            default: break;
        }
    }

    private void addQuestionToCard(int checkedItem, int count) {
        // Todo
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: onBackPressed(); return true;
            case R.id.question_action_shopping_cart: return true; // Todo
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
