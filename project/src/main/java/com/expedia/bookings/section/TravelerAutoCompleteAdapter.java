package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Context;
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

	private static final int ITEM_VIEW_TYPE_SELECT_CONTACT = 0;
	private static final int ITEM_VIEW_TYPE_TRAVELER = 1;
	private static final int ITEM_VIEW_TYPE_ADD_TRAVELER = 2;
	private static final int ITEM_VIEW_TYPE_COUNT = 3;

	private TravelersFilter mFilter = new TravelersFilter();
	private String mFilterStr;
	private int mTravelerNumber = -1;

	private boolean isAddTravelerEnabled = true;
	private int mTravelerBackgroundDrawable = R.drawable.traveler_circle;

	public TravelerAutoCompleteAdapter(Context context) {
		super(context, R.layout.traveler_autocomplete_row);
	}

	public TravelerAutoCompleteAdapter(Context context, boolean addTravelerEnabled, int travelerDrawable) {
		super(context, R.layout.traveler_autocomplete_row);
		isAddTravelerEnabled = addTravelerEnabled;
		mTravelerBackgroundDrawable = travelerDrawable;
	}

	@Override
	public int getCount() {
		return getAvailableTravelers().size() + (isAddTravelerEnabled ? 2 : 1);
	}

	@Override
	public long getItemId(int position) {
		if (getItem(position) != null) {
			return getItem(position).getTuid();
		}
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public Traveler getItem(int position) {
		int itemType = getItemViewType(position);
		if (itemType == ITEM_VIEW_TYPE_TRAVELER) {
			if (getCount() > position - 1) {
				return getAvailableTravelers().get(position - 1);
			}
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

	private static class ViewHolder {
		TextView tv;
		TextView initials;
		ImageView icon;
		public ViewHolder(View v) {
			tv = Ui.findView(v, R.id.text1);
			icon = Ui.findView(v, R.id.icon);
			initials = Ui.findView(v, R.id.initials);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int itemType = getItemViewType(position);
		Traveler trav = getItem(position);
		ViewHolder vh;
		View retView = convertView;

		switch (itemType) {
		case ITEM_VIEW_TYPE_SELECT_CONTACT:
			if (retView == null) {
				retView = View.inflate(getContext(), R.layout.travelers_popup_header_footer_row, null);
				vh = new ViewHolder(retView);
				retView.setTag(vh);
			}
			else {
				vh = (ViewHolder) retView.getTag();
			}
			vh.tv.setText(R.string.select_traveler);
			vh.icon.setBackgroundResource(R.drawable.select_contact);
			retView.setEnabled(false);
			break;
		case ITEM_VIEW_TYPE_TRAVELER:
			if (retView == null) {
				retView = View.inflate(getContext(), R.layout.traveler_autocomplete_row, null);
				vh = new ViewHolder(retView);
				retView.setTag(vh);
			}
			else {
				vh = (ViewHolder) retView.getTag();
			}
			vh.tv.setText(trav.getFullName());
			vh.initials.setBackgroundResource(mTravelerBackgroundDrawable);
			vh.initials.setText(TravelerIconUtils.getInitialsFromDisplayName(trav.getFullName()));
			break;
		case ITEM_VIEW_TYPE_ADD_TRAVELER:
			if (retView == null) {
				retView = View.inflate(getContext(), R.layout.travelers_popup_header_footer_row, null);
				vh = new ViewHolder(retView);
				retView.setTag(vh);
			}
			else {
				vh = (ViewHolder) retView.getTag();
			}
			vh.tv.setText(R.string.add_new_traveler);
			vh.icon.setBackgroundResource(R.drawable.add_plus);
			break;
		}

		return retView;
	}

	@Override
	public int getViewTypeCount() {
		return ITEM_VIEW_TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		if (isAddTravelerEnabled && position == getCount() - 1) {
			return ITEM_VIEW_TYPE_ADD_TRAVELER;
		}
		else if (position == 0) {
			return ITEM_VIEW_TYPE_SELECT_CONTACT;
		}
		else {
			return ITEM_VIEW_TYPE_TRAVELER;
		}
	}

	private ArrayList<Traveler> getAvailableTravelers() {
		if (User.isLoggedIn(getContext()) && Db.getUser() != null && Db.getUser().getAssociatedTravelers() != null) {
			ArrayList<Traveler> availableTravelers = new ArrayList<Traveler>(Db.getUser().getAssociatedTravelers());
			availableTravelers.add(Db.getUser().getPrimaryTraveler());
			for (int i = availableTravelers.size() - 1; i >= 0; i--) {
				Traveler trav = availableTravelers.get(i);

				//Remove the working traveler from the list of available travelers
				if (Db.getWorkingTravelerManager() != null
					&& Db.getWorkingTravelerManager().getWorkingTraveler() != null) {
					Traveler workingTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
					if (trav.compareNameTo(workingTraveler) == 0) {
						availableTravelers.get(i).setIsSelectable(false);
						continue;
					}
				}

				//Remove travelers based on filter text
				if (!TextUtils.isEmpty(mFilterStr) && !TextUtils.isEmpty(trav.getFullName()) && !trav.getFullName()
					.toLowerCase(Locale.getDefault()).contains(mFilterStr.toLowerCase(Locale.getDefault()))) {
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
