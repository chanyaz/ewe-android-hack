package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.mobiata.android.ImageCache;
import com.mobiata.hotellib.data.Media;

public class ImageAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<Media> mMedia;

	public ImageAdapter(Context context, List<Media> media) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMedia = media;
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
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.gallery_image, parent, false);
		}

		ImageView imageView = (ImageView) convertView;
		Media media = (Media) getItem(position);
		String url = media.getUrl();

		ImageCache imageCache = ImageCache.getInstance();
		if (imageCache.containsImage(url)) {
			imageView.setImageBitmap(imageCache.getImage(url));
		}
		else {
			// Set a placeholder image while we load the image
			imageView.setImageResource(R.drawable.ic_image_placeholder);
			imageCache.loadImage(media.getUrl(), imageView);
		}

		return convertView;
	}
}
