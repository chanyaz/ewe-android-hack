package com.mobiata.android;

import java.util.List;

import android.content.Context;

/**
 * These utilities all format data into human-readable strings.
 */
public class FormatUtils {

	public enum Conjunction {
		AND(R.string.and),
		OR(R.string.or),
		NONE(0);

		private int mResourceId;

		private Conjunction(int resourceId) {
			mResourceId = resourceId;
		}

		public int getResourceId() {
			return mResourceId;
		}
	}

	public static String series(Context context, List<String> elements, String delimiter,
			Conjunction serialConjunction) {
		if (elements == null) {
			return null;
		}

		if (serialConjunction == null) {
			serialConjunction = Conjunction.NONE;
		}

		int size = elements.size();
		if (size == 1) {
			return elements.get(0);
		}
		else if (size == 2 && serialConjunction != Conjunction.NONE) {
			return elements.get(0) + " " + context.getString(serialConjunction.getResourceId()) + " " + elements.get(1);
		}

		StringBuilder sb = new StringBuilder();
		for (int a = 0; a < size; a++) {
			if (a > 0) {
				sb.append(delimiter + " ");
			}
			if (a + 1 == size && serialConjunction != Conjunction.NONE) {
				sb.append(context.getString(serialConjunction.getResourceId()) + " ");
			}
			sb.append(elements.get(a));
		}

		return sb.toString();
	}
}
