package com.expedia.bookings.utils;

import java.io.IOException;
import java.math.BigDecimal;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.payment.LoyaltyEarnInfo;
import com.expedia.bookings.data.payment.PointsEarnInfo;
import com.expedia.bookings.data.payment.PriceEarnInfo;
import com.expedia.bookings.data.pos.PointOfSale;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.mobiata.android.util.SettingUtils;

public class ShopWithPointsFlightsUtil {


	public static LoyaltyEarnInfo getLoyaltyEarnInfo(JsonReader reader) throws IOException {
		LoyaltyEarnInfo loyaltyEarnInfo = null;

		expectToken(reader, JsonToken.BEGIN_OBJECT);
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			if ("earn".equals(reader.nextName())) {
				loyaltyEarnInfo = getEarnInfo(reader);
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		return loyaltyEarnInfo;
	}

	private static LoyaltyEarnInfo getEarnInfo(JsonReader reader) throws IOException {
		LoyaltyEarnInfo loyaltyEarnInfo = null;

		expectToken(reader, JsonToken.BEGIN_OBJECT);
		reader.beginObject();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			String name = reader.nextName();
			if ("price".equals(name)) {
				Money base = null;
				Money bonus = null;
				Money total = null;

				expectToken(reader, JsonToken.BEGIN_OBJECT);
				reader.beginObject();
				while (!reader.peek().equals(JsonToken.END_OBJECT)) {
					String pointsType = reader.nextName();
					if ("base".equals(pointsType)) {
						base = getMoneyFromPrice(reader);
					}
					else if ("bonus".equals(pointsType)) {
						bonus = getMoneyFromPrice(reader);
					}
					else if ("total".equals(pointsType)) {
						total = getMoneyFromPrice(reader);
					}
					else {
						reader.skipValue();
					}
				}
				loyaltyEarnInfo = new LoyaltyEarnInfo(null, new PriceEarnInfo(base, bonus, total));
				reader.endObject();
			}
			else if ("points".equals(name)) {
				int total = 0;
				int bonus = 0;
				int base = 0;
				expectToken(reader, JsonToken.BEGIN_OBJECT);
				reader.beginObject();
				while (!reader.peek().equals(JsonToken.END_OBJECT)) {
					String pointsType = reader.nextName();
					if ("base".equals(pointsType)) {
						base = reader.nextInt();
					}
					else if ("bonus".equals(pointsType)) {
						bonus = reader.nextInt();
					}
					else if ("total".equals(pointsType)) {
						total = reader.nextInt();
					}
					else {
						reader.skipValue();
					}
				}
				loyaltyEarnInfo = new LoyaltyEarnInfo(new PointsEarnInfo(base, bonus, total),
					null);

				reader.endObject();
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();

		return loyaltyEarnInfo;
	}

	private static Money getMoneyFromPrice(JsonReader reader) throws IOException {
		expectToken(reader, JsonToken.BEGIN_OBJECT);
		reader.beginObject();
		Money money = new Money();
		while (!reader.peek().equals(JsonToken.END_OBJECT)) {
			String priceFormatType = reader.nextName();
			if ("formattedPrice".equals(priceFormatType)) {
				money.formattedPrice = reader.nextString();
			}
			else if ("currencyCode".equals(priceFormatType)) {
				money.currencyCode = reader.nextString();
			}
			else if ("formattedWholePrice".equals(priceFormatType)) {
				money.formattedWholePrice = reader.nextString();
			}
			else if ("amount".equals(priceFormatType)) {
				money.amount = new BigDecimal(reader.nextDouble());
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return money;
	}

	private static void expectToken(JsonReader reader, JsonToken expectedToken) throws IOException {
		if (!reader.peek().equals(expectedToken)) {
			throw new RuntimeException("Expected " + expectedToken + ", got " + reader.peek());
		}
	}

	public static CharSequence getEarnInfoTextToDisplay(Context context, FlightTrip trip) {
		LoyaltyEarnInfo earnInfo = trip.getEarnInfo();
		if (earnInfo != null) {
			CharSequence earnInfoTextToDisplay = com.expedia.bookings.extensions.LoyaltyEarnInfoExtensionsKt.getEarnMessage(earnInfo, context);
			if (Strings.isNotEmpty(earnInfoTextToDisplay)) {
				return earnInfoTextToDisplay;
			}
		}

		return null;
	}

	public static boolean isShopWithPointsEnabled(Context context) {
		if (ExpediaBookingApp.isInstrumentation()) {
			return true;
		}
		if (BuildConfig.DEBUG && SettingUtils.get(context, R.string.preference_swp_flights_earn_messaging, false)) {
			return true;
		}
		return PointOfSale.getPointOfSale().isEarnMessageEnabledForFlights();
	}
}
