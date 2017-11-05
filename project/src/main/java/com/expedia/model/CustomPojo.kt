package com.expedia.model

open class CustomPojo (var monthAndYear: String, var featureList: Array<FeaturePojo>) {

    init {
        println("Customer initialized with value ${monthAndYear}")
    }

    override fun toString() = "$monthAndYear" + "\n" + listToString(featureList)

    fun listToString(list : Array<FeaturePojo>): String {
        var string = ""
        for (item in list) {
            string += "       -" + item
            string += "\n"
        }
        return string
    }
}