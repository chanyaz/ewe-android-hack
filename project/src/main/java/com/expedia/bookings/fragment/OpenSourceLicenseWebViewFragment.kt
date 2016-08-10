package com.expedia.bookings.fragment

import android.os.Bundle
import com.google.android.gms.common.GoogleApiAvailability
import com.mobiata.android.util.HtmlUtils
import com.mobiata.android.util.IoUtils
import java.io.IOException

class OpenSourceLicenseWebViewFragment : WebViewFragment() {

    companion object {
        @JvmStatic
        fun newInstance(): OpenSourceLicenseWebViewFragment {
            val frag = OpenSourceLicenseWebViewFragment()

            // provide empty args as WebViewFragment requires non-null args
            frag.arguments = Bundle()
            frag.retainInstance = true

            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val license = GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(activity)
        var gps = ""
        if (license != null) {
            gps = "<h3>Google Play Services</h3>\n<pre>\n" + HtmlUtils.escape(license) + "</pre>\n"
        }

        try {
            mHtmlData = IoUtils.convertStreamToString(activity.assets.open("open_source_licenses.html")).replace("{gps}", gps)
        } catch (e: IOException) {
            mHtmlData = HtmlUtils.wrapInHeadAndBody(gps)
        }
    }
}