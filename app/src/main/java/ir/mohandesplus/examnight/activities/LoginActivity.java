package ir.mohandesplus.examnight.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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
import ir.mohandesplus.examnight.app.AppController;
import ir.mohandesplus.examnight.utils.PhoneUtils;
import ir.mohandesplus.examnight.utils.PreferenceUtils;
import ir.mohandesplus.examnight.utils.TimeUtils;
import ir.mohandesplus.examnight.utils.WebUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener,
        LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private View[] clickableViews;
    private View noConnectionLayout;
    private TextView noConnectionText;
    private View mProgressView;
    private View mLoginFormView;
    private EditText mPasswordView;
    private AutoCompleteTextView mEmailView;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setUpToolbar();
        initializeViews();
        organizeViews();

    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.ic_account_circle);
        }
    }

    private void initializeViews() {
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.progress_bar);
        noConnectionLayout = findViewById(R.id.no_connection);
        mPasswordView = (EditText) findViewById(R.id.password);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        noConnectionText = (TextView) findViewById(R.id.no_connection_text);
        clickableViews = new View[]{
                findViewById(R.id.email_sign_in_button),
                findViewById(R.id.email_sign_up_button),
                findViewById(R.id.no_connection_button)
        };
    }

    private void organizeViews() {
        // Set up the login form.
        handleClicks();
        showContent();
        populateAutoComplete();
        populatePasswordChecker();
    }

    private void populatePasswordChecker() {
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    private void handleClicks() {
        for (View view : clickableViews) view.setOnClickListener(this);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    private boolean checkDataValidity(String email, String password) {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        }
        return cancel;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        if (!checkDataValidity(email, password)) {

            showProgressBar();

            HashMap<String, String> params = new HashMap<>();
            params.put("Request", "Get");
            params.put("Username", email);
            params.put("Password", password);

            final String cacheKey = WebUtils.generateCacheKeyWithParam(BuildConfig.URL_USER, params);

            Cache cache = AppController.getInstance().getRequestQueue().getCache();
            Cache.Entry entry = cache.get(cacheKey);

            if (entry != null && TimeUtils.
                    getMinuteDifference(entry.serverDate, System.currentTimeMillis()) <= 30) {
                try {
                    String content = new String(entry.data, "UTF-8");
                    JSONObject model = new JSONObject(content);
                    switch (model.getInt("Result")) {
                        case 0:
                            // Wrong username/password!
                            Toast.makeText(this, R.string.wrong_data, Toast.LENGTH_LONG).show();
                            break;
                        case 1:
                            // Successfully logged in!
                            PreferenceUtils.setHasLoggedIn(this, true);
                            PreferenceUtils.setEmail(this, email);
                            PreferenceUtils.setPassword(this, password);
                            Toast.makeText(this, R.string.signed_in_successfully,
                                    Toast.LENGTH_LONG).show();
                            finish();
                            break;
                        default:
                            // WTF?
                            Toast.makeText(this, "خطای سامانه! لطفا مجددا تلاش کنید",
                                    Toast.LENGTH_LONG).show();
                            break;
                    }
                    showContent();
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
                        WebUtils.generateUrlWithGetParams(BuildConfig.URL_USER, params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject model) {
                                try {
                                    switch (model.getInt("Result")) {
                                        case 0:
                                            // Wrong username/password!
                                            Toast.makeText(LoginActivity.this, R.string.wrong_data, Toast.LENGTH_LONG).show();
                                            break;
                                        case 1:
                                            // Successfully logged in!
                                            PreferenceUtils.setHasLoggedIn(LoginActivity.this, true);
                                            PreferenceUtils.setEmail(LoginActivity.this, email);
                                            PreferenceUtils.setPassword(LoginActivity.this, password);
                                            Toast.makeText(LoginActivity.this, R.string.signed_in_successfully,
                                                    Toast.LENGTH_LONG).show();
                                            finish();
                                            break;
                                        default:
                                            // WTF?
                                            Toast.makeText(LoginActivity.this, "خطای سامانه! لطفا مجددا تلاش کنید",
                                                    Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                    showContent();
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

    }

    private void attemptSignUp() {

        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        if (!checkDataValidity(email, password)) {

            showProgressBar();

            HashMap<String, String> params = new HashMap<>();
            params.put("Request", "Insert");
            params.put("IMEI", PhoneUtils.getDeviceIMEI(this));
            params.put("Username", email);
            params.put("Password", password);

            final String cacheKey = WebUtils.generateCacheKeyWithParam(BuildConfig.URL_USER, params);

            Cache cache = AppController.getInstance().getRequestQueue().getCache();
            Cache.Entry entry = cache.get(cacheKey);

            if (entry != null && TimeUtils.
                    getMinuteDifference(entry.serverDate, System.currentTimeMillis()) <= 30) {
                try {
                    String content = new String(entry.data, "UTF-8");
                    JSONObject model = new JSONObject(content);
                    switch (model.getInt("Result")) {
                        case 0:
                            // Email is already taken!
                            mEmailView.setError(getString(R.string.email_already_taken));
                            mEmailView.requestFocus();
                            break;
                        case 1:
                            // Successfully registered!
                            PreferenceUtils.setHasLoggedIn(this, true);
                            PreferenceUtils.setEmail(this, email);
                            PreferenceUtils.setPassword(this, password);
                            Toast.makeText(this, R.string.registered_successfully,
                                    Toast.LENGTH_LONG).show();
                            break;
                        default:
                            // WTF?
                            Toast.makeText(this, "خطای سامانه! لطفا مجددا تلاش کنید",
                                    Toast.LENGTH_LONG).show();
                            break;
                    }
                    showContent();
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
                        WebUtils.generateUrlWithGetParams(BuildConfig.URL_USER, params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject model) {
                                try {
                                    switch (model.getInt("Result")) {
                                        case 0:
                                            // Email is already taken!
                                            mEmailView.setError(getString(R.string.email_already_taken));
                                            mEmailView.requestFocus();
                                            break;
                                        case 1:
                                            // Successfully registered!
                                            PreferenceUtils.setHasLoggedIn(LoginActivity.this, true);
                                            PreferenceUtils.setEmail(LoginActivity.this, email);
                                            PreferenceUtils.setPassword(LoginActivity.this, password);
                                            Toast.makeText(LoginActivity.this, R.string.registered_successfully,
                                                    Toast.LENGTH_LONG).show();
                                            break;
                                        default:
                                            // WTF?
                                            Toast.makeText(LoginActivity.this, "خطای سامانه! لطفا مجددا تلاش کنید",
                                                    Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                    showContent();
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

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void showProgressBar() {
        mLoginFormView.setVisibility(View.GONE);
        mProgressView.setVisibility(View.VISIBLE);
        noConnectionLayout.setVisibility(View.GONE);
    }

    private void showContent() {
        mProgressView.setVisibility(View.GONE);
        mLoginFormView.setVisibility(View.VISIBLE);
        noConnectionLayout.setVisibility(View.GONE);
    }

    private void showNoConnectionLayout() {
        mProgressView.setVisibility(View.GONE);
        mLoginFormView.setVisibility(View.GONE);
        noConnectionLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };
        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.email_sign_in_button: attemptLogin(); break;
            case R.id.email_sign_up_button: attemptSignUp(); break;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}

