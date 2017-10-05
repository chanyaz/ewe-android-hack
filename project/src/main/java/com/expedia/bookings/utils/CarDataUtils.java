package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCategory;

public class CarDataUtils {

	@SuppressWarnings("serial")
	private static final Map<CarCategory, Integer> CAR_TYPE_DESCRIPTION_MAP = new HashMap<CarCategory, Integer>() {
		{
			put(CarCategory.MINI, R.string.car_category_mini);
			put(CarCategory.ECONOMY, R.string.car_category_economy);
			put(CarCategory.COMPACT, R.string.car_category_compact);
			put(CarCategory.MIDSIZE, R.string.car_category_midsize);
			put(CarCategory.STANDARD, R.string.car_category_standard);
			put(CarCategory.FULLSIZE, R.string.car_category_fullsize);
			put(CarCategory.PREMIUM, R.string.car_category_premium);
			put(CarCategory.LUXURY, R.string.car_category_luxury);
			put(CarCategory.MINI_ELITE, R.string.car_category_mini_elite);
			put(CarCategory.ECONOMY_ELITE, R.string.car_category_economy_elite);
			put(CarCategory.COMPACT_ELITE, R.string.car_category_compact_elite);
			put(CarCategory.SPECIAL, R.string.car_category_special);
			put(CarCategory.MIDSIZE_ELITE, R.string.car_category_midsize_elite);
			put(CarCategory.STANDARD_ELITE, R.string.car_category_standard_elite);
			put(CarCategory.FULLSIZE_ELITE, R.string.car_category_fullsize_elite);
			put(CarCategory.PREMIUM_ELITE, R.string.car_category_premium_elite);
			put(CarCategory.LUXURY_ELITE, R.string.car_category_luxury_elite);
			put(CarCategory.OVERSIZE, R.string.car_category_oversize);
		}
	};

	@SuppressWarnings("serial")
	private static final Map<CarCategory, Integer> CAR_TYPE_SHARE_MSG_MAP = new HashMap<CarCategory, Integer>() {
		{
			put(CarCategory.MINI, R.string.share_template_short_car_type_mini);
			put(CarCategory.ECONOMY, R.string.share_template_short_car_type_economy);
			put(CarCategory.COMPACT, R.string.share_template_short_car_type_compact);
			put(CarCategory.MIDSIZE, R.string.share_template_short_car_type_midsize);
			put(CarCategory.STANDARD, R.string.share_template_short_car_type_standard);
			put(CarCategory.FULLSIZE, R.string.share_template_short_car_type_fullsize);
			put(CarCategory.PREMIUM, R.string.share_template_short_car_type_premium);
			put(CarCategory.LUXURY, R.string.share_template_short_car_type_luxury);
			put(CarCategory.SPECIAL, R.string.share_template_short_car_type_special);
			put(CarCategory.MINI_ELITE, R.string.share_template_short_car_type_mini_elite);
			put(CarCategory.ECONOMY_ELITE, R.string.share_template_short_car_type_economy_elite);
			put(CarCategory.COMPACT_ELITE, R.string.share_template_short_car_type_compact_elite);
			put(CarCategory.MIDSIZE_ELITE, R.string.share_template_short_car_type_midsize_elite);
			put(CarCategory.STANDARD_ELITE, R.string.share_template_short_car_type_standard_elite);
			put(CarCategory.FULLSIZE_ELITE, R.string.share_template_short_car_type_fullsize_elite);
			put(CarCategory.PREMIUM_ELITE, R.string.share_template_short_car_type_premium_elite);
			put(CarCategory.LUXURY_ELITE, R.string.share_template_short_car_type_luxury_elite);
			put(CarCategory.OVERSIZE, R.string.share_template_short_car_type_oversize);
		}
	};

	public static String getShareMessageFor(Context c, CarCategory category) {
		return c.getResources().getString(CAR_TYPE_SHARE_MSG_MAP.get(category));
	}

	public static String getCategoryStringFor(Context c, CarCategory category) {
		return c.getResources().getString(CAR_TYPE_DESCRIPTION_MAP.get(category));
	}
}
