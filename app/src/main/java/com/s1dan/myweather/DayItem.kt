package com.s1dan.myweather

data class DayItem(
    val city: String,
    val day: String,
    val condition: String,
    val imageUrl: String,
    val currentTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val hours: String
    )
