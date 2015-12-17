package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Intent;
import android.os.Bundle;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

@RunWith(RobolectricRunner.class)
public class JSONUtilsTest {

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

	@Test
	public void testJSONInIntents() {
		Intent intent = new Intent();
		JSONObject orig = new MyJSONable().toJson();

		// Test null insertion not inserting anything
		JSONUtils.putJSONObject(intent, KEY, null);
		Assert.assertFalse(intent.hasExtra(KEY_NULL));
	}

	@Test
	public void testJSONablesInBundles() {
		Bundle bundle = new Bundle();
		MyJSONable orig = new MyJSONable();

		// Test round trip of JSONable
		JSONUtils.putJSONable(bundle, KEY, orig);
		MyJSONable back = JSONUtils.getJSONable(bundle, KEY, MyJSONable.class);
		Assert.assertEquals(orig, back);

		// Test null insertion not inserting anything
		JSONUtils.putJSONable(bundle, KEY_NULL, null);
		Assert.assertFalse(bundle.containsKey(KEY_NULL));

		// Test null retrieval returning null
		back = JSONUtils.getJSONable(bundle, KEY_NULL, MyJSONable.class);
		Assert.assertNull(back);
	}

	@Test
	public void testJSONableListInBundle() {
		// Test round trip of JSONable list
		List<MyJSONable> list = new ArrayList<JSONUtilsTest.MyJSONable>();
		for (int a = 0; a < 10; a++) {
			list.add(new MyJSONable());
		}

		Bundle bundle = new Bundle();
		JSONUtils.putJSONableList(bundle, KEY, list);
		List<MyJSONable> back = (List<MyJSONable>) JSONUtils.getJSONableList(bundle, KEY, MyJSONable.class);

		Assert.assertNotNull(back);
		Assert.assertEquals(list.size(), back.size());
		for (int a = 0; a < list.size(); a++) {
			Assert.assertEquals(list.get(a), back.get(a));
		}

		// Test null insertion not inserting anything
		JSONUtils.putJSONableList(bundle, KEY_NULL, null);
		Assert.assertFalse(bundle.containsKey(KEY_NULL));

		// Test null retrieval returning null
		back = (List<MyJSONable>) JSONUtils.getJSONableList(bundle, KEY_NULL, MyJSONable.class);
		Assert.assertNull(back);
	}

	@Test
	public void testListsWithNulls() throws JSONException {
		List<MyJSONable> list = new ArrayList<JSONUtilsTest.MyJSONable>();
		for (int a = 0; a < 10; a++) {
			if (a % 2 == 0) {
				list.add(new MyJSONable());
			}
			else {
				list.add(null);
			}
		}

		JSONObject obj = new JSONObject();
		JSONUtils.putJSONableList(obj, "listWithNulls", list);
		List<MyJSONable> back = JSONUtils.getJSONableList(obj, "listWithNulls", MyJSONable.class);

		Assert.assertNotNull(back);
		Assert.assertEquals(list.size(), back.size());
		for (int a = 0; a < list.size(); a++) {
			Assert.assertEquals(list.get(a), back.get(a));
		}
	}

	@Test
	public void testJSONableListInJSON() throws JSONException {
		// Test round trip of JSONable list
		List<MyJSONable> list = new ArrayList<JSONUtilsTest.MyJSONable>();
		for (int a = 0; a < 10; a++) {
			list.add(new MyJSONable());
		}

		JSONObject obj = new JSONObject();
		JSONUtils.putJSONableList(obj, KEY, list);
		List<MyJSONable> back = (List<MyJSONable>) JSONUtils.getJSONableList(obj, KEY, MyJSONable.class);

		Assert.assertNotNull(back);
		Assert.assertEquals(list.size(), back.size());
		for (int a = 0; a < list.size(); a++) {
			Assert.assertEquals(list.get(a), back.get(a));
		}

		// Test null insertion not inserting anything
		JSONUtils.putJSONableList(obj, KEY_NULL, null);
		Assert.assertFalse(obj.has(KEY_NULL));

		// Test null retrieval returning null
		back = (List<MyJSONable>) JSONUtils.getJSONableList(obj, KEY_NULL, MyJSONable.class);
		Assert.assertNull(back);
	}

	@Test
	public void testJSONObjectInBundle() {
		Bundle bundle = new Bundle();
		JSONObject orig = new MyJSONable().toJson();

		// Test round trip of JSONObject
		JSONUtils.putJSONObject(bundle, KEY, orig);
		JSONObject back = JSONUtils.getJSONObject(bundle, KEY);
		Assert.assertEquals(orig.toString(), back.toString());

		// Test null insertion not inserting anything
		JSONUtils.putJSONObject(bundle, KEY, null);
		Assert.assertFalse(bundle.containsKey(KEY_NULL));

		// Test null retrieval returning null
		back = JSONUtils.getJSONObject(bundle, KEY_NULL);
		Assert.assertNull(back);
	}

	@Test
	public void testJSONablesInJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		MyJSONable orig = new MyJSONable();

		// Test round trip of JSONObject
		JSONUtils.putJSONable(obj, KEY, orig);
		MyJSONable back = JSONUtils.getJSONable(obj, KEY, MyJSONable.class);
		Assert.assertEquals(orig, back);

		// Test null insertion not inserting anything
		JSONUtils.putJSONable(obj, KEY_NULL, null);
		Assert.assertFalse(obj.has(KEY_NULL));

		// Test null retrieval returning null
		back = JSONUtils.getJSONable(obj, KEY_NULL, MyJSONable.class);
		Assert.assertNull(back);
	}

	@Test
	public void testStringMapInJSON() throws JSONException {
		JSONObject obj = new JSONObject();

		// Test round trip
		Map<String, String> map = new HashMap<String, String>();
		for (int a = 0; a < 10; a++) {
			map.put("key" + a, "val" + a);
		}

		JSONUtils.putStringMap(obj, KEY, map);
		Map<String, String> back = JSONUtils.getStringMap(obj, KEY);
		Assert.assertEquals(map, back);

		// Test null insertion not inserting anything
		JSONUtils.putStringMap(obj, KEY_NULL, null);
		Assert.assertFalse(obj.has(KEY_NULL));

		// Test null retrieval returning null
		back = JSONUtils.getStringMap(obj, KEY_NULL);
		Assert.assertNull(back);
	}
}
