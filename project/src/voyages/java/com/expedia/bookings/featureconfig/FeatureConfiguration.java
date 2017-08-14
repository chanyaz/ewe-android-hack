package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AboutActivity;
import com.expedia.bookings.activity.VSCLocaleChangeReceiver;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;
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
		return null;
	}

	public String getAppSupportUrl(Context context) {
		return PointOfSale.getPointOfSale().getAppSupportUrl();
	}

	public int getCrossSellStringResourceIdForShareEmail() {
		return R.string.share_template_long_ad_vsc;
	}

	public boolean isAppCrossSellInActivityShareContentEnabled() {
		return false;
	}

	public boolean isAppCrossSellInCarShareContentEnabled() {
		return false;
	}

	public String getHostnameForShortUrl() {
		return "v.vygs.co";
	}

	public boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	public String getActionForLocaleChangeEvent() {
		return VSCLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	public boolean wantsCustomHandlingForLocaleConfiguration() {
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

	public boolean shouldShowBrandLogoOnAccountButton() {
		return true;
	}

	public int getLoginContainerBackgroundResId(Context context) {
		return R.drawable.btn_login_hotels;
	}

	public boolean doesLoginTextViewHaveCompoundDrawables() {
		return true;
	}

	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.VSC;
	}

	public boolean isAdXEnabled() {
		return false;
	}

	public int getAdXPosIdentifier() {
		throw new UnsupportedOperationException("AdX not enabled for VSC.");
	}

	public String getOmnitureReportSuiteIds() {
		if (BuildConfig.RELEASE) {
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

	public boolean shouldUseDotlessDomain(EndPoint endpoint) {
		return endpoint != EndPoint.PRODUCTION;
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

	public boolean isLeanPlumEnabled() {
		return false;
	}

	public boolean isKahunaEnabled() {
		return false;
	}

	public boolean isWeAreHiringInAboutEnabled() {
		return false;
	}

	public boolean isClearPrivateDataInAboutEnabled() {
		return true;
	}

	public String getCopyrightLogoUrl(Context context) {
		return context.getString(Ui.obtainThemeResID(context, R.attr.skin_aboutInfoUrlString));
	}

	public boolean areSocialMediaMenuItemsInAboutEnabled() {
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

	public boolean isLocalExpertEnabled() {
		return false;
	}

	public boolean isFacebookLoginIntegrationEnabled() {
		return false;
	}

	public boolean isFacebookShareIntegrationEnabled() {
		return false;
	}

	public boolean isGoogleWalletPromoEnabled() {
		return false;
	}

	public boolean isTrackingWithFlightTrackEnabled() {
		return false;
	}

	public boolean isHangTagProgressBarEnabled() {
		return true;
	}

	public boolean isSettingsInMenuVisible() {
		return false;
	}

	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		//1608. VSC - Apparently since we are forcing FR locale the dateUtils is not formatting appropriately.
		//Ugly hack to ensure European date format.
		String customDateFormat = "dd/MM/yyyy";
		DateTimeFormatter dtf = DateTimeFormat.forPattern(customDateFormat);
		return dtf.print(dateTime);
	}

	public int getHotelSalePriceTextColorResourceId(Context context) {
		//1747. VSC Change price text to sale color
		return Ui.obtainThemeColor(context, R.attr.skin_hotelPriceSaleColor);
	}

	public void setupOtherAppsCrossSellInConfirmationScreen(final Context context, View view) {

		TextView actionTextView = Ui.findView(view, R.id.call_action_text_view);
		actionTextView.setText(R.string.vsc_customer_support);

		View vscAppDivider = Ui.findView(view, R.id.vsc_app_divider);
		vscAppDivider.setVisibility(View.VISIBLE);

		LinearLayout vscAppCrossSellLayout = Ui.findView(view, R.id.vscAppCrossSellLayout);
		vscAppCrossSellLayout.setVisibility(View.VISIBLE);
		vscAppCrossSellLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SocialUtils.openSite(context, AndroidUtils.getMarketAppLink(context, "com.vsct.vsc.mobile.horaireetresa.android"));
			}
		});

		TextView rowTitleView = Ui.findView(view, R.id.row_title);
		rowTitleView.setText(R.string.VSC_Voyages_SNF);
		TextView descriptionView = Ui.findView(view, R.id.row_description);
		descriptionView.setText(R.string.VSC_Voyages_SNF_description);
		ImageView imageView = Ui.findView(view, R.id.image);
		imageView.setImageResource(R.drawable.ic_vsc_train_app);

		Ui.setOnClickListener(view, R.id.call_action_text_view, getCallActionTextViewClickListener(context));
	}

	private View.OnClickListener getCallActionTextViewClickListener(final Context context) {
		return new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 1617. VSC Contact URL
				WebViewActivity.IntentBuilder webBuilder = new WebViewActivity.IntentBuilder(context);
				webBuilder.setUrl("http://voyages-sncf.mobi/aide-appli-2/aide-appli-hotel/pagecontactandroid.html");
				webBuilder.setTheme(R.style.Theme_Phone);
				webBuilder.setTitle(R.string.vsc_customer_support);
				context.startActivity(webBuilder.getIntent());
			}
		};
	}

	public boolean wantsOtherAppsCrossSellInConfirmationScreen() {
		return true;
	}

	public boolean isETPEnabled() {
		return true;
	}

	public String getClientShortName() {
		return "vsc";
	}

	public boolean isLOBChooserScreenEnabled() {
		return false;
	}

	public String getAdXKey() {
		//Key not available for VSC for now, so passing blank.
		return "";
	}

	public boolean isAppSupportUrlEnabled() {
		return true;
	}

	public boolean isSigninEnabled() {
		return true;
	}

	public boolean isAppCrossSellInHotelShareContentEnabled() {
		return true;
	}

	public boolean isAppCrossSellInFlightShareContentEnabled() {
		return true;
	}

	public boolean isItinDisabled() {
		return true;
	}

	public boolean isAppIntroEnabled() {
		return false;
	}

	public void launchAppIntroScreen(Context context) {
		throw new UnsupportedOperationException("App intro not supported.");
	}

	public boolean shouldSendSiteIdInRequests() {
		return false;
	}

	public String getPhoneCollectionId() {
		return "PhoneDestinations";
	}

	public int getSearchResultDealImageDrawable() {
		//No deal image
		return 0;
	}

	public int getHotelDetailsDealImageDrawable() {
		//No deal image
		return 0;
	}

	public int getCollectionCount() {
		// No need to hard code count
		return 0;
	}

	@Override
	public int getFlightSearchProgressImageResId() {
		return R.drawable.search_progress_static_flight_vsc;
	}

	@Override
	public HotelFilter.Sort getDefaultSort() {
		return HotelFilter.Sort.POPULAR;
	}

	@Override
	public boolean sortByDistanceForCurrentLocation() {
		return true;
	}

	@Override
	public boolean isAbacusTestEnabled() {
		return false;
	}

	@Override
	public AlertDialog getUnsupportedVersionDialog(Context context) {
		return null;
	}
}
