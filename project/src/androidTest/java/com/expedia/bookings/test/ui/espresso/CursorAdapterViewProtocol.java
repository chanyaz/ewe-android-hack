package com.expedia.bookings.test.ui.espresso;

import java.util.List;

import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.google.android.apps.common.testing.ui.espresso.action.AdapterViewProtocol;
import com.google.android.apps.common.testing.ui.espresso.action.AdapterViewProtocols;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class CursorAdapterViewProtocol implements AdapterViewProtocol {
	public Object getDataFromCursor(CursorAdapter cursorAdapter, Cursor cursor) {
		return cursorAdapter.convertToString(cursor);
	}

	@Override
	public Iterable<AdaptedData> getDataInAdapterView(AdapterView<? extends Adapter> adapterView) {
		CursorAdapter adapter = (CursorAdapter) adapterView.getAdapter();

		List<AdaptedData> datas = Lists.newArrayList();
		for (int i = 0; i < adapterView.getCount(); i++) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
			datas.add(new AdaptedData.Builder()
					.withData(getDataFromCursor(adapter, cursor))
					.withOpaqueToken(i)
					.build());
		}
		return datas;
	}

	@Override
	public Optional<AdaptedData> getDataRenderedByView(AdapterView<? extends Adapter> adapterView, View descendantView) {
		if (adapterView == descendantView.getParent()) {
			int position = adapterView.getPositionForView(descendantView);
			if (position != AdapterView.INVALID_POSITION) {
				CursorAdapter adapter = (CursorAdapter) adapterView.getAdapter();
				Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
				return Optional.of(new AdaptedData.Builder()
						.withData(getDataFromCursor(adapter, cursor))
						.withOpaqueToken(Integer.valueOf(position))
						.build());
			}
		}
		return Optional.absent();
	}

	@Override
	public void makeDataRenderedWithinAdapterView(AdapterView<? extends Adapter> adapterView, AdaptedData data) {
		AdapterViewProtocols.standardProtocol().makeDataRenderedWithinAdapterView(adapterView, data);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isDataRenderedWithinAdapterView(AdapterView<? extends Adapter> adapterView, AdaptedData adaptedData) {
		return AdapterViewProtocols.standardProtocol().isDataRenderedWithinAdapterView(adapterView, adaptedData);
	}
}
