package com.example.qup.data

import com.google.android.gms.maps.model.LatLng

data class Attraction (
    var name: String,       //attraction name
    var latlng: LatLng      //attraction coordinates
)