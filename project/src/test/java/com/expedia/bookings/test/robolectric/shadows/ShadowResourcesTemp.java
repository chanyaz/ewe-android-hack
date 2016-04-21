package com.expedia.bookings.test.robolectric.shadows;

import java.util.List;

import org.robolectric.annotation.Implements;
import org.robolectric.res.Attribute;
import org.robolectric.shadows.ShadowAssetManager;
import org.robolectric.shadows.ShadowResources;
import org.robolectric.shadows.ShadowTypedArray;

import android.content.res.Resources;
import android.content.res.TypedArray;

@Implements(Resources.class)
public class ShadowResourcesTemp extends ShadowResources {
	// There are some drawables referred form drawables.xml which Robolectric is not able to resolve.
	// There is an open issue : https://github.com/robolectric/robolectric/issues/1554

	public TypedArray createTypedArray(List<Attribute> set, int[] attrs) {
		CharSequence[] stringData = new CharSequence[attrs.length];
		int[] data = new int[attrs.length * ShadowAssetManager.STYLE_NUM_ENTRIES];
		int[] indices = new int[attrs.length + 1];
		int nextIndex = 0;
		for (Attribute attribute : set) {
			if (attribute.value.toString().contains("checkout_logout_logo")) {
				return ShadowTypedArray.create(Resources.getSystem(), attrs, data, indices, nextIndex, stringData);
			}
		}
		return super.createTypedArray(set, attrs);
	}
}
