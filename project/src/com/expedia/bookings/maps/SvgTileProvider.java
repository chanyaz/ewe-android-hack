package com.expedia.bookings.maps;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.RectF;

import com.expedia.bookings.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.mobiata.android.Log;

public class SvgTileProvider implements TileProvider {
	public static void addToMap(Context context, GoogleMap map) {
		map.addTileOverlay(new TileOverlayOptions().tileProvider(new SvgTileProvider(context)));
		map.setMapType(GoogleMap.MAP_TYPE_NONE);
		map.setMyLocationEnabled(false);

		UiSettings settings = map.getUiSettings();
		settings.setCompassEnabled(false);
		settings.setMyLocationButtonEnabled(false);
		settings.setRotateGesturesEnabled(false);
		settings.setTiltGesturesEnabled(false);
		settings.setZoomControlsEnabled(false);
		// FIXME: Keep this disabled for development
		//settings.setScrollGesturesEnabled(false);
		//settings.setZoomGesturesEnabled(false);
	}

	private final ConcurrentLinkedQueue<TileGenerator> mPool = new ConcurrentLinkedQueue<TileGenerator>();
	private static final int POOL_MAX_SIZE = 10;

	private static final int BASE_TILE_SIZE = 256;

	private Picture mParentPicture;

	public SvgTileProvider(Context context) {
		SVG mapSvg = SVGParser.getSVGFromResource(context.getResources(), R.raw.wallpaper_bg_night);
		mParentPicture = mapSvg.getPicture();
	}

	@Override
	public Tile getTile(int x, int y, int zoom) {
		TileGenerator tileGenerator = getGenerator();
		byte[] tileData = tileGenerator.getTileImageData(x, y, zoom);
		restoreGenerator(tileGenerator);
		return new Tile(BASE_TILE_SIZE, BASE_TILE_SIZE, tileData);
	}

	public TileGenerator getGenerator() {
		TileGenerator i = mPool.poll();
		if (i == null) {
			return new TileGenerator();
		}
		return i;
	}

	public void restoreGenerator(TileGenerator tileGenerator) {
		if (mPool.size() < POOL_MAX_SIZE && mPool.offer(tileGenerator)) {
			return;
		}
		// pool is too big or returning to pool failed, so just try to clean
		// up.
		tileGenerator.cleanUp();
	}

	public class TileGenerator {
		private Picture mPicture;
		private Bitmap mBitmap;
		private ByteArrayOutputStream mStream;

		public TileGenerator() {
			mPicture = new Picture(mParentPicture);
			mBitmap = Bitmap.createBitmap(BASE_TILE_SIZE, BASE_TILE_SIZE, Bitmap.Config.ARGB_8888);
			// TODO - Is this large enough? The png should be smaller but need to check
			mStream = new ByteArrayOutputStream(BASE_TILE_SIZE * BASE_TILE_SIZE * 4);
		}

		public byte[] getTileImageData(int x, int y, int zoom) {
			mStream.reset();

			float tileWidthAtZoom = (float) (mPicture.getWidth() / Math.pow(2, zoom));
			float scale = BASE_TILE_SIZE / tileWidthAtZoom;

			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			matrix.postTranslate(-x * BASE_TILE_SIZE, -y * BASE_TILE_SIZE);

			mBitmap.eraseColor(Color.TRANSPARENT);
			Canvas c = new Canvas(mBitmap);
			c.setMatrix(matrix);
			mPicture.draw(c);

			BufferedOutputStream stream = new BufferedOutputStream(mStream);
			mBitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
			try {
				stream.close();
			} catch (IOException e) {
				Log.e("TileGenerator getTileImageData: Error while closing tile byte stream.", e);
			}
			return mStream.toByteArray();
		}

		public void cleanUp() {
			mBitmap.recycle();
			mBitmap = null;
			try {
				mStream.close();
			} catch (IOException e) {
				Log.e("TileGenerator cleanUp: Error while closing tile byte stream.", e);
			}
			mStream = null;
		}
	}
}

