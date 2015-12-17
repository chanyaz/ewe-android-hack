package com.expedia.bookings.widget;

import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CheckBoxFilterWidget.OnCheckedChangeListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class AirportFilterWidget extends TextView {

	private PopupWindow mPopup;

	private int mLegNumber;
	private boolean mDepartureAirport;
	private Set<String> mAirportCodes;
	private FlightFilter mFilter;
	private OnCheckedChangeListener mAirportCheckChangeListener;

	private AirportFilterWidgetListener mAirportFilterWidgetListener;

	public interface AirportFilterWidgetListener {
		void onPopupToggled(boolean isShowing, boolean departureAirport);
	}

	public AirportFilterWidget(Context context) {
		this(context, null);
	}

	public AirportFilterWidget(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AirportFilterWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleDropdown();
			}
		});
	}

	public void bind(int legNumber, boolean departureAirport, Set<String> airportCodes, FlightFilter filter,
			OnCheckedChangeListener checkChangeListener, AirportFilterWidgetListener airportFilterListener) {
		mLegNumber = legNumber;
		mDepartureAirport = departureAirport;
		mAirportCodes = airportCodes;
		mFilter = filter;
		mAirportCheckChangeListener = checkChangeListener;
		mAirportFilterWidgetListener = airportFilterListener;

		setVisibility(mAirportCodes.size() < 2 ? View.GONE : View.VISIBLE);

		bindLabel();
	}

	public void bindLabel() {
		Set<String> airportsInFilter = mFilter.getAirports(mDepartureAirport);
		Set<String> airportsAll = Db.getFlightSearch().queryTrips(mLegNumber).getAirportCodes(mDepartureAirport);

		SpannableBuilder sb = new SpannableBuilder();
		if (airportsInFilter.size() >= airportsAll.size()) {
			sb.append(getResources().getString(R.string.all_airports));
		}
		else {
			sb.append(Strings.joinWithoutEmpties(", ", airportsInFilter));
		}
		setText(sb.build(), android.widget.TextView.BufferType.SPANNABLE);
	}

	private void dismissPopup() {
		mPopup.dismiss();
		mAirportFilterWidgetListener.onPopupToggled(false, mDepartureAirport);
	}

	private void toggleDropdown() {
		if (mPopup != null && mPopup.isShowing()) {
			dismissPopup();
		}
		else {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			final View content = inflater.inflate(R.layout.snippet_flight_airport_filter, null);
			android.widget.LinearLayout vg = Ui.findView(content, R.id.airport_filter_container);
			vg.setDividerPadding(8);
			mPopup = new PopupWindow(content, getWidth(), LayoutParams.WRAP_CONTENT, true);
			mPopup.setBackgroundDrawable(new BitmapDrawable());
			mPopup.setOutsideTouchable(true);
			mPopup.setTouchable(true);
			mPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					dismissPopup();
				}
			});

			Map<String, FlightTrip> cheapestPerAirport = Db.getFlightSearch().queryTrips(mLegNumber)
					.getCheapestTripsByAirport(mDepartureAirport);

			// Add the checkbox widgets
			FlightTrip flightTrip;
			for (String code : mAirportCodes) {
				Airport airport = FlightStatsDbUtils.getAirport(code);

				SpannableBuilder sb = new SpannableBuilder();
				if (airport.mName == null) {
					Log.e("Airport name was null for airport code=" + airport.mAirportCode);
					sb.append(airport.mAirportCode, FontCache.getSpan(FontCache.Font.ROBOTO_BOLD));
				}
				else {
					sb.append(airport.mAirportCode + " - ", FontCache.getSpan(FontCache.Font.ROBOTO_BOLD));
					sb.append(airport.mName, FontCache.getSpan(FontCache.Font.ROBOTO_REGULAR));
				}

				CheckBoxFilterWidget widget = new CheckBoxFilterWidget(getContext());

				flightTrip = cheapestPerAirport.get(airport.mAirportCode);
				widget.setDescription(sb.build());
				if (flightTrip != null) {
					widget.setPrice(flightTrip.getAverageTotalFare(), false);
					widget.setEnabled(true);
				}
				else {
					widget.setEnabled(false);
				}

				widget.setTag(Boolean.toString(mDepartureAirport) + ";" + code);
				widget.setChecked(mFilter.containsAirport(mDepartureAirport, airport.mAirportCode));
				widget.setOnCheckedChangeListener(mAirportCheckChangeListener);

				vg.addView(widget);
			}

			// Set a click listener for the done button
			Button doneButton = Ui.findView(content, R.id.airport_filter_done);
			doneButton.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_REGULAR));
			doneButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismissPopup();
				}
			});

			// Show the popup off screen to render it fully.
			mPopup.showAtLocation(this, Gravity.NO_GRAVITY, -1000, -1000);

			// Update the Popup position to above or below based on whether or not it clips at bottom.
			content.post(new Runnable() {
				@Override
				public void run() {
					int popupHeight = content.getHeight();

					int[] anchorLoc = new int[2];
					AirportFilterWidget.this.getLocationOnScreen(anchorLoc);

					int anchorYPos = anchorLoc[1];
					int anchorHeight = AirportFilterWidget.this.getHeight();
					Point screenSize = AndroidUtils.getScreenSize(getContext());

					Log.d("AirportFilterWidget - popupHeight=" + popupHeight + " anchorY=" + anchorYPos
							+ " anchorHeight=" + anchorHeight + " screenY=" + screenSize.y);

					int popupBottom = popupHeight + anchorYPos + anchorHeight;

					int y;
					if (popupBottom > screenSize.y) {
						y = anchorYPos - popupHeight;
					}
					else {
						y = anchorYPos + anchorHeight;
					}
					mPopup.update(anchorLoc[0], y, -1, -1);
				}
			});

			mAirportFilterWidgetListener.onPopupToggled(true, mDepartureAirport);
		}
	}
}
