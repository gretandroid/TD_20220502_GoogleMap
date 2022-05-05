package com.example.td_20220502_googlemap

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class Service(
    val name: String,
    val category : String,
    val username : String,
    val latLng: LatLng,
    val address: String
): ClusterItem {
    override fun getPosition(): LatLng =
        latLng

    override fun getTitle(): String =
        name

    override fun getSnippet(): String =
        address

}

