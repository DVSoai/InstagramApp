package com.example.instagramapp.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.adapter.UserAdapter
import com.example.instagramapp.databinding.FragmentSearchBinding
import com.example.instagramapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var userAdapter: UserAdapter
    private var mUser: MutableList<User>? = null
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerViewSearch
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        mUser = ArrayList()
        userAdapter = UserAdapter(requireContext(), mUser as ArrayList<User>, true)
        recyclerView.adapter = userAdapter


        binding.etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(binding.etSearch.text.toString() == "") {

                }
                else {
                    recyclerView.visibility = View.VISIBLE
                    retrieveUsers()
                    searchUser(s.toString().lowercase())
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }


        })
        return binding.root
    }

    private fun searchUser(lowercase: String) {

        val query = FirebaseDatabase.getInstance().getReference().child("Users")
            .orderByChild("fullName")
            .startAt(lowercase)
            .endAt(lowercase + "\uf0ff")

        query.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                mUser?.clear()
                for(data in snapshot.children){
                    val user = data.getValue(User::class.java)
                    if (user !=null) {
                        mUser?.add(user)
                    }
                }

                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    private fun retrieveUsers() {

        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(binding.etSearch.text.toString() == ""){
                    mUser?.clear()
                    for(data in snapshot.children){
                        val user = data.getValue(User::class.java)
                        if (user !=null) {
                            mUser?.add(user)
                        }
                    }

                    userAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


}