package com.expedia.bookings.data.abacus

enum class AbacusVariant(val value: Int) {
    DEBUG(-1),
    NO_BUCKET(-1),
    CONTROL(0),
    BUCKETED(1),
    ONE(1),
    TWO(2),
    THREE(3);
}
