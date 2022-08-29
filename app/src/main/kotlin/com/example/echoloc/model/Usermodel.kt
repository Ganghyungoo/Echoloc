package com.example.echoloc.model

import android.net.Uri

class Usermodel() {

   lateinit var id:String
   lateinit var name:String
   lateinit var email:String
   lateinit var pass:String
   lateinit var call:String
   lateinit var profileImageUrl:String

    constructor(
        id:String,
        name:String,
        email:String,
        pass:String,
        call:String,
        profileImageUrl: String
        ) : this() {
        this.id=id
        this.email=email
        this.name=name
        this.pass=pass
        this.call=call
        this.profileImageUrl=profileImageUrl
    }
}