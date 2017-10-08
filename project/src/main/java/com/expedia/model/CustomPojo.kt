package com.expedia.model

open class CustomPojo (var monthAndYear: String, var featureList: Array<String>) {

    init {
        println("Customer initialized with value ${monthAndYear}")
    }

    override fun toString() = "$monthAndYear" + "\n" + listToString(featureList)

    fun listToString(list : Array<String>): String {
        var string = ""
        for (item in list) {
            string += "       -" + item
            string += "\n"
        }
        return string
    }
}