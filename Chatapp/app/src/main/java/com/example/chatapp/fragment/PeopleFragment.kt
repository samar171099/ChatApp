package com.example.chatapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.AppConstants
import com.example.chatapp.ChatActivity
import com.example.chatapp.R
import com.example.chatapp.recyclerview.item.PersonItem
import com.example.chatapp.util.FirestoreUtil
import com.google.firebase.firestore.ListenerRegistration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_people.*
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast


class PeopleFragment : Fragment() {


    private lateinit var userListenerRegistration : ListenerRegistration



    private var shouldInitRecylerView = true

    private lateinit var peopleSection : Section

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        userListenerRegistration = FirestoreUtil.addUsersListener(this.requireActivity(), this::updateRecyclerView)




        return inflater.inflate(R.layout.fragment_people, container, false)

    }

    override fun onDestroy() {
        super.onDestroy()
        FirestoreUtil.removeListener(userListenerRegistration)
        shouldInitRecylerView = true
    }

    private fun updateRecyclerView(items: List<Item>){

        fun init(){

            recycler_view_people.apply {

              layoutManager = LinearLayoutManager(this@PeopleFragment.context)
                adapter = GroupAdapter<ViewHolder>().apply {

                    peopleSection = Section(items)
                    add(peopleSection)

                    setOnItemClickListener{ item, view ->

                        toast("Inside")

                        if(item is PersonItem) {
                            startActivity<ChatActivity>(
                                AppConstants.USER_NAME to item.person.name,
                                AppConstants.USER_ID to item.userId
                            )
                        }
                    }

                }
            }

            shouldInitRecylerView = false

        }

        fun updateItems() = peopleSection.update(items)

        if(shouldInitRecylerView)
            init()
        else
            updateItems()


    }


}