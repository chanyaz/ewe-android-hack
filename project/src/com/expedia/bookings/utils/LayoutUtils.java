package com.expedia.bookings.utils;

import java.text.DecimalFormat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.actionbarsherlock.internal.ActionBarSherlockCompat;
import com.actionbarsherlock.internal.ResourcesCompat;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelFilter.SearchRadius;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Property.Amenity;
import com.expedia.bookings.data.Rate;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.ViewUtils;

public class LayoutUtils {

	/**
	 * If you set a background resource it can sometimes blow away the old padding on the
	 * View.  This method ensures that both the old padding from the View and the new
	 * padding that may have been introduced (via InsetDrawable) are preserved after
	 * setting a background resource.
	 *
	 * @param v
	 * @param resId
	 */
	public static void setBackgroundResource(View v, int resId) {
		int left = v.getPaddingLeft();
		int top = v.getPaddingTop();
		int right = v.getPaddingRight();
		int bottom = v.getPaddingBottom();

		v.setBackgroundResource(resId);

		v.setPadding(v.getPaddingLeft() + left, v.getPaddingTop() + top, v.getPaddingRight() + right,
				v.getPaddingBottom() + bottom);
	}

	/**
	 * Convenience method for modifying the padding of a View instead of completely
	 * resetting it.
	 */
	public static void addPadding(View v, int left, int top, int right, int bottom) {
		v.setPadding(v.getPaddingLeft() + left, v.getPaddingTop() + top, v.getPaddingRight() + right,
				v.getPaddingBottom() + bottom);
	}

	public static float getSaleTextSize(Context context) {
		TextPaint textPaint = new TextPaint();
		textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		return ViewUtils.getTextSizeForMaxLines(context, context.getString(R.string.percent_off_template, 50.0),
				textPaint, 58, 1, 11.5f, 1);
	}

	public static Drawable getDividerDrawable(Context context) {
		BitmapDrawable drawable = new BitmapDrawable(BitmapFactory.decodeResource(context.getResources(),
				R.drawable.list_stroke_shadow));
		drawable.setTileModeY(TileMode.REPEAT);
		return drawable;
	}

	public static void configureRadiusFilterLabels(Context context, ViewGroup radiusFilterGroup, HotelFilter filter) {
		// The radius filter buttons depend on whether the user's locale leans
		// towards miles or kilometers.  For now, we just use US == miles,
		// everything else == kilometers (pending a better way to determine this).
		DistanceUnit distanceUnit = (filter != null) ? filter.getDistanceUnit() : DistanceUnit.getDefaultDistanceUnit();
		int distanceStrId = (distanceUnit == DistanceUnit.MILES) ? R.string.filter_distance_miles_template
				: R.string.filter_distance_kilometers_template;

		DecimalFormat df = new DecimalFormat("#.#");
		((RadioButton) radiusFilterGroup.findViewById(R.id.radius_small_button)).setText(context.getString(
				distanceStrId, df.format(SearchRadius.SMALL.getRadius(distanceUnit))));
		((RadioButton) radiusFilterGroup.findViewById(R.id.radius_medium_button)).setText(context.getString(
				distanceStrId, df.format(SearchRadius.MEDIUM.getRadius(distanceUnit))));
		((RadioButton) radiusFilterGroup.findViewById(R.id.radius_large_button)).setText(context.getString(
				distanceStrId, df.format(SearchRadius.LARGE.getRadius(distanceUnit))));
	}

	private static final float MAX_AMENITY_TEXT_WIDTH_IN_DP = 60.0f;

	private static final class AmenityInfo {
		public Amenity amenity;
		public int resId;
		public Amenity[] aliases;

		public AmenityInfo(Amenity amenity, int resId, Amenity... aliases) {
			this.amenity = amenity;
			this.resId = resId;
			this.aliases = aliases;
		}
	}

	// These will be displayed in the order they're in this array
	private static final AmenityInfo[] sAmenityInfo = new AmenityInfo[] {
		new AmenityInfo(Amenity.POOL, R.drawable.ic_amenity_pool, Amenity.POOL_INDOOR, Amenity.POOL_OUTDOOR),
		new AmenityInfo(Amenity.INTERNET, R.drawable.ic_amenity_internet),
		new AmenityInfo(Amenity.BREAKFAST, R.drawable.ic_amenity_breakfast),
		new AmenityInfo(Amenity.PARKING, R.drawable.ic_amenity_parking, Amenity.EXTENDED_PARKING, Amenity.FREE_PARKING),
		new AmenityInfo(Amenity.PETS_ALLOWED, R.drawable.ic_amenity_pets),
		new AmenityInfo(Amenity.RESTAURANT, R.drawable.ic_amenity_restaurant),
		new AmenityInfo(Amenity.FITNESS_CENTER, R.drawable.ic_amenity_fitness_center),
		new AmenityInfo(Amenity.ROOM_SERVICE, R.drawable.ic_amenity_room_service),
		new AmenityInfo(Amenity.SPA, R.drawable.ic_amenity_spa),
		new AmenityInfo(Amenity.BUSINESS_CENTER, R.drawable.ic_amenity_business),
		new AmenityInfo(Amenity.FREE_AIRPORT_SHUTTLE, R.drawable.ic_amenity_airport_shuttle),
		new AmenityInfo(Amenity.ACCESSIBLE_BATHROOM, R.drawable.ic_amenity_accessible_bathroom),
		new AmenityInfo(Amenity.HOT_TUB, R.drawable.ic_amenity_hot_tub),
		new AmenityInfo(Amenity.JACUZZI, R.drawable.ic_amenity_jacuzzi),
		new AmenityInfo(Amenity.WHIRLPOOL_BATH, R.drawable.ic_amenity_whirl_pool),
		new AmenityInfo(Amenity.KITCHEN, R.drawable.ic_amenity_kitchen),
		new AmenityInfo(Amenity.KIDS_ACTIVITIES, R.drawable.ic_amenity_children_activities),
		new AmenityInfo(Amenity.BABYSITTING, R.drawable.ic_amenity_baby_sitting),
		new AmenityInfo(Amenity.ACCESSIBLE_PATHS, R.drawable.ic_amenity_accessible_ramp),
		new AmenityInfo(Amenity.ROLL_IN_SHOWER, R.drawable.ic_amenity_accessible_shower),
		new AmenityInfo(Amenity.HANDICAPPED_PARKING, R.drawable.ic_amenity_handicap_parking),
		new AmenityInfo(Amenity.IN_ROOM_ACCESSIBILITY, R.drawable.ic_amenity_accessible_room),
		new AmenityInfo(Amenity.DEAF_ACCESSIBILITY_EQUIPMENT, R.drawable.ic_amenity_deaf_access),
		new AmenityInfo(Amenity.BRAILLE_SIGNAGE, R.drawable.ic_amenity_braille_signs)
	};

	public static void addAmenities(Context context, Property property, ViewGroup amenitiesContainer) {
		for (AmenityInfo ai : sAmenityInfo) {
			if (property.hasAmenity(ai.amenity) || property.hasAnyAmenity(ai.aliases)) {
				addAmenity(context, amenitiesContainer, ai.amenity, ai.resId);
			}
		}
	}

	private static void addAmenity(Context context, ViewGroup amenitiesTable, Amenity amenity, int iconResourceId) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		TextView amenityTextView = (TextView) layoutInflater.inflate(R.layout.snippet_amenity, amenitiesTable, false);

		String amenityStr = context.getString(amenity.getStrId());

		// measure the length of the amenity string and determine whether it is short enough
		// to fit within the acceptable width. If not, reduce the font size in an attempt to
		// get it to fit.
		float acceptableWidth = context.getResources().getDisplayMetrics().density * MAX_AMENITY_TEXT_WIDTH_IN_DP;
		float measuredWidthOfStr = amenityTextView.getPaint().measureText(context.getString(amenity.getStrId()));

		if (amenityStr.contains(" ") || measuredWidthOfStr > acceptableWidth) {
			amenityTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
					context.getResources().getDimension(R.dimen.amenity_text_size_small));
		}

		amenityTextView.setText(amenityStr);
		amenityTextView.setCompoundDrawablesWithIntrinsicBounds(0, iconResourceId, 0, 0);

		amenitiesTable.addView(amenityTextView);
	}

	public static String noHotelsFoundMessage(Context context) {
		StringBuilder sb = new StringBuilder();
		if (CalendarUtils.isSearchDateTonight(Db.getHotelSearch().getSearchParams())) {
			sb.append(context.getResources().getString(R.string.no_hotels_availiable_tonight));
		}
		else {
			sb.append(context.getResources().getString(R.string.no_hotels_availiable));
		}
		sb.append("\n");
		sb.append(context.getResources().getString(R.string.please_try_a_different_location_or_date));
		return sb.toString();
	}

	public static void layoutRoomLongDescription(Context context, Rate rate, TextView roomDetailsTextView) {
		CharSequence longDescription = rate.getRoomLongDescription();
		if (longDescription != null) {
			// Do a bit of formatting on it...
			longDescription = longDescription.toString().replace("<strong>", "").replace("</strong>", ". ")
					.replace(". .", ".");
			longDescription = Html.fromHtml(longDescription.toString());
		}
		else {
			longDescription = context.getString(R.string.error_room_type_nonexistant);
		}
		roomDetailsTextView.setText(longDescription);
	}

	// 10758: render this view on a software layer
	// to avoid the fuzziness of the saved section background
	@TargetApi(11)
	public static void sayNoToJaggies(View... views) {
		int sdkVersion = AndroidUtils.getSdkVersion();
		if (sdkVersion >= 11 && sdkVersion <= 13) {
			for (View v : views) {
				v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
			}
		}
	}

	// Use a condensed rows if the screen width is not large enough
	@TargetApi(13)
	public static boolean isScreenNarrow(Context context) {
		Configuration config = context.getResources().getConfiguration();
		if (AndroidUtils.getSdkVersion() >= 13) {
			return config.screenWidthDp <= 800;
		}
		return config.orientation == Configuration.ORIENTATION_PORTRAIT;
	}

	private static final int[] STYLEABLE_ACTION_BAR_SIZE = new int[] { android.R.attr.actionBarSize };

	public static int getActionBarSize(Context context) {
		TypedArray a = context.obtainStyledAttributes(null, R.styleable.SherlockActionBar, R.attr.actionBarStyle, 0);
		int heightRes = a.getResourceId(R.styleable.SherlockActionBar_height, 0);
		a.recycle();

		int actionBarHeight = 0;
		if (heightRes != 0) {
			actionBarHeight = (int) Math.round(context.getResources().getDimension(heightRes));
		}
		else {
			// Getting here indicates that we're not using ABS for the action bar;
			// get the height directly.  Use attributes (as height may not be set
			// yet on the action bar, depending on timing - F911).
			a = context.obtainStyledAttributes(null, STYLEABLE_ACTION_BAR_SIZE);
			actionBarHeight = a.getDimensionPixelSize(0, 0);
			a.recycle();
		}

		return actionBarHeight;
	}

	/**
	 * Adjusts the top and bottom padding of a View based on its Activity and state.
	 *
	 * This method makes a few assumptions, namely that you're setting things like the
	 * action bar/split's height and the uiOptions in the XML rather than dynamically.
	 * If you're doing it dynamically then you're on your own (but I can't see any use
	 * for a *more* dynamic version of this method at the moment).
	 *
	 * @param activity     the Activity who made have overlay
	 * @param rootView     the root view to add padding to
	 * @param hasMenuItems there appears to be no easy way to tell if the menu actually has any items,
	 *        so we use this variable to determine whether or not to adjust for bottom padding
	 */
	public static void adjustPaddingForOverlayMode(Activity activity, View rootView,
			boolean hasMenuItems) {
		TypedArray a = activity.getTheme().obtainStyledAttributes(R.styleable.SherlockTheme);
		boolean inOverlayMode = a.getBoolean(R.styleable.SherlockTheme_windowActionBarOverlay, false);
		a.recycle();

		if (inOverlayMode) {
			Resources res = activity.getResources();
			int extraTopPadding = 0;
			int extraBottomPadding = 0;

			// Get top padding
			a = activity.obtainStyledAttributes(null, R.styleable.SherlockActionBar,
					R.attr.actionBarStyle, 0);
			int heightRes = a.getResourceId(R.styleable.SherlockActionBar_height, 0);
			a.recycle();
			if (heightRes != 0) {
				extraTopPadding = (int) Math.round(res.getDimension(heightRes));
			}
			else {
				// Getting here indicates that we're not using ABS for the action bar;
				// get the height directly.  Use attributes (as height may not be set
				// yet on the action bar, depending on timing - F911).
				a = activity.obtainStyledAttributes(null, STYLEABLE_ACTION_BAR_SIZE);
				extraTopPadding = a.getDimensionPixelSize(0, 0);
				a.recycle();
			}

			// Get bottom padding (if in split action bar mode)
			if (needsBottomPaddingForOverlay(activity, hasMenuItems)) {
				a = activity.obtainStyledAttributes(null, R.styleable.SherlockActionBar,
						R.attr.actionBarSplitStyle, 0);
				heightRes = a.getResourceId(R.styleable.SherlockActionBar_height, 0);
				a.recycle();
				if (heightRes != 0) {
					extraBottomPadding = (int) Math.round(res.getDimension(heightRes));
				}
				else {
					// See comment above for explanation on this similar code
					a = activity.obtainStyledAttributes(null, STYLEABLE_ACTION_BAR_SIZE);
					extraBottomPadding = a.getDimensionPixelSize(0, 0);
					a.recycle();
				}
			}

			// Reset the padding with the additional top (and maybe bottom) padding
			rootView.setPadding(rootView.getPaddingLeft(), rootView.getPaddingTop() + extraTopPadding,
					rootView.getPaddingRight(), rootView.getPaddingBottom() + extraBottomPadding);
		}
	}

	public static boolean needsBottomPaddingForOverlay(Activity activity, boolean hasMenuItems) {
		if (hasMenuItems) {
			int uiOptions = ActionBarSherlockCompat.loadUiOptionsFromManifest(activity);
			boolean splitWhenNarrow = (uiOptions & ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW) != 0;
			return splitWhenNarrow
					&& ResourcesCompat.getResources_getBoolean(activity, R.bool.abs__split_action_bar_is_narrow);
		}
		return false;
	}
}
