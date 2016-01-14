package com.expedia.bookings.data.payment

class CalculatePointsParams(val tripId: String, val programName: ProgramName, val amount: String, val rateId: String) {
    class Builder {
        private var tripId: String? = null
        private var programName: ProgramName? = null
        private var amount: String? = null
        private var rateId: String? = null

        fun tripId(tripId: String?): CalculatePointsParams.Builder {
            this.tripId = tripId
            return this
        }

        fun programName(programName: ProgramName?): CalculatePointsParams.Builder {
            this.programName = programName
            return this
        }

        fun amount(amount: String?): CalculatePointsParams.Builder {
            this.amount = amount
            return this
        }

        fun rateId(rateId: String?): CalculatePointsParams.Builder {
            this.rateId = rateId
            return this
        }

        fun build(): CalculatePointsParams {
            return CalculatePointsParams(tripId ?: throw IllegalArgumentException(),
                    programName ?: throw IllegalArgumentException(),
                    amount ?: throw IllegalArgumentException(),
                    rateId ?: throw IllegalArgumentException())
        }
    }
}
