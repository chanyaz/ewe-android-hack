package com.expedia.bookings.section;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
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
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.TravelerIconUtils;
import com.mobiata.android.util.Ui;

import java.util.ArrayList;
import java.util.Locale;

public class TravelerAutoCompleteAdapter extends ArrayAdapter<Traveler> implements Filterable {

	private static final int ITEM_VIEW_TYPE_SELECT_CONTACT = 0;
	private static final int ITEM_VIEW_TYPE_TRAVELER = 1;
	private static final int ITEM_VIEW_TYPE_ADD_TRAVELER = 2;
	private static final int ITEM_VIEW_TYPE_COUNT = 3;

	private TravelersFilter mFilter = new TravelersFilter();
	private String mFilterStr;


	private int mTravelerBackgroundDrawable = R.drawable.traveler_circle;

	public TravelerAutoCompleteAdapter(Context context) {
		super(context, R.layout.traveler_autocomplete_row);
	}

	public TravelerAutoCompleteAdapter(Context context, int travelerDrawable) {
		super(context, R.layout.traveler_autocomplete_row);
		mTravelerBackgroundDrawable = travelerDrawable;
	}

	@Override
	public int getCount() {
		return getAvailableTravelers().size() + 2;
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
		Traveler traveler = getItem(position);
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
			vh.tv.setText(traveler.getFullName());
			vh.initials.setBackgroundResource(mTravelerBackgroundDrawable);
			vh.initials.setText(TravelerIconUtils.getInitialsFromDisplayName(traveler.getFullName()));
			toggleViewHolderSelectedStyle(vh, traveler);
			break;
		case ITEM_VIEW_TYPE_ADD_TRAVELER:
			if (retView == null) {
				retView = View.inflate(getContext(), R.layout.travelers_popup_header_footer_row, null);
				retView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_checkout_saved_spinner_row));
				vh = new ViewHolder(retView);
				retView.setTag(vh);
			}
			else {
				vh = (ViewHolder) retView.getTag();
			}
			vh.tv.setText(R.string.add_new_traveler);
			vh.icon.setBackgroundResource(R.drawable.add_plus);
			toggleViewHolderSelectedStyle(vh, traveler);
			break;
		}

		return retView;
	}

	private void toggleViewHolderSelectedStyle(ViewHolder vh, Traveler traveler) {
		float textViewAlpha = 1f;
		int iconAlpha = 100;

		if (traveler != null && !traveler.isSelectable()) {
			textViewAlpha = 0.15f;
			iconAlpha = 15;
		}

		vh.tv.setAlpha(textViewAlpha);
		if (vh.initials != null) {
			vh.initials.setAlpha(textViewAlpha);
		}
		if (vh.icon != null) {
			vh.icon.setImageAlpha(iconAlpha);
		}
	}

	@Override
	public int getViewTypeCount() {
		return ITEM_VIEW_TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		boolean isAddNewTraveler = position == getCount() - 1;
		if (isAddNewTraveler) {
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
					if (trav.nameEquals(workingTraveler)) {
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
			if (availableTravelers.size() == 1 && Strings.isEmpty(availableTravelers.get(0).getFullName())) {
				return new ArrayList<>();
			}
			return availableTravelers;
		}
		return new ArrayList<>();
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

	@Override
	public boolean isEnabled(int position) {
		Traveler traveler = getItem(position);
		if (traveler != null) {
			return traveler.isSelectable();
		}
		else if (position != 0) { // add new traveler button
			return true;
		}
		// "Select Contact" button (list position:0)
		return false;
	}
}
