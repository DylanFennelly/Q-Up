package com.example.qup.data

import com.google.android.gms.maps.model.LatLng

//temporary attraction data testing purposes
class AttractionData {
    //list of attractions for setu
    private val setuAttractionList = listOf(
        Attraction("Attraction 1", LatLng(0.0, 0.0)),
        Attraction("Attraction 2", LatLng(1.0, 0.0)),
        Attraction("Attraction 3", LatLng(2.0, 0.0)),
    )

    fun getSetuAttractions(): List<Attraction> = setuAttractionList.toMutableList()

}