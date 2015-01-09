package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.RoomsAndRatesListActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.base.Fragment;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.Ui;

public class PayLaterInfoFragment extends Fragment {

	private Property mProperty;

	private TextView mPayNowCurrencyTv;
	private TextView mPayLaterCurrencyTv;
	private TextView mSelectRoomButtonTv;

	public static PayLaterInfoFragment newInstance() {
		return new PayLaterInfoFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mProperty = Db.getHotelSearch().getSelectedProperty();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_pay_later_info_screen, container, false);

		mPayNowCurrencyTv = Ui.findView(v, R.id.etp_pay_now_currency_text);
		mPayLaterCurrencyTv = Ui.findView(v, R.id.etp_pay_later_currency_text);

		String userCountryCode = PointOfSale.getPointOfSale().getThreeLetterCountryCode();
		String hotelCountryCode = mProperty.getLocation().getCountryCode();
		mPayNowCurrencyTv.setText(getString(R.string.etp_pay_now_currency_text_TEMPLATE, CurrencyUtils.currencyForLocale(userCountryCode)));
		mPayLaterCurrencyTv.setText(getString(R.string.etp_pay_later_currency_text_TEMPLATE, CurrencyUtils.currencyForLocale(hotelCountryCode)));


		mSelectRoomButtonTv = Ui.findView(v, R.id.select_room_button);
		if (mProperty.isAvailable()) {
			mSelectRoomButtonTv.setVisibility(View.VISIBLE);
			mSelectRoomButtonTv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(RoomsAndRatesListActivity.createIntent(getActivity()));
				}
			});
		}
		else {
			mSelectRoomButtonTv.setVisibility(View.GONE);
		}

		return v;
	}
}
