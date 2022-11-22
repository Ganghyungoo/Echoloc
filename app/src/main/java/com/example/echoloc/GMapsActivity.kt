package com.example.echoloc

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.echoloc.database.Pref
import com.example.echoloc.model.LocationModel
import com.example.echoloc.model.MessageModel
import com.example.echoloc.model.Usermodel
import com.example.echoloc.util.getDateTime
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
import kotlinx.android.synthetic.main.activity_gmaps.*
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

    lateinit var markers: ArrayList<MarkerOptions>
    lateinit var bitmaps: ArrayList<Bitmap>
    lateinit var url: String

    lateinit var group_title: String
    lateinit var myBitmap: Bitmap

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
        group_title = intent.extras!!.getString("group_title", "")
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
                                    pref.saveData("latitude", location.latitude.toString())
                                    pref.saveData("longitude", location.longitude.toString())
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
                var i : Int = 0
                for (data in snapshot.children) {
                    val locationModel = data.getValue(LocationModel::class.java)

                    // 사용자(나)는 마커 표시 x
                    if (pref.getData("id") != locationModel!!.user_id) {

                        mMap.addMarker(MarkerOptions().position(LatLng(locationModel.latitude, locationModel.longitude))
                            .title(locationModel.user_name + "," + locationModel.user_call)
                            .snippet(locationModel.profileImageUrl))

//                        // 첫 접속 그룹원의 마커 추가
//                        if (markers.isEmpty() || i > markers.size-1) {
//                            val bitmapUrl = locationModel!!.profileImageUrl
//                            CoroutineScope(Dispatchers.Main).launch {
//                                var bitmap = withContext(Dispatchers.IO) {
//                                    BitmapFactory.decodeStream(URL (bitmapUrl).openStream())
//                                }
//
//                                bitmap  = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
//                                var markerOption = MarkerOptions().position(LatLng(locationModel.latitude, locationModel.longitude))
//                                    .title(locationModel.user_name + "," + locationModel.user_call)
//                                    .snippet(locationModel.profileImageUrl)
//                                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
//
//                                markers.add(markerOption)
//                            }
//                        } else {
//
//                            if (markers[i].title!!.split(",")[0] == locationModel.user_name) {
//                                markers[i].position(LatLng(locationModel!!.latitude, locationModel.longitude))
//                                    .title(locationModel.user_name + "," + locationModel.user_call)
//                                    .snippet(locationModel.profileImageUrl)
//                            }
//                        }
//
//                        i++
                    }

                }
//
//                for (j in 0 until markers.size) {
//                    mMap.addMarker(markers.get(j))
//                }
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
                // 호출
                val messageModel = MessageModel(pref.getData("name") + "님이 긴급호출을 요청했습니다.", getDateTime(), pref.getData("id"), pref.getData("name"), "1", "0", null)
                val databaseReference2 = database.getReference("Echoloc").child("chattings")
                val key = databaseReference2.push().key
                databaseReference2.child(group_id).child(key!!).setValue(messageModel)

                //알림(Notification)을 관리하는 관리자 객체를 운영체제(Context)로부터 소환하기
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                var builder: NotificationCompat.Builder? = null

                //Oreo 버전(API26 버전)이상에서는 알림시에 NotificationChannel 이라는 개념이 필수 구성요소가 됨.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    var channelID = "channel_01"
                    var channelName = "Echoloc"

                    //알림채널 객체 만들기
                    val channel = NotificationChannel(
                        channelID,
                        channelName,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )

                    notificationManager.createNotificationChannel(channel)
                    builder = NotificationCompat.Builder(this, channelID)
                }

                builder!!.setSmallIcon(R.drawable.logo)
                builder.setContentTitle(group_title)
                builder.setContentText(pref.getData("name") + "님이 긴급호출을 요청했습니다.")

                val intent = Intent(applicationContext, PublicGroupChatting::class.java)
                intent.putExtra("group_id", group_id)
                val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                builder.setContentIntent(pendingIntent)

                // 알림 진동 설정
                builder.setVibrate(longArrayOf(0, 2000, 1000, 3000))


                var notification = builder.build()
                notificationManager.notify(1, notification)
            }

            btn_setting -> {
                intent = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(intent)
                finish()
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
        mUserProfileImg.clipToOutline = true

        val tag = marker.title!!.split(",")

        val mUserName = mDialogView.findViewById<TextView>(R.id.tv_userName)
        mUserName.text = tag.get(0).trim()

        val mUserCall = mDialogView.findViewById<TextView>(R.id.tv_userCall)
        mUserCall.text = tag.get(1)

        val mNavigation = mDialogView.findViewById<Button>(R.id.btn_navigation)
        mNavigation.setOnClickListener {
            val intent = Intent(applicationContext, GDirectionActivity::class.java)
            intent.putExtra("sName", pref.getData("name").toString())
            intent.putExtra("eName", tag.get(0).trim().toString())
            intent.putExtra("sLat", myLatLng.latitude.toDouble())
            intent.putExtra("sLon", myLatLng.longitude.toDouble())
            intent.putExtra("eLat", marker.position.latitude.toDouble())
            intent.putExtra("eLon", marker.position.longitude.toDouble())
            intent.putExtra("group_id", group_id)
            intent.putExtra("profile", marker.snippet)
            startActivity(intent)
            mAlertDialog.dismiss()
        }

        return true
    }
}