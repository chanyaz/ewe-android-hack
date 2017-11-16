package android.security

import com.expedia.ui.javademo.FeedItem
import com.expedia.ui.javademo.FeedItemList
import org.junit.Test

class FeedItemListTests {
    @Test
    fun testMyClass() {
        val feedItemList = FeedItemList()
        feedItemList.add(FeedItem("", "asdasd"))
        feedItemList.add(FeedItem("", 2))

        val get = feedItemList.get(1)
        get.bindingData as String
        assert(false)


        val asd = ArrayList<String>()
               val a1sd :List<Any> = asd.map{it as Any}

    }
}