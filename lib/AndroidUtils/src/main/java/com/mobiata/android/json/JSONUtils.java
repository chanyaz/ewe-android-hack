package com.mobiata.android.json;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.mobiata.android.Log;

public class JSONUtils {

	//////////////////////////////////////////////////////////////////////////
	// JSONObject utilities

	public static void putJSONObject(Bundle bundle, String key, JSONObject obj) {
		if (bundle != null && !TextUtils.isEmpty(key) && obj != null) {
			bundle.putString(key, obj.toString());
		}
	}

	public static void putJSONObject(Intent intent, String key, JSONObject obj) {
		if (intent != null && !TextUtils.isEmpty(key) && obj != null) {
			intent.putExtra(key, obj.toString());
		}
	}

	public static JSONObject getJSONObject(Bundle bundle, String key) {
		if (bundle != null && bundle.containsKey(key)) {
			try {
				String jsonStr = bundle.getString(key);
				return new JSONObject(jsonStr);
			}
			catch (JSONException e) {
				Log.e("Could not retrieve JSONObject \"" + key + "\" from Bundle.", e);
			}
		}

		return null;
	}

	public static JSONObject getJSONObject(Intent intent, String key) {
		return getJSONObject(intent.getExtras(), key);
	}

	public static String safeToString(JSONObject obj, int indent) {
		try {
			return obj.toString(indent);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}

	public static void dump(JSONObject obj) {
		String[] lines = safeToString(obj, 2).split("\\r?\\n");
		for (String line : lines) {
			Log.v("JSON dump: " + line);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable utilities

	public static void putJSONable(JSONObject obj, String key, JSONable jsonable) throws JSONException {
		if (obj != null && !TextUtils.isEmpty(key) && jsonable != null) {
			obj.putOpt(key, jsonable.toJson());
		}
	}

	public static void putJSONable(JSONArray arr, JSONable jsonable) throws JSONException {
		if (arr != null && jsonable != null) {
			arr.put(jsonable.toJson());
		}
	}

	public static void putJSONable(Bundle bundle, String key, JSONable jsonable) {
		if (jsonable != null) {
			putJSONObject(bundle, key, jsonable.toJson());
		}
	}

	public static void putJSONable(Intent intent, String key, JSONable jsonable) {
		if (jsonable != null) {
			putJSONObject(intent, key, jsonable.toJson());
		}
	}

	public static <T extends JSONable> T getJSONable(JSONObject obj, String key, Class<T> c) {
		if (obj != null && !TextUtils.isEmpty(key)) {
			JSONObject jsonableObj = obj.optJSONObject(key);
			return convertJSONObjectToJSONable(jsonableObj, c);
		}

		return null;
	}

	public static <T extends JSONable> T getJSONable(JSONArray arr, int index, Class<T> c) {
		if (arr != null) {
			JSONObject jsonableObj = arr.optJSONObject(index);
			return convertJSONObjectToJSONable(jsonableObj, c);
		}

		return null;
	}

	public static <T extends JSONable> T getJSONable(Bundle bundle, String key, Class<T> c) {
		if (bundle != null && !TextUtils.isEmpty(key)) {
			JSONObject obj = getJSONObject(bundle, key);
			return convertJSONObjectToJSONable(obj, c);
		}

		return null;
	}

	public static <T extends JSONable> T getJSONable(Intent intent, String key, Class<T> c) {
		if (intent != null) {
			return getJSONable(intent.getExtras(), key, c);
		}

		return null;
	}

	public static <T extends JSONable> T getJSONable(String jsonString, Class<T> c) {
		try {
			JSONObject json = new JSONObject(jsonString);
			return convertJSONObjectToJSONable(json, c);
		}
		catch (JSONException e) {
			Log.e("Could not convert string to JSONable of type \"" + c + "\".", e);
		}

		return null;
	}

	private static <T extends JSONable> T convertJSONObjectToJSONable(JSONObject obj, Class<T> c) {
		if (obj != null) {
			try {
				JSONable jsonable = (JSONable) c.newInstance();
				if (jsonable.fromJson(obj)) {
					return c.cast(jsonable);
				}
			}
			catch (Exception e) {
				Log.e("Could not convert JSONObject to JSONable of type \"" + c + "\".", e);
			}
		}

		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// List<JSONable> utilities

	// Due to the lack of covariance, we have to trust that you're passing in a list of JSONable objects

	public static void putJSONableList(JSONObject obj, String key, Collection<?> jsonableList) throws JSONException {
		if (obj != null && jsonableList != null) {
			obj.putOpt(key, convertJSONableListToJSONArray(jsonableList));
		}
	}

	public static void putJSONableList(JSONArray arr, Collection<?> jsonableList) throws JSONException {
		if (arr != null && jsonableList != null) {
			arr.put(convertJSONableListToJSONArray(jsonableList));
		}
	}

	public static void putJSONableList(Bundle bundle, String key, Collection<?> jsonableList) {
		if (bundle != null && !TextUtils.isEmpty(key) && jsonableList != null) {
			bundle.putString(key, convertJSONableListToJSONArray(jsonableList).toString());
		}
	}

	public static void putJSONableList(Intent intent, String key, Collection<?> jsonableList) {
		if (intent != null && !TextUtils.isEmpty(key) && jsonableList != null) {
			intent.putExtra(key, convertJSONableListToJSONArray(jsonableList).toString());
		}
	}

	public static <T extends JSONable> List<T> getJSONableList(Bundle bundle, String key, Class<T> c) {
		if (bundle != null && bundle.containsKey(key)) {
			String str = bundle.getString(key);

			try {
				JSONArray arr = new JSONArray(str);
				return convertJSONArrayToJSONableList(arr, c);
			}
			catch (JSONException e) {
				Log.e("Could not retrieve JSONable list", e);
			}
		}

		return null;
	}

	public static <T extends JSONable> List<T> getJSONableList(Intent intent, String key, Class<T> c) {
		if (intent != null) {
			return getJSONableList(intent.getExtras(), key, c);
		}

		return null;
	}

	public static <T extends JSONable> List<T> getJSONableList(JSONObject obj, String key, Class<T> c) {
		if (obj != null && obj.has(key)) {
			JSONArray arr = obj.optJSONArray(key);
			return convertJSONArrayToJSONableList(arr, c);
		}
		return null;
	}

	public static <T extends JSONable> List<T> getJSONableList(JSONArray arr, int index, Class<T> c) {
		if (arr != null) {
			JSONArray jsonableArr = arr.optJSONArray(index);
			return convertJSONArrayToJSONableList(jsonableArr, c);
		}
		return null;
	}

	private static JSONArray convertJSONableListToJSONArray(Collection<?> jsonableList) {
		if (jsonableList != null) {
			JSONArray arr = new JSONArray();
			for (Object jsonable : jsonableList) {
				if (jsonable != null) {
					arr.put(((JSONable) jsonable).toJson());
				}
				else {
					arr.put(null);
				}
			}
			return arr;
		}

		return null;
	}

	private static <T extends JSONable> List<T> convertJSONArrayToJSONableList(JSONArray arr, Class<T> c) {
		if (arr != null) {
			int len = arr.length();
			List<T> list = new ArrayList<>(len);

			try {
				for (int a = 0; a < len; a++) {
					JSONable jsonable = (JSONable) c.newInstance();
					if (arr.isNull(a)) {
						list.add(null);
					}
					else if (jsonable.fromJson(arr.optJSONObject(a))) {
						list.add(c.cast(jsonable));
					}
				}

				return list;
			}
			catch (Exception e) {
				Log.e("Could not convert JSONArray to JSONable list of type \"" + c + "\".", e);
			}
		}

		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Enums

	public static void putEnum(Bundle bundle, String key, Enum<?> val) {
		if (bundle != null && !TextUtils.isEmpty(key) && val != null) {
			bundle.putString(key, val.name());
		}
	}

	public static void putEnum(JSONObject obj, String key, Enum<?> val) throws JSONException {
		if (obj != null && !TextUtils.isEmpty(key) && val != null) {
			obj.put(key, val.name());
		}
	}

	public static void putEnum(JSONArray arr, Enum<?> val) {
		if (arr != null && val != null) {
			arr.put(val.name());
		}
	}

	public static <T extends Enum<T>> T getEnum(Bundle bundle, String key, Class<T> c) {
		if (bundle != null && !TextUtils.isEmpty(key) && bundle.containsKey(key)) {
			return convertNameToEnum(bundle.getString(key), c);
		}
		return null;
	}

	public static <T extends Enum<T>> T getEnum(JSONObject obj, String key, Class<T> c) {
		if (obj != null && !TextUtils.isEmpty(key) && obj.has(key)) {
			return convertNameToEnum(obj.optString(key), c);
		}
		return null;
	}

	public static <T extends Enum<T>> T getEnum(JSONArray arr, int index, Class<T> c) {
		if (arr != null && index >= 0 && index < arr.length()) {
			return convertNameToEnum(arr.optString(index), c);
		}
		return null;
	}

	private static <T extends Enum<T>> T convertNameToEnum(String name, Class<T> c) {
		if (!TextUtils.isEmpty(name)) {
			return (T) Enum.valueOf(c, name);
		}
		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Specialized data structure handling

	public static void putIntList(JSONObject obj, String key, List<Integer> intList) throws JSONException {
		if (obj != null && !TextUtils.isEmpty(key) && intList != null) {
			JSONArray arr = new JSONArray();
			for (int i : intList) {
				arr.put(i);
			}
			obj.putOpt(key, arr);
		}
	}

	public static List<Integer> getIntList(JSONObject obj, String key) {
		if (obj != null && obj.has(key)) {
			JSONArray arr = obj.optJSONArray(key);
			int len = arr.length();
			List<Integer> intList = new ArrayList<>(len);
			for (int a = 0; a < len; a++) {
				intList.add(arr.optInt(a));
			}
			return intList;
		}
		return null;
	}

	public static void putStringList(JSONObject obj, String key, Collection<String> stringList) throws JSONException {
		if (obj != null && !TextUtils.isEmpty(key) && stringList != null) {
			JSONArray arr = new JSONArray();
			for (String str : stringList) {
				arr.put(str);
			}
			obj.putOpt(key, arr);
		}
	}

	public static List<String> getStringList(JSONObject obj, String key) {
		if (obj != null && obj.has(key)) {
			JSONArray arr = obj.optJSONArray(key);
			List<String> stringList = new ArrayList<>();
			int len = arr.length();
			for (int a = 0; a < len; a++) {
				stringList.add(arr.optString(a));
			}
			return stringList;
		}
		return null;
	}

	public static void putStringMap(JSONObject obj, String key, Map<String, String> stringMap) throws JSONException {
		// Just stores a map as a list of strings
		if (obj != null && !TextUtils.isEmpty(key) && stringMap != null && stringMap.size() > 0) {
			List<String> mapList = new ArrayList<>(stringMap.size() * 2);
			for (String mapKey : stringMap.keySet()) {
				mapList.add(mapKey);
				mapList.add(stringMap.get(mapKey));
			}
			putStringList(obj, key, mapList);
		}
	}

	public static Map<String, String> getStringMap(JSONObject obj, String key) {
		if (obj != null && obj.has(key)) {
			List<String> mapList = getStringList(obj, key);
			int len = mapList.size();
			Map<String, String> stringMap = new HashMap<>(len / 2);
			for (int a = 0; a < len; a += 2) {
				String mapKey = mapList.get(a);
				String mapValue = mapList.get(a + 1);
				stringMap.put(mapKey, mapValue);
			}
			return stringMap;
		}
		return null;
	}

	public static void putJSONableStringMap(JSONObject obj, String key, Map<String, ? extends JSONable> map) throws JSONException {
		if (map != null) {
			JSONObject mapObj = new JSONObject();
			for (String mapKey : map.keySet()) {
				mapObj.putOpt(mapKey, map.get(mapKey).toJson());
			}
			obj.putOpt(key, mapObj);
		}
	}

	@SuppressWarnings("rawtypes")
	public static <T extends JSONable> Map<String, T> getJSONableStringMap(JSONObject obj, String key, Class<T> c,
			Map<String, T> defaultVal) {
		try {
			JSONObject mapObj = obj.getJSONObject(key);
			Map<String, T> retMap = new HashMap<>();

			Iterator it = mapObj.keys();
			while (it.hasNext()) {
				String mapKey = (String) it.next();
				T jsonable = c.newInstance();
				jsonable.fromJson(mapObj.getJSONObject(mapKey));
				retMap.put(mapKey, jsonable);
			}

			return retMap;
		}
		catch (Exception e) {
			//ignore
		}

		return defaultVal;
	}

	//////////////////////////////////////////////////////////////////////////
	// Specialized retrieval methods

	/**
	 * This will retrieve a JSONArray from a JSONObject.  If the key
	 * leads to a JSONObject instead, it will be wrapped in a JSONArray then
	 * returned.  (This is to simplify parsing code.)
	 * @param obj
	 * @param key
	 * @return
	 */
	public static JSONArray getOrWrapJSONArray(JSONObject obj, String key) throws JSONException {
		if (obj == null || !obj.has(key)) {
			return null;
		}

		JSONArray arr;
		try {
			arr = obj.getJSONArray(key);
		}
		catch (JSONException e) {
			JSONObject singleObj = obj.getJSONObject(key);
			arr = new JSONArray();
			arr.put(singleObj);
		}

		return arr;
	}

	public static String getNormalizedString(JSONObject obj, String key) throws JSONException {
		String str = obj.getString(key);
		return Normalizer.normalize(str, Normalizer.Form.NFC);
	}

	public static String optNormalizedString(JSONObject obj, String key, String def) {
		String str = obj.optString(key, def);
		if (str == null) {
			return null;
		}
		else {
			return Normalizer.normalize(str, Normalizer.Form.NFC);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Miscellaneous utils

	public static <T extends JSONable> T clone(T jsonable, Class<T> c) {
		if (jsonable != null) {
			try {
				T clone = c.newInstance();
				clone.fromJson(jsonable.toJson());
				return clone;
			}
			catch (Exception e) {
				Log.w("Could not clone jsonable of class " + c, e);
			}
		}

		return null;
	}

	public static <T extends JSONable> List<T> cloneList(List<T> jsonableList, Class<T> c) {
		if (jsonableList != null) {
			try {
				List<T> clone = new ArrayList<>();
				for (T jsonable : jsonableList) {
					T cloneItem = c.newInstance();
					cloneItem.fromJson(jsonable.toJson());
					clone.add(cloneItem);
				}
				return clone;
			}
			catch (Exception e) {
				Log.w("Could not clone jsonable list of class " + c, e);
			}
		}

		return null;
	}

	public static String toString(JSONable jsonable) {
		return toString(jsonable, 0);
	}

	public static String toString(JSONable jsonable, int indentSpaces) {
		JSONObject obj = jsonable.toJson();
		if (obj != null) {
			try {
				return obj.toString(indentSpaces);
			}
			catch (JSONException e) {
				// This should never happen, but in case it does, return the error string
				return e.toString();
			}
		}

		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Deprecated methods (kept for backwards compatibility)

	/**
	 * @deprecated Use getJSONable() instead
	 */
	@Deprecated
	public static <T extends JSONable> T parseJSONableFromIntent(Intent intent, String key, Class<T> c) {
		return getJSONable(intent, key, c);
	}

	/**
	 * @deprecated Use getJSONObject()
	 */
	@Deprecated
	public static JSONObject parseJSONObjectFromIntent(Intent intent, String key) {
		return getJSONObject(intent, key);
	}

	/**
	 * @deprecated Use getJSONObject()
	 */
	@Deprecated
	public static JSONObject parseJSONObjectFromBundle(Bundle bundle, String key) {
		return getJSONObject(bundle, key);
	}

	/**
	 * FYI, this method was horribly misnamed.  It should be JSONable it's retrieving
	 *
	 * @deprecated Use getJSONObject()
	 */
	@Deprecated
	public static <T extends JSONable> T parseJSONObjectFromBundle(Bundle bundle, String key, Class<T> c) {
		return getJSONable(bundle, key, c);
	}
}
