package com.example.echoloc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_private_group_chatting.*

class PrivateGroupChatting : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_group_chatting)

        btn_back.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            btn_back -> {
                onBackPressed()
            }
        }
    }
}