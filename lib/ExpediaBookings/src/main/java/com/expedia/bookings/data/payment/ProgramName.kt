package com.expedia.bookings.data.payment

enum class ProgramName {
    ExpediaRewards, Orbucks, CheapCash, BonusPlus, MrJetCash;

    companion object {
        fun valueOf(name: String): ProgramName? {
            try {
                return ProgramName.valueOf(name)
            } catch (e: IllegalArgumentException) {
                return null
            }
        }
    }
}
