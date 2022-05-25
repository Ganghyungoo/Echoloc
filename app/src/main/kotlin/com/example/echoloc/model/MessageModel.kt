package com.example.echoloc.model

class MessageModel() {

    lateinit var message: String
    lateinit var date_time: String
    lateinit var sender_id: String
    lateinit var sender_name: String
    lateinit var message_type: String
    lateinit var isAdmin_msg: String

    constructor(
        message: String,
        date_time: String,
        sender_id: String,
        sender_name: String,
        message_type: String,
        isAdmin_msg: String
    ) : this() {
        this.message = message
        this.date_time = date_time
        this.sender_id = sender_id
        this.sender_name = sender_name
        this.message_type = message_type
        this.isAdmin_msg = isAdmin_msg
    }
}