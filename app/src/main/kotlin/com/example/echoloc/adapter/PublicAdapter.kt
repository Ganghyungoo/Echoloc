package com.example.echoloc.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.echoloc.R
import com.example.echoloc.model.RoomModel
import kotlinx.android.synthetic.main.rv_layout.view.*
//앙
class PublicAdapter(var context: Context, var list: ArrayList<RoomModel>) :RecyclerView.Adapter<PublicAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var joinroom = itemView.joinroom
        var txt_roomname = itemView.txt_roomname
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txt_roomname.text = list[position].roomname

        if (list[position].isAdmin) {
            holder.joinroom.visibility = View.GONE
        } else {
            holder.joinroom.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}