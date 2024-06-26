package com.example.qup.data

import kotlinx.serialization.Serializable

@Serializable
data class Attraction(
    val id: Int,                                           //Internal ID of attraction
    val name: String = "Attraction",                       //Attraction name
    val description: String = "Attraction Description",    //Attraction description
    val type: String = "Attraction Type",                  //Type of attraction (rollercoaster, exhibit, show, restaurant, etc.)
    val status: String = "Closed",                         //Status of attraction (Open, Maintenance, Closed)
    val cost: Float = 0.00f,                               //Cost of attraction    - TODO: localised currencies
    val length: Int = 0,                                   //Length of attraction in seconds
    val lat: Double = 0.0,                                 //Map latitude of attraction
    val lng: Double = 0.0,                                 //Map longitude of attraction
    val avg_capacity: Int = 1,                             //average amount of people on attraction per run - TODO: doesnt account for multiple ride cars at once as is
    val max_capacity: Int = 0,                             //max amount of people attraction can support
    val in_queue: Int = 0,                                 //number of people in queue for attraction
)