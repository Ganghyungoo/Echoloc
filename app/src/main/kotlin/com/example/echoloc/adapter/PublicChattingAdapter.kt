package com.example.echoloc.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.echoloc.R
import com.example.echoloc.model.MessageModel
import kotlinx.android.synthetic.main.rv_message.view.*
import kotlinx.android.synthetic.main.rv_other.view.*

class PublicChattingAdapter(list: ArrayList<MessageModel>, user_id: String):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var list: ArrayList<MessageModel> = list
    var user_id: String = user_id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view:View
        if (viewType == 0) {
            return MessageViewModel(
                LayoutInflater.from(parent.context).inflate(R.layout.rv_message, parent, false)
            )
        }
        return GroupJoinedModel(
            LayoutInflater.from(parent.context).inflate(R.layout.rv_other, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (list[position].message_type == "0") {
            (holder as MessageViewModel).bindView(list[position], user_id)
        } else {
            (holder as GroupJoinedModel).bindView(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].message_type.toInt()
    }

    class MessageViewModel(itemview: View): RecyclerView.ViewHolder(itemview) {

        var send_message = itemview.send_message
        var recieve_message_layout = itemview.recieve_message_layout
        var recieve_message_name = itemview.recieve_message_name
        var recieve_message_time = itemview.recieve_message_time
        var send_message_name = itemview.send_message_name
        var sendmessage_datetime = itemview.sendmessage_datetime


        public fun bindView(model: MessageModel, user_id: String) {

            if (model.sender_id == user_id) {
                send_message.visibility = View.VISIBLE
                recieve_message_layout.visibility = View.GONE
                send_message_name.text = model.message
                sendmessage_datetime.text = model.date_time
            } else {
                send_message.visibility = View.GONE
                recieve_message_layout.visibility = View.VISIBLE
                recieve_message_name.text = model.message
                recieve_message_time.text = model.date_time
            }
        }
    }

    class GroupJoinedModel(itemview: View): RecyclerView.ViewHolder(itemview) {
        var text_tittle = itemview.text_tittle
        public fun bindView(model: MessageModel) {
            text_tittle.text = model.message
        }
    }
}