package com.example.chatapp.recyclerview.item

import android.content.Context
import com.example.chatapp.R
import com.example.chatapp.model.TextMessage
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder

class TextMessageItem(
    val message: TextMessage?,
    val context: Context)

    : Item(){
    override fun bind(viewHolder: ViewHolder, position: Int) {

    }

    override fun getLayout(): Int  = R.layout.item_text_message

}