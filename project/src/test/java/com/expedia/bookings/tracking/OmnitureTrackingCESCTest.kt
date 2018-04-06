package com.expedia.bookings.tracking

import com.expedia.bookings.analytics.AppAnalytics
import com.expedia.bookings.analytics.cesc.CESCTrackingUtil
import com.expedia.bookings.analytics.cesc.PersistingCESCDataUtil
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class OmnitureTrackingCESCTest {
    private lateinit var adms: AppAnalytics
    private lateinit var dateNow: DateTime
    private lateinit var validVisitTime: DateTime
    private lateinit var validMarketingTime: DateTime
    private lateinit var expiredVisitTime: DateTime
    private lateinit var expiredMarketingTime: DateTime
    private lateinit var persistingDataUtil: PersistingCESCDataUtil
    private lateinit var cescTrackingUtil: CESCTrackingUtil
    private lateinit var deepLinkArgs: HashMap<String, String>

    @Before
    fun setup() {
        persistingDataUtil = PersistingCESCDataUtil(MockCESCPersistenceProvider())
        cescTrackingUtil = CESCTrackingUtil(persistingDataUtil)
        deepLinkArgs = HashMap()
        adms = AppAnalytics()
        dateNow = DateTime.now()
        validVisitTime = dateNow.minusMinutes(20)
        validMarketingTime = dateNow.minusDays(20)
        expiredVisitTime = dateNow.minusMinutes(40)
        expiredMarketingTime = dateNow.minusDays(40)
    }

    @Test
    fun testTrackNewCidAndCidVisit() {
        deepLinkArgs.put("affcid", "aff_cid")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, dateNow)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertCidAndCidVisitTrackedCorrectly(trackCid = true, trackCidVisit = true)
    }

    @Test
    fun testTrackValidCidWithoutExpiredCidVisit() {
        deepLinkArgs.put("affcid", "aff_cid")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, expiredVisitTime)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertCidAndCidVisitTrackedCorrectly(trackCid = true, trackCidVisit = false)
    }

    @Test
    fun testTrackValidCidAndCidVisit() {
        deepLinkArgs.put("affcid", "aff_cid")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, validVisitTime)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertCidAndCidVisitTrackedCorrectly(trackCid = true, trackCidVisit = true)
    }

    @Test
    fun testDontTrackExpiredCidAndCidVisit() {
        deepLinkArgs.put("affcid", "aff_cid")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, expiredMarketingTime)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertCidAndCidVisitTrackedCorrectly(trackCid = false, trackCidVisit = false)
    }

    @Test
    fun testTrackAffLid() {
        deepLinkArgs.put("affcid", "aff_cid")
        deepLinkArgs.put("afflid", "aff_lid")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, dateNow)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertEquals("AFF.aff_cid.aff_lid", adms.getEvar(16))
    }

    @Test
    fun testTrackEmlDtl() {
        deepLinkArgs.put("emlcid", "eml_cid")
        deepLinkArgs.put("emldtl", "eml_dtl")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, dateNow)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertEquals("EML.eml_cid.eml_dtl", adms.getEvar(13))
    }

    @Test
    fun testTrackIcmDtl() {
        deepLinkArgs.put("icmcid", "icm_cid")
        deepLinkArgs.put("icmdtl", "icm_dtl")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, dateNow)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertEquals("ICM.icm_cid.icm_dtl", adms.getEvar(21))
    }

    @Test
    fun testTrackMdpDtl() {
        deepLinkArgs.put("mdpcid", "mdp_cid")
        deepLinkArgs.put("mdpdtl", "mdp_dtl")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, dateNow)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertEquals("MDP.mdp_cid.mdp_dtl", adms.getEvar(14))
    }

    @Test
    fun testTrackOlaDtl() {
        deepLinkArgs.put("olacid", "ola_cid")
        deepLinkArgs.put("oladtl", "ola_dtl")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, dateNow)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertEquals("OLA.ola_cid.ola_dtl", adms.getEvar(19))
    }

    @Test
    fun testDontTrackLidOrDtl() {
        deepLinkArgs.put("affcid", "aff_cid")
        deepLinkArgs.put("afflid", "aff_lid")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, expiredMarketingTime)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertEquals(null, adms.getEvar(16))
    }

    @Test
    fun testTrackSemDtl() {
        deepLinkArgs.put("semdtl", "sem_dtl")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, dateNow)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertEquals("sem_dtl", adms.getEvar(36))
    }

    @Test
    fun testDontTrackSemDtl() {
        deepLinkArgs.put("semdtl", "sem_dtl")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, expiredMarketingTime)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertEquals(null, adms.getEvar(36))
    }

    @Test
    fun testTrackKword() {
        deepLinkArgs.put("semcid", "sem_cid")
        deepLinkArgs.put("kword", "kword_id")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, dateNow)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertEquals("SEM.sem_cid.kword_id", adms.getEvar(15))
    }

    @Test
    fun testTrackGcLid() {
        deepLinkArgs.put("gclid", "gc_lid")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, dateNow)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertEquals("gc_lid", adms.getEvar(26))
    }

    @Test
    fun testTrackPushCid() {
        deepLinkArgs.put("pushcid", "push_cid")
        cescTrackingUtil.storeMarketingCode(deepLinkArgs, dateNow)
        cescTrackingUtil.setEvars(adms, dateNow)

        assertEquals("push_cid", adms.getEvar(11))
    }

    private fun assertCidAndCidVisitTrackedCorrectly(trackCid: Boolean, trackCidVisit: Boolean, evarValue: String = "AFF.aff_cid") {
        if (trackCid) assertEquals(evarValue, adms.getEvar(10)) else assertEquals(null, adms.getEvar(10))
        if (trackCidVisit) assertEquals(evarValue, adms.getEvar(0)) else assertEquals(null, adms.getEvar(0))
        assertEquals(null, adms.getEvar(16))
    }
}
