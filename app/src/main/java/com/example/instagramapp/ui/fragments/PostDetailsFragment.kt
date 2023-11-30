package com.example.instagramapp.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.adapter.PostAdapter
import com.example.instagramapp.databinding.FragmentPostDetailsBinding
import com.example.instagramapp.model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PostDetailsFragment : Fragment() {

    private lateinit var binding: FragmentPostDetailsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var postList: MutableList<Post>? = null
    private var postId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPostDetailsBinding.inflate(inflater, container, false)


        val preferences = context?.getSharedPreferences("FREFS", Context.MODE_PRIVATE)

        if (preferences!= null){
            postId = preferences.getString("postId", "none").toString()

        }

        recyclerView = binding.recyclerViewPostDetailsFrag
        recyclerView.setHasFixedSize(true)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        postList = ArrayList()
        postAdapter = PostAdapter(requireContext(), postList as ArrayList<Post>)
        recyclerView.adapter = postAdapter
        retrievePosts()

        return  binding.root
    }


    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")
            .child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                postList?.clear()
                val post = snapshot.getValue(Post::class.java)
                postList!!.add(post!!)
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


}