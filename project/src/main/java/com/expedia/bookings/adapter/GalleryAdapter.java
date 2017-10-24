package com.expedia.bookings.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.lob.lx.ui.activity.LXBaseActivity;
import com.synnapps.carouselview.CarouselView;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by siaggarwal on 10/25/17.
 */

public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static String[] activityIds = {"182983", "183589", "269114", "278043", "266375"};
    static int[] sampleImages = {R.drawable.i1, R.drawable.i2, R.drawable.i3, R.drawable.i4, R.drawable.i5};
    static String[] sampleTitles = {"New York City Explorer Pass", "Empire State Building", "Statue of Liberty & Ellis Island Tour with Pedestal Access", "Hop-On Hop-Off Bus Tour", "National September 11 Memorial & Museum"};
    static String[] prices = {"$84", "$34", "$57", "$54", "$26"};

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // do the binding here

        ((ViewHolder) holder).bind(position);

    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @InjectView(R.id.act_title)
        TextView title;

        @InjectView(R.id.act_price)
        TextView price;

        @InjectView(R.id.carouselView)
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void bind(int position)
        {
            image.setImageDrawable(getApplicationContext().getResources().getDrawable(sampleImages[position]));
            title.setText(sampleTitles[position]);
            price.setText(prices[position]);
        }

        @Override
        public void onClick(View view) {
            int position = this.getPosition();
            Intent intent = new Intent(getApplicationContext(), LXBaseActivity.class);
            intent.putExtra("TAG_FROM_DEEPLINK_TO_DETAILS", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("location", "New York");
            intent.putExtra("activityId", activityIds[position]);
            getApplicationContext().startActivity(intent);
        }
    }
}
