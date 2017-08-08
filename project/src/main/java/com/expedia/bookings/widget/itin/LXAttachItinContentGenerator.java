package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.trips.ItinCardDataLXAttach;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LXNavUtils;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;

public class LXAttachItinContentGenerator extends ItinButtonContentGenerator<ItinCardDataLXAttach> {

	public LXAttachItinContentGenerator(Context context, ItinCardDataLXAttach itinCardData) {
		super(context, itinCardData);
	}

	@Override
	public Type getType() {
		return Type.HOTEL;
	}

	@Override
	public View.OnClickListener getOnItemClickListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppLXNavigateToSRP)) {
					LXNavUtils.goToActivities(v.getContext(), null, getItinCardData().getLxSearchParams(v.getContext()),
							NavUtils.FLAG_OPEN_RESULTS);
				}
				else {
					LXNavUtils.goToActivities(v.getContext(), null, getItinCardData().getLxSearchParams(v.getContext()),
							NavUtils.FLAG_OPEN_SEARCH);
				}
				OmnitureTracking.trackAddLxItin();
			}
		};
	}

	@Override
	public View getDetailsView(View convertView, ViewGroup container) {
		if (convertView == null) {
			convertView = getLayoutInflater().inflate(R.layout.include_itin_button_lx_attach, container, false);
		}

		final String buttonText;

		Property property = getItinCardData().getProperty();
		if (property != null && Strings.isNotEmpty(property.getLocation().getCity())) {

			buttonText = getContext().getString(R.string.add_lx_TEMPLATE,
				property.getLocation().getCity());
		}
		else {
			buttonText = getContext().getString(R.string.add_lx_fallback);
		}

		Ui.setText(convertView, R.id.action_text_view, buttonText);

		return convertView;
	}
}
