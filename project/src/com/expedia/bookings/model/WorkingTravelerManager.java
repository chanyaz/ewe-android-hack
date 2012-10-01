package com.expedia.bookings.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;

import org.json.JSONObject;

import android.content.Context;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.mobiata.android.Log;

public class WorkingTravelerManager {

	private static final String WORKING_TRAVELER_FILE_NAME = "working_traveler";

	private WorkingTravelerManager() {
	}

	private static WorkingTravelerManager mManager;
	private Traveler mWorkingTraveler; //The traveler we want to use
	private Traveler mBaseTraveler; //The traveler the working traveler was copied from... this is to give us an idea of what changed...
	private Semaphore mTravelerSaveSemaphore;

	public static WorkingTravelerManager getInstance() {
		if (mManager == null) {
			mManager = new WorkingTravelerManager();
		}
		return mManager;
	}

	public boolean hasTravelerOnDisk(Context context) {
		String[] files = context.fileList();
		for (String filename : files) {
			if (filename.compareTo(WORKING_TRAVELER_FILE_NAME) == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If there is a working traveler stored on disk, load it in
	 */
	public void loadWorkingTravelerFromDisk(final Context context) {

		Runnable loadTravalerFromDisk = new Runnable() {
			@Override
			public void run() {
				try {
					if (mWorkingTraveler == null) {
						//If traveler is null attemp to load the traveler from the cache
						if (hasTravelerOnDisk(context)) {
							//We have found our cache file which indicates we should load it up...
							FileInputStream stream = context.openFileInput(WORKING_TRAVELER_FILE_NAME);
							InputStreamReader reader = new InputStreamReader(stream);
							BufferedReader bufferedReader = new BufferedReader(reader);
							String contents = "";
							String line = bufferedReader.readLine();
							while (line != null) {
								contents += line;
								line = bufferedReader.readLine();
							}
							reader.close();
							stream.close();

							JSONObject json = new JSONObject(contents);
							getWorkingTraveler().fromJson(json);
						}

					}
				}
				catch (Exception ex) {
					Log.e("Exception loading saved traveler file.", ex);
				}
				finally {
					mTravelerSaveSemaphore.release();
				}
			}
		};

		if (tryAquireSaveTravelerSemaphore()) {
			Thread loadSavedTravelerThread = new Thread(loadTravalerFromDisk);
			loadSavedTravelerThread.start();
			//Block until our operation is done.
			mTravelerSaveSemaphore.acquireUninterruptibly();
			mTravelerSaveSemaphore.release();
		}
	}

	/**
	 * Set the current "working" traveler to be a copy of the traveler argument
	 * @param traveler
	 */
	public void rebaseWorkingTraveler(Traveler traveler) {
		mWorkingTraveler = new Traveler();
		mBaseTraveler = new Traveler();
		JSONObject json = traveler.toJson();
		mWorkingTraveler.fromJson(json);
		mBaseTraveler.fromJson(json);
	}

	/**
	 * Get a working traveler object. This will be a persistant traveler object that can be used to manipulate
	 * @return
	 */
	public Traveler getWorkingTraveler() {
		if (mWorkingTraveler == null) {
			mWorkingTraveler = new Traveler();
		}
		return mWorkingTraveler;
	}

	/**
	 * If the current traveler was created by calling rebaseWorkingTraveler we store a copy of the argument traveler.
	 * We can then use origin traveler to compare to our working traveler and figure out what has changed.
	 * @return
	 */
	public Traveler getBaseTraveler() {
		return mBaseTraveler;
	}

	/**
	 * Save the current working traveler to the Db object effectively commiting the changes locally.
	 * @param travelerNumber
	 */
	public void commitWorkingTravelerToDB(int travelerNumber) {
		while (travelerNumber >= Db.getTravelers().size()) {
			Db.getTravelers().add(new Traveler());
		}
		Traveler commitTrav = new Traveler();
		commitTrav.fromJson(mWorkingTraveler.toJson());
		Db.getTravelers().set(travelerNumber, commitTrav);
	}

	/**
	 * Call tryAquireState on the mTravelerSAveSempahore
	 * @return
	 */
	public boolean tryAquireSaveTravelerSemaphore() {
		if (mTravelerSaveSemaphore == null) {
			mTravelerSaveSemaphore = new Semaphore(1);
		}
		return mTravelerSaveSemaphore.tryAcquire();
	}

	/**
	 * Wait for the TravelerSaveSemaphore
	 */
	public void aquireSaveTravelerSemaphore() {
		if (mTravelerSaveSemaphore == null) {
			mTravelerSaveSemaphore = new Semaphore(1);
		}
		try {
			mTravelerSaveSemaphore.acquire();
		}
		catch (InterruptedException e) {
			Log.e("Thread interrupted while waiting to aquire the semaphore", e);
		}
	}

	/**
	 * This attempts to save the working traveler to a file in it's own thread.
	 * If another save operation is currently being performed this will be skipped.
	 * 
	 * @param context
	 * @param force - If true we wait to aquire the semaphore. If false we only run if the semaphore is available. 
	 * Basically if we are saving progress this should always be false, because we assume it will be called again after another change. 
	 * However, if the traveler is in a state that we want to make sure the save gets written to the disk, this should be set to true.
	 */
	public void attemptWorkingTravelerSave(final Context context, boolean force) {

		Runnable saveTempTraveler = new Runnable() {
			@Override
			public void run() {
				try {
					//TODO: remove any private data we don't want to write to disk
					String travelerString = mWorkingTraveler.toJson().toString();
					FileOutputStream fos = context.openFileOutput(WORKING_TRAVELER_FILE_NAME, Context.MODE_PRIVATE);
					fos.write(travelerString.getBytes());
					fos.flush();
					fos.close();
				}
				catch (IOException e) {
					Log.e("Error saving working traveler.", e);
				}
				finally {
					mTravelerSaveSemaphore.release();
				}
			}
		};

		if (force) {
			aquireSaveTravelerSemaphore();
			Thread saveThread = new Thread(saveTempTraveler);
			saveThread.start();
		}
		else if (tryAquireSaveTravelerSemaphore()) {
			Thread saveThread = new Thread(saveTempTraveler);
			saveThread.start();
		}
	}

	/**
	 * Clear out the working traveler
	 */
	public void clearWorkingTraveler(Context context) {
		mWorkingTraveler = null;
		mBaseTraveler = null;
		context.deleteFile(WORKING_TRAVELER_FILE_NAME);
	}

}
