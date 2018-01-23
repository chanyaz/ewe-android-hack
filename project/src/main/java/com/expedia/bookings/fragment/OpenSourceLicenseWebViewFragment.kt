package com.expedia.bookings.fragment

import android.os.Bundle
import com.expedia.bookings.R
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

        mHtmlData = try {
            IoUtils.convertStreamToString(activity.assets.open("open_source_licenses.html"))
        } catch (e: IOException) {
            HtmlUtils.wrapInHeadAndBody(this.getString(R.string.open_source_software_licenses_error))
        }
    }
}
