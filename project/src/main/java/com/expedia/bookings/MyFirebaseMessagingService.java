/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.expedia.bookings;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.expedia.bookings.data.Codes;
import com.expedia.bookings.lob.lx.ui.activity.LXBaseActivity;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService{

    private static final String TAG = "MyFirebaseMsgService";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final GeoActivity geoActivity = new GeoActivity();
    public static final String EXTRA_IS_GROUND_TRANSPORT = "IS_GROUND_TRANSPORT";
    /**
     *
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;

    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }
    /**
     * The list of geofences used in this sample.
     */
    private ArrayList<Geofence> mGeofenceList;

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
//        String expuserId = Db.getUser().getExpediaUserId();
//
//        Log.d(TAG, "expuserId: " + expuserId);

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ false) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow(remoteMessage);
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow(RemoteMessage remoteMessage) {
        Log.d(TAG, "Short lived task is done.");

        String location = remoteMessage.getData().get("locationString");
        String locationAirportCode = remoteMessage.getData().get("locationAirportCode");
        String suggestedProduct = remoteMessage.getData().get("suggestedProduct");
        String intent = remoteMessage.getData().get("intent");
        String startDate = remoteMessage.getData().get("startDate");
        String lat = remoteMessage.getData().get("lat");
        String longitude = remoteMessage.getData().get("long");

        if ("gf".equals(suggestedProduct)) {
            geoActivity.onCreate(getApplicationContext());
            geoActivity.addGeofencesButtonHandler(getApplicationContext());
        } else {
            createNotification(suggestedProduct, location, startDate);
        }
    }


//    *********** show notification *****************

    private void createNotification(String suggestedProduct, String location, String startDate) {

        PendingIntent intent = null;
        Bitmap bitmap_image = null;
        Bitmap bitmap_icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_notification);
        NotificationCompat.Builder nb= new NotificationCompat.Builder(this);
        nb.setSmallIcon(R.drawable.ic_stat_expedia);
        nb.setLargeIcon(bitmap_icon);
//        nb.setContentTitle("Expedia!");
        nb.setAutoCancel(true);
        //get the bitmap to show in notification bar
        // BEGIN_INCLUDE(intent)
        //Create Intent to launch this Activity again if the notification is clicked.
        NotificationCompat.BigPictureStyle s = new NotificationCompat.BigPictureStyle();
        if ("rh".equals(suggestedProduct)) {
            intent = createRhIntent();
            bitmap_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.rh_notification_img);
            nb.setContentText("Ride now with Uber");
            s.setSummaryText("Ride now for hassle free airport commute");
            nb.setContentTitle("Need a ride?");
        } else if ("gt".equals(suggestedProduct)) {
            intent = createGtIntent(location, startDate);
            bitmap_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.gt_notification_img);
            nb.setContentText("Schedule a ride with us");
            nb.setContentTitle("Need Ground Transfer?");
            s.setSummaryText("Great selection of airport transfers");
        } else if ("lx".equals(suggestedProduct)) {
            intent = createLxIntent(location, startDate);
            bitmap_image = BitmapFactory.decodeResource(this.getResources(), R.drawable.lx_notification_img);
            nb.setContentText("Explore what's near by");
            nb.setContentTitle("Feeling bored?");
            s.setSummaryText("Great selection of activities around you");
        }
        s.bigPicture(bitmap_image);
        s.bigLargeIcon(bitmap_icon);
        nb.setStyle(s);
        nb.setContentIntent(intent);
        // END_INCLUDE(intent)
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(0, nb.build());
    }
//				intent.putExtra(Codes.FROM_DEEPLINK_TO_DETAILS, true);
    private PendingIntent createLxIntent(String location, String startDate){
        Intent intent1 = new Intent(this, LXBaseActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.putExtra(Codes.EXTRA_OPEN_RESULTS, true);
        intent1.putExtra("location", location);
        intent1.putExtra("startDateStr", startDate);
//        intent1.putExtra("endDateStr", "2017-06-30");
        PendingIntent intent = PendingIntent.getActivity(this, 0, intent1,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }

    private PendingIntent createGtIntent(String location, String startDate){
        Intent intent1 = new Intent(this, LXBaseActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent1.putExtra(EXTRA_IS_GROUND_TRANSPORT, true);
        intent1.putExtra(Codes.EXTRA_OPEN_RESULTS, true);
        intent1.putExtra("location", location);
        intent1.putExtra("startDateStr", startDate);
        PendingIntent intent = PendingIntent.getActivity(this, 0, intent1,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }

    private PendingIntent createRhIntent(){
        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
        resultIntent.setData(Uri.parse("http://ubr.to/2noQerV"));
        PendingIntent intent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return intent;
    }
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.sample_main);
//    }
    /**
     * Create and show a notification with a custom layout.
     * This callback is defined through the 'onClick' attribute of the
     * 'Show Notification' button in the XML layout.
     *
     * @param v
     */
//    public void showNotificationClicked(View v) {
//        createNotification();
//    }

//    private fun sendTravelNotifications(String expediaUserId, tuid: String, deviceId: String, email: String, tripId: String?, itinNumber: String?){
//        val thread = Thread(Runnable {
//            try {
//                val httpclient = DefaultHttpClient()
//                val httppost = HttpPost("http://DELC02NG1WGG3QC.sea.corp.expecn.com:8080/notification/bookingConfirmation")
//                httppost.addHeader("Accept", "application/json")
//                httppost.addHeader("Content-Type", "application/json")
//                val json = JSONObject()
//                json.put("expUserId", expediaUserId)
//                json.put("tuId", tuid)
//                json.put("emailAddress", email)
//                json.put("deviceId", deviceId)
//                json.put("tripId", tripId)
//                json.put("itinId", itinNumber)
//                val params = StringEntity(json.toString())
//                httppost.entity = params
//                //execute http post
//                val response = httpclient.execute(httppost)
//                Log.e(response.toString())
//                //Your code goes here
//            } catch (e: Exception) {
//                Log.e("ERROR Occurred!!!")
//                e.printStackTrace()
//            }
//        })
//        thread.start()
//    }
}
