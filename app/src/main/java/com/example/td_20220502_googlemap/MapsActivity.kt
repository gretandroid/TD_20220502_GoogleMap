package com.example.td_20220502_googlemap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.td_20220502_googlemap.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.clustering.ClusterManager
import kotlin.math.max
import kotlin.math.min

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.InfoWindowAdapter {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val LOCATION_REQ_CODE = 456
    private lateinit var stations: ArrayList<Station>
    private lateinit var services: ArrayList<Service>

    private lateinit var servicesFilteredList: ArrayList<Service>
    lateinit var clusterManager: ClusterManager<Service>

    var xMin = -180.0
    var xMax = 180.0
    var yMin = -90.0
    var yMax = 90.0


    private val perfumeIcon: BitmapDescriptor by lazy {
        //val color = ContextCompat.getColor(this, R.color.black)
        BitmapHelper.vectorToBitmap(this, R.drawable.perfume_icon)
    }

    private val shoesIcon: BitmapDescriptor by lazy {
        //val color = ContextCompat.getColor(this, R.color.black)
        BitmapHelper.vectorToBitmap(this, R.drawable.shoes_icon)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        services = arrayListOf()
        servicesFilteredList = arrayListOf()

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categories = resources.getStringArray(R.array.Categories)

        val spinner = findViewById<Spinner>(R.id.spinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, categories)
            spinner.adapter = adapter
            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
                    Toast.makeText(this@MapsActivity,
                        getString(R.string.selected_item) + " " +
                                "" + categories[position], Toast.LENGTH_LONG).show()

                    if (services.size > 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            servicesFilteredList.addAll(services)
                            servicesFilteredList.removeIf { it.category != categories[position] }
                            addClusteredMarkers(mMap)

                        }
                    }

                    Toast.makeText(
                        this@MapsActivity,
                        "Texte du toast "+ services.size + " " + servicesFilteredList.size,
                        Toast.LENGTH_LONG).show()

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }
        }


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
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), LOCATION_REQ_CODE
            )
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
        // [Pour le lateinit]
        clusterManager = ClusterManager<Service>(this, mMap)

        //2) on s’abonne  au clic sur la fenêtre d'un marker
        mMap.setOnInfoWindowClickListener(this)
        //Créer sa propre fenêtre lors d'un clic sur un marker
        mMap.setInfoWindowAdapter(this)

        // Add a marker in Sydney and move the camera
       // val sydney = LatLng(-34.0, 151.0)
     //   mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
     //   mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        // Au démarrage, on affiche notre position actuelle
        val locationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location=locationManager.getLastKnownLocation(locationManager.getBestProvider(Criteria(),true)!!)
        if (location!=null){
            val position = LatLng(location.latitude, location.longitude)
            mMap.addMarker(MarkerOptions().position(position).title("Ma position"))
            // zoomLevel is set at 10 for a City-level view

            val zoomLevel = 10f
            // latLng contains the coordinates where the marker is added
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,zoomLevel))
            addItems()
            //Toast.makeText(this,location.longitude.toString()+" "+location.latitude.toString(),Toast.LENGTH_LONG).show()
        }


        if (getLocation()) mMap.setMyLocationEnabled(true);
    }

    @SuppressLint("MissingPermission")
    fun afficher(view: View) {

        //Pour obtenir une animation plutot qu’un saut on remplace
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(lieu));
//        addMarkers(mMap)

        addClusteredMarkers(mMap)
        clusterManager.clearItems()

    }
    @SuppressLint("MissingPermission")
    fun afficher_tout(view: View) {

        val googleMap = mMap
        clusterManager.renderer =
            ServiceRenderer(
                this,
                googleMap,
                clusterManager
            )

        //[clearing the items]
        clusterManager.clearItems()

        clusterManager.addItems(services)
        clusterManager.cluster()

        // Set ClusterManager as the OnCameraIdleListener so that it
        // can re-cluster when zooming in and out.
        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }
        // TODO : corriger deplacement map pour l'affichage de tous les points
        setXYMinAndMax()
        //val position2=LatLng((xMin+xMax)/2, (yMin+yMax)/2)
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(position2))
        //Toast.makeText(this, "xmin et max : "+xMin + " " + xMax,Toast.LENGTH_LONG).show()

    }

    override fun onInfoWindowClick(marker: Marker) {
        //3)Des que l'on clique sur le titre un toast va s'afficher
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show()
    }

    /*override fun getInfoWindow(marker: Marker): View? {
        // 1. Get tag
        val station = marker?.tag as? Station ?: return null

        // 2. Inflate view and set title, address, and rating
        val view = LayoutInflater.from(applicationContext).inflate(
            R.layout.marker_layout, null
        )
        view.findViewById<TextView>(
            R.id.text_view_title
        ).text = station.name
        view.findViewById<TextView>(
            R.id.text_view_address
        ).text = station.address
        return view
    }*/

    override fun getInfoWindow(marker: Marker): View? {
        // 1. Get tag
        val service = marker?.tag as? Service ?: return null

        // 2. Inflate view and set title, address, and rating
        val view = LayoutInflater.from(applicationContext).inflate(
            R.layout.marker_layout, null
        )
        view.findViewById<TextView>(
            R.id.text_view_title
        ).text = service.name
        view.findViewById<TextView>(
            R.id.text_view_address
        ).text = service.address
        return view
    }

    override fun getInfoContents(p0: Marker): View? {
        return null
    }

    fun getLocation(): Boolean {
        return ((ActivityCompat.checkSelfPermission(
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
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (getLocation()) {
            if (mMap != null) mMap.isMyLocationEnabled = true
        }
    }

    private fun addItems() {

        // Set some lat/lng coordinates to start with.

            val service1 = Service(
                "Hamid parfum",
                //"Parfum",
                getString(R.string.PARFUM),
                "@hamid",
                LatLng(48.595125, 2.582722),
                "Savigny-le-Temple",
                perfumeIcon)
            services.add(service1)

            val service2 = Service(
                "Collection Exclusive",
                getString(R.string.PARFUM),
                "@co_exclu",
                LatLng(48.652951, 2.395266),
                "Grigny",
                perfumeIcon)
            services.add(service2)

            val service3 = Service(
                "Nanou Parfums",
                getString(R.string.PARFUM),
                "@nanou",
                LatLng(48.881568, 2.375982),
                "Paris",
                perfumeIcon)
            services.add(service3)

            val service4 = Service(
                "Bob L'anyienss",
                //"Prêt-à-porter",
                getString(R.string.PRET_A_PORTER),
                "@boblanyienss",
                LatLng(48.628814, 2.426340),
                "Evry",
                shoesIcon)
            services.add(service4)

            val service5 = Service(
                "Clicli",
                getString(R.string.PRET_A_PORTER),
                "@clicli_puce",
                LatLng(48.902010, 2.342847),
                "Saint-Ouen",
                shoesIcon)
            services.add(service5)

        }
    private fun addMarkers(googleMap: GoogleMap) {
        addItems()

        servicesFilteredList.forEach { service ->
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .title(service.name)
                    .position(service.latLng)
                    .icon(service.icon)

            )
            //On ajoute la service comme tag au marker pour l’afficher
            marker?.tag = service
        }

//On definit la position de départ de la camera qui peut etre le point initial des poi
        //val position=LatLng(48.59343582342353, 2.5773162739219613)
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(position))
    }

    private fun addClusteredMarkers(googleMap: GoogleMap) {

        clusterManager.renderer =
            ServiceRenderer(
                this,
                googleMap,
                clusterManager
            )


        //New: clearing the items
        clusterManager.clearItems()

        clusterManager.addItems(servicesFilteredList)
        clusterManager.cluster()

        // Set ClusterManager as the OnCameraIdleListener so that it
        // can re-cluster when zooming in and out.
        googleMap.setOnCameraIdleListener {
            clusterManager.onCameraIdle()
        }
        // Londres
        //val position=LatLng(51.5145160, -0.1270060)
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(position))
    }

    private fun setXYMinAndMax() {
        if (services.size > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                xMin = services.get(0).latLng.longitude
                xMax = xMin
                yMin = services.get(0).latLng.latitude
                yMax = yMin
                for (i in services) {
                    xMin = min(xMin, i.latLng.longitude)
                    xMax = max(xMax, i.latLng.longitude)
                    yMin = min(yMin, i.latLng.latitude)
                    yMax = max(yMax, i.latLng.latitude)
                }
            }
        }
    }

    }