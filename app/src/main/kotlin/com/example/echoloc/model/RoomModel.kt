package com.example.echoloc.model

class RoomModel() {

    lateinit var group_id:String
    lateinit var roomname:String
    lateinit var roompass:String
    lateinit var admin_name:String
    lateinit var admin_id:String
    lateinit var admin_call:String
    var isgroupjoined = false

    constructor(
        group_id: String,
        roomname: String,
        roompass: String,
        admin_name: String,
        admin_id: String,
        admin_call: String

    ) : this() {
        this.admin_id = admin_id
        this.admin_name = admin_name
        this.roompass = roompass
        this.admin_call = admin_call
        this.group_id = group_id
        this.roomname = roomname
    }

    public fun isGroupjoined(isAdmin: Boolean) {
        this.isgroupjoined = isAdmin
    }
}