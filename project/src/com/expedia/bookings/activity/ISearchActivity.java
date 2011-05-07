package com.expedia.bookings.activity;

import com.expedia.bookings.activity.SearchActivity.MapViewListener;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.Session;

public interface ISearchActivity {
	public void addSearchListener(SearchListener searchListener);
	public void setMapViewListener(MapViewListener mapViewListener);
	public SearchParams getSearchParams();
	public Session getSession();
}
