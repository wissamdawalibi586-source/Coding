package com.example.fakestore.ui.common

import java.text.NumberFormat
import java.util.Locale

/** FakeStore prices are in US dollars. */
fun Double.formatPrice(): String = NumberFormat.getCurrencyInstance(Locale.US).format(this)
