package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.mobiata.android.Log;

public class FacebookShareActivity extends Activity {

	private static final String LOGGING_TAG = "FacebookShareActivity";

	public static final String KEY_SHARE_NAME = "KEY_SHARE_NAME";
	public static final String KEY_SHARE_DESCRIPTION = "KEY_SHARE_DESCRIPTION";
	public static final String KEY_SHARE_URL = "KEY_SHARE_URL";
	public static final String KEY_SHARE_THUMBNAIL_URL = "KEY_SHARE_THUMBNAIL_URL";

	// https://developers.facebook.com/docs/android/share-dialog/
	private UiLifecycleHelper uiHelper;

	private String mShareName;
	private String mShareDescription;
	private String mShareURL;
	private String mShareThumbnailURL;

	//////////////////////////////////////////////////////////////////////////////////////////
	// Static Methods

	public static final Intent createIntent(Context context, ItinContentGenerator<?> itin) {
		// This is the bold header title (name) shown in the Facebook post.
		String shareName = itin.getFacebookShareName();
		String shareThumbnail = itin.getSharableImageURL();
		String detailsUrl = itin.getItinCardData().getSharableDetailsUrl();

		// We need to split the already created shortMessage contents to fit with the Facebook share model.
		// The first two lines from the shortMessage should serve as description.
		String shortMessage = itin.getShareTextShort();
		String[] shareMsgSplit = shortMessage.split("\n");
		String shareDescription = shareMsgSplit[0] + "\n" + shareMsgSplit[1];

		return createIntent(context, shareName, shareDescription, detailsUrl, shareThumbnail);
	}

	private static final Intent createIntent(Context context, String shareName, String shareDescription,
			String shareURL, String shareThumbnailURL) {
		Intent intent = new Intent(context, FacebookShareActivity.class);
		intent.putExtra(KEY_SHARE_NAME, shareName);
		intent.putExtra(KEY_SHARE_DESCRIPTION, shareDescription);
		intent.putExtra(KEY_SHARE_URL, shareURL);
		intent.putExtra(KEY_SHARE_THUMBNAIL_URL, shareThumbnailURL);
		return intent;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Lifecycle Methods

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		uiHelper = new UiLifecycleHelper(this, new StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
			}
		});
		uiHelper.onCreate(savedInstanceState);

		mShareName = getIntent().getStringExtra(KEY_SHARE_NAME);
		mShareDescription = getIntent().getStringExtra(KEY_SHARE_DESCRIPTION);
		mShareURL = getIntent().getStringExtra(KEY_SHARE_URL);
		mShareThumbnailURL = getIntent().getStringExtra(KEY_SHARE_THUMBNAIL_URL);

		Log.i(LOGGING_TAG, "ShareName = " + mShareName);
		Log.i(LOGGING_TAG, "ShareDescription = " + mShareDescription);
		Log.i(LOGGING_TAG, "ShareURL = " + mShareURL);
		Log.i(LOGGING_TAG, "ShareThumbnailURL = " + mShareThumbnailURL);
		share();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
			@Override
			public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
				Log.e(LOGGING_TAG, String.format("Facebook share error: %s", error.toString()));
			}

			@Override
			public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
				Log.i(LOGGING_TAG, "Facebook sharing successful");
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	public void share() {
		FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
				.setDescription(mShareDescription)
				.setName(mShareName)
				.setLink(mShareURL)
				.setPicture(mShareThumbnailURL)
				.build();
		uiHelper.trackPendingDialogCall(shareDialog.present());
	}
}