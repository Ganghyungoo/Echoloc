package com.example.echoloc.model

class RoomModel {

    private lateinit var group_id:String
    private lateinit var roomname:String
    private var isprivate:Int=0
    private lateinit var admin_name:String
    private lateinit var admin_id:String
    private lateinit var admin_call:String

    constructor(
        group_id:String,
        roomname:String,
        isprivate:Int,
        admin_name:String,
        admin_id:String,
        admin_call:String
    )
    {
        this.admin_id=admin_id
        this.admin_name=admin_name
        this.admin_call=admin_call
        this.group_id=group_id
        this.isprivate=isprivate
        this.roomname=roomname
    }
}