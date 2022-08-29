package com.example.echoloc


import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.echoloc.database.Pref
import com.example.echoloc.locationapi.LocationManager
import com.example.echoloc.locationapi.NaverAPI
import com.example.echoloc.locationapi.ResultPath
import com.example.echoloc.locationapi.service.DistanceManager
import com.example.echoloc.model.LocationModel
import com.example.echoloc.permission.PermissionManager
import com.google.firebase.database.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.widget.LocationButtonView
import kotlinx.android.synthetic.main.activity_nmaps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NMapsActivity : AppCompatActivity(), OnMapReadyCallback, Overlay.OnClickListener {

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
            btn_start.visibility = View.GONE
            btn_stop.visibility = View.VISIBLE
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

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            Toast.makeText(this, "위치 공유 종료", Toast.LENGTH_SHORT).show()
            LocationManager.stop(this)
            btn_start.visibility = View.VISIBLE
            btn_stop.visibility = View.GONE
        }

        findViewById<Button>(R.id.btn_menu).setOnClickListener {
            btn_menu.visibility = View.GONE
            btn_chat.visibility = View.VISIBLE
            btn_home.visibility = View.VISIBLE
            btn_cancel.visibility = View.VISIBLE
            btn_call.visibility = View.VISIBLE
            btn_setting.visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            btn_menu.visibility = View.VISIBLE
            btn_chat.visibility = View.GONE
            btn_home.visibility = View.GONE
            btn_cancel.visibility = View.GONE
            btn_call.visibility = View.GONE
            btn_setting.visibility = View.GONE
        }

        findViewById<Button>(R.id.btn_home).setOnClickListener {
            LocationManager.stop(this)
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btn_chat).setOnClickListener {
            val intent = Intent(applicationContext, PublicGroupChatting::class.java)
            intent.putExtra("group_id", group_id)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btn_call).setOnClickListener {
            // 호출
        }

        findViewById<Button>(R.id.btn_setting).setOnClickListener {
            // 설정 창 만들면 추가
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
            var uri = pref.getData("profile")

            val locationModel =
                LocationModel(pref.getData("id"), pref.getData("name"), pref.getData("call"), latitude, longitude, pref.getData("profile"))
            databaseReference.child(group_id).child(pref.getData("id")).setValue(locationModel).addOnCompleteListener{
                pref.saveData("latitude", latitude.toString())
                pref.saveData("longitude", longitude.toString())
                getMarkers()
            }
        }
    }

    private fun getMarkers() {

        databaseReference.child(group_id).addValueEventListener(object : ValueEventListener {
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
//                    marker.icon = OverlayImage.fromBitmap()
                    marker.tag = locationModel.user_name + "," + locationModel.user_call + "," + locationModel!!.latitude + "," + locationModel!!.longitude
                    marker.onClickListener = this@NMapsActivity
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

    override fun onClick(overlay: Overlay): Boolean {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.activity_profile, null)
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setView(mDialogView)

        val mAlertDialog = mBuilder.show()

        var tag = overlay.tag.toString().split(",")
        val mUserName = mDialogView.findViewById<TextView>(R.id.tv_userName)
        mUserName.text = tag[0].trim()

        val mUserTellNum = mDialogView.findViewById<TextView>(R.id.tv_userTellNum)
        mUserTellNum.text = tag[1].trim()

        val mCall = mDialogView.findViewById<Button>(R.id.btn_call)
        mCall.setOnClickListener {
            // 전화걸기
        }

        val mNavigation = mDialogView.findViewById<Button>(R.id.btn_navigation)
        mNavigation.setOnClickListener {
            // 길찾기

            val APIKEY_ID = "cyp9xrblgx"
            val APIKEY = "ouFouz1MiHpPUZOsuhmaZuFgRdzTSkPOyQuJaDMu"
            //레트로핏 객체 생성
            val retrofit = Retrofit.Builder().
            baseUrl("https://naveropenapi.apigw.ntruss.com/map-direction/").
            addConverterFactory(GsonConverterFactory.create()).
            build()

            val api = retrofit.create(NaverAPI::class.java)
            //근처에서 길찾기
            val distance = DistanceManager.getDistance(pref.getData("latitude").toDouble(), pref.getData("longitude").toDouble(), tag[2].toDouble(), tag[3].toDouble())
            val callgetPath = api.getPath(APIKEY_ID, APIKEY, "${pref.getData("longitude")}, ${pref.getData("latitude")}", "${tag[3]}, ${tag[2]}", distance)

            callgetPath.enqueue(object : Callback<ResultPath>{
                override fun onResponse(
                    call: Call<ResultPath>,
                    response: Response<ResultPath>
                ) {
                    val path_cords_list = response.body()?.route?.traoptimal
                    //경로 그리기 응답바디가 List<List<Double>> 이라서 2중 for문 썼음
                    val path = PathOverlay()
                    //MutableList에 add 기능 쓰기 위해 더미 원소 하나 넣어둠
                    val path_container : MutableList<LatLng>? = mutableListOf(LatLng(0.1,0.1))
                    for(path_cords in path_cords_list!!){
                        for(path_cords_xy in path_cords?.path!!){
                            //구한 경로를 하나씩 path_container에 추가해줌
                            path_container?.add(LatLng(path_cords_xy[1], path_cords_xy[0]))
                        }
                    }
                    //더미원소 드랍후 path.coords에 path들을 넣어줌.
                    path.coords = path_container?.drop(1)!!
                    path.color = Color.GREEN
                    path.passedColor = Color.GRAY
                    path.map = naverMap

                    //경로 시작점으로 화면 이동
                    if(path.coords != null) {
                        val cameraUpdate = CameraUpdate.scrollTo(path.coords[0]!!)
                            .animate(CameraAnimation.Fly, 3000)
                        naverMap!!.moveCamera(cameraUpdate)
                        naverMap.locationTrackingMode = LocationTrackingMode.Follow

                        Toast.makeText(this@NMapsActivity, "경로 안내가 시작됩니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResultPath>, t: Throwable) {
                    Toast.makeText(this@NMapsActivity, "경로 안내가 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
            mAlertDialog.dismiss()
        }
        return false
    }

}

