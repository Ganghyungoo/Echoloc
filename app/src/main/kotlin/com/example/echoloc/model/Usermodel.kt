package com.example.echoloc.model

class Usermodel() {

   private lateinit var id:String
   private lateinit var name:String
   private lateinit var email:String
   private lateinit var pass:String
   private lateinit var call:String

    constructor(
        id:String,
        name:String,
        email:String,
        pass:String,
        call:String
        ) : this() {
        this.id=id
        this.email=email
        this.name=name
        this.pass=pass
        this.call=call
    }
}