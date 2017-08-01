package com.expedia.bookings.tracking

import android.content.Context
import com.expedia.bookings.ADMS_Measurement
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.google.common.base.Strings
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class OmnitureTrackingDeeplinkTest {

    private lateinit var context: Context;
    private lateinit var adms: ADMS_Measurement;

    private val AFFCID_TEST_LINK = "TEST_BRAD_AFFCID_UNIVERSAL_LINK"
    private val AFFLID_TEST_LINK = "TEST_BRAD_AFFLID_UNIVERSAL_LINK"
    private val MDPCID_TEST_LINK = "TEST_BRAD_MDPCID_UNIVERSAL_LINK"
    private val MDPDTL_TEST_LINK = "TEST_BRAD_MDPDTL_UNIVERSAL_LINK"
    private val ICMCID_TEST_LINK = "TEST_BRAD_ICMCID_UNIVERSAL_LINK"
    private val ICMDTL_TEST_LINK = "TEST_BRAD_ICMDTL_UNIVERSAL_LINK"
    private val OLACID_TEST_LINK = "TEST_BRAD_OLACID_UNIVERSAL_LINK"
    private val OLADTL_TEST_LINK = "TEST_BRAD_OLADTL_UNIVERSAL_LINK"
    private val SEMCID_TEST_LINK = "TEST_BRAD_SEMCID_UNIVERSAL_LINK"
    private val SEOCID_TEST_LINK = "TEST_BRAD_SEOCID_UNIVERSAL_LINK"
    private val KWORD_TEST_LINK = "Brads_Super_Duper_Test_App_Links"
    private val GCLID_TEST_LINK = "SEMGCLID_KRABI_TEST_GCLID"
    private val BRANDCID_TEST_LINK = "TEST_BRAD_BRANDCID_UNIVERSAL_LINK"

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        OmnitureTracking.getDeepLinkArgs().clear()
        adms = ADMS_Measurement.sharedInstance(context)
        adms.setEvar(22, null)
        adms.setEvar(26, null)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_affcid() {
        OmnitureTracking.setDeepLinkTrackingParams("affcid", AFFCID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("AFF.$AFFCID_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_affcid_afflid() {
        OmnitureTracking.setDeepLinkTrackingParams("affcid", AFFCID_TEST_LINK)
        OmnitureTracking.setDeepLinkTrackingParams("afflid", AFFLID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("AFF.$AFFCID_TEST_LINK&AFFLID=$AFFLID_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_afflid_no_affcid() {
        OmnitureTracking.setDeepLinkTrackingParams("afflid", AFFLID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertTrue { Strings.isNullOrEmpty(adms.getEvar(22)) }
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_emlcid() {
        OmnitureTracking.setDeepLinkTrackingParams("emlcid", MDPCID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("EML.$MDPCID_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_icmcid_icmdtl() {
        OmnitureTracking.setDeepLinkTrackingParams("icmcid", ICMCID_TEST_LINK)
        OmnitureTracking.setDeepLinkTrackingParams("icmdtl", ICMDTL_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("ICM.$ICMCID_TEST_LINK&ICMDTL=$ICMDTL_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_icmcid() {
        OmnitureTracking.setDeepLinkTrackingParams("icmcid", ICMCID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("ICM.$ICMCID_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_icmdtl_no_icmcid() {
        OmnitureTracking.setDeepLinkTrackingParams("icmdtl", ICMDTL_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertTrue { Strings.isNullOrEmpty(adms.getEvar(22)) }
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_mdpcid() {
        OmnitureTracking.setDeepLinkTrackingParams("mdpcid", MDPCID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("MDP.$MDPCID_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_mdpcid_mdpdtl() {
        OmnitureTracking.setDeepLinkTrackingParams("mdpcid", MDPCID_TEST_LINK)
        OmnitureTracking.setDeepLinkTrackingParams("mdpdtl", MDPDTL_TEST_LINK)


        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("MDP.$MDPCID_TEST_LINK&MDPDTL=$MDPDTL_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_mdpdtl_no_mdpcid() {
        OmnitureTracking.setDeepLinkTrackingParams("mdpdtl", MDPDTL_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertTrue { Strings.isNullOrEmpty(adms.getEvar(22)) }
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_olacid() {
        OmnitureTracking.setDeepLinkTrackingParams("olacid", OLACID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("OLA.$OLACID_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_olacid_oladtl() {
        OmnitureTracking.setDeepLinkTrackingParams("olacid", OLACID_TEST_LINK)
        OmnitureTracking.setDeepLinkTrackingParams("oladtl", OLADTL_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("OLA.$OLACID_TEST_LINK&OLADTL=$OLADTL_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_oladtl_no_olacid() {
        OmnitureTracking.setDeepLinkTrackingParams("oladtl", OLADTL_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertTrue { Strings.isNullOrEmpty(adms.getEvar(22)) }
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_semcid() {
        OmnitureTracking.setDeepLinkTrackingParams("semcid", SEMCID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("SEM.$SEMCID_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_semcid_kword() {
        OmnitureTracking.setDeepLinkTrackingParams("semcid", SEMCID_TEST_LINK)
        OmnitureTracking.setDeepLinkTrackingParams("kword", KWORD_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("SEM.$SEMCID_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_gclid_semcid() {
        OmnitureTracking.setDeepLinkTrackingParams("semcid", SEMCID_TEST_LINK)
        OmnitureTracking.setDeepLinkTrackingParams("gclid", GCLID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("$GCLID_TEST_LINK", adms.getEvar(26))
        assertEquals("SEM.$SEMCID_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_seocid() {
        OmnitureTracking.setDeepLinkTrackingParams("seocid", SEOCID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("SEO.$SEOCID_TEST_LINK", adms.getEvar(22))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_brandcid() {
        OmnitureTracking.setDeepLinkTrackingParams("brandcid", BRANDCID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals("Brand.$BRANDCID_TEST_LINK", adms.getEvar(22))
    }


    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_gclid() {
        OmnitureTracking.setDeepLinkTrackingParams("gclid", GCLID_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals(GCLID_TEST_LINK, adms.getEvar(26))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun deeplink_kword() {
        OmnitureTracking.setDeepLinkTrackingParams("kword", KWORD_TEST_LINK)

        OmnitureTracking.addDeepLinkData(adms)
        assertEquals(KWORD_TEST_LINK, adms.getEvar(15))
        assertTrue { Strings.isNullOrEmpty(adms.getEvar(22)) }
    }
}