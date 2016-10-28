package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Rate.UserPriceType;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.VipBadgeClickListener;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.squareup.phrase.Phrase;


public class HotelDetailsPricePromoFragment extends Fragment {

	private View mVipIcon;

	private View mRateInfoContainer;
	private View mUnavailableContainer;
	private TextView mSoldOutTextView;

	public static HotelDetailsPricePromoFragment newInstance() {
		return new HotelDetailsPricePromoFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details_price_promo, container, false);

		mRateInfoContainer = Ui.findView(view, R.id.rate_info_container);
		mUnavailableContainer = Ui.findView(view, R.id.unavailable_container);
		mSoldOutTextView = Ui.findView(view, R.id.sold_out_text_view);

		populateViews(view);
		return view;
	}

	public void populateViews() {
		populateViews(getView());
	}

	private void populateViews(View view) {
		populatePricePromoBar(view, Db.getHotelSearch().getSelectedProperty());
		populateVipIcon(view, Db.getHotelSearch().getSelectedProperty());
	}

	private void populatePricePromoBar(View view, Property property) {
		if (property == null) {
			return;
		}

		Rate rate = property.getLowestRate();

		if (rate == null) {
			mRateInfoContainer.setVisibility(View.GONE);
			mUnavailableContainer.setVisibility(View.VISIBLE);

			HotelSearchParams params = Db.getHotelSearch().getSearchParams();
			if (params.isDefaultStay()) {
				mSoldOutTextView.setText(
					Phrase.from(getActivity(), R.string.not_currently_available_from_brand_TEMPLATE)
					.put("brand", BuildConfig.brand)
					.format());
			}
			else {
				String dates = DateFormatUtils.formatDateRange(getActivity(), params, DateFormatUtils.FLAGS_DATE_ABBREV_ALL);
				mSoldOutTextView.setText(
					Phrase.from(getActivity(), R.string.not_currently_available_from_brand_dates_TEMPLATE)
					.put("brand", BuildConfig.brand)
					.put("dates", dates)
					.format());
			}
		}
		else {
			mRateInfoContainer.setVisibility(View.VISIBLE);
			mUnavailableContainer.setVisibility(View.GONE);

			// Sale banner
			TextView saleBannerTextView = Ui.findView(view, R.id.sale_banner_text_view);
			final TextView promoTextView = Ui.findView(view, R.id.promo_text_view);
			final View centerFiller = Ui.findView(view, R.id.center_filler);
			TextView airAttachBannerTextView = Ui.findView(view, R.id.air_attach_banner_text_view);

			if (rate.isSaleTenPercentOrBetter()) {
				if (rate.isAirAttached()) {
					airAttachBannerTextView.setVisibility(View.VISIBLE);
					saleBannerTextView.setVisibility(View.GONE);
					airAttachBannerTextView.setText(getString(R.string.minus_x_percent, rate.getDiscountPercent()));
				}
				else {
					airAttachBannerTextView.setVisibility(View.GONE);
					saleBannerTextView.setVisibility(View.VISIBLE);
					saleBannerTextView.setText(getString(R.string.minus_x_percent, rate.getDiscountPercent()));
				}
			}
			else {
				saleBannerTextView.setVisibility(View.GONE);
			}
			promoTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

			// Promo text, i.e. "Mobile Exclusive!" or "Tonight Only!"
			if (property.isLowestRateTonightOnly()) {
				promoTextView.setText(getString(R.string.tonight_only));
				promoTextView.setVisibility(View.VISIBLE);
			}
			else if (property.isLowestRateMobileExclusive()) {
				if (ProductFlavorFeatureConfiguration.getInstance().getHotelDetailsDealImageDrawable() != 0) {
					promoTextView.setCompoundDrawablesWithIntrinsicBounds(
						ProductFlavorFeatureConfiguration.getInstance().getHotelDetailsDealImageDrawable(), 0, 0, 0);
					promoTextView.setText("");
				}
				else {
					promoTextView.setText(R.string.mobile_exclusive);
				}
				promoTextView.setVisibility(View.VISIBLE);
			}
			else {
				if (centerFiller != null) {
					centerFiller.setVisibility(View.VISIBLE);
				}
				promoTextView.setVisibility(View.GONE);
			}

			// "<strike>$400</strike>" (if it's on sale)
			TextView strikethroughTextView = Ui.findView(view, R.id.strikethrough_price_text_view);
			if (rate.isOnSale()) {
				strikethroughTextView.setText(HtmlCompat.fromHtml(
					getString(R.string.strike_template, StrUtils.formatHotelPrice(rate.getDisplayBasePrice())),
					null,
					new StrikethroughTagHandler()));
				strikethroughTextView.setVisibility(View.VISIBLE);
			}
			else {
				strikethroughTextView.setVisibility(View.GONE);
			}

			// Rate
			TextView rateTextView = Ui.findView(view, R.id.rate_text_view);
			rateTextView.setText(StrUtils.formatHotelPrice(rate.getDisplayPrice()));
			view.findViewById(R.id.per_nt_text_view).setVisibility(
				rate.getUserPriceType() != UserPriceType.PER_NIGHT_RATE_NO_TAXES ? View.GONE : View.VISIBLE);

			if (centerFiller != null) {
				//3858. Let's check to see if the promoText can fit in the available space. If not let's pull the plug on it.
				promoTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						promoTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						Layout textLayout = promoTextView.getLayout();
						if (textLayout != null) {
							int lines = textLayout.getLineCount();
							if (lines > 0) {
								if (textLayout.getEllipsisCount(lines - 1) > 0) {
									centerFiller.setVisibility(View.VISIBLE);
									promoTextView.setVisibility(View.GONE);
								}
							}
						}
					}
				});
			}
		}
	}

	public void populateVipIcon(View root, Property property) {
		if (property == null) {
			return;
		}

		if (PointOfSale.getPointOfSale().supportsVipAccess() && property.isVipAccess()) {
			mVipIcon = Ui.findView(root, R.id.vip_badge);
			mVipIcon.setVisibility(View.VISIBLE);
			mVipIcon.setOnClickListener(new VipBadgeClickListener(getResources(), getFragmentManager()));
		}
	}

	public void setVipIconEnabled(boolean enabled) {
		if (mVipIcon != null) {
			mVipIcon.setEnabled(enabled);
		}
	}
}
