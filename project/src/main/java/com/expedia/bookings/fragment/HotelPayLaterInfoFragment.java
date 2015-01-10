package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelRoomsAndRatesActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.base.Fragment;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.Ui;

public class HotelPayLaterInfoFragment extends Fragment {

	public static HotelPayLaterInfoFragment newInstance() {
		return new HotelPayLaterInfoFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_pay_later_info_screen, container, false);

		Property property = Db.getHotelSearch().getSelectedProperty();
		String userCountryCode = PointOfSale.getPointOfSale().getThreeLetterCountryCode();
		String hotelCountryCode = property.getLocation().getCountryCode();

		setPayMessage(v, R.id.etp_pay_now_currency_text, R.string.etp_pay_now_currency_text_TEMPLATE, userCountryCode);
		setPayMessage(v, R.id.etp_pay_later_currency_text, R.string.etp_pay_later_currency_text_TEMPLATE, hotelCountryCode);

		TextView selectRoomTv = Ui.findView(v, R.id.select_room_button);
		if (property.isAvailable()) {
			selectRoomTv.setVisibility(View.VISIBLE);
			selectRoomTv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(HotelRoomsAndRatesActivity.createIntent(getActivity()));
				}
			});
		}
		else {
			selectRoomTv.setVisibility(View.GONE);
		}

		return v;
	}

	/**
	 * @param view             parent View
	 * @param viewResId        resId of TextView to set an etp pay message
	 * @param strTemplateResId str template for the etp pay message
	 * @param countryCode      used to determine the currency to be displayed in the message
	 */
	private void setPayMessage(View view, @IdRes int viewResId, @StringRes int strTemplateResId, String countryCode) {
		String currency = CurrencyUtils.currencyForLocale(countryCode);
		CharSequence payMsg = getString(strTemplateResId, currency);
		Ui.setText(view, viewResId, payMsg);
	}
}
