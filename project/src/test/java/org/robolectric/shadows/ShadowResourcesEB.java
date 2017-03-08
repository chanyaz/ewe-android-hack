package org.robolectric.shadows;

import java.util.Iterator;
import java.util.List;

import org.robolectric.annotation.Implements;
import org.robolectric.res.Attribute;

import android.content.res.Resources;
import android.content.res.TypedArray;

@Implements(Resources.class)
public class ShadowResourcesEB extends ShadowResources {
	// There are some drawables referred form drawables.xml which Robolectric is not able to resolve.
	// There is an open issue : https://github.com/robolectric/robolectric/issues/1554

	public TypedArray createTypedArray(List<Attribute> set, int[] attrs) {
		Iterator<Attribute> iterator = set.iterator();
		while (iterator.hasNext()) {
			Attribute attribute = iterator.next();
			if (isDrawableAttribute(attribute)) {
				int drawableResId = realResources
					.getIdentifier(getDrawableName(attribute.value), "drawable", "com.expedia.bookings");
				if (isReferenceDrawable(drawableResId)) {
					iterator.remove();
				}
			}
		}
		return null; //super.createTypedArray(set, attrs);
	}

	private boolean isDrawableAttribute(Attribute attribute) {
		return attribute.value.startsWith("@com.expedia.bookings:drawable/");
	}

	private String getDrawableName(String attributeValue) {
		String[] drawableFullNameWithPackageName = attributeValue.split("/");
		return drawableFullNameWithPackageName[drawableFullNameWithPackageName.length - 1];
	}

	private boolean isReferenceDrawable(int drawableResId) {
		try {
			realResources.getDrawable(drawableResId);
		}
		catch (Exception e) {
			//It will crash in case roboelectric fails to get reference drawable
			return true;
		}
		return false;
	}
}
