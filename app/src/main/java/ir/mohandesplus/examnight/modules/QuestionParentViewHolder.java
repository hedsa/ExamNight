package ir.mohandesplus.examnight.modules;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;

import ir.mohandesplus.examnight.R;

public class QuestionParentViewHolder extends ParentViewHolder {

    private static final float INITIAL_POSITION = 0.0f;
    private static final float ROTATED_POSITION = 180f;

    View parent;
    TextView questionTitle;
    ImageButton expandButton;

    public QuestionParentViewHolder(View parent, TextView questionTitle, ImageButton expandButton) {
        super(parent);
        this.parent = parent;
        this.expandButton = expandButton;
        this.questionTitle = questionTitle;
    }

    public static QuestionParentViewHolder newInstance(View parent) {
        TextView questionTitle = (TextView) parent.findViewById(R.id.question_parent_text);
        ImageButton expandButton = (ImageButton) parent.findViewById(R.id.question_parent_dropdown);
        return new QuestionParentViewHolder(parent, questionTitle, expandButton);
    }

    public void setTitle(String title) {
        questionTitle.setText(title);
    }

    public TextView getQuestionTitle() {
        return this.questionTitle;
    }

    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);
        if (expanded) expandButton.setRotation(ROTATED_POSITION);
        else expandButton.setRotation(INITIAL_POSITION);
    }

}
