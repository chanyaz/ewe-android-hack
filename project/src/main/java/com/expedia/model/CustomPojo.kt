package com.expedia.model

/**
 * Created by nbirla on 20/11/17.
 */

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