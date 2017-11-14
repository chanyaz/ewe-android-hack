package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.bitmaps.IMedia;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.phrase.Phrase;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RecyclerGallery extends RecyclerView {
	/**
	 * Image will fill the width of the view
	 */
	public static final int MODE_FILL = 0;
	/**
	 * Image will center in the middle of the screen with padding
	 */
	public static final int MODE_CENTER = 1;

	private GalleryItemListener mListener;
	private RecyclerAdapter mAdapter;
	private SpaceDecoration mDecoration;
	private A11yLinearLayoutManager mLayoutManager;
	private GalleryItemScrollListener mScrollListener;
	public boolean showPhotoCount = true;
	public boolean canScroll = true;

	public class A11yLinearLayoutManager extends LinearLayoutManager {

		private boolean canA11yScroll = false;

		public A11yLinearLayoutManager(Context context) {
			super(context);
		}

		public void setCanA11yScroll(boolean canScroll) {
			canA11yScroll = canScroll;
		}

		@Override
		public boolean canScrollHorizontally() {
			if (AccessibilityUtil.isTalkBackEnabled(getContext())) {
				return canA11yScroll;
			}
			else {
				return canScroll && super.canScrollHorizontally();
			}
		}
	}

	private int mMode = MODE_FILL;

	private IImageViewBitmapLoadedListener imageViewBitmapLoadedListener;

	private boolean enableProgressBarOnImageViews = false;

	private ColorFilter mColorFilter = null;

	public void setColorFilter(ColorFilter colorFilter) {
		mColorFilter = colorFilter;
	}

	public void addImageViewCreatedListener(IImageViewBitmapLoadedListener imageViewBitmapLoadedListener) {
		this.imageViewBitmapLoadedListener = imageViewBitmapLoadedListener;
	}

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
	protected void onFinishInflate() {
		super.onFinishInflate();
		initViews();
	}

	@Override
	public boolean fling(int velocityX, int velocityY) {
		snapTo(velocityX);
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
			v.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
			offset = mLayoutManager.getRightDecorationWidth(v);
		}
		else {
			position = mLayoutManager.findLastVisibleItemPosition();
			v = mLayoutManager.findViewByPosition(position);
			if (v == null) {
				return;
			}
			v.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
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

		if (ExpediaBookingApp.isDeviceShitty()) {
			mLayoutManager = new A11yLinearLayoutManager(getContext());
		}
		else {
			mLayoutManager = new A11yLinearLayoutManager(getContext()) {
				@Override
				protected int getExtraLayoutSpace(State state) {
					if (state.hasTargetScrollPosition()) {
						return AndroidUtils.getScreenSize(getContext()).x;
					}
					else {
						return 0;
					}
				}
			};
		}
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

	protected boolean useCollapsedGalleryContDesc() {
		return false;
	}

	public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.GalleryViewHolder> {
		private List<? extends IMedia> mMedia;
		private FrameLayout.LayoutParams mLayoutParams;

		private RecyclerAdapter(List<? extends IMedia> media) {
			mMedia = media;
			setWidth();
		}

		private void setWidth() {
			Point screen = Ui.getScreenSize(getContext());
			final float imageWidth;
			if (mMode == MODE_FILL) {
				imageWidth = screen.x;

				mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
			}
			else {
				imageWidth = screen.x * 0.60f;
				mLayoutParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			}
			mLayoutParams.width = (int) imageWidth;
		}

		public class GalleryViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
			@InjectView(R.id.gallery_item_progress_bar)
			public ProgressBar progressBar;
			@InjectView(R.id.photo_count_textview)
			public TextView photoCountTextView;
			@InjectView(R.id.gallery_item_image_view)
			public RecyclerGalleryImageView mImageView;

			public GalleryViewHolder(View root) {
				super(root);
				ButterKnife.inject(this, itemView);
				mImageView.setLayoutParams(mLayoutParams);
				mImageView.setTag(callback);
				mImageView.setOnClickListener(this);
			}

			public void bind() {
				if (!showPhotoCount) {
					photoCountTextView.setVisibility(View.GONE);
				}
				else {
					photoCountTextView.setVisibility(View.VISIBLE);
					photoCountTextView.setText(Phrase.from(getContext(), R.string.gallery_photo_count_TEMPLATE)
						.put("index", String.valueOf(getAdapterPosition() + 1))
						.put("count", String.valueOf(getItemCount()))
						.format().toString());
				}
				updateContDesc();
			}

			protected void updateContDesc() {
				if (useCollapsedGalleryContDesc()) {
					itemView.setContentDescription(getContext().getString(R.string.gallery_cont_desc));
				}
				else {
					IMedia media = mMedia.get(getAdapterPosition());
					String contDesc;
					String imageDescription = media.getDescription();
					if (imageDescription != null) {
						contDesc = Phrase
							.from(getContext(), R.string.gallery_photo_count_plus_description_cont_desc_TEMPLATE)
							.put("index", String.valueOf(getAdapterPosition() + 1))
							.put("count", String.valueOf(getItemCount()))
							.put("api_description", imageDescription)
							.format().toString();
					}
					else {
						contDesc = Phrase.from(getContext(), R.string.gallery_photo_count_cont_desc_TEMPLATE)
							.put("index", String.valueOf(getAdapterPosition() + 1))
							.put("count", String.valueOf(getItemCount()))
							.format().toString();
					}
					itemView.setContentDescription(contDesc);
				}
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
					if (imageViewBitmapLoadedListener != null) {
						imageViewBitmapLoadedListener.onImageViewBitmapLoaded(getAdapterPosition());
					}

					if (mMode == MODE_FILL) {
						mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					}
					mImageView.setBackgroundColor(Color.TRANSPARENT);
					if (enableProgressBarOnImageViews) {
						progressBar.setVisibility(View.GONE);
					}
					mImageView.setImageBitmap(bitmap);
					mImageView.setColorFilter(mColorFilter);
				}

				@Override
				public void onBitmapFailed(Drawable errorDrawable) {
					super.onBitmapFailed(errorDrawable);
					if (enableProgressBarOnImageViews) {
						progressBar.setVisibility(View.GONE);
					}
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
		public GalleryViewHolder onCreateViewHolder(ViewGroup parent,
			int viewType) {
			View root = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.gallery_image, parent, false);
			GalleryViewHolder vh = new GalleryViewHolder(root);

			vh.itemView.setAccessibilityDelegate(new AccessibilityDelegate () {
				@Override
				public void onInitializeAccessibilityNodeInfo(View v, AccessibilityNodeInfo info) {
					super.onInitializeAccessibilityNodeInfo(v, info);

					if (useCollapsedGalleryContDesc()) {
						String description = getContext().getString(R.string.collapsed_gallery_photo_click_cont_desc);
						AccessibilityAction customClick = new AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, description);
						info.addAction(customClick);
					}
				}
			});
			return vh;
		}

		@Override
		public void onBindViewHolder(final GalleryViewHolder holder, int position) {
			IMedia media = mMedia.get(position);
			if (media.getIsPlaceHolder()) {
				media.loadErrorImage(holder.mImageView, holder.callback, media.getFallbackImage());
			}
			else {
				media.loadImage(holder.mImageView, holder.callback,
					mMode == MODE_CENTER ? media.getPlaceHolderId() : 0, false);
			}
			if (enableProgressBarOnImageViews) {
				holder.progressBar.setVisibility(View.VISIBLE);
			}
			holder.bind();
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

	public RecyclerAdapter.GalleryViewHolder getSelectedViewHolder() {
		ViewHolder vh = findViewHolderForAdapterPosition(getSelectedItem());
		if (vh != null) {
			return (RecyclerAdapter.GalleryViewHolder) vh;
		}
		return null;
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

	public interface GalleryItemListener {
		void onGalleryItemClicked(Object item);
	}

	public interface GalleryItemScrollListener {
		void onGalleryItemScrolled(int position);
	}

	public void setOnItemClickListener(GalleryItemListener listener) {
		mListener = listener;
	}

	public void setOnItemChangeListener(GalleryItemScrollListener listener) {
		mScrollListener = listener;
	}

	public interface IImageViewBitmapLoadedListener {
		void onImageViewBitmapLoaded(int index);
	}
}
