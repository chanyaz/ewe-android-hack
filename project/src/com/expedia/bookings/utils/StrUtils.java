package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;
import android.location.Address;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.mobiata.android.LocationServices;

public class StrUtils {

	/**
	 * Formats the display of how many adults and children are picked currently.
	 * This will display 0 adults or children.
	 */
	public static String formatGuests(Context context, int numAdults, int numChildren) {
		StringBuilder sb = new StringBuilder();
		Resources r = context.getResources();
		sb.append(r.getQuantityString(R.plurals.number_of_adults, numAdults, numAdults));

		if (numChildren > 0) {
			sb.append(", ");
			sb.append(r.getQuantityString(R.plurals.number_of_children, numChildren, numChildren));
		}

		return sb.toString();
	}

	public static String formatGuests(Context context, SearchParams searchParams) {
		return formatGuests(context, searchParams.getNumAdults(), searchParams.getNumChildren());
	}

	public static String formatAddressStreet(Location location) {
		return formatAddress(location, F_STREET_ADDRESS);
	}

	public static String formatAddressCity(Location location) {
		return formatAddress(location, F_CITY + F_STATE_CODE + F_POSTAL_CODE);
	}

	public static String formatAddress(Location location) {
		return formatAddress(location, F_STREET_ADDRESS + F_CITY + F_STATE_CODE + F_POSTAL_CODE);
	}

	public static String formatAddressShort(Location location) {
		return formatAddress(location, F_CITY + F_STATE_CODE + F_COUNTRY_CODE);
	}

	public static final int F_STREET_ADDRESS = 1;
	public static final int F_CITY = 2;
	public static final int F_STATE_CODE = 4;
	public static final int F_POSTAL_CODE = 8;
	public static final int F_COUNTRY_CODE = 16;

	private static final int SEPARATOR_SPACE = 1;
	private static final int SEPARATOR_COMMA = 2;
	private static final int SEPARATOR_NEWLINE = 3;

	/**
	 * This is an all-purpose address formatter.  Provide flags for what
	 * information you want to display (if it is available)
	 */
	public static String formatAddress(Location location, int flags) {
		List<Object> tokens = new ArrayList<Object>();

		if ((flags & F_STREET_ADDRESS) != 0) {
			List<String> streetAddress = location.getStreetAddress();
			if (streetAddress != null) {
				int len = streetAddress.size();
				for (int a = 0; a < len; a++) {
					if (a != 0) {
						tokens.add(SEPARATOR_COMMA);
					}
					tokens.add(streetAddress.get(a));
				}

				tokens.add(SEPARATOR_NEWLINE);
			}
		}

		if ((flags & F_CITY) != 0) {
			tokens.add(location.getCity());
		}

		String stateCode = location.getStateCode();
		if ((flags & F_STATE_CODE) != 0 && stateCode != null && stateCode.length() > 0) {
			tokens.add(SEPARATOR_COMMA);
			tokens.add(stateCode);
		}

		String postalCode = location.getPostalCode();
		if ((flags & F_POSTAL_CODE) != 0 && postalCode != null && postalCode.length() > 0) {
			tokens.add(SEPARATOR_SPACE);
			tokens.add(postalCode);
		}

		String countryCode = location.getCountryCode();
		if ((flags & F_COUNTRY_CODE) != 0 && countryCode != null && countryCode.length() > 0) {
			tokens.add(SEPARATOR_COMMA);
			tokens.add(countryCode);
		}

		// Parse through the tokens, de-duplicating separators and removing missing data
		StringBuilder sb = new StringBuilder();
		int lastSeparator = 0;
		for (Object token : tokens) {
			if (token instanceof Integer) {
				if (lastSeparator != 0) {
					continue;
				}
				else {
					lastSeparator = (Integer) token;
				}
			}
			else if (token != null) {
				if (lastSeparator != 0) {
					switch (lastSeparator) {
					case SEPARATOR_SPACE:
						sb.append(" ");
						break;
					case SEPARATOR_COMMA:
						sb.append(", ");
						break;
					case SEPARATOR_NEWLINE:
						sb.append("\n");
						break;
					}
				}

				sb.append((String) token);
				lastSeparator = 0;
			}
		}

		return sb.toString().trim();
	}

	public static CharSequence[] formatAddresses(List<Address> addresses) {
		final int size = addresses.size();
		final CharSequence[] freeformLocations = new CharSequence[addresses.size()];
		for (int i = 0; i < size; i++) {
			String formattedAddress = LocationServices.formatAddress(addresses.get(i));
			formattedAddress = formattedAddress.replace(", USA", "");
			freeformLocations[i] = formattedAddress;
		}
		return freeformLocations;
	}

	public static String removeUSAFromAddress(Address address) {
		String formattedAddress = LocationServices.formatAddress(address);
		formattedAddress = formattedAddress.replace(", USA", "");
		return formattedAddress;
	}

	public static String formatHotelPrice(Money money) {
		return money.getFormattedMoney(Money.F_NO_DECIMAL + Money.F_ROUND_DOWN);
	}

	public static String formatHotelPrice(Money money, String currencyCode) {
		return money.getFormattedMoney(Money.F_NO_DECIMAL + Money.F_ROUND_DOWN, currencyCode);
	}
	
	public static List<String> getImageUrls(Property property) {
		List<String> urls = new ArrayList<String>(property.getMediaCount());
		Set<String> usedUrls = new HashSet<String>();
		
		if(property.getMediaList() == null) {
			return urls;
		}
		
		for (Media media : property.getMediaList()) {
			String url = media.getUrl();
			if (!usedUrls.contains(url)) {
				urls.add(url);
				usedUrls.add(url);
			}
		}
		return urls;
	}
	
	public static List<Media> getUniqueMediaList(Property property) {
		List<Media> uniqueMediaList = new ArrayList<Media>(property.getMediaCount());
		Set<Media> usedMedia = new HashSet<Media>();
		
		if(property.getMediaList() == null) {
			return uniqueMediaList;
		}
		
		for(Media media : property.getMediaList()) {
			if(!usedMedia.contains(media)) {
				uniqueMediaList.add(media);
				usedMedia.add(media);
			}
		}
		
		return uniqueMediaList;
	}
}
