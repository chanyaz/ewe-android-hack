package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.mobiata.android.Log;

public class FacebookShareActivity extends Activity {

	private static final String TAG = "FacebookShareActivity";

	public static final String KEY_SHARE_NAME = "KEY_SHARE_NAME";
	public static final String KEY_SHARE_DESCRIPTION = "KEY_SHARE_DESCRIPTION";
	public static final String KEY_SHARE_URL = "KEY_SHARE_URL";
	public static final String KEY_SHARE_THUMBNAIL_URL = "KEY_SHARE_THUMBNAIL_URL";

	//////////////////////////////////////////////////////////////////////////////////////////
	// Static Methods

	public static Intent createIntent(Context context, ItinContentGenerator<?> itin) {
		// This is the bold header title (name) shown in the Facebook post.
		String shareName = itin.getFacebookShareName();
		String shareThumbnail = itin.getSharableImageURL();
		String detailsUrl;

		// #2189: Only use share URL with hotels/flights
		if ((itin.getItinCardData() instanceof ItinCardDataHotel)
			|| (itin.getItinCardData() instanceof ItinCardDataFlight)) {
			detailsUrl = itin.getItinCardData().getSharableDetailsUrl();
		}
		else {
			// Product decided to link the FB share post to the app download desktop page (below) - for anything but flights and hotels.
			detailsUrl = PointOfSale.getPointOfSale().getAppInfoUrl();
		}

		// The shortText now consists of the shortenedURL embedded in it. So let's just get rid of that when sharing on FB
		String shortMessage = itin.getShareTextShort();
		String[] shareMsgSplit = shortMessage.split(" http");
		String shareDescription = shareMsgSplit[0];

		Intent intent = new Intent(context, FacebookShareActivity.class);
		intent.putExtra(KEY_SHARE_NAME, shareName);
		intent.putExtra(KEY_SHARE_DESCRIPTION, shareDescription);
		intent.putExtra(KEY_SHARE_URL, detailsUrl);
		intent.putExtra(KEY_SHARE_THUMBNAIL_URL, shareThumbnail);
		return intent;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Lifecycle Methods

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String mShareName = getIntent().getStringExtra(KEY_SHARE_NAME);
		String mShareDescription = getIntent().getStringExtra(KEY_SHARE_DESCRIPTION);
		String mShareURL = getIntent().getStringExtra(KEY_SHARE_URL);
		String mShareThumbnailURL = getIntent().getStringExtra(KEY_SHARE_THUMBNAIL_URL);

		Log.i(TAG, "ShareName = " + mShareName);
		Log.i(TAG, "ShareDescription = " + mShareDescription);
		Log.i(TAG, "ShareURL = " + mShareURL);
		Log.i(TAG, "ShareThumbnailURL = " + mShareThumbnailURL);

		ShareDialog dialog = new ShareDialog(this);

		ShareLinkContent.Builder linkContentBuilder = new ShareLinkContent.Builder()
			.setContentTitle(mShareName)
			.setContentDescription(mShareDescription);

		if (!TextUtils.isEmpty(mShareThumbnailURL)) {
			linkContentBuilder.setImageUrl(Uri.parse(mShareThumbnailURL));
		}
		linkContentBuilder.setContentUrl(Uri.parse(
			ProductFlavorFeatureConfiguration.getInstance().shouldDisplayItinTrackAppLink() && !TextUtils
				.isEmpty(mShareURL) ? mShareURL : PointOfSale.getPointOfSale().getAppInfoUrl()));

		dialog.show(linkContentBuilder.build());
		finish();
	}
}
