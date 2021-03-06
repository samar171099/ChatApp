package com.example.chatapp.util

import android.content.Context
import android.util.Log
import com.example.chatapp.model.ChatChannel
import com.example.chatapp.model.MessageType
import com.example.chatapp.model.TextMessage
import com.example.chatapp.model.User
import com.example.chatapp.recyclerview.item.PersonItem
import com.example.chatapp.recyclerview.item.TextMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.kotlinandroidextensions.Item

object FirestoreUtil {

    private  val firestoreInstance : FirebaseFirestore by lazy {FirebaseFirestore.getInstance()}

    private val cureentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")


    private val chatChannelsCollectionRef = firestoreInstance.collection("chatChannels")


    fun initCureentUserIfFirstTime(onComplete: () -> Unit){

        cureentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if(!documentSnapshot.exists()){
                val newUser = User(FirebaseAuth.getInstance().currentUser?.displayName?: "", "", null)

                cureentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }else
                onComplete()
        }
    }

    fun updateCurrentUser(name: String = "", bio : String = "", profilePicturePath : String? = null){
        val userFieldMap = mutableMapOf<String, Any>()
        if(name.isNotBlank()) userFieldMap["name"] = name
        if(bio.isNotBlank()) userFieldMap["bio"] = bio
        if(profilePicturePath != null) userFieldMap["profilePicturePath"] = profilePicturePath

        cureentUserDocRef.update(userFieldMap)

    }

    fun getCurrentUser(onComplete: (User) -> Unit){
        cureentUserDocRef.get()
            .addOnSuccessListener {
                onComplete(it.toObject(User::class.java)!!)
            }
    }


    fun addUsersListener(context: Context, onListen : (List<Item>) -> Unit) : ListenerRegistration {

        return firestoreInstance.collection("users")
            .addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
                    if(firebaseFirestoreException != null) {
                        Log.e("FIRESTORE", "User Listener error", firebaseFirestoreException)
                        return@addSnapshotListener
                    }

                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach{
                    if(it.id != FirebaseAuth.getInstance().currentUser?.uid)
                        items.add(PersonItem(it.toObject(User::class.java)!!, it.id, context))
                }

                onListen(items)
            }

    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun getOrCreateChatChannel(
        otherUserId: String,
        onComplete: (channelId : String) -> Unit ){
        cureentUserDocRef.collection("engagedChatChannels")
            .document(otherUserId).get().addOnSuccessListener {
                if(it.exists()){
                    onComplete(it["channelId"] as String)
                    return@addOnSuccessListener
                }
                val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

                val newChannel = chatChannelsCollectionRef.document()
                newChannel.set(ChatChannel(mutableListOf(currentUserId, otherUserId)))

                cureentUserDocRef.collection("engagedChatChannels")
                                    .document(otherUserId)
                                    .set(mapOf("channelId" to newChannel.id))

                firestoreInstance.collection("users").document(otherUserId)
                                    .collection("engagedChatChannels")
                                    .document(currentUserId)
                                    .set(mapOf("channelId" to newChannel.id))

                onComplete(newChannel.id)
            }
    }

    fun addChatMessageListener(channelId: String, context: Context,  onListen: (List<Item>) -> Unit) : ListenerRegistration{

        return chatChannelsCollectionRef.document(channelId).collection("messages")
            .orderBy("time")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException != null){
                    Log.e("Firestore", "ChatMessageListener error", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                val items = mutableListOf<Item>()
                querySnapshot?.documents?.forEach {
                    if(it["type"] == MessageType.TEXT)
                        items.add(TextMessageItem(it.toObject(TextMessage::class.java), context))
                    else
                        TODO("Add image message")
                }
                onListen(items)
            }
    }
}