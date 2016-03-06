package ir.mohandesplus.examnight.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ir.mohandesplus.examnight.fragments.SearchFragment;

public class SearchPagerAdapter extends FragmentPagerAdapter {

    String searchQuery;
    String[] titles = new String[]{"سوال‌ها", "بسته‌ها"};

    public SearchPagerAdapter(FragmentManager fragmentManager, String searchQuery) {
        super(fragmentManager);
        this.searchQuery = searchQuery;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return SearchFragment.newInstance(SearchFragment.CONTENT_QUESTION, searchQuery);
            case 1: return SearchFragment.newInstance(SearchFragment.CONTENT_PACKAGE, searchQuery);
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return titles.length;
    }
}
