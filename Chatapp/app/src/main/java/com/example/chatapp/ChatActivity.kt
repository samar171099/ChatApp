package com.example.chatapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.util.FirestoreUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item
import org.jetbrains.anko.toast

class ChatActivity : AppCompatActivity() {

    private lateinit var messageListenerRegisteration : ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra(AppConstants.USER_NAME)

        val otherUserId = intent.getStringExtra(AppConstants.USER_ID)

        FirestoreUtil.getOrCreateChatChannel(otherUserId!!){ channelId ->
            messageListenerRegisteration =
                FirestoreUtil.addChatMessageListener(channelId, this, this::onMessagesChanged)
        }
    }


    private fun onMessagesChanged(messages: List<Item>){

        toast("onMessagesChangedRunnin!")
    }

}