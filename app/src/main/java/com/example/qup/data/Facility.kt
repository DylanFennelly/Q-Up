package com.example.qup.data

import com.google.android.gms.maps.model.LatLng

data class Facility (
    var name: String,   //name of the facility
    var location: LatLng,       //Location of facility to zoom into on map
    var mapZoom: Float,         //level of zoom for map
    var Attractions: List<Attraction>       //list of Attractions
)