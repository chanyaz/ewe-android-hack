package com.mobiata.android;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import com.mobiata.android.util.AndroidUtils;

/**
 * Class for downloading server messages in the background.  It allows
 * an Activity to register for the response, keeping the actual management
 * of when to display results out of the hands of the Activity (which is a damn
 * mess otherwise).
 * 
 * One important concept for using this class is the idea of a download key; each
 * download should have a unique key which links the program back to that download.
 */
public class BackgroundDownloader {

	/**
	 * Since ConcurrentHashMaps can't store null values, this is variable acts as the null result.
	 */
	private final Integer NULL_RESULT = -1;

	/**
	 * Keeps track of all the downloads currently in progress.
	 */
	private ConcurrentHashMap<String, DownloadTask> mTasks;

	/**
	 * This holds the results of downloads that finished and had no callback set at the time.
	 * For example, this can happen when a download finishes but the user is rotating the screen. 
	 */
	private ConcurrentHashMap<String, Object> mResults;

	/**
	 * Holds all of the listeners for downloads.
	 */
	private ConcurrentHashMap<String, DownloadListener> mListeners;

	private BackgroundDownloader() {
		mResults = new ConcurrentHashMap<>();
		mTasks = new ConcurrentHashMap<>();
		mListeners = new ConcurrentHashMap<>();
	}

	private static class BackgroundDownloaderHolder {
		private static final BackgroundDownloader INSTANCE = new BackgroundDownloader();
	}

	public static BackgroundDownloader getInstance() {
		return BackgroundDownloaderHolder.INSTANCE;
	}

	/**
	 * Method for both starting and registering an initial callback.  Generally,
	 * one should check if there is a download in progress and if so call 
	 * registerDownloadCallback() instead to save time.
	 * @param key
	 * @param download
	 * @param callback
	 */
	@TargetApi(11)
	public <T> void startDownload(String key, Download<T> download, OnDownloadComplete<T> callback) {
		Log.d(Params.LOGGING_TAG, "Starting download: " + key);

		// Check that we're not already downloading; if we are, just register
		// the callback.
		if (isDownloading(key)) {
			Log.d(Params.LOGGING_TAG, "Download already in progress, continuing: " + key);
			registerDownloadCallback(key, callback);
			return;
		}

		// Start the task
		if (!mTasks.containsKey(key)) {
			DownloadTask task = new DownloadTask(key, download);
			mTasks.put(key, task);

			// To keep old behavior, we need to execute on the thread pool executor.  For more info,
			// see the blog post below:
			//
			// http://commonsware.com/blog/2012/04/20/asynctask-threading-regression-confirmed.html
			if (AndroidUtils.getSdkVersion() >= Build.VERSION_CODES.HONEYCOMB) {
				task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			else {
				task.execute();
			}
		}

		// Register the callback
		registerDownloadCallback(key, callback);
	}

	public void addDownloadListener(String key, DownloadListener listener) {
		mListeners.put(key, listener);
	}

	public void removeDownloadListener(String key) {
		mListeners.remove(key);
	}

	/**
	 * @param key The download key
	 * @return true if the download is in progress (or has finished but the results have not yet been retrieved).
	 */
	public boolean isDownloading(String key) {
		return mResults.containsKey(key) || mTasks.containsKey(key);
	}

	/**
	 * Cancels a download in progress, or deletes the results of a finished download where the results
	 * have yet to be retrieved.
	 * @param key The download key
	 * @return true if the task was canceled (or the task did not exist), false otherwise
	 */
	public boolean cancelDownload(String key) {
		Log.d(Params.LOGGING_TAG, "Cancelling download: " + key);

		boolean retVal = true;
		if (mResults.containsKey(key)) {
			// If we got results but the download was cancelled, delete the results
			mResults.remove(key);
		}
		else if (mTasks.containsKey(key)) {
			// If the task is currently running, cancel it
			DownloadTask task = mTasks.get(key);
			boolean success = task.cancel(true);
			mTasks.remove(key); // We need to remove here in case someone wants to immediately start a new rather than wait
			retVal = success;
		}

		if (mListeners.containsKey(key)) {
			mListeners.get(key).onCancel();
			mListeners.remove(key);
		}

		// If there was no task to cancel, but that's good enough - just want to
		// make sure it's not running.
		return retVal;
	}

	/**
	 * Registers a callback for an in-progress download.  If the download is already complete,
	 * it immediately executes the callback.  If there is no download related to the key, this
	 * does nothing.
	 * 
	 * @param key the download key
	 * @param callback the callback to be executed when download finishes.
	 */
	public void registerDownloadCallback(String key, OnDownloadComplete<?> callback) {
		Log.v(Params.LOGGING_TAG, "Registering download: " + key);

		// Check if we got results recently for this request; this is in
		// order to handle the rare circumstance where the results come in
		// RIGHT as the user flips orientation, such that the data isn't cached
		// yet in the Activity itself.
		if (mResults.containsKey(key)) {
			doCallback(key, callback);
			mResults.remove(key);
			return;
		}

		if (mTasks.containsKey(key)) {
			mTasks.get(key).registerCallback(callback);
		}
	}

	public void unregisterDownloadCallback(String key) {
		Log.v(Params.LOGGING_TAG, "Unregistering download: " + key);

		if (mTasks.containsKey(key)) {
			DownloadTask task = mTasks.get(key);
			task.unregisterCallback();
		}
	}

	public void unregisterDownloadCallback(String key, OnDownloadComplete<?> onDownloadComplete) {
		if (mTasks.containsKey(key)) {
			DownloadTask task = mTasks.get(key);
			task.unregisterCallback(onDownloadComplete);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doCallback(String key, OnDownloadComplete callback) {
		Log.d(Params.LOGGING_TAG, "Doing callback: " + key);

		Object results = mResults.get(key);
		// Workaround to handle null items in a ConcurrentHashMap
		if (NULL_RESULT.equals(results)) {
			results = null;
		}
		callback.onDownload(results);
	}

	/**
	 * Defines what to do for the actual download process.
	 */
	public interface Download<T> {
		public T doDownload();
	}

	/**
	 * Defines how to handle the download after it is complete.
	 */
	public interface OnDownloadComplete<T> {
		public void onDownload(T results);
	}

	/**
	 * Allows a class to "listen" to the download to see if the status
	 * changes mid-download.
	 */
	public interface DownloadListener {
		public void onCancel();
	}

	/**
	 * Task which downloads in the background.  Automatically handles
	 * registering itself with downloader's task list and results cache.
	 */
	@SuppressWarnings("rawtypes")
	private class DownloadTask extends AsyncTask<Void, Void, Object> {

		private String mKey;
		private Download mDownload;
		private List<OnDownloadComplete> mCallbacks;

		public DownloadTask(String key, Download download) {
			mKey = key;
			mDownload = download;
			mCallbacks = new ArrayList<>();
		}

		public void registerCallback(OnDownloadComplete callback) {
			if (mCallbacks == null) {
				mCallbacks = new ArrayList<>();
			}

			if (!mCallbacks.contains(callback)) {
				mCallbacks.add(callback);
			}
		}

		public void unregisterCallback() {
			mCallbacks.clear();
		}

		public void unregisterCallback(OnDownloadComplete callback) {
			mCallbacks.remove(callback);
		}

		@Override
		protected Object doInBackground(Void... params) {
			return mDownload.doDownload();
		}

		@Override
		protected void onPostExecute(Object data) {
			// Workaround to handle null items in a ConcurrentHashMap
			if (data == null) {
				data = NULL_RESULT;
			}
			mResults.put(mKey, data);
			mTasks.remove(mKey);
			mListeners.remove(mKey);
			if (mCallbacks != null && mCallbacks.size() > 0) {
				for (OnDownloadComplete callBack : mCallbacks) {
					doCallback(mKey, callBack);
				}
				mResults.remove(mKey);
			}
		}
	}
}
