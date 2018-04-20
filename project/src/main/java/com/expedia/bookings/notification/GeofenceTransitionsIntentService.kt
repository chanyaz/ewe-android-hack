package com.expedia.bookings.notification

import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.expedia.bookings.R
import com.expedia.bookings.data.foursquare.FourSquareResponse
import com.expedia.bookings.data.foursquare.Item_
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.services.FourSquareServices
import com.expedia.bookings.utils.Ui
import com.google.android.gms.location.GeofencingEvent
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import java.io.InputStream
import java.net.URL
import javax.inject.Inject
import `in`.mamga.carousalnotification.Carousal
import `in`.mamga.carousalnotification.CarousalItem
import com.expedia.bookings.data.foursquare.Items
import com.expedia.bookings.notification.carousel.CarouselNotificationManager
import com.expedia.bookings.notification.carousel.NotificationModel
import com.expedia.bookings.utils.FoursquareResponseUtil
import java.util.Arrays


/**
 * Created by nbirla on 14/02/18.
 */
class GeofenceTransitionsIntentService(val TAG: String = "GeofenceTransitionsIS") : IntentService(TAG) {

    lateinit var fourSquareService: FourSquareServices
        @Inject set
    var searchSubscriber: Disposable? = null

    override fun onCreate() {
        super.onCreate()

        Ui.getApplication(this).defaultFourSquareComponents()
        fourSquareService = Ui.getApplication(this).fourSquareComponent().fourSquareServices()
    }

    override fun onHandleIntent(intent: Intent?) {
        val event = GeofencingEvent.fromIntent(intent)
        if (event.hasError()) {
            Log.e(TAG, "GeofencingEvent Error: " + event.errorCode)
            return
        }

        val oldResponse = FoursquareResponseUtil.loadResponse(this)

        val venueId = GeofencingEvent.fromIntent(intent).triggeringGeofences.first().requestId

        val item = oldResponse?.response?.groups?.first()?.items?.find { it.venue.id.equals(venueId) }

        if (item != null) {
            searchSubscriber = fourSquareService?.getImages(venueId)?.subscribeObserver(makeResultsObserver(item.venue.name + ": " + item.tips.get(0).text, "google.navigation:q=" + item.venue.location.lat + "," + item.venue.location.lng))
        }
    }

    private fun makeResultsObserver(desc: String, deeplink: String): Observer<FourSquareResponse> {
        return object : DisposableObserver<FourSquareResponse>() {
            override fun onError(e: Throwable) {
                //reset alarm
            }

            override fun onComplete() {
                //do nothing
            }

            override fun onNext(t: FourSquareResponse) {
                if (t.response.photos.items.isNotEmpty()) {
                    sendNotification(desc, t.response.photos.items, deeplink)
                }
            }

        }
    }

    private fun sendNotification(notificationDetails: String, items: List<Items>, pi: String) {

        val model = NotificationModel()
        model.title = "Expedia Companion"
        model.contentText = notificationDetails
        model.doPlaySound = 1
        model.imageCarouselArray = arrayListOf()
        model.deeplink = pi

        items.forEach {
            model.imageCarouselArray.add(it.prefix + "500x300" + it.suffix)
        }

        val carouselNotificationManager = CarouselNotificationManager(applicationContext)
        carouselNotificationManager.processGCMImageCarousel(1, model)


        //=======================================================

//        val carousal = Carousal.with(this).beginTransaction()
//
//        carousal.setContentTitle("Expedia Companion").setContentText(notificationDetails)
//
//        items.forEach {
//            val imageUrl = it.prefix + "500x300" + it.suffix
//            val cItem = CarousalItem("Item Id here", null, "Item Content",imageUrl)
//            carousal.addCarousalItem(cItem);
//        }
//
//        carousal.setBigContentTitle("Expedia Companion").setBigContentText(notificationDetails)
//
//        carousal.buildCarousal();


        //===========================================================================================



//        // Create an explicit content Intent that starts MainActivity.
//        val notificationIntent = Intent(this, PhoneLaunchActivity::class.java)
//
//        // Get a PendingIntent containing the entire back stack.
//        val notificationPendingIntent = PendingIntent.getActivities(this, 0, arrayOf(notificationIntent), PendingIntent.FLAG_UPDATE_CURRENT)
//
//        // Get a notification builder that's compatible with platform versions >= 4
//
//        val builder = NotificationCompat.Builder(this)
//
//        // Define the notification settings.
//        builder.setColor(Color.RED)
//                .setSmallIcon(R.drawable.ic_expedia_white_logo_small)
//                .setContentTitle(notificationDetails)
//                .setContentIntent(notificationPendingIntent)
//                .setAutoCancel(true)
//                .setStyle(NotificationCompat.BigPictureStyle()
//                .bigPicture(decodeStream(URL(imageUrl).getContent() as InputStream)))
//                .addAction(android.R.drawable.ic_dialog_map, "", pi)
//
//        // Fire and notify the built Notification.
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(0, builder.build())
    }

    companion object {
        val NOTIFICATION_DESC = "NOTIFICATION_DESC"
        val VENUE_ID = "VENUE_ID"
        val VENUE_LOC = "VENUE_LOC"

        @JvmStatic
        fun generateSchedulePendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, GeofenceTransitionsIntentService::class.java)
            return PendingIntent.getService(context, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}
