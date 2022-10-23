package com.example.echoloc.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.echoloc.R
import com.example.echoloc.adapter.MemberAdapter.*
import com.example.echoloc.model.Usermodel

class MemberAdapter (var context: Context)
    : RecyclerView.Adapter<ViewHolder>(){

    var list = mutableListOf<Usermodel>()

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val tv_name = itemView.findViewById<TextView>(R.id.tv_memberName)
        private val iv_profile = itemView.findViewById<ImageView>(R.id.iv_memberProfile)

        fun bind(item: Usermodel) {
            tv_name.text = item.name
            Glide.with(itemView).load(Uri.parse(item.profileImageUrl)).into(iv_profile)
            iv_profile.clipToOutline = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_member_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

}