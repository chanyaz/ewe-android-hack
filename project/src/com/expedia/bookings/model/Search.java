package com.expedia.bookings.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;

import com.activeandroid.ActiveRecordBase;
import com.activeandroid.annotation.Column;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchParams.SearchType;

public class Search extends ActiveRecordBase<Search> {
	public Search(Context context, SearchParams searchParams) {
		super(context);

		mSearchType = searchParams.getSearchType().toString();
		mFreeformLocation = searchParams.getFreeformLocation();
		mCheckInDate = searchParams.getCheckInDate();
		mCheckOutDate = searchParams.getCheckOutDate();
		mNumAdults = searchParams.getNumAdults();
		mNumChildren = searchParams.getNumChildren();
		mSearchLatitude = searchParams.getSearchLatitude();
		mSearchLongitude = searchParams.getSearchLongitude();
	}

	@Column(name = "SearchType")
	private String mSearchType;

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

	@Column(name = "SearchLatitude")
	private double mSearchLatitude;

	@Column(name = "SearchLongitude")
	private double mSearchLongitude;

	public SearchParams getSearchParams() {
		SearchParams searchParams = new SearchParams();
		searchParams.setSearchType(SearchType.valueOf(mSearchType));
		searchParams.setFreeformLocation(mFreeformLocation);
		searchParams.setCheckInDate(mCheckInDate);
		searchParams.setCheckOutDate(mCheckOutDate);
		searchParams.setNumAdults(mNumAdults);
		searchParams.setNumChildren(mNumChildren);
		searchParams.setSearchLatLon(mSearchLatitude, mSearchLongitude);

		return searchParams;
	}
	
	public static List<SearchParams> getAllSearchParams(Context context) {
		List<SearchParams> searchParams = new ArrayList<SearchParams>();
		List<Search> searches = query(context, Search.class);
		for(Search search : searches) {
			searchParams.add(search.getSearchParams());
		}
		
		return searchParams;
	}
}
