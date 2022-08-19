package com.example.echoloc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.echoloc.R
import com.example.echoloc.model.Usermodel
import kotlinx.android.synthetic.main.rv_member_layout.view.*

class MemberAdapter (var list: ArrayList<Usermodel>):RecyclerView.Adapter<MemberAdapter.Viewholder>(){
    class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
       var txt_Membername=itemView.txt_membername
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
       return Viewholder(LayoutInflater.from(parent.context).inflate(R.layout.rv_member_layout,parent,false))
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
       holder.txt_Membername.text=list[position].name
    }

    override fun getItemCount(): Int {
       return list.size

    }
}