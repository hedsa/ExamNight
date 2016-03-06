package ir.mohandesplus.examnight.modules;

import android.text.TextUtils;

public class Square {

    public String title, imageUrl, packages;

    public Square() {
        super();
    }

    public String[] getPackageIds() {
        if (TextUtils.isEmpty(packages)) return new String[0];
        else return packages.split(",");
    }

}
