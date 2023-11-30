package com.example.instagramapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.adapter.PostAdapter
import com.example.instagramapp.adapter.StoryAdapter
import com.example.instagramapp.databinding.FragmentHomeBinding
import com.example.instagramapp.model.Post
import com.example.instagramapp.model.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewStory: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var storyAdapter: StoryAdapter
    private var postList: MutableList<Post>? = null
    private var followingList: MutableList<String>?= null
    private var storyList: MutableList<Story>? =null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)


        recyclerView = binding.recyclerViewHome
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false )

        postList = ArrayList()
        postAdapter = PostAdapter(requireContext(), postList as ArrayList<Post>)
        recyclerView.adapter = postAdapter


//        recyclerViewStory = binding.recyclerViewStory
//        val layoutManager1 = LinearLayoutManager(requireContext())
//        layoutManager1.reverseLayout= true
//        layoutManager1.stackFromEnd = true
//        recyclerViewStory.layoutManager = layoutManager1
//
//        storyList = ArrayList()
//        storyAdapter = StoryAdapter(requireContext(), storyList as ArrayList<Story>)
//        recyclerViewStory.adapter = storyAdapter


        checkFollowings()

        return binding.root
    }

    private fun checkFollowings() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
//                    @Suppress("UNCHECKED_CAST")
                    (followingList as ArrayList<String>).clear()

                    for (data in snapshot.children) {
                        data.key?.let {

                            (followingList as ArrayList<String>).add(it)
                        }
                    }

                    retrievePosts()
//                    retrieveStories()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun retrieveStories() {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")
        storyRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val timeCurrent = System.currentTimeMillis()
                (storyList as ArrayList<Story>).clear()
                (storyList as ArrayList<Story>).add(Story("",0,0,"", FirebaseAuth.getInstance().currentUser!!.uid))
                for(id in followingList!!){
                    var countStory = 0
                    var story: Story? = null
                    for (data in snapshot.child(id).children){
                        story = data.getValue(Story::class.java)
                        if (timeCurrent>story!!.timeStart && timeCurrent<story!!.timeEnd){
                            countStory ++
                        }
                    }
                    if (countStory>0){
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                }

                storyAdapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear()
                for (data in snapshot.children){
                    val post = data.getValue(Post::class.java)


                    for (id in  (followingList as ArrayList<String>)){
                        if(post!!.publisher == id){
                            postList!!.add(post)
                        }



                        postAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


}