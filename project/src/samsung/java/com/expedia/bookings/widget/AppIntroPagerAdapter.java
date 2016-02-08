package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.squareup.phrase.Phrase;

public class AppIntroPagerAdapter extends PagerAdapter {

	LayoutInflater layoutInflater;

	int[] backgrounds = {
		R.drawable.app_intro_collection,
		R.drawable.app_intro_sale
	};

	int[] titles = {
		R.string.app_intro_collections_title,
		R.string.app_intro_hotels_savings_title
	};

	public AppIntroPagerAdapter(Context context) {
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return backgrounds.length;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View itemView = layoutInflater.inflate(R.layout.intro_pager_item, container, false);

		ImageView imageView = (ImageView) itemView.findViewById(R.id.item_image);
		imageView.setImageResource(backgrounds[position]);

		TextView title = (TextView) itemView.findViewById(R.id.pager_title);
		title.setText(Phrase.from(container.getContext(), titles[position])
			.putOptional("brand", container.getContext().getString(R.string.app_intro_display_name)).format());

		container.addView(itemView);

		return itemView;
	}

}
