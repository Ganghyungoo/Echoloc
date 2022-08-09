package com.example.echoloc.model

class LocationModel() {
    lateinit var user_id: String
    lateinit var user_name: String
    lateinit var user_call: String
    var latitude: Double = 0.0
    var longitude: Double = 0.0

    constructor(
        user_id: String,
        user_name: String,
        user_call: String,
        latitude: Double,
        longitude: Double
    ) : this() {
        this.user_id = user_id
        this.user_name = user_name
        this.user_call = user_call
        this.latitude = latitude
        this.longitude = longitude
    }
}
