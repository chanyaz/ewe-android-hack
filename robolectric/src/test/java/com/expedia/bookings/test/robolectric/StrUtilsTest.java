package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.LegalClickableSpan;
import com.expedia.bookings.utils.StrUtils;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class StrUtilsTest {

	private Context getContext() {
		return Robolectric.application;
	}

	@Before
	public void before() {
		Robolectric.application.setTheme(R.style.V2_Theme_Cars);
	}

	@Test
	public void testLegalTextContent() {

		SpannableStringBuilder legalText = StrUtils.generateLegalClickableLink(getContext(), "");

		String expectedText = getLegalText();
		assertEquals(expectedText, legalText.toString());
	}

	@Test
	public void testLegalTextSpans() {
		SpannableStringBuilder legalTextSpanBuilder = StrUtils.generateLegalClickableLink(getContext(), "");

		String rulesText = getContext().getString(R.string.rules_and_restrictions);
		String termsText = getContext().getString(R.string.terms_and_conditions);
		String privacyText = getContext().getString(R.string.privacy_policy);

		String legalText = getLegalText();
		int rulesStart = legalText.indexOf(rulesText);
		int termStart = legalText.indexOf(termsText);
		int privacyStart = legalText.indexOf(privacyText);
		int rulesEnd = rulesStart + rulesText.length();
		int termEnd = termStart + termsText.length();
		int privacyEnd = privacyStart + privacyText.length();

		Object[] rulesSpans = legalTextSpanBuilder.getSpans(rulesStart, rulesEnd, Object.class);
		Object[] termsSpans = legalTextSpanBuilder.getSpans(termStart, termEnd, Object.class);
		Object[] privacySpans = legalTextSpanBuilder.getSpans(privacyStart, privacyEnd, Object.class);

		List<Object[]> spansList = new ArrayList<>();
		spansList.add(rulesSpans);
		spansList.add(termsSpans);
		spansList.add(privacySpans);

		for (Object[] spans : spansList) {
			assertEquals(spans[0].getClass(), LegalClickableSpan.class);
			assertEquals(spans[1].getClass(), StyleSpan.class);
			assertEquals(spans[2].getClass(), UnderlineSpan.class);
			assertEquals(spans[3].getClass(), ForegroundColorSpan.class);
		}
	}

	@Test
	public void testHTMLFormatting() {
		final String htmlString = "<p>The New/York Pass offers something just right for you.</p>";
		final String pTagOpen = "<p>";
		final String pTagClose = "</p>";
		String formattedString = StrUtils.stripHTMLTags(htmlString);
		Assert.assertThat(formattedString, allOf(not(containsString(pTagOpen)), not(containsString(pTagClose))));
	}

	@Test
	public void testGenerateBulletedList() {
		List<String> items = new ArrayList<>();
		items.add("Item1");
		items.add("Item2");
		items.add("Item3");
		String newline = "\n";
		SpannableStringBuilder stringBuilder = StrUtils.generateBulletedList(items);
		BulletSpan[] bulletSpan = stringBuilder.getSpans(0, stringBuilder.length(), BulletSpan.class);
		assertEquals(items.size(), bulletSpan.length);
		// Check if newline added to each item except the last one.
		assertEquals(items.get(0) + newline, stringBuilder.subSequence(0, 6).toString());
		assertEquals(items.get(1) + newline, stringBuilder.subSequence(6, 12).toString());
		assertEquals(items.get(2), stringBuilder.subSequence(12, 17).toString());
	}

	private String getLegalText() {
		String rulesText = getContext().getString(R.string.rules_and_restrictions);
		String termsText = getContext().getString(R.string.terms_and_conditions);
		String privacyText = getContext().getString(R.string.privacy_policy);

		String legalText = getContext().getString(R.string.legal_TEMPLATE, rulesText, termsText, privacyText);
		return legalText;
	}
}
