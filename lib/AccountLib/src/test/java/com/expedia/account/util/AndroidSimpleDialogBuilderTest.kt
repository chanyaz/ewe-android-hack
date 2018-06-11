package com.expedia.account.util

import android.content.Context
import android.content.DialogInterface
import com.expedia.account.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
class AndroidSimpleDialogBuilderTest {

    val context: Context = RuntimeEnvironment.application

    @Test
    fun showSimpleDialog_showsDialog() {
        val sut = AndroidSimpleDialogBuilder(context)

        sut.showSimpleDialog(R.string.acct__Create_account_failed_TITLE, R.string.acct__Create_account_failed, android.R.string.ok)

        val dialogShadow = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertEquals("Account creation failed", dialogShadow.title)
        assertEquals("Please try again.", dialogShadow.message)
    }

    @Test
    fun showDialogWithItemList_showsDialog() {
        val sut = AndroidSimpleDialogBuilder(context)
        var mostRecentClickedItem = Integer.MAX_VALUE

        sut.showDialogWithItemList("My dialog title", arrayOf("Item 1", "Item 2"),
                DialogInterface.OnClickListener { _, which -> mostRecentClickedItem = which })

        val dialogShadow = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertEquals("My dialog title", dialogShadow.title)
        assertEquals(2, dialogShadow.items.size)
        assertEquals("Item 1", dialogShadow.items[0])
        assertEquals("Item 2", dialogShadow.items[1])

        dialogShadow.clickOnItem(0)
        assertEquals(0, mostRecentClickedItem)
    }
}
