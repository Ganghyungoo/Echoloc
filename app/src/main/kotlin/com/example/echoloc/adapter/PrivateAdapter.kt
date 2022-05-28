package com.example.echoloc.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.echoloc.R
import com.example.echoloc.model.RoomModel
import kotlinx.android.synthetic.main.rv_layout.view.*

class PrivateAdapter(var context: Context,
                     var list: ArrayList<RoomModel>,
                     var user_id: String,
                     var ongroupJoin: onClick) :
    RecyclerView.Adapter<PrivateAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var joinroom = itemView.joinroom
        var txt_roomname = itemView.txt_roomname
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.rv_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txt_roomname.text = list[position].roomname

        if (list[position].isgroupjoined) {
            holder.joinroom.visibility = View.GONE
        } else {
            holder.joinroom.visibility = View.VISIBLE
            holder.joinroom.text="방 참여하기"
        }


      holder.joinroom.setOnClickListener {
        if(holder.joinroom.text=="방 참여하기") {
            ongroupJoin.onGroupJoined(list[position])
        }
      }

    }

    override fun getItemCount(): Int {
        return list.size
    }
    public interface onClick {
        fun onGroupJoined(roomModel: RoomModel)
    }
}