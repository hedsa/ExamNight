package ir.mohandesplus.examnight.views;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int sideSpace, space;

    public SpaceItemDecoration(int sideSpace, int space) {
        this.sideSpace = sideSpace;
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = sideSpace;
        outRect.right = sideSpace;
        outRect.bottom = space;
        // Add top margin only for the first item to avoid double space between items
        int firstPosition = parent.getChildLayoutPosition(view);
        if(firstPosition==0 || firstPosition==1 || firstPosition==2) outRect.top = space;
    }

}
