package com.expedia.bookings.test.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.jayway.android.robotium.solo.*;

public class ScreenshotUtils{
	private static final String LOG_TAG = "Robotium Screenshot";
	private Solo mSolo;
	private String mDirectory;
	
	public ScreenshotUtils(/*Instrumentation instrumentation, Activity activity,*/String directoryName, Solo mSolo) {
		mDirectory = directoryName;
		this.mSolo = mSolo;
	}
	
	public void screenshot(String title){
		View currentView = mSolo.getCurrentViews().get(0);
		takeScreenshot(currentView, title);
	}
	
	public void setScreenshotDir(String directory){
		mDirectory = directory;
	}
	
	public void takeScreenshot(final View view, final String name){
		
		mSolo.getCurrentActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(view !=null){
					view.destroyDrawingCache();
					view.buildDrawingCache(false);
					Bitmap b = view.getDrawingCache();
					FileOutputStream fos = null;
					SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss");
					String fileName = null;
					if(name == null)
						fileName = sdf.format( new Date()).toString()+ ".jpg";
					else
						fileName = name + ".jpg";
					File directory = new File(Environment.getExternalStorageDirectory() + "/" + mDirectory +"/");
					directory.mkdir();

					File fileToSave = new File(directory,fileName);
					try {
						fos = new FileOutputStream(fileToSave);
						if (b.compress(Bitmap.CompressFormat.JPEG, 100, fos) == false)
							Log.d(LOG_TAG, "Compress/Write failed");
						fos.flush();
						fos.close();
					} catch (Exception e) {
						Log.d(LOG_TAG, "Can't save the screenshot! Requires write permission (android.permission.WRITE_EXTERNAL_STORAGE) in AndroidManifest.xml of the application under test.");
						e.printStackTrace();
					}
					view.destroyDrawingCache();
				}
			}
		});
	}

}
