package com.expedia.bookings.text

import android.annotation.TargetApi
import android.os.Build
import android.text.Html
import android.text.Spanned

class HtmlCompat {
    companion object {
        private val htmlObject = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            HtmlCompatNougat()
        } else {
            HtmlCompatBase()
        }

        @JvmStatic fun fromHtml(source: String): Spanned {
            return htmlObject.fromHtml(source)
        }

        @JvmStatic fun stripHtml(source: String): String {
            return htmlObject.fromHtml(source).toString()
        }

        @JvmStatic fun fromHtml(source: String, imageGetter: Html.ImageGetter?, tagHandler: Html.TagHandler?): Spanned {
            return htmlObject.fromHtml(source, imageGetter, tagHandler)
        }
    }

    private interface IHtmlCompat {
        fun fromHtml(source: String): Spanned
        fun fromHtml(source: String, imageGetter: Html.ImageGetter?, tagHandler: Html.TagHandler?): Spanned
    }

    @TargetApi(Build.VERSION_CODES.N)
    private class HtmlCompatNougat : IHtmlCompat {
        override fun fromHtml(source: String): Spanned {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
        }

        override fun fromHtml(source: String, imageGetter: Html.ImageGetter?, tagHandler: Html.TagHandler?): Spanned {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY, imageGetter, tagHandler)
        }
    }

    @Suppress("DEPRECATION")
    private class HtmlCompatBase : IHtmlCompat {
        override fun fromHtml(source: String): Spanned {
            return Html.fromHtml(source)
        }

        override fun fromHtml(source: String, imageGetter: Html.ImageGetter?, tagHandler: Html.TagHandler?): Spanned {
            return Html.fromHtml(source, imageGetter, tagHandler)
        }
    }
}
