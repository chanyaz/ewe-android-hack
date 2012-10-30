package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Destination;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.LaunchHotelData;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.ObjectAnimator;

public class LaunchHotelAdapter extends LaunchBaseAdapter<Property> {

	private static final String THUMBNAIL_SIZE = Media.IMAGE_BIG_SUFFIX;

	private Context mContext;
	private LayoutInflater mInflater;

	private Distance.DistanceUnit mDistanceUnit;

	private View[] mViewCache;

	public LaunchHotelAdapter(Context context) {
		super(context, R.layout.row_launch_tile_hotel);
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Add enough blank items so that we can show blank tiles before loading
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int height = wm.getDefaultDisplay().getHeight();
		int tileHeight = context.getResources().getDimensionPixelSize(R.dimen.launch_tile_height_hotel);
		int numTiles = (height / tileHeight) + (height % tileHeight);
		for (int a = 0; a < numTiles; a++) {
			add(null);
		}
		mViewCache = new View[numTiles];
	}

	public void setProperties(LaunchHotelData launchHotelData) {
		this.clear();

		if (launchHotelData != null && launchHotelData.getProperties() != null) {
			mDistanceUnit = launchHotelData.getDistanceUnit();

			for (Property property : launchHotelData.getProperties()) {
				add(property);
			}

			mViewCache = new View[launchHotelData.getProperties().size()];
		}

		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {
		Property property = getItem(position);
		if (property == null) {
			return 0;
		}

		return Integer.valueOf(property.getPropertyId());
	}

	@Override
	public int getItemViewType(int position) {
		return AdapterView.ITEM_VIEW_TYPE_IGNORE;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int cacheIndex = position % mViewCache.length;
		View view = mViewCache[cacheIndex];

		// Use the Tag as a flag to indicate this view has been populated
		if (view != null && view.getTag() != null) {
			return view;
		}

		// Inflate the view if possible
		if (view == null) {
			view = mInflater.inflate(R.layout.row_launch_tile_hotel, parent, false);
			mViewCache[cacheIndex] = view;
		}

		Property property = getItem(position);

		// If we're just measuring the height/width of the row, just return the view without doing anything to it.
		if (isMeasuring() || property == null) {
			return view;
		}

		View container = Ui.findView(view, R.id.launch_tile_container);
		TextView titleTextView = Ui.findView(view, R.id.launch_tile_title_text_view);
		FontCache.setTypeface(titleTextView, FontCache.Font.ROBOTO_LIGHT);

		TextView distanceTextView = Ui.findView(view, R.id.launch_tile_distance_text_view);
		FontCache.setTypeface(distanceTextView, FontCache.Font.ROBOTO_LIGHT);

		TextView priceTextView = Ui.findView(view, R.id.launch_tile_price_text_view);
		FontCache.setTypeface(priceTextView, FontCache.Font.ROBOTO_BOLD);

		// Bottom banner/label
		titleTextView.setText(property.getName());
		distanceTextView.setText(property.getDistanceFromUser().formatDistance(mContext, mDistanceUnit,
				true) + " - ");

		Rate lowestRate = property.getLowestRate();
		final String hotelPrice = StrUtils.formatHotelPrice(lowestRate.getDisplayRate());
		priceTextView.setText(hotelPrice);

		TextView sale = Ui.findView(view, R.id.launch_tile_sale_text_view);

		// Sale
		boolean toggleSale = false;
		if (property.isLowestRateTonightOnly()) {
			sale.setText(mContext.getString(R.string.percent_minus_template, lowestRate.getDiscountPercent()));
			sale.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tonight_only, 0, 0, 0);
			sale.setVisibility(View.VISIBLE);
			toggleSale = true;
		}
		else if (property.isLowestRateMobileExclusive()) {
			sale.setText(mContext.getString(R.string.percent_minus_template, lowestRate.getDiscountPercent()));
			sale.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mobile_only, 0, 0, 0);
			sale.setVisibility(View.VISIBLE);
			toggleSale = true;
		}
		else if (property.getLowestRate().isSaleTenPercentOrBetter()) {
			sale.setText(mContext.getString(R.string.percent_minus_template, lowestRate.getDiscountPercent()));
			sale.setVisibility(View.VISIBLE);
			toggleSale = true;
		}
		else {
			sale.setVisibility(View.GONE);
		}

		// Background image
		ViewGroup banner = Ui.findView(view, R.id.launch_tile_banner_container);

		String url = property.getThumbnail().getUrl(THUMBNAIL_SIZE);
		if (ImageCache.containsImage(url)) {
			Log.i("imageContained: " + position + " url: " + url);
			container.setBackgroundDrawable(new BitmapDrawable(ImageCache.getImage(url)));
			toggleTile(sale, banner, true, toggleSale);
		}
		else {
			Log.i("imageNotContained: " + position + " url: " + url);
			loadImageForLaunchStream(url, container, banner, sale, toggleSale);
			toggleTile(sale, banner, false, toggleSale);
		}

		// We're just using the Tag as a flag to indicate this view has been populated
		view.setTag(new Object());

		return view;
	}

	private boolean loadImageForLaunchStream(String url, final View layout, final ViewGroup banner,
			final TextView sale, final boolean toggleSale) {
		String key = layout.toString();
		Log.v("Loading RelativeLayout bg " + key + " with " + url);

		// Begin a load on the ImageView
		ImageCache.OnImageLoaded callback = new ImageCache.OnImageLoaded() {
			public void onImageLoaded(String url, Bitmap bitmap) {
				Log.v("ImageLoaded: " + url);

				layout.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
				banner.setVisibility(View.VISIBLE);
				if (toggleSale) {
					sale.setVisibility(View.VISIBLE);
				}
				else {
					sale.setVisibility(View.GONE);
				}

				ObjectAnimator.ofFloat(layout, "alpha", 0.0f, 1.0f).setDuration(DURATION_FADE_MS).start();
			}

			public void onImageLoadFailed(String url) {
				Log.v("Image load failed: " + url);
			}
		};

		return ImageCache.loadImage(key, url, callback);
	}

	private void toggleTile(TextView sale, ViewGroup banner, boolean loaded, boolean saleOn) {
		int visibility = loaded ? View.VISIBLE : View.GONE;
		banner.setVisibility(visibility);

		int saleVisibility = saleOn && loaded ? View.VISIBLE : View.GONE;
		sale.setVisibility(saleVisibility);
	}
}
