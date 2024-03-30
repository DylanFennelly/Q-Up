package com.example.qup.data

import kotlinx.serialization.Serializable

@Serializable
data class Attraction(
    val id: Int,                //Internal ID of attraction
    val name: String,           //Attraction name
    val description: String,    //Attraction description
    val type: String,           //Type of attraction (rollercoaster, exhibit, show, restaurant, etc.)
    val status: String,         //Status of attraction (Open, Maintenance, Closed)
    val cost: Float,            //Cost of attraction    - TODO: localised currencies
    val length: Int,          //Length of attraction in seconds
    val lat: Double,            //Map latitude of attraction
    val lng: Double             //Map longitude of attraction
)