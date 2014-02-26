package com.expedia.bookings.section;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.mobiata.android.util.Ui;

public class TravelerSpinnerAdapter extends ArrayAdapter<Traveler> {

	public TravelerSpinnerAdapter(Context context) {
		super(context, R.layout.simple_spinner_traveler_item);
		setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
	}

	@Override
	public int getCount() {
		int availableTravelerCount = getAvailableTravelers().size();
		if (availableTravelerCount > 0) {
			//The +1 is for the first item which we display as empty
			return availableTravelerCount + 1;
		}
		return 0;
	}

	@Override
	public Traveler getItem(int position) {
		if (getCount() > position) {
			if (position == 0) {
				return new Traveler();//Blank first item
			}
			return getAvailableTravelers().get(position - 1);
		}
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View retView = super.getView(position, convertView, parent);
		TextView tv = Ui.findView(retView, android.R.id.text1);
		Traveler trav = getItem(position);
		if (trav == null || position == 0) {
			tv.setText(R.string.select_a_traveler);
		}
		else {
			tv.setText(trav.getFirstName() + " " + trav.getLastName());
		}
		return retView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View retView = super.getDropDownView(position, convertView, parent);
		TextView tv = Ui.findView(retView, android.R.id.text1);
		Traveler trav = getItem(position);
		if (trav == null || position == 0) {
			tv.setText("");
		}
		else {
			tv.setText(trav.getFirstName() + " " + trav.getLastName());
		}
		return retView;
	}

	private ArrayList<Traveler> getAvailableTravelers() {
		boolean removeWorkingTraveler = true;
		boolean removeDbTravelers = false;

		if (User.isLoggedIn(getContext()) && Db.getUser() != null && Db.getUser().getAssociatedTravelers() != null) {
			ArrayList<Traveler> availableTravelers = new ArrayList<Traveler>(Db.getUser().getAssociatedTravelers());
			for (int i = availableTravelers.size() - 1; i >= 0; i--) {
				Traveler trav = availableTravelers.get(i);

				//Remove the working traveler from the list of available travelers
				if (removeWorkingTraveler && Db.getWorkingTravelerManager() != null && Db.getWorkingTravelerManager().getWorkingTraveler() != null) {
					Traveler workingTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
					if (trav.compareNameTo(workingTraveler) == 0) {
						availableTravelers.remove(i);
						continue;
					}
				}

				//Remove the travelers already in Db from the list of available travelers
				if (removeDbTravelers && Db.getTravelers() != null) {
					for (Traveler dbTrav : Db.getTravelers()) {
						if (dbTrav.compareNameTo(trav) == 0) {
							availableTravelers.remove(i);
							break;
						}
					}
				}
			}

			return availableTravelers;
		}
		return new ArrayList<Traveler>();
	}


}
