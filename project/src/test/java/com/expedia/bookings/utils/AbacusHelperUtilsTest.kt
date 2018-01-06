package com.expedia.bookings.utils

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusResponse
import com.expedia.bookings.data.abacus.AbacusTest
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.util.ForceBucketPref
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class AbacusHelperUtilsTest {
    val context = RuntimeEnvironment.application

    lateinit var test1: ABTest
    lateinit var test2: ABTest
    lateinit var test3: ABTest

    @Before
    fun setup() {
        val activeTests = AbacusUtils.getActiveTests()
        assertTrue(activeTests.size > 2)

        test1 = ABTest(activeTests[0])
        test2 = ABTest(activeTests[1])
        test3 = ABTest(activeTests[2])

        val abacusResponse = getNewResponse()
        Db.sharedInstance.setAbacusResponse(abacusResponse)

        assertFalse(AbacusFeatureConfigManager.isUserBucketedForTest(context, test1))
        assertTrue(AbacusFeatureConfigManager.isUserBucketedForTest(context, test2))
        assertFalse(AbacusFeatureConfigManager.isUserBucketedForTest(context, test3))
    }

    @Test
    fun testForceBucketingOverridesOnlyOneTest() {
        //Force bucket the user
        ForceBucketPref.setUserForceBucketed(context, true)
        ForceBucketPref.saveForceBucketedTestKeyValue(context, test1.key, 1)
        Db.sharedInstance.abacusResponse.forceUpdateABTest(test1.key, 1)

        assertEquals(1, ForceBucketPref.getForceBucketedTestValue(context, test1.key, -1))
        assertTrue(AbacusFeatureConfigManager.isUserBucketedForTest(context, test1))
        assertTrue(AbacusFeatureConfigManager.isUserBucketedForTest(context, test2))
        assertFalse(AbacusFeatureConfigManager.isUserBucketedForTest(context, test3))
        assertEquals(0, Db.sharedInstance.abacusResponse.testForKey(test3).value)

        AbacusHelperUtils.updateAbacusResponse(getNewResponse())
        AbacusHelperUtils.updateForceBucketedTests(context)

        assertEquals(1, ForceBucketPref.getForceBucketedTestValue(context, test1.key, -1))
        assertTrue(AbacusFeatureConfigManager.isUserBucketedForTest(context, test1))
        assertTrue(AbacusFeatureConfigManager.isUserBucketedForTest(context, test2))
        assertFalse(AbacusFeatureConfigManager.isUserBucketedForTest(context, test3))
        assertEquals(0, Db.sharedInstance.abacusResponse.testForKey(test3).value)
    }

    private fun getNewResponse(): AbacusResponse? {
        val response = AbacusResponse()
        val map: Map<Int, AbacusTest> = mapOf(
                Pair(test1.key, getTest(test1.key, 0)),
                Pair(test2.key, getTest(test2.key, 1)),
                Pair(test3.key, getTest(test3.key, 0)))

        response.setAbacusTestMap(map)
        return response
    }

    private fun getTest(id: Int, value: Int): AbacusTest {
        val test = AbacusTest()
        test.id = id
        test.value = value
        test.instanceId = 0
        return test
    }
}
