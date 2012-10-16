package com.expedia.bookings.fragment;

import java.util.List;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.UserReviewsListActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelDescription;
import com.expedia.bookings.data.HotelDescription.DescriptionSection;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.utils.DbPropertyHelper;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class HotelDetailsPricePromoFragment extends Fragment {

	public static HotelDetailsPricePromoFragment newInstance() {
		return new HotelDetailsPricePromoFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details_price_promo, container, false);
		populateViews(view);
		return view;
	}

	public void populateViews() {
		populateViews(getView());
	}

	private void populateViews(View view) {
		populatePricePromoBar(view, DbPropertyHelper.getBestRateProperty());
	}

	private void populatePricePromoBar(View view, Property property) {
		if (property == null) {
			return;
		}

		Rate rate = property.getLowestRate();

		// Sale banner
		TextView saleBannerTextView = Ui.findView(view, R.id.sale_banner_text_view);
		TextView promoTextView = Ui.findView(view, R.id.promo_text_view);
		if (rate.isSaleTenPercentOrBetter()) {
			saleBannerTextView.setVisibility(View.VISIBLE);
			saleBannerTextView.setText(getString(R.string.minus_x_percent, rate.getDiscountPercent()));
		}
		else {
			saleBannerTextView.setVisibility(View.GONE);
		}

		// Promo text, i.e. "Mobile Exclusive!" or "Tonight Only!"
		if (property.isLowestRateTonightOnly()) {
			promoTextView.setText(getString(R.string.tonight_only));
			promoTextView.setVisibility(View.VISIBLE);
		}
		else if (property.isLowestRateMobileExclusive()) {
			promoTextView.setText(getString(R.string.mobile_exclusive));
			promoTextView.setVisibility(View.VISIBLE);
		}
		else {
			promoTextView.setVisibility(View.GONE);
		}

		// "<strike>$400</strike>" (if it's on sale)
		TextView strikethroughTextView = Ui.findView(view, R.id.strikethrough_price_text_view);
		if (rate.isOnSale()) {
			strikethroughTextView.setText(Html.fromHtml(
					getString(R.string.strike_template, StrUtils.formatHotelPrice(rate.getDisplayBaseRate())), null,
					new StrikethroughTagHandler()));
			strikethroughTextView.setVisibility(View.VISIBLE);
		}
		else {
			strikethroughTextView.setVisibility(View.GONE);
		}

		// Rate
		TextView rateTextView = Ui.findView(view, R.id.rate_text_view);
		rateTextView.setText(StrUtils.formatHotelPrice(rate.getDisplayRate()));
		view.findViewById(R.id.per_nt_text_view).setVisibility(
				rate.showInclusivePrices() ? View.GONE : View.VISIBLE);
	}
}
