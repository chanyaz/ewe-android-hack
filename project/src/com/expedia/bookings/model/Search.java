package com.expedia.bookings.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;

import com.activeandroid.ActiveRecordBase;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchParams.SearchType;

@Table(name = "Searches")
public class Search extends ActiveRecordBase<Search> {
	public Search(Context context) {
		super(context);
	}

	public Search(Context context, SearchParams searchParams) {
		super(context);

		mFreeformLocation = searchParams.getFreeformLocation().trim();
		mCheckInDate = searchParams.getCheckInDate();
		mCheckOutDate = searchParams.getCheckOutDate();
		mNumAdults = searchParams.getNumAdults();
		mNumChildren = searchParams.getNumChildren();
	}

	@Column(name = "FreeFormLocation")
	private String mFreeformLocation;

	@Column(name = "CheckInDate")
	private Calendar mCheckInDate;

	@Column(name = "CheckOutDate")
	private Calendar mCheckOutDate;

	@Column(name = "NumAdults")
	private int mNumAdults;

	@Column(name = "NumChildren")
	private int mNumChildren;

	public SearchParams getSearchParams() {
		SearchParams searchParams = new SearchParams();
		searchParams.setSearchType(SearchType.FREEFORM);
		searchParams.setFreeformLocation(mFreeformLocation);
		searchParams.setCheckInDate(mCheckInDate);
		searchParams.setCheckOutDate(mCheckOutDate);
		searchParams.setNumAdults(mNumAdults);
		searchParams.setNumChildren(mNumChildren);

		return searchParams;
	}

	public static List<SearchParams> getAllSearchParams(Context context) {
		List<SearchParams> searchParams = new ArrayList<SearchParams>();
		List<Search> searches = query(context, Search.class, null, null, "Id DESC");
		for (Search search : searches) {
			searchParams.add(search.getSearchParams());
		}

		return searchParams;
	}

	public static void add(Context context, SearchParams searchParams) {
		if (searchParams.getSearchType() == SearchType.MY_LOCATION || searchParams.getFreeformLocation() == null
				&& searchParams.getFreeformLocation().length() > 0) {

			return;
		}

		Search.delete(context, Search.class, "lower(FreeFormLocation) = '"
				+ searchParams.getFreeformLocation().toLowerCase().trim() + "'");
		new Search(context, searchParams).save();
	}
}
