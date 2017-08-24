package com.expedia.bookings.unit;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.GsonUtil;
import com.google.gson.reflect.TypeToken;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GsonUtilTest {

	@Test
	public void readWithMoneyTypeAdapter() {
		String moneyString = "{\"total\" : {\"amount\" : 123.4, \"currency\" : \"USD\"}}";
		JSONObject moneyJson = new JSONObject(moneyString);
		Assert.assertNotNull(moneyJson);

		Money result = GsonUtil.getForJsonable(moneyJson, "total", Money.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(new BigDecimal("123.4"), result.amount);
		Assert.assertEquals("USD", result.currencyCode);


		moneyString = "{\"total\" : {\"amount\" : \"123.4\", \"currency\" : \"USD\", \"ignore\" : \"me\"}}";
		moneyJson = new JSONObject(moneyString);
		Assert.assertNotNull(moneyJson);

		result = GsonUtil.getForJsonable(moneyJson, "total", Money.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(new BigDecimal("123.4"), result.amount);
		Assert.assertEquals("USD", result.currencyCode);
	}

	@Test
	public void writeAndReadWithMoneyTypeAdapter() throws Throwable {
		Money expectedMoney = new Money("123.4", "USD");

		JSONObject json = new JSONObject();
		GsonUtil.putForJsonable(json, "total", expectedMoney);

		JSONObject result = json.getJSONObject("total");
		Assert.assertEquals("123.4", result.getString("amount"));
		Assert.assertEquals("USD", result.getString("currency"));

		Money resultMoney = GsonUtil.getForJsonable(json, "total", Money.class);
		Assert.assertEquals(expectedMoney, resultMoney);
	}

	@Test
	public void nullNestedMoneyTypeAdapter() throws Throwable {
		// Regular put / get
		Money expectedMoney = null;
		JSONObject json = new JSONObject();
		GsonUtil.putForJsonable(json, "money", expectedMoney);
		Money resultMoney = GsonUtil.getForJsonable(json, "money", Money.class);
		assertNull(resultMoney);

		// List put / get
		List<ClassWithMoney> listWithNestedNullMonies = new ArrayList<>(2);
		listWithNestedNullMonies.add(new ClassWithMoney());
		listWithNestedNullMonies.add(new ClassWithMoney());
		json = new JSONObject();
		GsonUtil.putListForJsonable(json, "monies", listWithNestedNullMonies);

		Type moniesToken = new TypeToken<List<ClassWithMoney>>() {
		}.getType();
		List<ClassWithMoney> resultMonies = GsonUtil.getListForJsonable(json, "monies", moniesToken);
		assertEquals(2, resultMonies.size());
		assertNull(resultMonies.get(0).money);
		assertNull(resultMonies.get(1).money);
	}

	@Test
	public void instanceSerialization() {
		final String expectedName = "foo";
		final BigDecimal expectedMoney = new BigDecimal("120.45");
		final TestClass.SpecialEnum expectedType = TestClass.SpecialEnum.TWO;
		final TestClass expectedTestClass = new TestClass(expectedName, expectedMoney, expectedType);

		final String key = "myKey";
		JSONObject jsonObject = new JSONObject();
		GsonUtil.putForJsonable(jsonObject, key, expectedTestClass);
		TestClass deserialized = GsonUtil.getForJsonable(jsonObject, key, TestClass.class);

		assertTestClassEquals(expectedTestClass, deserialized);
	}

	@Test
	public void nullSerialization() {
		final String key = "key";
		JSONObject jsonObject = new JSONObject();
		GsonUtil.putForJsonable(jsonObject, key, null);
		TestClass deserializedObject = GsonUtil.getForJsonable(jsonObject, key, TestClass.class);

		assertNull(deserializedObject);
	}


	@Test
	public void listInstanceSerialization() {
		final String expectedName1 = "foo";
		final BigDecimal expectedMoney1 = new BigDecimal("120.45");
		final TestClass.SpecialEnum expectedType1 = TestClass.SpecialEnum.ONE;
		final TestClass unserialized1 = new TestClass(expectedName1, expectedMoney1, expectedType1);

		final String expectedName2 = "bar";
		final BigDecimal expectedMoney2 = new BigDecimal("123.45");
		final TestClass.SpecialEnum expectedType2 = TestClass.SpecialEnum.TWO;
		final TestClass unserialized2 = new TestClass(expectedName2, expectedMoney2, expectedType2);

		final List<TestClass> testClassList = new ArrayList<>(Arrays.asList(unserialized1, unserialized2));

		final String key = "myKey";
		JSONObject jsonObject = new JSONObject();
		GsonUtil.putListForJsonable(jsonObject, key, testClassList);
		List<TestClass> deserializedList = GsonUtil.getListForJsonable(jsonObject, key, listOfTestClassType);

		assertEquals(testClassList.size(), deserializedList.size());
		assertTestClassEquals(testClassList.get(0), deserializedList.get(0));
		assertTestClassEquals(testClassList.get(1), deserializedList.get(1));
	}

	@Test
	public void nullListSerialization() {
		final String key = "key";
		JSONObject jsonObject = new JSONObject();
		GsonUtil.putListForJsonable(jsonObject, key, null);
		List<TestClass> deserializedObject = GsonUtil.getListForJsonable(jsonObject, key, listOfTestClassType);

		assertNull(deserializedObject);
	}

	private void assertTestClassEquals(TestClass expected, TestClass deserialized) {
		assertEquals(expected.name, deserialized.name);
		assertEquals(expected.money, deserialized.money);
		assertEquals(expected.type, deserialized.type);
		assertNull(deserialized.nullVar);
	}

	private static class TestClass {
		enum SpecialEnum {
			ONE,
			TWO
		}

		final String name;
		final BigDecimal money;
		final SpecialEnum type;
		String nullVar;

		public TestClass(String name, BigDecimal money, SpecialEnum type) {
			this.name = name;
			this.money = money;
			this.type = type;
		}
	}

	private static final Type listOfTestClassType = new TypeToken<List<TestClass>>() {
	}.getType();

	private static class ClassWithMoney {
		final Money money;
	}

}
