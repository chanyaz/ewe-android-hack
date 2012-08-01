package com.expedia.bookings.data.pos;

import java.util.HashMap;
import java.util.Map;

public enum PointOfSale {
	UNKNOWN(0),
	ARGENTINA(1),
	AUSTRIA(2),
	AUSTRALIA(3),
	BELGIUM(4),
	BRAZIL(5),
	CANADA(6),
	GERMANY(7),
	DENMARK(8),
	SPAIN(9),
	FRANCE(10),
	HONG_KONG(11),
	INDONESIA(12),
	IRELAND(13),
	INDIA(14),
	ITALY(15),
	JAPAN(16),
	SOUTH_KOREA(17),
	MEXICO(18),
	MALAYSIA(19),
	NETHERLANDS(20),
	NORWAY(21),
	NEW_ZEALND(22),
	PHILIPPINES(23),
	SWEDEN(24),
	SINGAPORE(25),
	THAILAND(26),
	TAIWAN(27),
	UNITED_KINGDOM(28),
	UNITED_STATES(29),
	VIETNAM(30);

	private int mId;

	private PointOfSale(int id) {
		mId = id;
	}

	public int getId() {
		return mId;
	}

	private static Map<Integer, PointOfSale> sIdToPOS;

	public static PointOfSale getPointOfSaleFromId(int id) {
		if (sIdToPOS == null) {
			sIdToPOS = new HashMap<Integer, PointOfSale>();
			for (PointOfSale pos : PointOfSale.values()) {
				sIdToPOS.put(pos.mId, pos);
			}
		}

		return sIdToPOS.get(id);
	}
}
