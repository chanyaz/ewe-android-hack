package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;

public class FeatureConfiguration extends BaseFeatureConfiguration {
	@Override
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/TVLYServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/TravelocityPointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "TvlyBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url_tvly);
	}

	@Override
	public String getHostnameForShortUrl() {
		return "t.tvly.co";
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0x072b61;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.TRAVELOCITY;
	}

	@Override
	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("caller", "TRAVELOCITY"));
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("locale", PointOfSale.getPointOfSale().getLocaleIdentifier()));
		return additionalParamsForReviewsRequest;
	}

	@Override
	public View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent viewInsuranceIntent = new Intent(Intent.ACTION_VIEW);
				viewInsuranceIntent.setData(Uri.parse(insuranceTermsUrl));
				context.startActivity(viewInsuranceIntent);
			}
		};
	}

	@Override
	public String getClientShortName() {
		return "tvly";
	}

	@Override
	public PointOfSaleId getUSPointOfSaleId() {
		return PointOfSaleId.TRAVELOCITY;
	}

	@Override
	public boolean showUserRewardsEnrollmentCheck() {
		return false;
	}

	@Override
	public boolean showHotelLoyaltyEarnMessage() {
		return false;
	}
}
