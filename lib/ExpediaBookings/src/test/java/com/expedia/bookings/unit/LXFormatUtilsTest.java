package com.expedia.bookings.unit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.utils.LXFormatUtils;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.StringContains.containsString;

public class LXFormatUtilsTest {
	@Test
	public void testHTMLFormatting() {
		final String htmlString = "<p>The New/York Pass offers something just right for you.</p>";
		final String pTagOpen = "<p>";
		final String pTagClose = "</p>";
		String formattedString = LXFormatUtils.stripHTMLTags(htmlString);
		Assert.assertThat(formattedString, allOf(not(containsString(pTagOpen)), not(containsString(pTagClose))));
	}

	@Test
	public void testFormatHighlights() {
		final List<String> highlightsList = new ArrayList<>();
		final String pTagOpen = "<p>";
		final String pTagClose = "</p>";
		final String highlight = "Highlight";
		final String highlightWithTags = "<p>Highlight</p>";

		StringBuilder expected = new StringBuilder();
		for (int i = 0; i < 5; i++) {
			highlightsList.add(highlightWithTags);
			expected.append(highlight).append(LXFormatUtils.FULLSTOP_SPACE);
		}
		String formattedHighlight = LXFormatUtils.formatHighlights(highlightsList);
		Assert.assertThat(formattedHighlight, allOf(not(containsString(pTagOpen)), not(containsString(pTagClose))));
		Assert.assertEquals(expected.toString(), formattedHighlight);
	}
}
