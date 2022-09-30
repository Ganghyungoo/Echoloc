package com.example.echoloc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import com.skt.Tmap.TMapView
import android.os.Bundle
import android.widget.LinearLayout
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.example.echoloc.database.Pref
import com.example.echoloc.model.LocationModel
import com.example.echoloc.model.Usermodel
import com.google.android.gms.location.*
import com.google.firebase.database.*
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import kotlinx.android.synthetic.main.activity_tmaps.*
import java.io.IOException
import java.net.URL

class TMapsActivity : AppCompatActivity(), View.OnClickListener {

    val MY_PERMISSION_ACCESS_ALL = 100

    var API_Key = "l7xxa73f10582d1447c3a43f973a639cf0d2"
    lateinit var tMapView: TMapView
    lateinit var point: TMapPoint
    lateinit var tmapPoint: TMapPoint
    lateinit var markerList: ArrayList<TMapMarkerItem>

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var pref: Pref
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var group_id: String

    var width: Int = 40
    var height: Int = 40

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tmaps)

        btn_myLocation.setOnClickListener(this)
        btn_menu.setOnClickListener(this)
        btn_cancel.setOnClickListener(this)
        btn_home.setOnClickListener(this)
        btn_chat.setOnClickListener(this)
        btn_call.setOnClickListener(this)
        btn_setting.setOnClickListener(this)

        setTMapAuth()
        initialize()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        pref = Pref(applicationContext)
        group_id = intent.extras!!.getString("group_id", "")
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Echoloc").child("location").child(group_id)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

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

        tMapView.setTrackingMode(true)

        markerList = ArrayList()

    }

    private fun setTMapAuth() {
        tMapView = TMapView(this)
        tMapView.setSKTMapApiKey(API_Key)
    }

    private fun initialize() {
        tMapView.zoomLevel = 16
        tMapView.mapType = TMapView.MAPTYPE_STANDARD
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN)
        tMapView.setIconVisibility(true)

        val linearLayoutTmap = findViewById<View>(R.id.linearLayoutTmap) as LinearLayout
        linearLayoutTmap.addView(tMapView)

        tMapView.onCalloutRightButtonListener = mOncalloutRightButtonClickCallback
    }

    var mOncalloutRightButtonClickCallback: TMapView.OnCalloutRightButtonClickCallback
     = TMapView.OnCalloutRightButtonClickCallback() {
        val intent = Intent(applicationContext, TDirectionActivity::class.java)
        intent.putExtra("sName", pref.getData("name"))
        intent.putExtra("eName", it.name)
        intent.putExtra("sLat", tmapPoint.latitude)
        intent.putExtra("sLon", tmapPoint.longitude)
        intent.putExtra("eLat", it.latitude)
        intent.putExtra("eLon", it.longitude)
        intent.putExtra("group_id", group_id)
        startActivity(intent)
    }

    override fun finish() {
        super.finish()
        removeLocationUpdate()
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdate() {
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
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
                    database.getReference("Echoloc").child("Users").child(pref.getData("id")).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userModel = snapshot.getValue(Usermodel::class.java)
                            val locationModel = LocationModel(pref.getData("id"), pref.getData("name"),
                                pref.getData("call"), location.latitude, location.longitude, userModel!!.profileImageUrl)
                            if (pref.getData("id") != null) {
                                databaseReference.child(pref.getData("id")).setValue(locationModel).addOnSuccessListener {
                                    tmapPoint = TMapPoint(location.latitude, location.longitude)
                                    tMapView.setLocationPoint(tmapPoint.longitude, tmapPoint.latitude)
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

    private fun getMarker() {
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                tMapView.removeAllMarkerItem()
                var i = 0
                markerList.clear()

                for (data in snapshot.children) {
                    val locationModel = data.getValue(LocationModel::class.java)
                    point = TMapPoint(locationModel!!.latitude, locationModel.longitude)
                    if (pref.getData("id") != locationModel.user_id) {
                        if (locationModel.profileImageUrl != null) {
                            //val bitmapIcon = createMarkerIcon(true, 0, locationModel.profileImageUrl) // 불러와서 넣기
                            //val bitmapIcon = uriToBitmap(Uri.parse(locationModel.profileImageUrl))
                            //Log.d("알려줘ㅅㅂ", "${bitmapIcon}")
                            val markerItem = TMapMarkerItem()
//                            val urlImage: URL = URL(locationModel.profile)
//                            var bitmap: Bitmap
                            /*val result: kotlinx.coroutines.Deferred<Bitmap?> = GlobalScope.async {
                                urlImage.toBitmap()
                            }*/
//                            CoroutineScope(Dispatchers.Main).launch {
//                                bitmap = withContext(Dispatchers.IO) {
//                                    BitmapFactory.decodeStream(urlImage.openStream())
//                                }
//                                bitmap = Bitmap.createScaledBitmap(bitmap!!, 100, 100, false)
//                                markerItem.icon = bitmap
//                            }
                            //markerItem.icon = result.await()
                            /*var imageTask: URLtoBitmapTask = URLtoBitmapTask()
                            imageTask = URLtoBitmapTask().apply {
                                url = URL(locationModel.profileImageUrl)
                            }
                            var bitmap: Bitmap = imageTask.execute().get()*/ // 렉이 너무 심함
//                            markerItem.icon = bitmap

                            markerItem.tMapPoint = point
                            markerItem.name = locationModel.user_name
                            markerList.add(markerItem)
                            tMapView.addMarkerItem("marketLocation$i", markerItem)
                            setBalloonView(markerItem, markerItem.name)
                            i += 1

                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setBalloonView(marker: TMapMarkerItem, name: String) {
        marker.canShowCallout = true

        if (marker.canShowCallout) {
            marker.calloutTitle = name

            val bitmap = resourceToBitmap(R.drawable.direct)
            marker.calloutRightButtonImage = bitmap
        }
    }

    public fun resourceToBitmap(image: Int): Bitmap {

        var bitmap = BitmapFactory.decodeResource(applicationContext.resources, image)
        bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false)

        return bitmap
    }

    fun URL.toBitmap(): Bitmap? {
        var bitmap: Bitmap
        try {
            bitmap = BitmapFactory.decodeStream(openStream())
            bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
            return bitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }
        /*return try {
            BitmapFactory.decodeStream(openStream())
        } catch (e: IOException) {
            null
        }*/
        return null
    }

    private fun createMarkerIcon(isUrl: Boolean, image: Int?, imageUri: String?): Bitmap {

        var bitmap: Bitmap? = null

        if (isUrl && imageUri != null) {
            /*try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(applicationContext.contentResolver,
                        imageUri
                    ))
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(applicationContext.contentResolver, imageUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }*/
            print(imageUri)
        } else {
            bitmap = BitmapFactory.decodeResource(applicationContext.resources, image!!)
        }
        bitmap = Bitmap.createScaledBitmap(bitmap!!, 50, 50, false)
        return bitmap!!
    }

    override fun onClick(v: View?) {
        when(v) {

            btn_myLocation -> {
                tMapView.setCenterPoint(tmapPoint.longitude, tmapPoint.latitude)
            }

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
}