package com.example.td_20220502_googlemap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.td_20220502_googlemap.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.InfoWindowAdapter {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val LOCATION_REQ_CODE = 456

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //4) Si on n'a pas la permission on la demande
        if ((ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
                    ) && (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_REQ_CODE
            );
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //2) on s’abonne  au clic sur la fenêtre d'un marker
        mMap.setOnInfoWindowClickListener(this);
        //Créer sa propre fenêtre lors d'un clic sur un marker
        mMap.setInfoWindowAdapter(this);

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        if (getLocation())  mMap.setMyLocationEnabled(true);
    }

    fun afficher(view: View) {
        val lieu = LatLng(45.45, 4.50)
        mMap.addMarker(MarkerOptions().position(lieu).title("Marker in lyon"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(lieu))
        //Pour obtenir une animation plutot qu’un saut on remplace
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(lieu));

    }

    override fun onInfoWindowClick(marker: Marker) {
        //3)Des que l'on clique sur le titre un toast va s'afficher
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();
    }

    override fun getInfoWindow(marker: Marker): View? {
        // 1. Get tag
        val station1=Station("Title", LatLng(45.45, 4.50),"Adresse")

        // 2. Inflate view and set title, address, and rating
        val view = LayoutInflater.from(applicationContext).inflate(
            R.layout.marker_layout, null
        )
        view.findViewById<TextView>(
            R.id.text_view_title
        ).text = station1.name
        view.findViewById<TextView>(
            R.id.text_view_address
        ).text = station1.address
        return view
    }

    override fun getInfoContents(p0: Marker): View? {
        return null
    }

    fun getLocation(): Boolean {
        return  ((ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                ) && (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                ))
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (getLocation()) {
            if (mMap != null) mMap.isMyLocationEnabled = true
        }
    }

}