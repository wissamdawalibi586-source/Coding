package com.example.fakestore.data.auth

import javax.inject.Inject

/**
 * Source of the current time. Injected so tests can move time forward instantly
 * instead of waiting 60 real seconds for a token to expire.
 */
fun interface Clock {
    fun nowMillis(): Long
}

class SystemTimeClock @Inject constructor() : Clock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
