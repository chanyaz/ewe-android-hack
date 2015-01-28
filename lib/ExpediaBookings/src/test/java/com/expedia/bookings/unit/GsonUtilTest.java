package com.expedia.bookings.unit;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

import com.expedia.bookings.utils.GsonUtil;
import com.google.gson.reflect.TypeToken;

import static org.junit.Assert.assertEquals;

public class GsonUtilTest {

	@Test
	public void getPutSerialization() {
		final String expectedName = "foo";
		BigDecimal expectedMoney = new BigDecimal(120.45d);
		TestClass.SpecialEnum expectedType = TestClass.SpecialEnum.TWO;
		TestClass expectedTestClass = new TestClass(expectedName, expectedMoney, expectedType);

		final String key = "myKey";
		JSONObject jsonObject = new JSONObject();
		GsonUtil.putForJsonable(jsonObject, key, expectedTestClass);
		TestClass deserialized = GsonUtil.getForJsonable(jsonObject, key, TestClass.class);

		assertTestClassEquals(expectedTestClass, deserialized);
	}

	@Test
	public void getPutListSerialization() {
		final String expectedName1 = "foo";
		BigDecimal expectedMoney1 = new BigDecimal(120.45d);
		TestClass.SpecialEnum expectedType1 = TestClass.SpecialEnum.ONE;
		TestClass unserialized1 = new TestClass(expectedName1, expectedMoney1, expectedType1);

		final String expectedName2 = "bar";
		BigDecimal expectedMoney2 = new BigDecimal(123.45d);
		TestClass.SpecialEnum expectedType2 = TestClass.SpecialEnum.TWO;
		TestClass unserialized2 = new TestClass(expectedName2, expectedMoney2, expectedType2);

		final List<TestClass> testClassList = new ArrayList<>(Arrays.asList(unserialized1, unserialized2));

		final String key = "myKey";
		JSONObject jsonObject = new JSONObject();
		GsonUtil.putListForJsonable(jsonObject, key, testClassList);
		Type testClassType = new TypeToken<List<TestClass>>() {
		}.getType();
		List<TestClass> deserializedList = GsonUtil.getListForJsonable(jsonObject, key, testClassType);

		assertEquals(testClassList.size(), deserializedList.size());
		assertTestClassEquals(testClassList.get(0), deserializedList.get(0));
		assertTestClassEquals(testClassList.get(1), deserializedList.get(1));
	}

	private void assertTestClassEquals(TestClass expected, TestClass deserialized) {
		assertEquals(expected.name, deserialized.name);
		assertEquals(expected.money, deserialized.money);
		assertEquals(expected.type, deserialized.type);
	}

	private static class TestClass {
		enum SpecialEnum {
			ONE,
			TWO
		}

		String name;
		BigDecimal money;
		SpecialEnum type;

		public TestClass(String name, BigDecimal money, SpecialEnum type) {
			this.name = name;
			this.money = money;
			this.type = type;
		}

	}

}
