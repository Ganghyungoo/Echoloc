package com.example.echoloc.model

class Usermodel() {
   lateinit var id:String
   lateinit var name:String
   lateinit var email:String
   lateinit var pass:String
   lateinit var call:String

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