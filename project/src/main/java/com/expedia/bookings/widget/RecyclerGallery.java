package com.expedia.bookings.widget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.squareup.picasso.Picasso;

public class RecyclerGallery extends RecyclerView {
	/**
	 * Image will fill the width of the view
	 */
	public static final int MODE_FILL = 0;
	/**
	 * Image will center in the middle of the screen with padding
	 */
	public static final int MODE_CENTER = 1;

	private static final int DEFAULT_FLIP_INTERVAL = 4000;

	private RecyclerGallery.GalleryItemClickListner mListener;
	private RecyclerAdapter mAdapter;
	private SpaceDecoration mDecoration;
	private LinearLayoutManager mLayoutManager;

	private int mMode = MODE_FILL;

	/**
	 * Auto flipping
	 */
	private boolean mRunning = false;
	private boolean mStarted = false;
	private boolean mVisible = false;
	private boolean mUserPresent = true;
	private boolean mScrolling = false;
	private boolean mRegisteredReceiver = false;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				mUserPresent = false;
				updateRunning();
			}
			else if (Intent.ACTION_USER_PRESENT.equals(action)) {
				mUserPresent = true;
				updateRunning();
			}
		}
	};

	public RecyclerGallery(Context context) {
		super(context);
	}

	public RecyclerGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RecyclerGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// Listen for broadcasts related to user-presence
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		getContext().registerReceiver(mReceiver, filter);
		mRegisteredReceiver = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mVisible = false;

		if (mRegisteredReceiver) {
			getContext().unregisterReceiver(mReceiver);
			mRegisteredReceiver = false;
		}
		updateRunning();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		mVisible = visibility == VISIBLE;
		updateRunning();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initViews();
	}

	@Override
	public boolean fling(int velocityX, int velocityY) {
		snapTo(velocityX);
		setScrolling(true);
		return true;
	}

	private void snapTo(int velocityX) {
		int position, offset;
		View v;

		if (velocityX < 0) {
			position = mLayoutManager.findFirstVisibleItemPosition();
			v = mLayoutManager.findViewByPosition(position);
			if (v == null) {
				return;
			}
			offset = mLayoutManager.getRightDecorationWidth(v);
		}
		else {
			position = mLayoutManager.findLastVisibleItemPosition();
			v = mLayoutManager.findViewByPosition(position);
			if (v == null) {
				return;
			}
			offset = mLayoutManager.getLeftDecorationWidth(v);
		}

		smoothScrollBy(getScrollDistance(v, offset), 0);
	}

	private int getScrollDistance(View v, int offset) {
		return mMode == MODE_FILL ? v.getLeft() : v.getLeft() - offset * 4;
	}

	private void initViews() {
		mDecoration = new SpaceDecoration();
		addItemDecoration(mDecoration);

		mLayoutManager = new LinearLayoutManager(getContext());
		mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		setLayoutManager(mLayoutManager);

		mAdapter = new RecyclerAdapter(getContext(), new ArrayList<Media>());
		setAdapter(mAdapter);
	}

	public void setMode(int mode) {
		mMode = mode;
		removeItemDecoration(mDecoration);
		initViews();
	}

	private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
		private List<Media> mMedia;
		private Context mContext;
		private LinearLayout.LayoutParams mLayoutParams;
		private static final int MAX_IMAGES_LOADED = 5;

		private RecyclerAdapter(Context context, List<Media> media) {
			mContext = context;
			mMedia = media;
			setWidth();
		}

		private void setWidth() {
			Point screen = Ui.getScreenSize(mContext);
			final float imageWidth;
			if (mMode == MODE_FILL) {
				imageWidth = screen.x;
				mLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			}
			else {
				imageWidth = screen.x * 0.60f;
				mLayoutParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
				mLayoutParams.width = (int) imageWidth;
			}
			mLayoutParams.width = (int) imageWidth;
		}

		public class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
			public ImageView mImageView;

			public ViewHolder(ImageView v) {
				super(v);
				mImageView = v;
				mImageView.setTag(callback);
				v.setOnClickListener(this);
			}

			@Override
			public void onClick(View v) {
				Property selected = Db.getHotelSearch().getSelectedProperty();

				if (mListener != null && selected != null) {
					mListener.onGalleryItemClicked(mMedia.get(getPosition()));
				}
			}

			public PicassoTarget callback = new PicassoTarget() {
				@Override
				public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
					super.onBitmapLoaded(bitmap, from);
					mImageView.setBackgroundColor(Color.TRANSPARENT);
					mImageView.setImageBitmap(bitmap);
					if (mMode == MODE_FILL) {
						if (bitmap.getWidth() > bitmap.getHeight()) {
							mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
						}
						else {
							mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
						}
					}
				}

				@Override
				public void onBitmapFailed(Drawable errorDrawable) {
					super.onBitmapFailed(errorDrawable);
				}

				@Override
				public void onPrepareLoad(Drawable placeHolderDrawable) {
					super.onPrepareLoad(placeHolderDrawable);
					if (mMode == MODE_FILL) {
						mImageView.setImageBitmap(null);
					}
					else {
						mImageView.setBackgroundColor(getResources().getColor(R.color.placeholder_background_color));
						mImageView.setImageDrawable(placeHolderDrawable);
					}

				}
			};
		}

		@Override
		public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
			int viewType) {
			View root = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.gallery_image, parent, false);
			ImageView imageView = Ui.findView(root, R.id.gallery_item_image_view);
			imageView.setLayoutParams(mLayoutParams);
			ViewHolder vh = new ViewHolder(imageView);
			return vh;
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, int position) {
			Media media = mMedia.get(position);
			media.loadHighResImage(holder.mImageView, holder.callback,
				mMode == MODE_CENTER ? R.drawable.bg_tablet_hotel_results_placeholder : 0);
			preFetchImages(position);
		}

		@Override
		public int getItemCount() {
			return mMedia.size();
		}

		private void replaceWith(List<Media> media) {
			mMedia = media;
			notifyDataSetChanged();
		}

		private void preFetchImages(int position) {
			int left = position;
			int right = position;
			int loaded = 1;
			int len = mMedia.size();
			boolean hasMore = true;

			while (mContext != null && loaded < MAX_IMAGES_LOADED && hasMore) {
				hasMore = false;
				if (left > 0) {
					left--;
					mMedia.get(left).preloadHighResImage(mContext);
					loaded++;
					hasMore = true;
				}
				if (loaded == MAX_IMAGES_LOADED) {
					break;
				}
				if (right < len - 1) {
					right++;
					mMedia.get(right).preloadHighResImage(mContext);
					loaded++;
					hasMore = true;
				}
			}
		}
	}

	public int getSelectedItem() {
		int position = mLayoutManager.findFirstCompletelyVisibleItemPosition();
		if (position == -1) {
			position = mLayoutManager.findFirstVisibleItemPosition();
		}

		return position;
	}

	public void setDataSource(List<Media> media) {
		mAdapter.replaceWith(media);
	}

	private class SpaceDecoration extends RecyclerView.ItemDecoration {
		private int mPadding;

		public SpaceDecoration() {
			mPadding = getPadding();
		}

		private int getPadding() {
			if (mMode == MODE_FILL) {
				return 20;
			}
			else {
				Point screen = Ui.getScreenSize(getContext());
				final float imageWidth = screen.x * 0.60f;
				return (int) ((screen.x - imageWidth) / 8);
			}
		}

		@Override
		public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
			outRect.left = mPadding;
			outRect.right = mPadding;
			outRect.top = mMode == MODE_CENTER ? mPadding : 0;
			outRect.bottom = mMode == MODE_CENTER ? mPadding : 0;

			if (parent.getChildPosition(view) == 0) {
				outRect.left = mMode == MODE_CENTER ? mPadding * 4 : 0;
			}
			else if (parent.getChildPosition(view) == parent.getAdapter().getItemCount() - 1) {
				outRect.right = mMode == MODE_CENTER ? mPadding * 4 : 0;
			}
		}
	}

	/**
	 * Auto scrolling
	 */

	private void setScrolling(boolean isScrolling) {
		if (isScrolling != mScrolling) {
			mScrolling = isScrolling;
			updateRunning();
		}
	}

	public boolean isFlipping() {
		return mRunning;
	}

	public void startFlipping() {
		mStarted = true;
		updateRunning();
	}

	public void stopFlipping() {
		mStarted = false;
		updateRunning();
	}

	private void updateRunning() {
		boolean running = mVisible && mStarted && mUserPresent && !mScrolling;
		if (running != mRunning) {
			if (running) {
				Message msg = mHandler.obtainMessage(FLIP_MSG);
				mHandler.sendMessageDelayed(msg, DEFAULT_FLIP_INTERVAL);
			}
			else {
				mHandler.removeMessages(FLIP_MSG);
			}
			mRunning = running;
		}

		Log.d("updateRunning() mVisible=" + mVisible + ", mStarted=" + mStarted + ", mUserPresent=" + mUserPresent
			+ ", mRunning=" + mRunning + ", mScrolling=" + mScrolling);
	}

	private void showNext() {
		int position = mLayoutManager.findFirstVisibleItemPosition();
		View v = mLayoutManager.findViewByPosition(position);
		int offset = mLayoutManager.getRightDecorationWidth(v) * 2;
		smoothScrollBy(v.getMeasuredWidth() + offset, 0);
	}

	private static final int FLIP_MSG = 1;

	private static final class LeakSafeHandler extends Handler {
		private WeakReference<RecyclerGallery> mTarget;

		public LeakSafeHandler(RecyclerGallery target) {
			mTarget = new WeakReference<RecyclerGallery>(target);
		}

		@Override
		public void handleMessage(Message msg) {
			RecyclerGallery target = mTarget.get();
			if (target == null) {
				return;
			}

			if (msg.what == FLIP_MSG) {
				if (target.mRunning) {
					target.showNext();
					msg = obtainMessage(FLIP_MSG);
					sendMessageDelayed(msg, target.DEFAULT_FLIP_INTERVAL);
				}
			}
		}
	}

	private final Handler mHandler = new LeakSafeHandler(this);

	public interface GalleryItemClickListner {
		public void onGalleryItemClicked(Object item);
	}

	public void setOnItemClickListener(GalleryItemClickListner listener) {
		mListener = listener;
	}
}
