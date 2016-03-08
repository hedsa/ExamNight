package ir.mohandesplus.examnight.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;

import java.util.List;

import ir.mohandesplus.examnight.R;
import ir.mohandesplus.examnight.modules.Question;
import ir.mohandesplus.examnight.modules.QuestionChildViewHolder;
import ir.mohandesplus.examnight.modules.QuestionParentViewHolder;

public class ExpandablePackageAdapter extends
        ExpandableRecyclerAdapter<QuestionParentViewHolder, QuestionChildViewHolder> {

    Context context;
    List<Question> data;

    public ExpandablePackageAdapter(Context context, List<Question> questions) {
        super(questions);
        this.data = questions;
        this.context = context;
    }

    @Override
    public QuestionParentViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
        View questionParent = LayoutInflater.from(context)
                .inflate(R.layout.question_parent, viewGroup, false);
        return QuestionParentViewHolder.newInstance(questionParent);
    }

    @Override
    public QuestionChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
        View questionChild = LayoutInflater.from(context)
                .inflate(R.layout.question_child, viewGroup, false);
        return QuestionChildViewHolder.newInstance(questionChild);
    }

    @Override
    public void onBindParentViewHolder(QuestionParentViewHolder questionParentViewHolder,
                                       int position, ParentListItem parentListItem) {
    }

    @Override
    public void onBindChildViewHolder(QuestionChildViewHolder questionChildViewHolder,
                                      int position, Object childItem) {
        Question question = (Question) childItem;
        questionChildViewHolder.bindContent(question);
    }

    public void insertAtEnd(Question question) {
        this.data.add(question);
        super.mParentItemList = data;
        notifyParentItemInserted(data.size()-1);
        notifyChildItemInserted(data.size()-1, 0);
    }

}
