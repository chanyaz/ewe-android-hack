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
import android.graphics.ColorFilter;
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
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
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

	private GalleryItemListener mListener;
	private RecyclerAdapter mAdapter;
	private SpaceDecoration mDecoration;
	private LinearLayoutManager mLayoutManager;
	private GalleryItemScrollListener mScrollListener;

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
	private IImageViewBitmapLoadedListener imageViewBitmapLoadedListener;

	private ColorFilter mColorFilter = null;
	public void setColorFilter(ColorFilter colorFilter) {
		mColorFilter = colorFilter;
	}

	public void addImageViewCreatedListener(IImageViewBitmapLoadedListener imageViewBitmapLoadedListener) {
		this.imageViewBitmapLoadedListener = imageViewBitmapLoadedListener;
	}

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
		if (mScrollListener != null) {
			mScrollListener.onGalleryItemScrolled(position);
		}
		smoothScrollBy(getScrollDistance(v, offset), 0);

	}

	private int getScrollDistance(View v, int offset) {
		return mMode == MODE_FILL ? v.getLeft() : v.getLeft() - offset * 4;
	}

	private void initViews() {
		mDecoration = new SpaceDecoration();
		addItemDecoration(mDecoration);

		mLayoutManager = new LinearLayoutManager(getContext()) {
			@Override
			protected int getExtraLayoutSpace(State state) {
				return AndroidUtils.getScreenSize(getContext()).x;
			}
		};
		mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		setLayoutManager(mLayoutManager);

		mAdapter = new RecyclerAdapter(new ArrayList<IMedia>());
		setAdapter(mAdapter);
	}

	public void setMode(int mode) {
		mMode = mode;
		removeItemDecoration(mDecoration);
		initViews();
	}

	private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
		private List<? extends IMedia> mMedia;
		private LinearLayout.LayoutParams mLayoutParams;
		private static final int MAX_IMAGES_LOADED = 5;

		private RecyclerAdapter(List<? extends IMedia> media) {
			mMedia = media;
			setWidth();
		}

		private void setWidth() {
			Point screen = Ui.getScreenSize(getContext());
			final float imageWidth;
			if (mMode == MODE_FILL) {
				imageWidth = screen.x;

				mLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
					(int) (getContext().getResources().getDimension(R.dimen.car_details_image_size)));
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

				if (mListener != null) {
					mListener.onGalleryItemClicked(mMedia.get(getAdapterPosition()));
				}
			}

			public PicassoTarget callback = new PicassoTarget() {
				@Override
				public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
					super.onBitmapLoaded(bitmap, from);
					if (mMode == MODE_FILL) {
						mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					}
					mImageView.setBackgroundColor(Color.TRANSPARENT);
					mImageView.setImageBitmap(bitmap);
					mImageView.setColorFilter(mColorFilter);

					if (imageViewBitmapLoadedListener != null) {
						imageViewBitmapLoadedListener.onImageViewBitmapLoaded((HotelDetailsGalleryImageView) mImageView);
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
			IMedia media = mMedia.get(position);
			if (media.isPlaceHolder()) {
				media.loadErrorImage(holder.mImageView, holder.callback, R.drawable.room_fallback);
			}
			else {
				media.loadImage(holder.mImageView, holder.callback,
					mMode == MODE_CENTER ? Ui.obtainThemeResID(getContext(),
						R.attr.skin_HotelRowThumbPlaceHolderDrawable) : 0);
			}
		}

		@Override
		public int getItemCount() {
			return mMedia.size();
		}

		private void replaceWith(List<? extends IMedia> media) {
			mMedia = media;
			notifyDataSetChanged();
		}

	}

	public int getSelectedItem() {
		int position = NO_POSITION;

		if (mLayoutManager != null && getChildCount() > 0) {
			position = mLayoutManager.findFirstCompletelyVisibleItemPosition();
			if (position == NO_POSITION) {
				position = mLayoutManager.findFirstVisibleItemPosition();
			}
		}

		return position;
	}

	public void setDataSource(List<? extends IMedia> media) {
		mAdapter.replaceWith(media);
	}

	private class SpaceDecoration extends RecyclerView.ItemDecoration {
		private int mPadding;

		public SpaceDecoration() {
			mPadding = getPadding();
		}

		private int getPadding() {
			if (mMode == MODE_FILL) {
				return 0;
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

			if (parent.getChildAdapterPosition(view) == 0) {
				outRect.left = mMode == MODE_CENTER ? mPadding * 4 : 0;
			}
			else if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
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
		mScrolling = false;
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

	public void showNext() {
		int position = mLayoutManager.findFirstVisibleItemPosition() + 1;
		if (position >= 0 && position < mAdapter.getItemCount()) {
			if (mScrollListener != null) {
				mScrollListener.onGalleryItemScrolled(position);
			}
			smoothScrollToPosition(position);
		}
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

	public interface GalleryItemListener {
		public void onGalleryItemClicked(Object item);
	}

	public interface GalleryItemScrollListener {
		public void onGalleryItemScrolled(int position);
	}

	public void setOnItemClickListener(GalleryItemListener listener) {
		mListener = listener;
	}

	public void setOnItemChangeListener(GalleryItemScrollListener listener) {
		mScrollListener = listener;
	}

	public interface IImageViewBitmapLoadedListener {
		void onImageViewBitmapLoaded(HotelDetailsGalleryImageView hotelDetailsGalleryImageView);
	}
}
