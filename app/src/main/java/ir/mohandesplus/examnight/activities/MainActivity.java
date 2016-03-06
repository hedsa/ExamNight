package ir.mohandesplus.examnight.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dynamixsoftware.ErrorAgent;

import java.util.Locale;

import ir.mohandesplus.examnight.R;
import ir.mohandesplus.examnight.app.AppController;
import ir.mohandesplus.examnight.fragments.HomeFragment;
import ir.mohandesplus.examnight.fragments.ShoppingCartFragment;
import ir.mohandesplus.examnight.utils.LanguageUtils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public final static String LAUNCH_MODE = "LaunchMode";
    public final static int HOME=0, SHOPPING_CART=1, CATEGORIES=2, ARCHIVE=3;

    Toolbar toolbar;
    ActionBar actionBar;

    Locale defaultLocale;
    Fragment homeFragment, shoppingCartFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        defaultLocale = LanguageUtils.setPersianLocale(this);
        // Setting device's language to persian (rtl direction)
        // Registering the error handler (Sending errors to our server)
        ErrorAgent.register(this, AppController.WATCH_ID);

        // Setting Up the decoration of this activity, i.e. the toolbar and ...
        setUpDecoration();

        // Launching home fragment if the app is started
        int launchMode = getIntent().getIntExtra(LAUNCH_MODE, -1);
        if (launchMode >= 0) {
            switch (launchMode) {
                case HOME: launchHome(); break;
                case SHOPPING_CART: launchShoppingCart(); break;
                // Todo
            }
        } else if (savedInstanceState == null) launchHome();

    }

    private void launchHome() {
        // Setting the title of the action bar
        actionBar.setTitle(R.string.app_name);
        // Creating a new instance of home fragment if it wasn't created
        if (homeFragment == null) homeFragment = new HomeFragment();
        // Replacing the frame_container with home fragment (showing home fragment)
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, homeFragment);
        fragmentTransaction.commit();
    }

    private void launchShoppingCart() {
        // Setting the title of the action bar
        actionBar.setTitle(R.string.shopping_cart);
        // Creating a new instance of home fragment if it wasn't created
        if (shoppingCartFragment == null) shoppingCartFragment = new ShoppingCartFragment();
        // Replacing the frame_container with shopping cart fragment (showing shopping cart fragment)
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, shoppingCartFragment);
        fragmentTransaction.commit();
    }

    private void setUpDecoration() {
        // Setting up the toolbar
        setUpToolbar();
//        setUpFap();
        // Setting up the actionbar drawer arrow toggle
        setUpDrawer();
        // Setting up the navigation view
        setUpNavigationView();
    }

    private void setUpToolbar() {
        // Setting up the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
    }

    // Todo: Do we need FAB?
    /*private void setUpFap() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.hide();
    }*/

    private void setUpDrawer() {
        // Syncing the action bar drawer arrow toggle with the drawer layout
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    private void setUpNavigationView() {
        // Setting up the navigation view (sliding menu)
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        // Defining what to happen when the navigation view items are clicked
        navigationView.setNavigationItemSelectedListener(this);
        // Setting current navigation item selected to home
        navigationView.setCheckedItem(R.id.main_drawer_home);
    }

    @Override
    public void onBackPressed() {
        // If the navigation view is open, close it. Otherwise let Android decide what to happen
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflating the action bar menu
        getMenuInflater().inflate(R.menu.main, menu);
        // Inflating the search view in the actionbar
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
        // Defining what to happen when the search view is selected
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // When a query is searched, launch SearchActivity and pass the query to it
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra(SearchActivity.SEARCH_QUERY, query);
                startActivity(intent);
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
        // Is called when an item on the action bar is selected
        switch (item.getItemId()) {
            // Todo
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Is called when an item on the navigation view is selected
        switch (item.getItemId()) {
            // When home item is clicked we launch home fragment
            case R.id.main_drawer_home: launchHome(); break;
            case R.id.main_drawer_categories: break;
            case R.id.main_drawer_my_questions: break; // Todo
            case R.id.main_drawer_shopping_cart: launchShoppingCart(); break;
            case R.id.main_drawer_about_us: break;
            case R.id.main_drawer_send_message: break;
        }
        // Now we close the navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        // On Resuming the activity, we check if the device's language is persian
        if (!LanguageUtils.deviceHasPersianLocale()) {
            defaultLocale = LanguageUtils.setPersianLocale(this);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // When exiting the activity we set the device's language back to the original one
        super.onDestroy();
        LanguageUtils.setDefaultLocale(this, defaultLocale);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        // Attaching the base context to CalligraphyContextWrapper to let the library change the fonts
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
