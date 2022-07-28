package com.example.echoloc

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.echoloc.database.Pref
import com.example.echoloc.locationapi.LocationManager
import com.example.echoloc.locationapi.service.LocationService
import com.example.echoloc.permission.PermissionManager

class Test : AppCompatActivity() {

    lateinit var textViewState: TextView
    lateinit var textViewLocation: TextView

    lateinit var room_name: String
    lateinit var pref: Pref


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        pref = Pref(applicationContext)

        textViewLocation = findViewById(R.id.textViewLocation)
        textViewState = findViewById(R.id.textViewState)

        findViewById<Button>(R.id.buttonStart).setOnClickListener{
            // check permission here

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

        textViewState.text = "is location service running ? ${LocationService.isLocationServiceRunning}"

        if (LocationService.isLocationServiceRunning) {
            requestLocation()
        }

        findViewById<Button>(R.id.buttonStop).setOnClickListener{
            LocationManager.stop(this)
        }

    }

    private fun requestLocation() {
        room_name = pref.getData("roomname")
        LocationManager.Builder.create(this).request(true, room_name) { latitude, longitude ->

            val locationString = "$latitude\t$longitude"

            println(latitude)
            println(longitude)
            textViewLocation.text = locationString

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        PermissionManager.onActivityResult(resultCode, resultCode)
        super.onActivityResult(requestCode, resultCode, data)
    }


}