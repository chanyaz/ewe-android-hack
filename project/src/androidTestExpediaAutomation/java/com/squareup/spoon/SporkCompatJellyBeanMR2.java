package com.squareup.spoon;

import static android.graphics.Bitmap.CompressFormat.PNG;
import static com.squareup.spoon.Chmod.chmodPlusR;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.os.Build;

public class SporkCompatJellyBeanMR2 {

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	protected static void takeScreenshot(Instrumentation instrumentation, File file) throws IOException {
		final Bitmap bitmap = instrumentation.getUiAutomation().takeScreenshot();

		OutputStream fos = null;
		try {
			fos = new BufferedOutputStream(new FileOutputStream(file));
			bitmap.compress(PNG, 100 /* quality */, fos);

			chmodPlusR(file);
		}
		finally {
			bitmap.recycle();
			if (fos != null) {
				fos.close();
			}
		}

	}
}
