package com.example.echoloc.model

class LocationModel {

    lateinit var group_id: String
    lateinit var user_name: String
    var latitude: Double
    var longitude: Double

    constructor(group_id: String, user_name: String, latitude: Double, longitude: Double) {
        this.group_id = group_id
        this.user_name = user_name
        this.latitude = latitude
        this.longitude = longitude
    }

}