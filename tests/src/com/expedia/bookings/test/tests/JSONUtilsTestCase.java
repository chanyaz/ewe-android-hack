package com.expedia.bookings.test.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.test.AndroidTestCase;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class JSONUtilsTestCase extends AndroidTestCase {

	private static final String KEY = "key";

	private static final String KEY_NULL = "key_null";

	//////////////////////////////////////////////////////////////////////////
	// JSONable implementation for testing

	public static class MyJSONable implements JSONable {

		private boolean mBool;
		private int mInt;

		public MyJSONable() {
			Random rand = new Random();
			mBool = rand.nextBoolean();
			mInt = rand.nextInt();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof MyJSONable)) {
				return false;
			}

			MyJSONable other = (MyJSONable) o;
			return this.mBool == other.mBool && this.mInt == other.mInt;
		}

		@Override
		public JSONObject toJson() {
			try {
				JSONObject obj = new JSONObject();
				obj.putOpt("bool", mBool);
				obj.putOpt("int", mInt);
				return obj;
			}
			catch (JSONException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean fromJson(JSONObject obj) {
			mBool = obj.optBoolean("bool");
			mInt = obj.optInt("int");
			return true;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Tests

	// TODO: TEST NULL CONDITIONS

	public void testJSONablesInIntents() {
		Intent intent = new Intent();
		MyJSONable orig = new MyJSONable();

		// Test round trip of JSONable
		// TODO: Put into intent using JSONUtils method
		intent.putExtra(KEY, orig.toJson().toString());
		MyJSONable back = JSONUtils.parseJSONableFromIntent(intent, KEY, MyJSONable.class);
		assertEquals(orig, back);

		// Test null insertion not inserting anything
		// TODO: Put into Intent using JSONUtils method
		assertFalse(intent.hasExtra(KEY_NULL));

		// Test null retrieval returning null
		back = JSONUtils.parseJSONableFromIntent(intent, KEY_NULL, MyJSONable.class);

		assertNull(back);
	}

	public void testJSONInIntents() {
		Intent intent = new Intent();
		JSONObject orig = new MyJSONable().toJson();

		// Test round trip of JSONObject
		// TODO: Put into intent using JSONUtils method
		intent.putExtra(KEY, orig.toString());
		JSONObject back = JSONUtils.parseJSONObjectFromIntent(intent, KEY);
		assertEquals(orig.toString(), back.toString());

		// Test null insertion not inserting anything
		// TODO: Put into intent using JSONUtils method
		assertFalse(intent.hasExtra(KEY_NULL));

		// Test null retrieval returning null
		back = JSONUtils.parseJSONObjectFromIntent(intent, KEY_NULL);
		assertNull(back);
	}

	public void testJSONablesInBundles() {
		Bundle bundle = new Bundle();
		MyJSONable orig = new MyJSONable();

		// Test round trip of JSONable
		JSONUtils.putJSONable(bundle, KEY, orig);
		MyJSONable back = JSONUtils.getJSONable(bundle, KEY, MyJSONable.class);
		assertEquals(orig, back);

		// Test null insertion not inserting anything
		JSONUtils.putJSONable(bundle, KEY_NULL, null);
		assertFalse(bundle.containsKey(KEY_NULL));

		// Test null retrieval returning null
		back = JSONUtils.getJSONable(bundle, KEY_NULL, MyJSONable.class);
		assertNull(back);
	}

	public void testJSONableListInBundle() {
		// Test round trip of JSONable list
		List<MyJSONable> list = new ArrayList<JSONUtilsTestCase.MyJSONable>();
		for (int a = 0; a < 10; a++) {
			list.add(new MyJSONable());
		}

		Bundle bundle = new Bundle();
		JSONUtils.putJSONableList(bundle, KEY, list);
		List<MyJSONable> back = (List<MyJSONable>) JSONUtils.getJSONableList(bundle, KEY, MyJSONable.class);

		assertNotNull(back);
		assertEquals(list.size(), back.size());
		for (int a = 0; a < list.size(); a++) {
			assertEquals(list.get(a), back.get(a));
		}

		// Test null insertion not inserting anything
		JSONUtils.putJSONableList(bundle, KEY_NULL, null);
		assertFalse(bundle.containsKey(KEY_NULL));

		// Test null retrieval returning null
		back = (List<MyJSONable>) JSONUtils.getJSONableList(bundle, KEY_NULL, MyJSONable.class);
		assertNull(back);
	}

	public void testJSONableListInJSON() throws JSONException {
		// Test round trip of JSONable list
		List<MyJSONable> list = new ArrayList<JSONUtilsTestCase.MyJSONable>();
		for (int a = 0; a < 10; a++) {
			list.add(new MyJSONable());
		}

		JSONObject obj = new JSONObject();
		JSONUtils.putJSONableList(obj, KEY, list);
		List<MyJSONable> back = (List<MyJSONable>) JSONUtils.getJSONableList(obj, KEY, MyJSONable.class);

		assertNotNull(back);
		assertEquals(list.size(), back.size());
		for (int a = 0; a < list.size(); a++) {
			assertEquals(list.get(a), back.get(a));
		}

		// Test null insertion not inserting anything
		JSONUtils.putJSONableList(obj, KEY_NULL, null);
		assertFalse(obj.has(KEY_NULL));

		// Test null retrieval returning null
		back = (List<MyJSONable>) JSONUtils.getJSONableList(obj, KEY_NULL, MyJSONable.class);
		assertNull(back);
	}

	public void testJSONObjectInBundle() {
		Bundle bundle = new Bundle();
		JSONObject orig = new MyJSONable().toJson();

		// Test round trip of JSONObject
		// TODO: Add method for inserting JSONObject into Bundle
		bundle.putString(KEY, orig.toString());
		JSONObject back = JSONUtils.parseJSONObjectFromBundle(bundle, KEY);
		assertEquals(orig.toString(), back.toString());

		// Test null insertion not inserting anything
		// TODO: Add method for inserting JSONObject into Bundle
		assertFalse(bundle.containsKey(KEY_NULL));

		// Test null retrieval returning null
		back = JSONUtils.parseJSONObjectFromBundle(bundle, KEY_NULL);
		assertNull(back);
	}

	public void testJSONablesInJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		MyJSONable orig = new MyJSONable();

		// Test round trip of JSONObject
		JSONUtils.putJSONable(obj, KEY, orig);
		MyJSONable back = (MyJSONable) JSONUtils.getJSONable(obj, KEY, MyJSONable.class);
		assertEquals(orig, back);

		// Test null insertion not inserting anything
		JSONUtils.putJSONable(obj, KEY_NULL, null);
		assertFalse(obj.has(KEY_NULL));

		// Test null retrieval returning null
		back = (MyJSONable) JSONUtils.getJSONable(obj, KEY_NULL, MyJSONable.class);
		assertNull(back);
	}

	public void testIntListInJSON() throws JSONException {
		JSONObject obj = new JSONObject();

		// Test round trip
		List<Integer> list = new ArrayList<Integer>();
		for (int a = 0; a < 10; a++) {
			list.add(a);
		}

		JSONUtils.putIntList(obj, KEY, list);
		List<Integer> back = JSONUtils.getIntList(obj, KEY);
		assertNotNull(back);
		assertEquals(list.size(), back.size());
		for (int a = 0; a < list.size(); a++) {
			assertEquals(list.get(a), back.get(a));
		}

		// Test null insertion not inserting anything
		JSONUtils.putIntList(obj, KEY_NULL, null);
		assertFalse(obj.has(KEY_NULL));

		// Test null retrieval returning null
		back = JSONUtils.getIntList(obj, KEY_NULL);
		assertNull(back);
	}

	public void testStringListInJSON() throws JSONException {
		JSONObject obj = new JSONObject();

		// Test round trip
		List<String> list = new ArrayList<String>();
		for (int a = 0; a < 10; a++) {
			list.add("String " + a);
		}

		JSONUtils.putStringList(obj, KEY, list);
		List<String> back = JSONUtils.getStringList(obj, KEY);
		assertNotNull(back);
		assertEquals(list.size(), back.size());
		for (int a = 0; a < list.size(); a++) {
			assertEquals(list.get(a), back.get(a));
		}

		// Test null insertion not inserting anything
		JSONUtils.putIntList(obj, KEY_NULL, null);
		assertFalse(obj.has(KEY_NULL));

		// Test null retrieval returning null
		back = JSONUtils.getStringList(obj, KEY_NULL);
		assertNull(back);
	}

	public void testStringMapInJSON() throws JSONException {
		JSONObject obj = new JSONObject();

		// Test round trip
		Map<String, String> map = new HashMap<String, String>();
		for (int a = 0; a < 10; a++) {
			map.put("key" + a, "val" + a);
		}

		JSONUtils.putStringMap(obj, KEY, map);
		Map<String, String> back = JSONUtils.getStringMap(obj, KEY);
		assertEquals(map, back);

		// Test null insertion not inserting anything
		JSONUtils.putStringMap(obj, KEY_NULL, null);
		assertFalse(obj.has(KEY_NULL));

		// Test null retrieval returning null
		back = JSONUtils.getStringMap(obj, KEY_NULL);
		assertNull(back);
	}
}
