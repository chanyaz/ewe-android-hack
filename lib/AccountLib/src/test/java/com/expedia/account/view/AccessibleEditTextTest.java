package com.expedia.account.view;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import com.expedia.account.BuildConfig;
import com.expedia.account.R;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 22)
public class AccessibleEditTextTest {
	private Context context = RuntimeEnvironment.application;
	private final String TEST_HINT = "First Name";
	private final String TEST_NAME = "Oscar";
	private final String TEST_ERROR_CONT_DESC = "Invalid Name";
	private final String DEFAULT_ERROR_CONT_DESC = context.getString(R.string.acct__accessibility_cont_desc_role_error);
	private final String DEFAULT_VALID_CONT_DESC = context.getString(R.string.acct__accessibility_cont_desc_role_complete);

	private AccessibleEditText testEditText = new TestAccessibleEditText(context);

	@Before
	public void setUp() {
		testEditText.setHint(TEST_HINT);
	}

	@Test
	public void testContentDescriptionEmptyText() {
		AccessibilityNodeInfo testNode = AccessibilityNodeInfo.obtain();
		testEditText.setText("");
		testEditText.onInitializeAccessibilityNodeInfo(testNode);

		assertEquals(TEST_HINT + " ", testNode.getText().toString());
	}

	@Test
	public void testContDescWithText() {
		AccessibilityNodeInfo testNode = AccessibilityNodeInfo.obtain();
		testEditText.setText(TEST_NAME);
		testEditText.setStatus(AccessibleEditText.Status.DEFAULT);
		testEditText.onInitializeAccessibilityNodeInfo(testNode);

		assertEquals(TEST_HINT + " " + TEST_NAME, testNode.getText().toString());
	}

	@Test
	public void testContDescErrorNoPredefinedErrorText() {
		AccessibilityNodeInfo testNode = AccessibilityNodeInfo.obtain();
		testEditText.setText(TEST_NAME);
		testEditText.setStatus(AccessibleEditText.Status.INVALID);
		testEditText.onInitializeAccessibilityNodeInfo(testNode);

		assertEquals(TEST_HINT + " " + TEST_NAME + ". " + DEFAULT_ERROR_CONT_DESC, testNode.getText().toString());
	}

	@Test
	public void testContDescErrorPredefinedErrorText() {
		AccessibilityNodeInfo testNode = AccessibilityNodeInfo.obtain();
		testEditText.setText(TEST_NAME);
		testEditText.setStatus(AccessibleEditText.Status.INVALID);
		testEditText.setErrorContDesc(TEST_ERROR_CONT_DESC);
		testEditText.onInitializeAccessibilityNodeInfo(testNode);

		assertEquals(TEST_HINT + " " + TEST_NAME + ". " + TEST_ERROR_CONT_DESC, testNode.getText().toString());
	}

	@Test
	public void testContDescValid() {
		AccessibilityNodeInfo testNode = AccessibilityNodeInfo.obtain();
		testEditText.setText(TEST_NAME);
		testEditText.setStatus(AccessibleEditText.Status.VALID);
		testEditText.onInitializeAccessibilityNodeInfo(testNode);

		assertEquals(TEST_HINT + " " + TEST_NAME + ". " + DEFAULT_VALID_CONT_DESC, testNode.getText().toString());
	}

	private class TestAccessibleEditText extends AccessibleEditText {
		public TestAccessibleEditText(Context context) {
			super(context);
			/*
				Work around for http://stackoverflow.com/questions/23749912/nullpointerexception-thrown-while-creating-accessibilitynodeinfo-of-textview
				Basically don't want onInitializeAccessibilityNodeInfoInternal to be called when we make
				the super call in View.onInitializeAccessibilityNodeInfo
			 */
			setAccessibilityDelegate(new AccessibilityDelegate() {
				@Override
				public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
					//do nothing
				}
			});
		}
	}
}
