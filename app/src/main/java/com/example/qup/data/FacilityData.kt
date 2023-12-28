package com.example.qup.data

import com.google.android.gms.maps.model.LatLng

//temporary attraction data testing purposes
class FacilityData {
    //list of attractions for setu
    private val setuAttractionList = listOf(
        Attraction("Engineering & Sciences Building", LatLng(52.24603522725519, -7.139575116116906)),
        Attraction("School of Business", LatLng(52.24590088731708, -7.1384170061713)),
        Attraction("Walton Building", LatLng(52.2457368280431, -7.137318108777412)),
        Attraction("Tourism & Leisure Building", LatLng(52.245383829709844, -7.141781656807669)),
        Attraction("SETU Luke Wadding Library", LatLng(52.24541169986971, -7.137813952798044)),
    )

    private val facilitiesList = listOf(
        Facility("SETU", LatLng(52.245866910002846, -7.138898812594175), 11f, setuAttractionList)
    )

    fun getFacilities(): List<Facility> = facilitiesList.toMutableList()

}