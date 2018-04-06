package com.expedia.bookings.analytics.cesc

import com.expedia.bookings.analytics.AppAnalytics
import org.joda.time.DateTime

class CESCTrackingUtil(private val cescDataUtil: PersistingCESCDataUtil) {

    private val CID_MAP = mapOf("affcid" to "AFF",
            "brandcid" to "Brand",
            "emlcid" to "EML",
            "icmcid" to "ICM",
            "mdpcid" to "MDP",
            "olacid" to "OLA",
            "semcid" to "SEM",
            "seocid" to "SEO")

    private val LID_DTL_MAP = mapOf("affcid" to Pair("afflid", "aff"),
            "emlcid" to Pair("emldtl", "eml"),
            "icmcid" to Pair("icmdtl", "icm"),
            "mdpcid" to Pair("mdpdtl", "dps"),
            "olacid" to Pair("oladtl", "ola"),
            "semcid" to Pair("kword", "sem"))

    fun setEvars(s: AppAnalytics, dateNow: DateTime) {
        setAffEvars(s, dateNow)
        setBrandEvars(s, dateNow)
        setEmlEvars(s, dateNow)
        setIcmEvars(s, dateNow)
        setMdpEvars(s, dateNow)
        setOlaEvars(s, dateNow)
        setSemEvars(s, dateNow)
        setKwordEvar(s, dateNow)
        setGcLid(s, dateNow)
        setSeoCid(s, dateNow)
        setPushCid(s, dateNow)
    }

    fun storeMarketingCode(deepLinkHashMap: HashMap<String, String>, dateNow: DateTime) {
        deepLinkHashMap.forEach { (key, value) ->
            if (CID_MAP.containsKey(key)) {
                val marketingCode = CID_MAP[key]
                val evarValue = "$marketingCode.$value"
                cescDataUtil.add("cid", evarValue, dateNow)
                cescDataUtil.add("cidVisit", evarValue, dateNow)

                LID_DTL_MAP[key]?.let { (urlCode, cesc) ->
                    deepLinkHashMap[urlCode]?.let { lidOrDtl ->
                        cescDataUtil.add(cesc, "$evarValue.$lidOrDtl", dateNow)
                    }
                }
            }

            when (key) {
                "semdtl" -> cescDataUtil.add("semdtl", value, dateNow)
                "gclid" -> cescDataUtil.add("gclid", value, dateNow)
                "pushcid" -> cescDataUtil.add("push", value, dateNow)
            }
        }
    }

    private fun setAffEvars(s: AppAnalytics, dateNow: DateTime) {
        setCidEvars(s, dateNow)
        setLidOrDtlEvar(16, "aff", s, dateNow)
    }

    private fun setBrandEvars(s: AppAnalytics, dateNow: DateTime) {
        setCidEvars(s, dateNow)
    }

    private fun setEmlEvars(s: AppAnalytics, dateNow: DateTime) {
        setCidEvars(s, dateNow)
        setLidOrDtlEvar(13, "eml", s, dateNow)
    }

    private fun setIcmEvars(s: AppAnalytics, dateNow: DateTime) {
        setCidEvars(s, dateNow)
        setLidOrDtlEvar(21, "icm", s, dateNow)
    }

    private fun setMdpEvars(s: AppAnalytics, dateNow: DateTime) {
        setCidEvars(s, dateNow)
        setLidOrDtlEvar(14, "dps", s, dateNow)
    }

    private fun setOlaEvars(s: AppAnalytics, dateNow: DateTime) {
        setCidEvars(s, dateNow)
        setLidOrDtlEvar(19, "ola", s, dateNow)
    }

    private fun setSemEvars(s: AppAnalytics, dateNow: DateTime) {
        setCidEvars(s, dateNow)
        setLidOrDtlEvar(36, "semdtl", s, dateNow)
    }

    private fun setKwordEvar(s: AppAnalytics, dateNow: DateTime) {
        setLidOrDtlEvar(15, "sem", s, dateNow)
    }

    private fun setSeoCid(s: AppAnalytics, dateNow: DateTime) {
        setCidEvars(s, dateNow)
    }

    private fun setGcLid(s: AppAnalytics, dateNow: DateTime) {
        setLidOrDtlEvar(26, "gclid", s, dateNow)
    }

    private fun setPushCid(s: AppAnalytics, dateNow: DateTime) {
        if (cescDataUtil.shouldTrackStoredCidVisit(dateNow, "push")) {
            val evarValue = cescDataUtil.getEvarValue("push")!!
            cescDataUtil.add("push", evarValue, dateNow)
            s.setEvar(11, evarValue)
        }
    }

    private fun setCidEvars(s: AppAnalytics, dateNow: DateTime) {
        if (cescDataUtil.shouldTrackStoredCidVisit(dateNow)) {
            cescDataUtil.getEvarValue("cidVisit")?.let { evarValue ->
                s.setEvar(0, evarValue)
                s.setEvar(10, evarValue)
                cescDataUtil.add("cidVisit", evarValue, dateNow)
            }
        } else if (cescDataUtil.shouldTrackStoredCesc(dateNow, "cid")) {
            cescDataUtil.getEvarValue("cid")?.let { evarValue ->
                s.setEvar(10, evarValue)
            }
        }
    }

    private fun setLidOrDtlEvar(evarNum: Int, cesc: String, s: AppAnalytics, dateNow: DateTime) {
        if (cescDataUtil.shouldTrackStoredCesc(dateNow, cesc)) {
            s.setEvar(evarNum, cescDataUtil.getEvarValue(cesc))
        }
    }
}
