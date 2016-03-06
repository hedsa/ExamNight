package ir.mohandesplus.examnight.modules;

import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;

import ir.mohandesplus.examnight.R;
import ir.mohandesplus.examnight.views.CustomMathView;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class QuestionChildViewHolder extends ChildViewHolder {

    View parent;
    FrameLayout content;
    MaterialProgressBar progressBar;
    TextView questionBuy, shortBuy, longBuy;
    CustomMathView question, shortAnswer, longAnswer;

    public QuestionChildViewHolder(View parent, FrameLayout content,
                                   MaterialProgressBar progressBar,
                                   TextView[] textViews, CustomMathView[] mathViews) {

        super(parent);
        this.parent = parent;

        this.question = mathViews[0];
        this.shortAnswer = mathViews[1];
        this.longAnswer = mathViews[2];

        this.questionBuy = textViews[0];
        this.shortBuy = textViews[1];
        this.longBuy = textViews[2];

        this.content = content;
        this.progressBar = progressBar;

    }

    private void delayLoading(int milliseconds) {
        content.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                content.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }, milliseconds);
    }

    public static QuestionChildViewHolder newInstance(View parent) {
        FrameLayout content = (FrameLayout) parent.findViewById(R.id.child_content);
        MaterialProgressBar progressBar = (MaterialProgressBar) parent.findViewById(R.id.progress_bar);
        TextView questionBuy = (TextView) parent.findViewById(R.id.question_child_buy);
        TextView shortBuy = (TextView) parent.findViewById(R.id.short_buy);
        TextView longBuy = (TextView) parent.findViewById(R.id.complete_buy);
        CustomMathView question = (CustomMathView) parent.findViewById(R.id.question_child_problem);
        CustomMathView shortAnswer = (CustomMathView)
                parent.findViewById(R.id.question_child_short_answer);
        CustomMathView longAnswer = (CustomMathView)
                parent.findViewById(R.id.question_child_complete_answer);
        return new QuestionChildViewHolder(parent, content, progressBar,
                new TextView[]{questionBuy, shortBuy, longBuy},
                new CustomMathView[]{question, shortAnswer, longAnswer});
    }

    public void bindContent(Question question) {
        delayLoading(2500);
        organizeLaTeX(question);
    }

    private void organizeLaTeX(Question question) {
        questionBuy.setVisibility(View.GONE);
        this.question.setVisibility(View.VISIBLE);
        this.question.setHtmlText(question.content);
        shortBuy.setVisibility(View.GONE);
        shortAnswer.setVisibility(View.VISIBLE);
        shortAnswer.setHtmlText(question.shortAnswer);
        longBuy.setVisibility(View.GONE);
        longAnswer.setVisibility(View.VISIBLE);
        longAnswer.setHtmlText(question.longAnswer);
    }

}
