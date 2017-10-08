package com.expedia.model

open class CustomPojo (var name: String) {

    init {
        println("Customer initialized with value ${name}")
    }

    override fun toString() = "$name"
}