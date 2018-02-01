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
		return "ExpediaSharedData/LastMinuteServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/LastMinutePointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "LMBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url_lastminute);
	}

	@Override
	public String getHostnameForShortUrl() {
		return "l.ast.mn";
	}

	@Override
	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_lm;
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0x072b61;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.LASTMINUTE;
	}

	@Override
	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<BasicNameValuePair>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("caller", "LastMinute"));
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
		return "lastminute";
	}

	@Override
	public int getLaunchScreenActionLogo() {
		if (PointOfSale.getPointOfSale().getPointOfSaleId() == PointOfSaleId.LASTMINUTE_NZ) {
			return R.drawable.ic_ab_lm_nz_logo;
		}
		return R.drawable.ic_ab_lm_au_logo;
	}

	@Override
	public String getPOSSpecificBrandName(Context context) {
		if (PointOfSale.getPointOfSale().getPointOfSaleId() == PointOfSaleId.LASTMINUTE_NZ) {
			return context.getString(R.string.lastminute_nz);
		}
		else {
			return context.getString(R.string.lastminute_au);
		}
	}

	@Override
	public boolean showUserRewardsEnrollmentCheck() {
		return false;
	}

	@Override
	public boolean showHotelLoyaltyEarnMessage() {
		return false;
	}

	@Override
	public int getPOSSpecificBrandLogo() {
		if (PointOfSale.getPointOfSale().getPointOfSaleId() == PointOfSaleId.LASTMINUTE) {
			return R.drawable.app_copyright_logo_au;
		}
		return R.drawable.app_copyright_logo;
	}
}

