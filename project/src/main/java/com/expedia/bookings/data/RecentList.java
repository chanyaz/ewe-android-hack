package com.expedia.bookings.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.util.IoUtils;

/**
 * Easy method for storing/retrieving a list of JSONables.  Used for
 * retrieving recently used airports and airlines.  Note that you
 * cannot put duplicate strings inside - if a duplicate is found,
 * it's pushed to the front.
 *
 * NOTE: The JSONable must implement a correct version of equals() for
 * this to work properly!
 */
public class RecentList<E extends JSONable> {
	private List<E> mList;

	private int mMaxItems;

	private final Class<E> mClass;

	/**
	 * Creates an empty recent search list with max items of 5
	 */
	public RecentList(Class<E> cls) {
		mClass = cls;
		mList = new ArrayList<E>(5);
		mMaxItems = 5;
	}

	public RecentList(Class<E> cls, Context context, String filename, int maxItems) {
		this(cls);

		mMaxItems = maxItems;

		loadList(context, filename, true);
	}

	public void setMaxItems(int maxItems) {
		mMaxItems = maxItems;
	}

	public void addItem(E item) {
		// First, determine if the item is already in the list
		int toDelete = -1;
		for (int a = 0; a < mList.size(); a++) {
			if (item.equals(mList.get(a))) {
				toDelete = a;
				break;
			}
		}
		if (toDelete != -1) {
			mList.remove(toDelete);
		}

		// Add item to the front of the list
		mList.add(0, item);

		// Check that we haven't exceeded capacity
		if (mList.size() > mMaxItems) {
			mList.remove(mList.size() - 1);
		}
	}

	public List<E> getList() {
		return mList;
	}

	public void clear() {
		mList.clear();
	}

	public boolean isEmpty() {
		return mList.size() == 0;
	}

	public void saveList(Context context, String filename) {
		try {
			// Convert the list to JSON
			JSONObject obj = new JSONObject();
			obj.putOpt("maxItems", mMaxItems);
			JSONUtils.putJSONableList(obj, "list", mList);

			// Write to file
			IoUtils.writeStringToFile(filename, obj.toString(), context);
		}
		catch (Exception e) {
			Log.e("Could not save recent search list: " + e);
		}
	}

	public void loadList(Context context, String filename, boolean useCurrentMaxItems) {
		mList.clear();

		// Check that the file exists - if it does not, just start
		// a new list
		File file = context.getFileStreamPath(filename);
		if (!file.exists() || !file.canRead()) {
			Log.i("Could not find usable search list file at " + filename + ", creating new one.");
			return;
		}

		try {
			String str = IoUtils.readStringFromFile(filename, context);
			JSONObject obj = new JSONObject(str);

			if (!useCurrentMaxItems) {
				mMaxItems = obj.optInt("maxItems");
			}

			mList = JSONUtils.getJSONableList(obj, "list", mClass);

			if (mList.size() > mMaxItems) {
				mList = mList.subList(0, mMaxItems);
			}
		}
		catch (Exception e) {
			Log.e("Could not save recent search list: " + e);
		}
	}
}
