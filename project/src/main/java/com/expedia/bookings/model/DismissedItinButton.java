package com.expedia.bookings.model;

import java.util.HashSet;
import java.util.List;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.expedia.bookings.widget.itin.ItinButtonCard.ItinButtonType;

@Table(name = "DismissedItinButtons")
public class DismissedItinButton extends Model {
	@Column(name = "TripId")
	private String mTripId;
	@Column(name = "ItinButtonType")
	private ItinButtonType mItinButtonType;

	public String getTripId() {
		return mTripId;
	}

	public void setTripId(String tripId) {
		mTripId = tripId;
	}

	public ItinButtonType getItinButtonType() {
		return mItinButtonType;
	}

	public void setItinButtonType(ItinButtonType itinButtonType) {
		mItinButtonType = itinButtonType;
	}

	public static DismissedItinButton dismiss(String tripId, ItinButtonType itinButtonType) {
		DismissedItinButton dismissedItinButton = new DismissedItinButton();
		dismissedItinButton.setTripId(tripId);
		dismissedItinButton.setItinButtonType(itinButtonType);
		dismissedItinButton.save();

		return dismissedItinButton;
	}

	public static HashSet<String> getDismissedTripIds(ItinButtonType itinButtonType) {
		final List<DismissedItinButton> dismissedItinButtons = new Select()
				.from(DismissedItinButton.class)
				.where("ItinButtonType = ?", itinButtonType)
				.execute();

		final HashSet<String> dismissedTripIds = new HashSet<String>();
		for (DismissedItinButton dismissedItinButton : dismissedItinButtons) {
			dismissedTripIds.add(dismissedItinButton.getTripId());
		}

		return dismissedTripIds;
	}

	public static void clear() {
		new Delete().from(DismissedItinButton.class).execute();
	}
}
