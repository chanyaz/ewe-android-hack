package com.expedia.bookings.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class BedType implements JSONable {

	public BedTypeId mBedTypeId;
	public String mBedTypeDescription;

	public BedType() {
		// Default constructor for JSONable
	}

	public BedType(BedTypeId id, String description) {
		mBedTypeId = id;
		mBedTypeDescription = description;
	}

	public BedTypeId getBedTypeId() {
		return mBedTypeId;
	}

	public String getBedTypeDescription() {
		return mBedTypeDescription;
	}

	//////////////////////////////////////////////////////////////////////////
	// BedTypeId

	/*
	 * This enum represents the different bed types
	 * that can be returned from EAN. Note that
	 * some bed types have multiple ids pointing to them.
	 * The mappings were picked up from the following documentation:
	 * http://developer.ean.com/general_info/BedTypes
	 *
	 */
		enum BedTypeId {

		/*
		 * King bed types in order of priority
		 */
		ONE_KING_BED(new String[] { "KG", "4", "14" }),
		TWO_KING_BEDS(new String[] { "2KG", "22" }),
		THREE_KING_BEDS(new String[] { "56" }),
		FOUR_KING_BEDS(new String[] { "59" }),
		ONE_KING_ONE_SOFA(new String[] { "67" }),

		/*
		 * Queen bed types in order of priority
		 */
		ONE_QUEEN_BED(new String[] { "QN", "3", "15" }),
		TWO_QUEEN_BEDS(new String[] { "2QN", "7", "23" }),
		THREE_QUEEN_BEDS(new String[] { "57" }),
		FOUR_QUEEN_BEDS(new String[] { "60" }),
		ONE_QUEEN_ONE_SOFA(new String[] { "68" }),

		/*
		 * Double beds in order of priority
		 */
		ONE_DOUBLE_BED(new String[] { "DD", "2", "13" }),
		TWO_DOUBLE_BEDS(new String[] { "2DD", "6", "21" }),
		ONE_DOUBLE_ONE_SINGLE(new String[] { "63" }),
		ONE_DOUBLE_TWO_SINGLES(new String[] { "66" }),

		/*
		 * Twin beds in order of priority
		 */
		ONE_TWIN_BED(new String[] { "TW", "18" }),
		TWO_TWIN_BEDS(new String[] { "2TW", "5", "25" }),
		THREE_TWIN_BEDS(new String[] { "30" }),
		FOUR_TWIN_BEDS(new String[] { "34" }),

		/*
		 * Full beds in order of priority
		 */
		ONE_FULL_BED(new String[] { "46" }),
		TWO_FULL_BEDS(new String[] { "47" }),

		/*
		 * Single beds in order of priority
		 */
		ONE_SINGLE_BED(new String[] { "42" }),
		TWO_SINGLE_BEDS(new String[] { "43" }),
		THREE_SINGLE_BEDS(new String[] { "44" }),
		FOUR_SINGLE_BEDS(new String[] { "45" }),

		/*
		 * Remaining beds in order of priority
		 */
		ONE_BED(new String[] { "40" }),
		TWO_BEDS(new String[] { "41" }),
		ONE_TRUNDLE_BED(new String[] { "48" }),
		ONE_MURPHY_BED(new String[] { "49" }),
		ONE_BUNK_BED(new String[] { "50" }),
		ONE_SLEEPER_SOFA(new String[] { "51" }),
		TWO_SLEEPER_SOFAS(new String[] { "52" }),
		THREE_SLEEPER_SOFAS(new String[] { "53" }),
		JAPENESE_FUTON(new String[] { "54" }),
		THREE_BEDS(new String[] { "55" }),
		FOUR_BEDS(new String[] { "58" }),

		/*
		 * Handles all unknown bed type cases
		 */
		UNKNOWN(new String[] {});

		private Set<String> mIds;

		BedTypeId(String[] ids) {
			mIds = new HashSet<>();
			Collections.addAll(mIds, ids);
		}

		public static BedTypeId fromStringId(String id) {
			for (BedTypeId bedTypeId : values()) {
				if (bedTypeId.mIds.contains(id)) {
					return bedTypeId;
				}
			}
			return UNKNOWN;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			JSONUtils.putEnum(obj, "bedTypeId", mBedTypeId);
			obj.putOpt("bedTypeDescription", mBedTypeDescription);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mBedTypeId = JSONUtils.getEnum(obj, "bedTypeId", BedTypeId.class);
		mBedTypeDescription = obj.optString("bedTypeDescription", null);
		return true;
	}

}
