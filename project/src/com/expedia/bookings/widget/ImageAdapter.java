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
import com.expedia.bookings.data.Media;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;

public class ImageAdapter extends BaseAdapter {

	private Context mContext;

	private List<Media> mMedia;

	public ImageAdapter(Context context) {
		mContext = context;
	}

	public void setMedia(List<Media> media) {
		mMedia = media;
		notifyDataSetChanged();
	}

	public int getPositionOfMedia(Media media) {
		for (int i = 0; i < mMedia.size(); i++) {
			if (mMedia.get(i).equals(media)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int getCount() {
		return mMedia.size();
	}

	@Override
	public Object getItem(int position) {
		return mMedia.get(position);
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

		List<String> urls = mMedia.get(position).getBestUrls(imageView.getWidth());
		int placeholderResId = Ui.obtainThemeResID((Activity) mContext, R.attr.HotelRowThumbPlaceHolderDrawable);
		UrlBitmapDrawable.loadImageView(urls, imageView, placeholderResId);

		return convertView;
	}
}
