package com.example.echoloc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.echoloc.database.Pref
import com.example.echoloc.locationapi.LocationManager
import com.example.echoloc.locationapi.service.LocationService
import com.example.echoloc.model.LocationModel
import com.example.echoloc.permission.PermissionManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(),
    OnMapReadyCallback, View.OnClickListener,
    GoogleMap.OnMyLocationButtonClickListener{

    private lateinit var  mMap: GoogleMap
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var group_id = ""

    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var pref: Pref

    lateinit var list: ArrayList<LocationModel>
    lateinit var mList: ArrayList<Marker>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        btn_menu.setOnClickListener(this)
        btn_cancel.setOnClickListener(this)
        btn_grp_select.setOnClickListener(this)
        btn_chat.setOnClickListener(this)
        btn_call.setOnClickListener(this)
        btn_setting.setOnClickListener(this)

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Echoloc").child("location")

        group_id = intent.extras!!.getString("group_id", "")
        pref = Pref(applicationContext)
        list = ArrayList()
        mList = ArrayList<Marker>()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.btn_start).setOnClickListener{
            Toast.makeText(this, "위치 공유 시작", Toast.LENGTH_SHORT).show()
            btn_start.visibility = View.GONE
            btn_stop.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionManager.requestPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                {
                    requestLocation()
                }
            } else {
                PermissionManager.requestPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                {
                    requestLocation()
                }
            }

            getMarkers()
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener{
            Toast.makeText(this, "위치 공유 종료", Toast.LENGTH_SHORT).show()
            LocationManager.stop(this)
            btn_start.visibility = View.VISIBLE
            btn_stop.visibility = View.GONE
        }

        if (LocationService.isLocationServiceRunning) {
            requestLocation()
        }

    }

    private fun requestLocation() {
        LocationManager.Builder.create(this).request(true) { latitude, longitude ->

            var locationModel = LocationModel(pref.getData("id"), pref.getData("name"), pref.getData("call"), latitude, longitude)
            var reference = databaseReference.child(group_id)
            reference.child(pref.getData("id")).setValue(locationModel).addOnCompleteListener{

                // 데이터 전송 성공 시 마커 생성/업데이트

//                myMarker = mMap.addMarker(MarkerOptions()
//                    .position(myLatLng)
//                    .title(pref.getData("name"))
//                    .snippet(pref.getData("call")))!!
//
//                if (myMarker != null) {
//                    myMarker.remove()
//                    myMarker = mMap.addMarker(MarkerOptions()
//                        .position(myLatLng)
//                        .title(pref.getData("name"))
//                        .snippet(pref.getData("call")))!!
//                }
            }

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        getLocation()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        mMap.isMyLocationEnabled = true
//        mMap.setOnMyLocationButtonClickListener(this)

        mMap!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                card_view.visibility = View.VISIBLE
                tv_user_name.text = marker.title
                tv_user_tellNum.text = marker.snippet

                return false
            }
        })

        mMap!!.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
            override fun onMapClick(p0: LatLng) {
                card_view.visibility = View.GONE
            }
        })

    }

    private  fun getLocation() {
        val task: Task<Location> = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        task.addOnSuccessListener {
            if (it != null) {
                val mylocation = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 17F))
            }
        }
    }


    override fun onClick(p0: View?) {
        when(p0)
        {
            btn_menu ->
            {
                btn_menu.visibility = View.GONE
                btn_grp_select.visibility = View.VISIBLE
                btn_chat.visibility = View.VISIBLE
                btn_cancel.visibility = View.VISIBLE
                btn_call.visibility = View.VISIBLE
                btn_setting.visibility = View.VISIBLE
            }

            btn_cancel ->
            {
                btn_menu.visibility = View.VISIBLE
                btn_grp_select.visibility = View.GONE
                btn_chat.visibility = View.GONE
                btn_cancel.visibility = View.GONE
                btn_call.visibility = View.GONE
                btn_setting.visibility = View.GONE
            }

            btn_grp_select ->
            {
                LocationManager.stop(this)
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            btn_chat ->
            {
                val intent = Intent(applicationContext, PublicGroupChatting::class.java)
                intent.putExtra("group_id", group_id)
                startActivity(intent)
            }

            btn_call ->
            {
                // 추가해야 됨
            }

            btn_setting ->
            {
                // 설정 창 만들면 추가
            }

        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "내 위치로 이동", Toast.LENGTH_SHORT)
            .show()
        return false
    }

    private fun getMarkers() {
        databaseReference.child(group_id).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                mMap.clear()
                for (data in snapshot.children) {
                    val locationModel = data.getValue(LocationModel::class.java)
                    list.add(locationModel!!)
                }
                for (i in 0 until list.size) {
                         mMap.addMarker(MarkerOptions().position(LatLng(list[i].latitude, list[i].longitude)).title(list[i].user_name))
                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}