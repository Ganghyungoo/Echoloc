package com.example.echoloc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.activity_map.btn_call

class MapActivity : AppCompatActivity(),
    OnMapReadyCallback, View.OnClickListener{

    private lateinit var  mMap: GoogleMap
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var group_id = ""

    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var pref: Pref


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
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener{
            Toast.makeText(this, "위치 공유 정지", Toast.LENGTH_SHORT).show()
            LocationManager.stop(this)
            btn_start.visibility = View.VISIBLE
            btn_stop.visibility = View.GONE
        }

        if (LocationService.isLocationServiceRunning) {
            requestLocation()
        }

        findViewById<ImageView>(R.id.iv_myLocation).setOnClickListener{
            Toast.makeText(this, "내 위치로 이동", Toast.LENGTH_SHORT).show()
            getLocation(true)
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // 화면 안꺼지게
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT // 화면 세로 고정

    }
    private fun requestLocation() {
        LocationManager.Builder.create(this).request(true) { latitude, longitude ->

            var locationModel = LocationModel(pref.getData("id"), pref.getData("name"), pref.getData("call"), latitude, longitude)
            var reference = databaseReference.child(group_id)
            reference.child(pref.getData("id")).setValue(locationModel).addOnCompleteListener{
                getMarkers()
            }

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        getLocation(false)

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

        mMap.setOnMarkerClickListener { marker ->
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.activity_profile, null)
            val mBuilder = AlertDialog.Builder(this)
            mBuilder.setView(mDialogView)

            val mAlertDialog = mBuilder.show()

            val mUserName = mDialogView.findViewById<TextView>(R.id.tv_userName)
            mUserName.text = marker.title

            val mUserTellNum = mDialogView.findViewById<TextView>(R.id.tv_userTellNum)
            mUserTellNum.text = marker.snippet

            val mCall = mDialogView.findViewById<Button>(R.id.btn_call)
            mCall.setOnClickListener {
                // 전화걸기
            }

            val mNavigation = mDialogView.findViewById<Button>(R.id.btn_navigation)
            mNavigation.setOnClickListener {
                // 길찾기


                mAlertDialog.dismiss()
            }

            false
        }


    }



    private  fun getLocation(isAnimate: Boolean) {
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
                if (isAnimate){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 17.5F))
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mylocation, 17.5F))
                }
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

    private fun getMarkers() {
        databaseReference.child(group_id).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mMap.clear()

                for (data in snapshot.children) {
                    val locationModel = data.getValue(LocationModel::class.java)
                    mMap.addMarker(MarkerOptions()
                        .position(LatLng(locationModel!!.latitude, locationModel.longitude))
                        .title(locationModel.user_name)
                        .snippet(locationModel.user_call))
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}