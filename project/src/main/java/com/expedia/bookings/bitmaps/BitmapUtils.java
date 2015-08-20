package com.expedia.bookings.bitmaps;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Matrix3f;
import android.renderscript.RenderScript;
import android.renderscript.Script.KernelID;
import android.renderscript.ScriptGroup;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicColorMatrix;
import android.renderscript.Type;

import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.TimingLogger;

public class BitmapUtils {

	/**
	 * This will return a copy of a the supplied bitmap that has been stack blurred and darkened
	 * according to the supplied params
	 *
	 * @param bmapToBlur       - the bitmap we plan to blur
	 * @param context
	 * @param reductionFactor  - this is an optimization, typically 4 works well, basically we can shrink the bitmap before we blur and
	 *                         thus have fewer pixels to blur.
	 * @param blurRadius       - the blur radius.
	 * @param darkenMultiplier - each color channel will be multiplied by this number
	 * @return
	 */
	public static Bitmap stackBlurAndDarken(Bitmap bmapToBlur, Context context, int reductionFactor, int blurRadius,
		float darkenMultiplier) {
		//Shrink it, we will have a lot fewer pixels, and they are going to get blurred so nobody should care...
		int w = bmapToBlur.getWidth() / reductionFactor;
		int h = bmapToBlur.getHeight() / reductionFactor;
		Bitmap shrunk = Bitmap.createScaledBitmap(bmapToBlur, w, h, false);

		int scaledBlurRadius = Math.round(blurRadius / reductionFactor);

		//Blur and darken it
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && !AndroidUtils.isGenymotion()) {
			return stackBlurAndDarkenRenderscript(shrunk, context, scaledBlurRadius, darkenMultiplier);
		}
		else if (!AndroidUtils.isGenymotion()) {
			return stackBlurAndDarkenJava(shrunk, scaledBlurRadius, darkenMultiplier);
		}
		else {
			return shrunk;
		}
	}

	/**
	 * Newer Devices get super fast renderscript blur and darken.
	 *
	 * @param bitmap
	 * @param context
	 * @return
	 */
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private static Bitmap stackBlurAndDarkenRenderscript(Bitmap bitmap, Context context, int blurRadius,
		float darkenMultiplier) {
		TimingLogger tictoc = new TimingLogger("STACK_BLUR_AND_DARKEN", "RENDERSCRIPT");

		Bitmap outputBmap = Bitmap.createBitmap(bitmap);
		tictoc.addSplit("Create output bitmap");

		RenderScript mRs = RenderScript.create(context);
		Allocation blurInputAllocation = Allocation.createFromBitmap(mRs, bitmap);
		Allocation blurOutputAllocation = Allocation.createFromBitmap(mRs, outputBmap);
		tictoc.addSplit("Create allocations");

		ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(mRs, Element.U8_4(mRs));
		blur.setRadius(blurRadius);

		ScriptIntrinsicColorMatrix darken = ScriptIntrinsicColorMatrix.create(mRs, Element.U8_4(mRs));
		Matrix3f colorMatrix = new Matrix3f(
			new float[] { darkenMultiplier, 0f, 0f, 0f, darkenMultiplier, 0f, 0f, 0f, darkenMultiplier });
		darken.setColorMatrix(colorMatrix);

		Type.Builder typeBuilder = new Type.Builder(mRs, Element.U8_4(mRs));
		typeBuilder.setX(blurInputAllocation.getType().getX());
		typeBuilder.setY(blurInputAllocation.getType().getY());

		KernelID blurId = blur.getKernelID();
		KernelID darkenId = darken.getKernelID();

		ScriptGroup.Builder builder = new ScriptGroup.Builder(mRs);
		builder.addKernel(blurId);
		builder.addKernel(darkenId);
		builder.addConnection(typeBuilder.create(), blurId, darkenId);
		ScriptGroup scriptGroup = builder.create();

		blur.setInput(blurInputAllocation);
		scriptGroup.setOutput(darkenId, blurOutputAllocation);
		tictoc.addSplit("Renderscript initialization");

		scriptGroup.execute();
		tictoc.addSplit("Actual Blurring");

		blurOutputAllocation.copyTo(outputBmap);
		tictoc.addSplit("Copy to outputBmap");
		tictoc.dumpToLog();

		return outputBmap;
	}

	//This does require some memory...
	private static Bitmap stackBlurAndDarkenJava(Bitmap bitmap, int blurRadius, float darkenMultiplier) {
		TimingLogger tictoc = new TimingLogger("STACK_BLUR_AND_DARKEN", "JAVA");
		if (bitmap == null) {
			return null;
		}

		final int radius = blurRadius;
		final int w = bitmap.getWidth();
		final int h = bitmap.getHeight();
		final int wm = w - 1;
		final int hm = h - 1;
		final int wh = w * h;
		final int div = radius + radius + 1;

		int[] pix = new int[wh];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int[] r = new int[wh];
		int[] g = new int[wh];
		int[] b = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int[] vmin = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int[] dv = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		tictoc.addSplit("Variable setup");

		try {
			for (y = 0; y < h; y++) {
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				for (i = -radius; i <= radius; i++) {
					p = pix[yi + Math.min(wm, Math.max(i, 0))];
					sir = stack[i + radius];
					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);
					rbs = r1 - Math.abs(i);
					rsum += sir[0] * rbs;
					gsum += sir[1] * rbs;
					bsum += sir[2] * rbs;
					if (i > 0) {
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					}
					else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}
				}
				stackpointer = radius;

				for (x = 0; x < w; x++) {

					r[yi] = dv[rsum];
					g[yi] = dv[gsum];
					b[yi] = dv[bsum];

					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;

					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];

					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];

					if (y == 0) {
						vmin[x] = Math.min(x + radius + 1, wm);
					}
					p = pix[yw + vmin[x]];

					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);

					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;

					stackpointer = (stackpointer + 1) % div;
					sir = stack[(stackpointer) % div];

					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];

					yi++;
				}
				yw += w;
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			// Genymotion likes to throw this due to some bug in creating bitmaps
			Log.e("stackBlurAndDarkenJava threw exception", e);
		}
		try {
			for (x = 0; x < w; x++) {
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				yp = -radius * w;
				for (i = -radius; i <= radius; i++) {
					yi = Math.max(0, yp) + x;

					sir = stack[i + radius];

					sir[0] = r[yi];
					sir[1] = g[yi];
					sir[2] = b[yi];

					rbs = r1 - Math.abs(i);

					rsum += r[yi] * rbs;
					gsum += g[yi] * rbs;
					bsum += b[yi] * rbs;

					if (i > 0) {
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					}
					else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}

					if (i < hm) {
						yp += w;
					}
				}
				yi = x;
				stackpointer = radius;
				for (y = 0; y < h; y++) {
					pix[yi] = 0xff000000 | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;

					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];

					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];

					if (x == 0) {
						vmin[y] = Math.min(y + r1, hm) * w;
					}
					p = x + vmin[y];

					sir[0] = r[p];
					sir[1] = g[p];
					sir[2] = b[p];

					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;

					stackpointer = (stackpointer + 1) % div;
					sir = stack[stackpointer];

					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];

					yi += w;
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException e) {
			// Genymotion likes to throw this due to some bug in creating bitmaps
			Log.e("stackBlurAndDarkenJava threw exception", e);
		}
		tictoc.addSplit("Blur");

		//Darken each pixel. (this should be the equivalent of adding a black .35 opacity mask)
		double maskValue = darkenMultiplier;
		for (int d = 0; d < pix.length; d++) {
			pix[d] = Color.rgb((int) (Color.red(pix[d]) * maskValue), (int) (Color.green(pix[d]) * maskValue),
				(int) (Color.blue(pix[d]) * maskValue));
		}
		tictoc.addSplit("Darken");

		Bitmap newbitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		newbitmap.setPixels(pix, 0, w, 0, 0, w, h);
		tictoc.addSplit("Copy to output bitmap");
		tictoc.dumpToLog();

		return newbitmap;
	}

	/**
	 * Creates a matrix to use in a drawable or ImageView to perform a TOP_CROP similar to CENTER_CROP
	 */
	public static Matrix createTopCropMatrix(int bitmapWidth, int bitmapHeight, int vwidth, int vheight) {
		Matrix matrix = new Matrix();

		float scale;
		if (bitmapWidth * vheight > vwidth * bitmapHeight) {
			scale = (float) vheight / (float) bitmapHeight;
		}
		else {
			scale = (float) vwidth / (float) bitmapWidth;
		}

		matrix.setScale(scale, scale);

		return matrix;
	}

	/**
	 * Creates a matrix to use in a drawable or ImageView to fit the image by width
	 */
	public static Matrix createFitWidthMatrix(int bitmapWidth, int bitmapHeight, int vwidth, int vheight) {
		Matrix matrix = new Matrix();

		float scale;
		scale = (float) vwidth / (float) bitmapWidth;

		matrix.setScale(scale, scale);

		return matrix;
	}

}
