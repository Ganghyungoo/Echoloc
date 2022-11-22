package com.example.echoloc

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.echoloc.Directions.FindElapsedTime
import com.example.echoloc.Directions.FindWalkingPath
import com.example.echoloc.database.Pref
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_gdirection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class GDirectionActivity : AppCompatActivity(), View.OnClickListener
    , OnMapReadyCallback {

    val MY_PERMISSION_ACCESS_ALL = 100
    private var API_Key = "l7xxa73f10582d1447c3a43f973a639cf0d2"

    lateinit var sName: String
    lateinit var eName: String
    lateinit var sLat: String
    lateinit var sLon: String
    lateinit var eLat: String
    lateinit var eLon: String
    lateinit var profile: String

    lateinit var pref: Pref
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var group_id: String
    private lateinit var  mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gdirection)

        btn_stop_direction.setOnClickListener(this)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.gMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sName = intent.extras!!.getString("sName").toString()
        eName = intent.extras!!.getString("eName").toString()
        sLat = intent.extras!!.getDouble("sLat").toString()
        sLon = intent.extras!!.getDouble("sLon").toString()
        eLat = intent.extras!!.getDouble("eLat").toString()
        eLon = intent.extras!!.getDouble("eLon").toString()
        profile = intent.extras!!.getString("profile").toString()

        pref = Pref(applicationContext)
        group_id = intent.extras!!.getString("group_id", "")
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Echoloc").child("location").child(group_id)

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(sLat.toDouble(), sLon.toDouble()), 17.5F))
        mMap.isMyLocationEnabled = true
//        CoroutineScope(Dispatchers.Main).launch {
//            var bitmap = withContext(Dispatchers.IO) {
//                BitmapFactory.decodeStream(URL(profile).openStream())
//            }
//            bitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
//            mMap.addMarker(MarkerOptions().position(LatLng(eLat.toDouble(), eLon.toDouble()))
//                .title(eName)
//                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)))
//        }

        mMap.addMarker(MarkerOptions().position(LatLng(eLat.toDouble(), eLon.toDouble()))
            .title(eName))

        try {
            val findWalkingPath =
                FindElapsedTime(applicationContext, group_id)
            findWalkingPath.execute("1", API_Key,
                sLon, sLat,
                eLon, eLat,
                sName, eName)

            val findWalkingPath2 =
                FindWalkingPath(
                    applicationContext,
                    mMap
                )
            findWalkingPath2.execute("1", API_Key,
            sLon, sLat,
            eLon, eLat,
            sName, eName)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onClick(p0: View?) {
        when(p0) {
            btn_stop_direction -> {
                val intent = Intent(applicationContext, GMapsActivity::class.java)
                intent.putExtra("group_id", group_id)
                startActivity(intent)
                finish()
            }
        }
    }

    fun drawPath(coordinates: ArrayList<LatLng>) {
        for (i in 0 until coordinates.size) {
            mMap.addMarker(MarkerOptions().position(coordinates.get(i)))
        }

    }
}