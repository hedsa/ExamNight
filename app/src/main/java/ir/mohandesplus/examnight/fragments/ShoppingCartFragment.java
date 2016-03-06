package ir.mohandesplus.examnight.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

import ir.mohandesplus.examnight.R;
import ir.mohandesplus.examnight.adapters.PackageAdapter;
import ir.mohandesplus.examnight.modules.*;
import ir.mohandesplus.examnight.modules.Package;
import ir.mohandesplus.examnight.views.AutofitRecyclerView;
import ir.mohandesplus.examnight.views.SpaceItemDecoration;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ShoppingCartFragment extends Fragment implements View.OnClickListener {

    View mainLayout;
    View[] clickableViews;
    View emptyShoppingCart;
    FloatingActionButton fab;
    MaterialProgressBar progressBar;
    AutofitRecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainLayout = inflater.inflate(R.layout.fragment_shopping_cart, container, false);
        if (mainLayout!=null && isAdded()) showContent();
        return mainLayout;
    }
    
    private void showContent() {
        initializeViews();
        addDecorations();
        organizeViews();
    }

    private void initializeViews() {
        fab = (FloatingActionButton) mainLayout.findViewById(R.id.fab);
        emptyShoppingCart = mainLayout.findViewById(R.id.empty_shopping_cart);
        progressBar = (MaterialProgressBar) mainLayout.findViewById(R.id.progress_bar);
        recyclerView = (AutofitRecyclerView) mainLayout.findViewById(R.id.shopping_cart_recycler);
        clickableViews = new View[]{fab};
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
        for (View view : clickableViews) view.setOnClickListener(this);
        loadContent();
    }

    private void loadContent() {
        List<Package> packages = Select.from(Package.class)
                .where(Condition.prop("save_mode").eq(SaveMode.CART))
                .list();
        feedPackagesData(packages);
    }

    private void feedPackagesData(List<Package> packages) {

        Context context = getActivity();
        if (context == null) return;

        if (packages.size() == 0) {
            showNothingFoundLayout();
            return;
        }

        GridLayoutManager manager = new GridLayoutManager(context, 2);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(manager);
        PackageAdapter adapter = new PackageAdapter(context, packages);
        recyclerView.setAdapter(adapter);
        showContentLayout();

    }

    private void hideFab() {
        fab.hide();
    }

    private void showFab() {
        fab.show();
    }

    private void showProgressBar() {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        emptyShoppingCart.setVisibility(View.GONE);
        hideFab();
    }

    private void showContentLayout() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyShoppingCart.setVisibility(View.GONE);
        showFab();
    }

    private void showNothingFoundLayout() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        emptyShoppingCart.setVisibility(View.VISIBLE);
        hideFab();
    }

    private void buyCart() {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab: buyCart(); break;
        }
    }

}
