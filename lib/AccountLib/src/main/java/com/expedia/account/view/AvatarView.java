package com.expedia.account.view;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.account.R;

public class AvatarView extends View {

	private Paint mCircleFillPaint;
	private Paint mCircleStrokePaint;
	private Paint mInitialsPaint;
	private Paint mShaderPaint;
	private BitmapShader mShader;
	private Matrix mShaderMatrix;

	private String mLookupKey;
	private Bitmap mPhoto;
	private String mInitials;

	private Drawable mEmptyDrawable;

	public AvatarView(Context context) {
		super(context);
		init(context, null);
	}

	public AvatarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	@TargetApi(21)
	public AvatarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		float density = getResources().getDisplayMetrics().density;
		float strokeWidth = 1f * density;
		int strokeColor = Color.WHITE;
		float textSize = 18f * density;
		int textColor = Color.WHITE;
		int fillColor = Color.argb(0x33, 0xff, 0xff, 0xff);

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.acct__AvatarView);
			strokeWidth = a.getDimension(R.styleable.acct__AvatarView_acct__ava_strokeWidth, strokeWidth);
			strokeColor = a.getColor(R.styleable.acct__AvatarView_acct__ava_strokeColor, strokeColor);
			textSize = a.getDimension(R.styleable.acct__AvatarView_android_textSize, textSize);
			textColor = a.getColor(R.styleable.acct__AvatarView_android_textColor, textColor);
			fillColor = a.getColor(R.styleable.acct__AvatarView_acct__ava_fillColor, fillColor);
			mEmptyDrawable = a.getDrawable(R.styleable.acct__AvatarView_acct__ava_emptyDrawable);
			a.recycle();
		}

		mCircleFillPaint = new Paint();
		mCircleFillPaint.setColor(fillColor);
		mCircleFillPaint.setStyle(Paint.Style.FILL);

		mCircleStrokePaint = new Paint();
		mCircleStrokePaint.setColor(strokeColor);
		mCircleStrokePaint.setStyle(Paint.Style.STROKE);
		mCircleStrokePaint.setAntiAlias(true);
		mCircleStrokePaint.setStrokeWidth(strokeWidth);

		mInitialsPaint = new Paint();
		mInitialsPaint.setStyle(Paint.Style.FILL);
		mInitialsPaint.setTextAlign(Paint.Align.CENTER);
		mInitialsPaint.setAntiAlias(true);
		mInitialsPaint.setColor(textColor);
		mInitialsPaint.setTextSize(textSize);

		mShaderMatrix = new Matrix();
		mShaderPaint = new Paint();
		mShaderPaint.setAntiAlias(true);

	}

	Rect mBounds = new Rect();

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mLookupKey == null && mEmptyDrawable != null) {
			canvas.getClipBounds(mBounds);
			mBounds.inset((mBounds.width() - mEmptyDrawable.getIntrinsicWidth()) / 2,
				(mBounds.height() - mEmptyDrawable.getIntrinsicHeight()) / 2);
			mEmptyDrawable.setBounds(mBounds);
			mEmptyDrawable.draw(canvas);
			return;
		}

		canvas.translate(getPaddingLeft(), getPaddingTop());

		float strokeWidth = mCircleStrokePaint.getStrokeWidth();

		// w,h = height and width of the rectangle into which to draw the avatar
		float w = getWidth() - getPaddingLeft() - getPaddingRight();
		float h = getHeight() - getPaddingTop() - getPaddingBottom();

		// radius = radius of the fill circle
		float radius = Math.min(w, h) / 2f - strokeWidth / 2f - 1f;

		if (mPhoto != null) {
			// Fill the circle up with the contact's pretty face
			mShaderMatrix.reset();

			// scale = make the smallest side of the photo fit into the smallest side of the view
			float scale = (Math.min(w, h) - strokeWidth / 2f) / Math.min(mPhoto.getWidth(), mPhoto.getHeight());

			mShaderMatrix.preScale(scale, scale);
			mShader.setLocalMatrix(mShaderMatrix);

			canvas.drawCircle(w / 2f, h / 2f, radius, mShaderPaint);
		}

		else {
			// Fill the circle up with 20% transparent white
			canvas.drawCircle(w / 2f, h / 2f, radius, mCircleFillPaint);

			if (mInitials != null) {
				// Now draw that contact's initials
				float textHeight = mInitialsPaint.descent() - mInitialsPaint.ascent();
				float textOffset = (textHeight / 2) - mInitialsPaint.descent();
				canvas.drawText(mInitials, w / 2f, h / 2f + textOffset, mInitialsPaint);
			}
		}

		// Put a ring on it
		canvas.drawCircle(w / 2f, h / 2f, radius, mCircleStrokePaint);
	}

	public void setContactOrInitials(String lookupKey, String initials) {
		mLookupKey = lookupKey;
		mPhoto = null;
		mShader = null;
		mShaderPaint.setShader(null);
		mInitials = initials;
//		if (lookupKey != null) {
//			lookupPhotoOrInitialsAsync();
//		}
		populatePhotoOrInitials(mInitials);
		invalidate();
	}

	///////////////////////////////////////////////////////////////////////////
	// Asynchronicity
	///////////////////////////////////////////////////////////////////////////
//
//	private WeakReference<BitmapWorkerTask> mTaskRef;
//
//	private void lookupPhotoOrInitialsAsync() {
//		if (mTaskRef != null) {
//			BitmapWorkerTask task = mTaskRef.get();
//			if (task != null) {
//				task.cancel(true);
//				mTaskRef = null;
//			}
//		}
//		BitmapWorkerTask task = new BitmapWorkerTask(this, mLookupKey);
//		task.execute();
//		mTaskRef = new WeakReference<>(task);
//	}
//
//	private static class BitmapWorkerTask extends AsyncTask<Void, Void, Object> {
//		private final WeakReference<AvatarView> viewRef;
//		private final String mLookupKey;
//
//		public BitmapWorkerTask(AvatarView view, String lookupKey) {
//			// Use a WeakReference to ensure the ImageView can be garbage collected
//			viewRef = new WeakReference<>(view);
//			mLookupKey = lookupKey;
//		}
//
//		// Object returned will be the Bitmap photo for the contact, if found,
//		// or the contact's initials. It could also be null if nothing is found.
//		@Override
//		protected Object doInBackground(Void... params) {
//			if (mLookupKey == null) {
//				return null;
//			}
//
//			AvatarView view = viewRef == null ? null : viewRef.get();
//			Context context = view == null ? null : view.getContext();
//			Bitmap bitmap = context == null ? null : getAvatar(context, mLookupKey);
//			if (bitmap != null) {
//				return bitmap;
//			}
//
//			// No picture, so let's at least get the contact's initials
//			view = viewRef == null ? null : viewRef.get();
//			context = view == null ? null : view.getContext();
//			return context == null ? null : ContactsLookupAdapter.lookupInitials(context, mLookupKey);
//		}
//
//		// Once complete, see if ImageView is still around and set bitmap.
//		@Override
//		protected void onPostExecute(Object object) {
//			if (viewRef == null) {
//				return;
//			}
//
//			final AvatarView view = viewRef.get();
//			if (view == null || view.mTaskRef == null || view.mTaskRef.get() != this) {
//				return;
//			}
//
//			view.populatePhotoOrInitials(object);
//		}
//	}

	private void populatePhotoOrInitials(Object object) {
		if (object == null) {
			// Do nothing
		}
		else if (object instanceof Bitmap) {
			mPhoto = (Bitmap) object;
			mShader = new BitmapShader(mPhoto, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
			mShaderPaint.setShader(mShader);
		}
		else if (object instanceof String) {
			if (mInitials == null) {
				mInitials = (String) object;
			}
		}

		invalidate();
	}

	///////////////////////////////////////////////////////////////////////////
	// Avatar Bitmap lookup
	///////////////////////////////////////////////////////////////////////////

	private static Bitmap getAvatar(Context context, String lookupKey) {
		Uri uri = getDataUri(context, lookupKey);
		if (uri == null) {
			return null;
		}
		String[] projection = new String[] { ContactsContract.Data.DATA15 };
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		if (cursor == null) {
			return null;
		}
		try {
			if (cursor.moveToFirst()) {
				byte[] bytes = cursor.getBlob(0);
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
			}
		}
		finally {
			cursor.close();
		}
		return null;
	}

	private static Uri getDataUri(Context context, String lookupKey) {
		Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
		String[] projection = new String[] { ContactsContract.Contacts.PHOTO_ID };
		Cursor cursor = context.getContentResolver().query(lookupUri, projection, null, null, null);
		if (cursor == null) {
			return null;
		}
		try {
			if (cursor.moveToFirst()) {
				long id = cursor.getLong(0);

				/**
				 * http://developer.android.com/reference/android/provider/ContactsContract.ContactsColumns.html#PHOTO_ID
				 * If PHOTO_ID is null, consult PHOTO_URI or PHOTO_THUMBNAIL_URI,
				 * which is a more generic mechanism for referencing the contact photo,
				 * especially for contacts returned by non-local directories (see ContactsContract.Directory).
				 */
				if (id == 0) {
					return getPhotoThumbnailUri(context, lookupKey);
				}
				return ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, id);
			}
		}
		finally {
			cursor.close();
		}
		return null;
	}

	private static Uri getPhotoThumbnailUri(Context context, String lookupKey) {
		Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
		String[] projection = new String[] { ContactsContract.Contacts.PHOTO_THUMBNAIL_URI };
		Cursor cursor = context.getContentResolver().query(lookupUri, projection, null, null, null);
		if (cursor == null) {
			return null;
		}
		try {
			if (cursor.moveToFirst()) {
				String uriString = cursor.getString(0);
				if (uriString != null) {
					return Uri.parse(uriString);
				}
			}
		}
		finally {
			cursor.close();
		}
		return null;
	}


	@NonNull
	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("instanceState", super.onSaveInstanceState());
		bundle.putString("lookupKey", mLookupKey);
		bundle.putString("initials", mInitials);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			mLookupKey = bundle.getString("lookupKey");
			mInitials = bundle.getString("initials");
			this.post(new Runnable() {
				@Override
				public void run() {
					setContactOrInitials(mLookupKey, mInitials);
				}
			});
			state = bundle.getParcelable("instanceState");
		}
		super.onRestoreInstanceState(state);
	}
}
