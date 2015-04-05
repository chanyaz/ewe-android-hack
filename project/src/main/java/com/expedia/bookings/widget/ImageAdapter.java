package com.expedia.bookings.widget;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.utils.Ui;

public class ImageAdapter extends BaseAdapter {

	private Context mContext;

	private List<HotelMedia> mHotelMedia;

	public ImageAdapter(Context context) {
		mContext = context;
	}

	public void setMedia(List<HotelMedia> hotelMedia) {
		mHotelMedia = hotelMedia;
		notifyDataSetChanged();
	}

	public int getPositionOfMedia(HotelMedia hotelMedia) {
		for (int i = 0; i < mHotelMedia.size(); i++) {
			if (mHotelMedia.get(i).equals(hotelMedia)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int getCount() {
		return mHotelMedia.size();
	}

	@Override
	public Object getItem(int position) {
		return mHotelMedia.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView = (ImageView) convertView;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.gallery_item, null);
			imageView = (ImageView) convertView.findViewById(R.id.image);
		}

		int placeholderResId = Ui.obtainThemeResID((Activity) mContext, R.attr.skin_HotelRowThumbPlaceHolderDrawable);
		mHotelMedia.get(position).fillImageView(imageView, placeholderResId);

		return convertView;
	}
}
