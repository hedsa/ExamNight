package ir.mohandesplus.examnight.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.squareup.picasso.Picasso;

import java.util.List;

import ir.mohandesplus.examnight.BuildConfig;
import ir.mohandesplus.examnight.R;
import ir.mohandesplus.examnight.activities.QuestionActivity;
import ir.mohandesplus.examnight.modules.PackageViewHolder;
import ir.mohandesplus.examnight.modules.Question;

public class QuestionAdapter extends UltimateViewAdapter<PackageViewHolder> {

    Context context;
    List<Question> data;

    final static String QUESTION_IMAGES_URL = BuildConfig.URL_IMAGES + "questions/";
    int lastPosition = -1;

    public QuestionAdapter(Context context, List<Question> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public long getHeaderId(int position) {
        return super.getHeaderId(position);
    }

    @Override
    public long generateHeaderId(int i) {
        /*if (data != null) return data.get(i).category.charAt(0);
        else return -1;*/
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        TextView textView = new TextView(context);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(15);
        textView.setPadding(5, 5, 5, 5);
        return new RecyclerView.ViewHolder(textView) {};
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
//        ((TextView)viewHolder.itemView).setText(data.get(position).category);
    }

    @Override
    public PackageViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.question_grid, parent, false);
        return PackageViewHolder.newInstance(view);
    }

    @Override
    public void onBindViewHolder(final PackageViewHolder holder, int position) {

        if ((position<getItemCount() &&
                (customHeaderView != null ? position<=data.size() : position<data.size()) &&
                (customHeaderView==null ||  position>0))) {

            holder.itemView.setVisibility(View.INVISIBLE);
            final Question question = data.get(position);
            holder.getParent().setTag(position);

            Picasso.with(context)
                    .load(QUESTION_IMAGES_URL + question.getId() + "/1.jpg")
                    .error(R.drawable.ic_action_no_signal)
                    .fit()
                    .into(holder.getImageView());

            holder.setTitleText(String.valueOf(question.getId()));

            holder.getParent().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    Intent intent = new Intent(context, QuestionActivity.class);
                    intent.putExtra(QuestionActivity.QUESTION_ID, String.valueOf(data.get(position).getId()));
                    context.startActivity(intent);
                }
            });

            if (position > lastPosition) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        holder.itemView.setVisibility(View.VISIBLE);
                        ObjectAnimator alpha = ObjectAnimator.ofFloat(holder.getParent(), "alpha", 0f, 1f);
                        AnimatorSet animSet = new AnimatorSet();
                        animSet.play(alpha);
                        animSet.setDuration(400);
                        animSet.start();

                    }
                }, 100);
                lastPosition = position;
            } else holder.itemView.setVisibility(View.VISIBLE);

        }

    }

    public void insertDataAtEnd(List<Question> newData) {
        for (Question question : newData) {
            insert(data, question, getAdapterItemCount());
        }
    }

    @Override
    public PackageViewHolder getViewHolder(View view) {
        return PackageViewHolder.newInstance(view);
    }

    @Override
    public int getAdapterItemCount() {
        return (data == null) ? 0 : data.size();
    }

}
