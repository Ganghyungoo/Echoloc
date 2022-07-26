package com.example.echoloc

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_map.*
import java.util.jar.Manifest

class MapActivity : AppCompatActivity(),
    OnMapReadyCallback, View.OnClickListener {

    private lateinit var  mMap: GoogleMap

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var group_id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        btn_menu.setOnClickListener(this)
        btn_cancel.setOnClickListener(this)
        btn_grp_select.setOnClickListener(this)
        btn_chat.setOnClickListener(this)
        btn_call.setOnClickListener(this)
        btn_setting.setOnClickListener(this)

        group_id = intent.extras!!.getString("group_id", "")

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        getLocation()

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true
    }

    private  fun getLocation() {
        val task: Task<Location> = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                val mylocation = LatLng(it.latitude, it.longitude)
//                mMap.addMarker(MarkerOptions().position(mylocation).title("Marker in mylocation"))
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
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
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


}