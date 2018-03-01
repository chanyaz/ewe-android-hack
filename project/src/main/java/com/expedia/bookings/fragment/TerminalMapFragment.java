package com.expedia.bookings.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.Ui;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.widget.SVGView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.flightlib.data.AirportMap;

public class TerminalMapFragment extends Fragment {

	private static final String NET_MAP_DOWNLOAD = "NET_MAP_DOWNLOAD";
	private static final String STATE_SVG = "STATE_SVG";
	private static final String STATE_AIRPORT_MAP = "STATE_AIRPORT_MAP";

	private SVG mMapSvg;
	private SVGView mSvgView;
	private AirportMap mCurrentMap;
	private ProgressBar mProgressSpinner;
	private boolean mMapLoaded = false;

	public static TerminalMapFragment newInstance() {
		return new TerminalMapFragment();
	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_terminal_map, container, false);

		mSvgView = Ui.findView(view, R.id.svg_view);
		mProgressSpinner = Ui.findView(view, R.id.loading_progress_bar);
		mSvgView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_SVG) && savedInstanceState.containsKey(STATE_AIRPORT_MAP)) {
				mMapSvg = savedInstanceState.getParcelable(STATE_SVG);
				mSvgView.setSVG(mMapSvg);
				mCurrentMap = JSONUtils.getJSONable(savedInstanceState, STATE_AIRPORT_MAP, AirportMap.class);
				mMapLoaded = true;
				setIsLoading(false);
			}
		}

		return view;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(NET_MAP_DOWNLOAD);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_MAP_DOWNLOAD);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(NET_MAP_DOWNLOAD)) {
			bd.registerDownloadCallback(NET_MAP_DOWNLOAD, mSvgDownloadComplete);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mMapLoaded) {
			outState.putParcelable(STATE_SVG, mMapSvg);
			JSONUtils.putJSONable(outState, STATE_AIRPORT_MAP, mCurrentMap);
		}
	}

	/**
	 * We load up the map
	 * @param map
	 */
	public void loadMap(AirportMap map) {
		if (mCurrentMap == null || (mCurrentMap != null && !mCurrentMap.mUrl.equalsIgnoreCase(map.mUrl))) {
			mMapLoaded = false;
			mCurrentMap = map;

			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			if (bd.isDownloading(NET_MAP_DOWNLOAD)) {
				bd.cancelDownload(NET_MAP_DOWNLOAD);
			}
			bd.startDownload(NET_MAP_DOWNLOAD, mSvgDownload, mSvgDownloadComplete);
		}
	}

	private void setIsLoading(final boolean loading) {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				if (loading) {
					mProgressSpinner.setVisibility(View.VISIBLE);
					mSvgView.setVisibility(View.GONE);
				}
				else {
					mProgressSpinner.setVisibility(View.GONE);
					mSvgView.setVisibility(View.VISIBLE);
				}
			}
		};
		getActivity().runOnUiThread(runner);
	}

	private final Download<SVG> mSvgDownload = new Download<SVG>() {
		@Override
		public SVG doDownload() {
			setIsLoading(true);
			ExpediaServices services = new ExpediaServices(getActivity());
			return services.getSvgFromUrl(mCurrentMap.mUrl);
		}
	};

	private final OnDownloadComplete<SVG> mSvgDownloadComplete = new OnDownloadComplete<SVG>() {
		@Override
		public void onDownload(SVG result) {

			if (result == null) {
				Log.e("Failure to download map");
			}
			else {
				mMapSvg = result;
				mSvgView.setSVG(mMapSvg);
				mMapLoaded = true;
			}
			setIsLoading(false);
		}
	};
}
