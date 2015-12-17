package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.facebook.Session;
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
	private UiLifecycleHelper mUiHelper;

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
		String detailsUrl = "";

		// #2189: Only use share URL with hotels/flights
		if ((itin.getItinCardData() instanceof ItinCardDataHotel)
				|| (itin.getItinCardData() instanceof ItinCardDataFlight)) {
			detailsUrl = itin.getItinCardData().getSharableDetailsUrl();
		}
		else {
			// Product decided to link the FB share post to the app download desktop page (below) - for anything but flights and hotels.
			detailsUrl = "http://www.expedia.com/app";
		}

		// The shortText now consists of the shortenedURL embedded in it. So let's just get rid of that when sharing on FB
		String shortMessage = itin.getShareTextShort();
		String[] shareMsgSplit = shortMessage.split(" http");
		String shareDescription = shareMsgSplit[0];

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

		mUiHelper = new UiLifecycleHelper(this, mFacebookStatusCallback);
		mUiHelper.onCreate(savedInstanceState);

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

		mUiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
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
		mUiHelper.onResume();
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().addCallback(mFacebookStatusCallback);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mUiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		mUiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUiHelper.onDestroy();
	}

	public void share() {
		Session currentSession = Session.getActiveSession();
		if (currentSession == null || currentSession.getState().isClosed()) {
			Session session = new Session.Builder(this).build();
			Session.setActiveSession(session);
			currentSession = session;
		}
		if (!currentSession.isOpened()) {
			Log.d("FB: doFacebookLogin - !currentSession.isOpened()");
			Session.OpenRequest openRequest = null;

			openRequest = new Session.OpenRequest(this);

			//We need an email address to do any sort of Expedia account creation/linking
			List<String> permissions = new ArrayList<String>();
			permissions.add("email");

			if (openRequest != null) {
				openRequest.setPermissions(permissions);
				currentSession.addCallback(mFacebookStatusCallback);
				currentSession.openForRead(openRequest);
			}
		}
		else {
			Log.d("FB: doFacebookLogin - currentSession.isOpened()");
			postToFacebook();
		}

	}

	Session.StatusCallback mFacebookStatusCallback = new Session.StatusCallback() {

		// callback when session changes state
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			handleFacebookResponse(session, state, exception);
		}
	};

	public void handleFacebookResponse(Session session, SessionState state, Exception exception) {
		if (session.isOpened()) {
			postToFacebook();
		}
		else if (session.isClosed()) {
			finish();
		}
		else {
			Log.d("FB: handleFacebookResponse - else " + state.name());
		}
	}

	private void postToFacebook() {
		FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
				.setDescription(mShareDescription)
				.setName(mShareName)
				.setLink(mShareURL)
				.setPicture(mShareThumbnailURL)
				.build();
		mUiHelper.trackPendingDialogCall(shareDialog.present());
	}
}
