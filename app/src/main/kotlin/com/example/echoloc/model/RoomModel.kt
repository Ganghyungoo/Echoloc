package com.example.echoloc.model

class RoomModel() {

    lateinit var group_id:String
    lateinit var roomname:String
    var isprivate:Int=0
    lateinit var admin_name:String
    lateinit var admin_id:String
    lateinit var admin_call:String
    var isAdmin = false

    constructor(
        group_id: String,
        roomname: String,
        isprivate: Int,
        admin_name: String,
        admin_id: String,
        admin_call: String

    ) : this() {
        this.admin_id = admin_id
        this.admin_name = admin_name
        this.admin_call = admin_call
        this.group_id = group_id
        this.isprivate = isprivate
        this.roomname = roomname
    }

    public fun isAdmin(isAdmin: Boolean) {
        this.isAdmin = isAdmin
    }
}