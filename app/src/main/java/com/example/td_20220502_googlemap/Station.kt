package com.example.td_20220502_googlemap

import com.google.android.gms.maps.model.LatLng


data class Station (
    val name: String,
    val latLng: LatLng,
    val address: String
)