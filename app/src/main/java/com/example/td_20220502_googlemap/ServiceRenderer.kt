package com.example.td_20220502_googlemap

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class ServiceRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<Service>
): DefaultClusterRenderer<Service>(context, map, clusterManager) {

    /**
     * The icon to use for each cluster item
     */

    private val perfumeIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(context, R.color.black)
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.perfume_icon)}

    private val shoesIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(context, R.color.black)
        BitmapHelper.vectorToBitmap(
            context,
            R.drawable.shoes_icon)}
    /**
     * Method called before the cluster item (the marker) is rendered.
     * This is where marker options should be set.
     */
    override fun onBeforeClusterItemRendered(item: Service, markerOptions: MarkerOptions) {
        markerOptions
            //.title("@hamidoux")
            //.title(item.name + " - " +"@hamidoux")
            .title(item.name)
            //.title(item.category)     // Ca ecrase parfum //TODO : trouver une autre facon de faire dans l'api
            .position(item.latLng)
            .icon(item.icon)
    }

    /**
     * Method called right after the cluster item (the marker) is rendered.
     * This is where properties for the Marker object should be set.
     */
    override fun onClusterItemRendered(clusterItem: Service, marker: Marker) {
        marker.tag = clusterItem
    }
}
