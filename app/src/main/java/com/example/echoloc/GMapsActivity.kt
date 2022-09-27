package com.example.echoloc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.echoloc.database.Pref
import com.example.echoloc.model.LocationModel
import com.example.echoloc.model.Usermodel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_tmaps.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class GMapsActivity : AppCompatActivity(), View.OnClickListener
    , OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    val MY_PERMISSION_ACCESS_ALL = 100

    private lateinit var  mMap: GoogleMap
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var pref: Pref
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var group_id: String
    lateinit var myLatLng: LatLng

    var start: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gmaps)

//        btn_myLocation.setOnClickListener(this)
        btn_menu.setOnClickListener(this)
        btn_cancel.setOnClickListener(this)
        btn_home.setOnClickListener(this)
        btn_chat.setOnClickListener(this)
        btn_call.setOnClickListener(this)
        btn_setting.setOnClickListener(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        pref = Pref(applicationContext)
        group_id = intent.extras!!.getString("group_id", "")
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Echoloc").child("location").child(group_id)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.gMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

    override fun finish() {
        super.finish()
        removeLocationUpdate()
    }

    fun removeLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode === MY_PERMISSION_ACCESS_ALL) {
            if (grantResults.size > 0) {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED)
                        System.exit(0)
                }
                requestLocationUpdate()
            }
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            if (locationResult == null) {
                return
            }

            for (location in locationResult.locations) {
                if (location != null && pref.getData("id") != null) {
//                    if (start) {
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 16F))
//                        start = false
//                    }
                    database.getReference("Echoloc").child("Users").child(pref.getData("id")).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userModel = snapshot.getValue(Usermodel::class.java)
                            val locationModel = LocationModel(pref.getData("id"), pref.getData("name"),
                                pref.getData("call"), location.latitude, location.longitude, userModel!!.profileImageUrl)
                            if (pref.getData("id") != null) {
                                databaseReference.child(pref.getData("id")).setValue(locationModel).addOnSuccessListener {
                                    myLatLng = LatLng(location.latitude, location.longitude)
                                    getMarker()
                                }
                            } else {
                                removeLocationUpdate()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        getLocation()
        mMap.isMyLocationEnabled = true

        mMap.setOnMarkerClickListener(this)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }
        val task: Task<Location> = fusedLocationProviderClient.lastLocation

        task.addOnSuccessListener {
            val myLocation = LatLng(it.latitude, it.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16F))
        }
    }

    // 마커 생성 이벤트
    private fun getMarker() {
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mMap.clear()

                for (data in snapshot.children) {
                    val locationModel = data.getValue(LocationModel::class.java)
                    if (pref.getData("id") != locationModel!!.user_id) {
                        var url = locationModel.profileImageUrl
                        var bitmapURL: String? = null // 이전의 url이랑 비교해서 다르면 바꾸게하고 같으면 그대로 사용하게 변경하기 메모리 많이 잡아서 시간 지나면 팅기는거 같음 04
                        if (url != bitmapURL) {
                            bitmapURL = url
                            CoroutineScope(Dispatchers.Main).launch {
                                var bitmap = withContext(Dispatchers.IO) {
                                    BitmapFactory.decodeStream(URL(bitmapURL).openStream())
                                }
                                bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
                                mMap.addMarker(MarkerOptions()
                                    .position(LatLng(locationModel.latitude, locationModel.longitude))
                                    .title(locationModel.user_name)
                                    .snippet(locationModel.profileImageUrl)
                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
                            }
                        } else {

                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onClick(v: View?) {
        when(v) {
            btn_menu -> {
                btn_menu.visibility = View.GONE
                btn_cancel.visibility = View.VISIBLE
                btn_chat.visibility = View.VISIBLE
                btn_home.visibility = View.VISIBLE
                btn_call.visibility = View.VISIBLE
                btn_setting.visibility = View.VISIBLE
            }

            btn_cancel -> {
                btn_menu.visibility = View.VISIBLE
                btn_cancel.visibility = View.GONE
                btn_chat.visibility = View.GONE
                btn_home.visibility = View.GONE
                btn_call.visibility = View.GONE
                btn_setting.visibility = View.GONE
            }

            btn_chat -> {
                val intent = Intent(applicationContext, PublicGroupChatting::class.java)
                intent.putExtra("group_id", group_id)
                startActivity(intent)
            }

            btn_home -> {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            btn_call -> {
                // 호출 <<<< 주원이가 한 거 추가
            }

            btn_setting -> {
                intent = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // 마커 클릭 이벤트
    override fun onMarkerClick(marker: Marker): Boolean {

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.activity_profile, null)
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setView(mDialogView)

        val mAlertDialog = mBuilder.show()

        val mUserProfileImg = mDialogView.findViewById<ImageView>(R.id.iv_profileImg)
        Glide.with(this).load(marker.snippet).into(mUserProfileImg)

        val mUserName = mDialogView.findViewById<TextView>(R.id.tv_userName)
        mUserName.text = marker.title!!.trim()

        //val mUserTellNum = mDialogView.findViewById<TextView>(R.id.tv_userTellNum)
        //mUserTellNum.text = marker.snippet!!.trim()

        val mNavigation = mDialogView.findViewById<Button>(R.id.btn_navigation)
        mNavigation.setOnClickListener {
            val intent = Intent(applicationContext, GDirectionActivity::class.java)
            intent.putExtra("sName", pref.getData("name"))
            intent.putExtra("eName", marker.title)
            intent.putExtra("sLat", myLatLng.latitude)
            intent.putExtra("sLon", myLatLng.longitude)
            intent.putExtra("eLat", marker.position.latitude)
            intent.putExtra("eLon", marker.position.longitude)
            intent.putExtra("group_id", group_id)
            intent.putExtra("profile", marker.snippet)
            startActivity(intent)
            mAlertDialog.dismiss()
        }

        return true
    }
}