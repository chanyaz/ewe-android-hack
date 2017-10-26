package com.expedia.account.util;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageButton;

public class AccessibilityUtil {
	public static void setFocusToToolBarUpIcon(Toolbar toolbar) {
		for (int i = 0; i < toolbar.getChildCount(); i++) {
			View v = toolbar.getChildAt(i);
			if (v instanceof ImageButton) {
				v.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_HOVER_ENTER);
				break;
			}
		}
	}
;	public static boolean isTalkbackEnabled(Context context){
		AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(context.ACCESSIBILITY_SERVICE);
		return accessibilityManager.isEnabled() && accessibilityManager.isTouchExplorationEnabled();
	}
}
