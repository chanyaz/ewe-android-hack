package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.AboutActivity;
import com.expedia.bookings.activity.VSCLocaleChangeReceiver;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.fragment.AboutSectionFragment;
import com.mobiata.android.util.AndroidUtils;

public class FeatureConfiguration implements IProductFlavorFeatureConfiguration {
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/VSCServerURLs.json";
	}

	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/VSCPointOfSaleConfig.json";
	}

	public String getAppNameForMobiataPushNameHeader() {
		return "VSCBookings";
	}

	public String getAppSupportUrl(Context context) {
		return context.getString(R.string.app_support_url_vsc);
	}

	public int getCrossSellStringResourceIdForShareEmail() {
		return R.string.share_template_long_ad_vsc;
	}

	public Boolean isAppCrossSellInActivityShareContentEnabled() {
		return false;
	}

	public Boolean isAppCrossSellInCarShareContentEnabled() {
		return false;
	}

	public String getHostnameForShortUrl() {
		return "v.vygs.co";
	}

	public Boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	public String getActionForLocaleChangeEvent() {
		return VSCLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	public Boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
	}

	public int getSearchProgressImageResId() {
		return 0;
	}

	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_vsc;
	}

	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	public Boolean shouldShowBrandLogoOnAccountButton() {
		return false;
	}

	public int getLoginContainerBackgroundResId(Context context) {
		return R.drawable.btn_login_hotels;
	}

	public Boolean doesLoginTextViewHaveCompoundDrawables() {
		return true;
	}

	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.VSC;
	}

	public Boolean isAdXEnabled() {
		return false;
	}

	public int getAdXPosIdentifier() {
		throw new UnsupportedOperationException("AdX not enabled for VSC.");
	}

	public String getOmnitureReportSuiteIds(Context context) {
		if (AndroidUtils.isRelease(context)) {
			return "expediaglobalapp" + ",expedia7androidapp";
		}
		else {
			return "expediaglobalappdev" + ",expedia7androidappdev";
		}
	}

	public String getOmnitureTrackingServer() {
		return "om.expedia.com";
	}

	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, "http://voyages-sncf.mobi/aide-appli-2/aide-appli-hotel/pagecontactandroid.html", false, false);
	}

	public int getResIdForErrorBookingSucceededWithErrors() {
		return R.string.e3_error_checkout_booking_succeeded_with_errors_vsc;
	}

	public int getResIdForErrorHotelServiceFatalFailure() {
		return R.string.e3_error_hotel_offers_hotel_service_failure_vsc;
	}

	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<BasicNameValuePair>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("origin", "VSC"));
		return additionalParamsForReviewsRequest;
	}

	public Boolean shouldUseDotlessDomain(ExpediaServices.EndPoint endpoint) {
		return endpoint != ExpediaServices.EndPoint.PRODUCTION;
	}

	public String touchupE3EndpointUrlIfRequired(String e3EndpointUrl) {
		return e3EndpointUrl;
	}

	public View.OnClickListener getInsuranceLinkViewClickListener(final Context context, final String insuranceTermsUrl) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(context);
				builder.setUrl(insuranceTermsUrl);
				builder.setTheme(R.style.ItineraryTheme);
				builder.setTitle(R.string.insurance);
				builder.setAllowMobileRedirects(false);
				context.startActivity(builder.getIntent());
			}
		};
	}

	public Boolean isLeanPlumEnabled() {
		return false;
	}

	public Boolean isWeAreHiringInAboutEnabled() {
		return false;
	}

	public Boolean isClearPrivateDataInAboutEnabled() {
		return true;
	}

	public String getCopyrightLogoUrl(Context context) {
		return context.getString(Ui.obtainThemeResID(context, R.attr.skin_aboutInfoUrlString));
	}

	public Boolean areSocialMediaMenuItemsInAboutEnabled() {
		return false;
	}

	public AboutSectionFragment getAboutSectionFragment(Context context) {
		AboutSectionFragment.Builder builder = new AboutSectionFragment.Builder(context);
		builder.setTitle(R.string.AlsoByVSC);

		AboutSectionFragment.RowDescriptor app = new AboutSectionFragment.RowDescriptor();
		app.title = context.getString(R.string.VSC_Voyages_SNF);
		app.description = context.getString(R.string.VSC_Voyages_SNF_description);
		app.clickId = AboutActivity.ROW_VSC_VOYAGES;
		app.drawableId = R.drawable.ic_vsc_train_app;
		builder.addRow(app);

		return builder.build();
	}

	public Boolean isLocalExpertEnabled() {
		return false;
	}

	public Boolean isFacebookLoginIntegrationEnabled() {
		return false;
	}

	public Boolean isFacebookShareIntegrationEnabled() {
		return false;
	}

	public Boolean isGoogleWalletPromoEnabled() {
		return false;
	}
}
