package com.example.echoloc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import com.example.echoloc.Directions.*
import com.example.echoloc.database.Pref
import com.example.echoloc.model.LocationModel
import com.google.android.gms.location.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapView
import kotlinx.android.synthetic.main.activity_tdirection.*

class TDirectionActivity : AppCompatActivity(), View.OnClickListener {

    val MY_PERMISSION_ACCESS_ALL = 100

    private var API_Key = "l7xxa73f10582d1447c3a43f973a639cf0d2"
    lateinit var tMapView: TMapView
    lateinit var tmapPoint: TMapPoint

    lateinit var sName: String
    lateinit var eName: String
    lateinit var sLat: String
    lateinit var sLon: String
    lateinit var eLat: String
    lateinit var eLon: String

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var pref: Pref
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var group_id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tdirection)

        btn_myLocation.setOnClickListener(this)
        btn_stop_direction.setOnClickListener(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setTMapAuth()
        initialize()

        sName = intent.extras!!.getString("sName").toString()
        eName = intent.extras!!.getString("eName").toString()
        sLat = intent.extras!!.getDouble("sLat").toString()
        sLon = intent.extras!!.getDouble("sLon").toString()
        eLat = intent.extras!!.getDouble("eLat").toString()
        eLon = intent.extras!!.getDouble("eLon").toString()

        tMapView.setCenterPoint(sLon.toDouble(), sLat.toDouble())
        val markerItem = TMapMarkerItem()
        markerItem.tMapPoint = TMapPoint(eLat.toDouble(), eLon.toDouble())
        markerItem.name = eName
        tMapView.addMarkerItem("End Point", markerItem)

        try {
            val findElapsedTimeTask = FindElapsedTimeTask(applicationContext, tMapView)
            findElapsedTimeTask.execute("1", API_Key,
            sLon.toString(), sLat.toString(),
            eLon.toString(), eLat.toString(),
            sName, eName)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        pref = Pref(applicationContext)
        group_id = intent.extras!!.getString("group_id", "")
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Echoloc").child("location").child(group_id)


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            var permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSION_ACCESS_ALL)
        } else {
            requestLocationUpdate()
        }

    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdate() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult == null) {
                return
            }

            for (location in locationResult.locations) {
                if (location != null) {
                    val locationModel = LocationModel(pref.getData("id"), pref.getData("name"),
                        pref.getData("call"), location.latitude, location.longitude, pref.getData("profile"))
                        databaseReference.child(pref.getData("id")).setValue(locationModel).addOnSuccessListener {
                        tmapPoint = TMapPoint(location.latitude, location.longitude)
                        tMapView.setLocationPoint(tmapPoint.longitude, tmapPoint.latitude)
                    }
                }
            }
        }
    }

    private fun setTMapAuth() {
        tMapView = TMapView(this)
        tMapView.setSKTMapApiKey(API_Key)
    }

    private fun initialize() {
        tMapView.zoomLevel = 18
        tMapView.mapType = TMapView.MAPTYPE_STANDARD
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN)
        tMapView.setIconVisibility(true)
        tMapView.setSightVisible(true)

        val linearLayoutTmap = findViewById<View>(R.id.linearLayoutTmapDirection) as LinearLayout
        linearLayoutTmap.addView(tMapView)

    }

    override fun onClick(p0: View?) {
        when(p0) {
            btn_myLocation -> {
                tMapView.setCenterPoint(tmapPoint.longitude, tmapPoint.latitude)
            }

            btn_stop_direction -> {
                val intent = Intent(applicationContext, TMapsActivity::class.java)
                intent.putExtra("group_id", group_id)
                startActivity(intent)
                finish()
            }
        }
    }



}