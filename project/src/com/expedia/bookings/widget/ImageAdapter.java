package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.hotellib.data.Media;

public class ImageAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<Media> mMedia;

	private static final LinearLayout.LayoutParams LAYOUT_WIDE = new LinearLayout.LayoutParams(
			LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	private static final LinearLayout.LayoutParams LAYOUT_TALL = new LinearLayout.LayoutParams(
			LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);

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
		Holder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.gallery_image, parent, false);

			holder = new Holder();
			holder.item = (ImageView) convertView.findViewById(R.id.gallery_item_image_view);

			convertView.setTag(holder);
		}
		else {
			holder = (Holder) convertView.getTag();
		}

		ImageView imageView = holder.item;
		Media media = (Media) getItem(position);
		String url = media.getUrl();

		// Don't depend on the callback to set the ImageView - just refresh the adapter with new data
		OnImageLoaded callback = new OnImageLoaded() {
			public void onImageLoaded(String url, Bitmap bitmap) {
				notifyDataSetChanged();
			}
		};

		ImageCache imageCache = ImageCache.getInstance();
		if (imageCache.containsImage(url)) {
			Bitmap bitmap = imageCache.getImage(url);
			if (bitmap.getWidth() > bitmap.getHeight()) {
				imageView.setLayoutParams(LAYOUT_WIDE);
				imageView.setBackgroundDrawable(null);
			}
			else {
				imageView.setLayoutParams(LAYOUT_TALL);
				imageView.setBackgroundResource(R.drawable.bg_gallery_item);
			}

			imageView.setImageBitmap(bitmap);
		}
		else {
			// Set a placeholder image while we load the image
			imageView.setImageResource(R.drawable.ic_image_placeholder);
			imageView.setLayoutParams(LAYOUT_WIDE);
			imageView.setBackgroundDrawable(null);
			imageCache.loadImage(media.getUrl(), callback);
		}

		return convertView;
	}

	private static class Holder {
		public ImageView item;
	}
}
