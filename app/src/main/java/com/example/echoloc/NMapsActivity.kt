package com.example.echoloc


import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.echoloc.database.Pref
import com.example.echoloc.locationapi.LocationManager
import com.example.echoloc.model.LocationModel
import com.example.echoloc.permission.PermissionManager
import com.google.android.gms.location.*
import com.google.firebase.database.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import com.naver.maps.map.widget.LocationButtonView
import com.naver.maps.map.widget.ZoomControlView
import kotlinx.android.synthetic.main.activity_nmaps.*

class NMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private val LOCATION_PERMISSION_REQUEST_CODE: Int = 1000
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap
    private lateinit var markers: ArrayList<Marker>
    var isCnt: Boolean = false
    lateinit var locationButtonView: LocationButtonView

    var group_id = ""
    private lateinit var pref: Pref
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nmaps)

        group_id = intent.extras!!.getString("group_id", "")
        pref = Pref(applicationContext)
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Echoloc").child("location")
        markers = ArrayList()

        mapView = findViewById(R.id.nMap)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            Toast.makeText(this, "위치 공유 시작", Toast.LENGTH_SHORT).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionManager.requestPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) {
                    requestLocation()
                }
            } else {
                PermissionManager.requestPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION) {
                    requestLocation()
                }
            }
        }

    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
        naverMap.uiSettings.isScaleBarEnabled = false
        naverMap.uiSettings.isLocationButtonEnabled = false

        locationButtonView = findViewById(R.id.locationButtonView)
        locationButtonView.map = naverMap
    }

    private fun requestLocation() {
        LocationManager.Builder.create(this).request(true) { latitude, longitude ->
            val locationModel =
                LocationModel(pref.getData("id"), pref.getData("name"), pref.getData("call"), latitude, longitude)
            databaseReference.child(group_id).child(pref.getData("id")).setValue(locationModel).addOnCompleteListener{
                getMarkers()
            }
        }
    }

    private fun getMarkers() {

        databaseReference.child(group_id).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (isCnt) {
                    for (i in 0 until markers.size) {
                        markers[i].map = null
                    }
                    markers.clear()
                }
                for (data in snapshot.children) {
                    // 마커찍기
                    val locationModel = data.getValue(LocationModel::class.java)
                    val marker = Marker()
                    marker.position = LatLng(locationModel!!.latitude, locationModel.longitude)
                    markers.add(marker)

                }
                for (i in 0 until markers.size) {
                    markers[i].map = naverMap
                    isCnt = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                requestLocation()
            }
        })
    }
}