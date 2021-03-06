package com.expedia.bookings.featureconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
	@Override
	public String getServerEndpointsConfigurationPath() {
		return "ExpediaSharedData/VSCServerURLs.json";
	}

	@Override
	public String getPOSConfigurationPath() {
		return "ExpediaSharedData/VSCPointOfSaleConfig.json";
	}

	@Override
	public String getAppNameForMobiataPushNameHeader() {
		return "VSCBookings";
	}

	@Override
	public String getAppSupportUrl(Context context) {
		return PointOfSale.getPointOfSale().getAppSupportUrl();
	}

	@Override
	public boolean isAppCrossSellInActivityShareContentEnabled() {
		return false;
	}

	@Override
	public boolean isAppCrossSellInCarShareContentEnabled() {
		return false;
	}

	@Override
	public String getHostnameForShortUrl() {
		return "v.vygs.co";
	}

	@Override
	public boolean shouldDisplayInsuranceDetailsIfAvailableOnItinCard() {
		return true;
	}

	@Override
	public String getActionForLocaleChangeEvent() {
		return VSCLocaleChangeReceiver.ACTION_LOCALE_CHANGED;
	}

	@Override
	public boolean wantsCustomHandlingForLocaleConfiguration() {
		return true;
	}

	@Override
	public int getSearchProgressImageResId() {
		return 0;
	}

	@Override
	public int getNotificationIconResourceId() {
		return R.drawable.ic_stat_vsc;
	}

	@Override
	public int getNotificationIndicatorLEDColor() {
		return 0xfbc51e;
	}

	@Override
	public boolean shouldShowBrandLogoOnAccountButton() {
		return true;
	}

	@Override
	public PointOfSaleId getDefaultPOS() {
		return PointOfSaleId.VSC;
	}

	@Override
	public void contactUsViaWeb(Context context) {
		AboutUtils.openWebsite(context, "http://voyages-sncf.mobi/aide-appli-2/aide-appli-hotel/pagecontactandroid.html", false, false);
	}

	@Override
	public List<BasicNameValuePair> getAdditionalParamsForReviewsRequest() {
		List<BasicNameValuePair> additionalParamsForReviewsRequest = new ArrayList<>();
		additionalParamsForReviewsRequest.add(new BasicNameValuePair("origin", "VSC"));
		return additionalParamsForReviewsRequest;
	}

	@Override
	public boolean shouldUseDotlessDomain(EndPoint endpoint) {
		return endpoint != EndPoint.PRODUCTION;
	}

	@Override
	public String touchupE3EndpointUrlIfRequired(String e3EndpointUrl) {
		return e3EndpointUrl;
	}

	@Override
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

	@Override
	public boolean isLeanPlumEnabled() {
		return false;
	}

	@Override
	public boolean isTuneEnabled() {
		return false;
	}

	@Override
	public boolean isWeAreHiringInAboutEnabled() {
		return false;
	}

	@Override
	public boolean isClearPrivateDataInAboutEnabled() {
		return true;
	}

	@Override
	public String getCopyrightLogoUrl(Context context) {
		return context.getString(Ui.obtainThemeResID(context, R.attr.skin_aboutInfoUrlString));
	}

	@Override
	public boolean areSocialMediaMenuItemsInAboutEnabled() {
		return false;
	}

	@Override
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

	@Override
	public boolean isFacebookLoginIntegrationEnabled() {
		return false;
	}

	@Override
	public boolean isFacebookShareIntegrationEnabled() {
		return false;
	}

	@Override
	public boolean isHangTagProgressBarEnabled() {
		return true;
	}

	@Override
	public boolean isSettingsInMenuVisible() {
		return false;
	}

	@Override
	public String formatDateTimeForHotelUserReviews(Context context, DateTime dateTime) {
		//1608. VSC - Apparently since we are forcing FR locale the dateUtils is not formatting appropriately.
		//Ugly hack to ensure European date format.
		String customDateFormat = "dd/MM/yyyy";
		DateTimeFormatter dtf = DateTimeFormat.forPattern(customDateFormat);
		return dtf.print(dateTime);
	}

	@Override
	public int getHotelSalePriceTextColorResourceId(Context context) {
		//1747. VSC Change price text to sale color
		return Ui.obtainThemeColor(context, R.attr.skin_hotelPriceSaleColor);
	}

	@Override
	public void setupOtherAppsCrossSellInConfirmationScreen(final Context context, View view) {
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
	}

	@Override
	public boolean wantsOtherAppsCrossSellInConfirmationScreen() {
		return true;
	}

	@Override
	public boolean isETPEnabled() {
		return true;
	}

	@Override
	public String getClientShortName() {
		return "vsc";
	}

	public String getAdXKey() {
		//Key not available for VSC for now, so passing blank.
		return "";
	}

	@Override
	public boolean isSigninEnabled() {
		return true;
	}

	public boolean isAppSupportUrlEnabled() {
		return true;
	}

	@Override
	public boolean isItinDisabled() {
		return true;
	}

	@Override
	public int getFlightSearchProgressImageResId() {
		return 0;
	}

	@Override
	public boolean isLOBIconCenterAligned() {
		return false;
	}

	@Override
	public int getLaunchScreenActionLogo() {
		return 0;
	}

	@Override
	public int updatePOSSpecificActionBarLogo() {
		//ignore
		return 0;
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
	public String getPOSSpecificBrandName(Context context) {
		return BuildConfig.brand;
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
	public boolean isFacebookTrackingEnabled() {
		return false;
	}

	@Override
	public boolean isAbacusTestEnabled() {
		return false;
	}
}
