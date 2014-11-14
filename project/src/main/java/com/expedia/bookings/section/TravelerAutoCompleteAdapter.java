package com.expedia.bookings.section;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.TravelerIconUtils;
import com.mobiata.android.util.Ui;

public class TravelerAutoCompleteAdapter extends ArrayAdapter<Traveler> implements Filterable {

	private TravelersFilter mFilter = new TravelersFilter();
	private String mFilterStr;
	private int mTravelerNumber = -1;

	public TravelerAutoCompleteAdapter(Context context) {
		super(context, R.layout.traveler_autocomplete_row);
	}

	@Override
	public int getCount() {
		return getAvailableTravelers().size();
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getTuid();
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public Traveler getItem(int position) {
		if (getCount() > position) {
			return getAvailableTravelers().get(position);
		}
		return null;
	}

	public void setTravelerNumber(int travelerNumber) {
		mTravelerNumber = travelerNumber;
	}

	public Traveler getItemFromId(long id) {
		if (Db.getUser() != null && Db.getUser().getAssociatedTravelers() != null) {
			for (Traveler trav : Db.getUser().getAssociatedTravelers()) {
				if (trav.getTuid() == id) {
					return trav;
				}
			}
		}
		return null;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Traveler trav = getItem(position);

		View retView = convertView;
		if (retView == null) {
			retView = View.inflate(getContext(), R.layout.traveler_autocomplete_row, null);
		}
		TextView tv = Ui.findView(retView, android.R.id.text1);
		ImageView icon = Ui.findView(retView, android.R.id.icon);
		tv.setText(trav.getFullName());
		retView.setEnabled(trav.isSelectable());

		//TODO: This might be sort of heavy because we are generating a new bitmap every time...
		icon.setImageBitmap(TravelerIconUtils.generateCircularInitialIcon(getContext(), trav.getFullName(),
			Color.parseColor("#FF373F4A")));

		return retView;
	}

	private ArrayList<Traveler> getAvailableTravelers() {
		boolean removeWorkingTraveler = true;
		boolean removeDbTravelers = true;

		if (User.isLoggedIn(getContext()) && Db.getUser() != null && Db.getUser().getAssociatedTravelers() != null) {
			ArrayList<Traveler> availableTravelers = new ArrayList<Traveler>(Db.getUser().getAssociatedTravelers());
			availableTravelers.add(Db.getUser().getPrimaryTraveler());
			for (int i = availableTravelers.size() - 1; i >= 0; i--) {
				Traveler trav = availableTravelers.get(i);

				//Remove the working traveler from the list of available travelers
				if (removeWorkingTraveler && Db.getWorkingTravelerManager() != null
					&& Db.getWorkingTravelerManager().getWorkingTraveler() != null) {
					Traveler workingTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
					if (trav.compareNameTo(workingTraveler) == 0) {
						availableTravelers.get(i).setIsSelectable(false);
						continue;
					}
				}

				//Remove the travelers already in Db from the list of available travelers
				if (removeDbTravelers && Db.getTravelers() != null) {
					boolean removed = false;
					for (int j = 0; j < Db.getTravelers().size(); j++) {
						Traveler dbTrav = Db.getTravelers().get(j);
						if ((!removeWorkingTraveler || j != mTravelerNumber) && dbTrav.compareNameTo(trav) == 0) {
							availableTravelers.get(i).setIsSelectable(false);
							removed = true;
							break;
						}
					}
					if (removed) {
						continue;
					}
				}

				//Remove travelers based on filter text
				if (!TextUtils.isEmpty(mFilterStr) && !TextUtils.isEmpty(trav.getFullName()) && !trav.getFullName()
					.toLowerCase().contains(mFilterStr.toLowerCase())) {
					availableTravelers.remove(i);
					continue;
				}
			}

			return availableTravelers;
		}
		return new ArrayList<Traveler>();
	}

	public void clearFilter() {
		mFilterStr = "";
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	private class TravelersFilter extends Filter {

		@Override
		//The return value of this gets stuck into the EditText when we select an item from the dropdown
		public CharSequence convertResultToString(Object resultValue) {
			if (resultValue == null || !(resultValue instanceof Traveler)) {
				return "";
			}
			else {
				if (PointOfSale.getPointOfSale().showLastNameFirst()) {
					return ((Traveler) resultValue).getLastName();
				}
				return ((Traveler) resultValue).getFirstName();
			}
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			//We are sort of ignoring the pattern here, we set our filter string, and let getAvailableTravelers
			//do the work.
			mFilterStr = "" + (constraint != null ? constraint : "");
			return new FilterResults();
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			notifyDataSetChanged();
		}
	}

}
