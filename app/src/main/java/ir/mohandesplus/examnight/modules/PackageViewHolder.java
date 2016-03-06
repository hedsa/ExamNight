package ir.mohandesplus.examnight.modules;

import android.text.Spannable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;

import ir.mohandesplus.examnight.R;

public class PackageViewHolder extends UltimateRecyclerviewViewHolder {

    View parent;
    ImageView imageView;
    TextView titleText, priceText;

    public PackageViewHolder(View parent, ImageView imageView, TextView titleText, TextView priceText) {
        super(parent);
        this.parent = parent;
        this.imageView = imageView;
        this.titleText = titleText;
        this.priceText = priceText;
    }

    public static PackageViewHolder newInstance(View parent) {
        TextView titleText = (TextView) parent.findViewById(R.id.question_title);
        ImageView imageView = (ImageView) parent.findViewById(R.id.question_image);
        TextView priceTextView = (TextView) parent.findViewById(R.id.question_price);
        return new PackageViewHolder(parent, imageView, titleText, priceTextView);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setTitleText(String title) {
        this.titleText.setText(title);
    }

    public void setTitleText(Spannable title) {
        this.titleText.setText(title);
    }

    public void setPriceText(String price) {
        this.priceText.setText(price);
    }

    public void setPriceText(Spannable price) {
        this.priceText.setText(price);
    }

    public View getParent() {
        return parent;
    }

}