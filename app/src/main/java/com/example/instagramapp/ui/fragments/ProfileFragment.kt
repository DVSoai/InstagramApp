package com.example.instagramapp.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.adapter.MyImagesAdapter
import com.example.instagramapp.databinding.FragmentProfileBinding
import com.example.instagramapp.model.Post
import com.example.instagramapp.model.User
import com.example.instagramapp.ui.AccountSettingsActivity
import com.example.instagramapp.ui.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.Collections


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var myImagesAdapter: MyImagesAdapter
    private lateinit var myImagesAdapterSavedImg: MyImagesAdapter
    private lateinit var recyclerViewUploadImage: RecyclerView
    private lateinit var recyclerViewSavedImages: RecyclerView

    lateinit var uploadedImagesBtn: ImageButton
    lateinit var savedImagesBtn: ImageButton

    var postList: List<Post>? = null
    var postListSaved: List<Post>? = null
    var mySavesImg: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       binding = FragmentProfileBinding.inflate(inflater, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val pref = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null) {
            this.profileId = pref.getString("profileId", "").toString()
        }

        if (profileId == firebaseUser.uid) {
            binding.btnEditAccount.text = "Edit Profile"
        }

        else if(profileId != firebaseUser.uid) {
            checkFollowAndFollowing()
        }

        //recycler View for Upload Image
        recyclerViewUploadImage = binding.recyclerViewUploadPic
        recyclerViewUploadImage.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadImage.layoutManager = linearLayoutManager
        postList = ArrayList()
        myImagesAdapter = MyImagesAdapter(requireContext(), postList as ArrayList<Post>)
        recyclerViewUploadImage.adapter = myImagesAdapter


        //recyclerView for Saved Images

        recyclerViewSavedImages=binding.recyclerViewSavePic
        recyclerViewSavedImages.setHasFixedSize(true)
        val linearLayoutManager2: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewSavedImages.layoutManager=linearLayoutManager2

        postListSaved = ArrayList()
        myImagesAdapterSavedImg = MyImagesAdapter(requireContext(), postListSaved as ArrayList<Post>)
        recyclerViewSavedImages.adapter = myImagesAdapterSavedImg



        recyclerViewSavedImages.visibility = View.GONE
        recyclerViewUploadImage.visibility = View.VISIBLE

        uploadedImagesBtn = binding.imgBtnGrid
        uploadedImagesBtn.setOnClickListener{
            recyclerViewSavedImages.visibility = View.GONE
            recyclerViewUploadImage.visibility = View.VISIBLE
        }

        savedImagesBtn = binding.imgBtnSave
        savedImagesBtn.setOnClickListener {
            recyclerViewSavedImages.visibility = View.VISIBLE
            recyclerViewUploadImage.visibility = View.GONE
        }

        binding.tvTotalFollowers.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        binding.tvTotalFollowing.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }

        binding.btnEditAccount.setOnClickListener {

            val getBtnText = binding.btnEditAccount.text.toString()

            when {
                getBtnText == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))

                getBtnText == "Follow" -> {

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .setValue(true)
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .setValue(true)
                    }

                    addNotification()

                }

                getBtnText == "Following" -> {

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .removeValue()
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .removeValue()
                    }

                }


            }
        }

        getFollowers()
        getFollowings()
        userInfo()
        myPhotos()
        getTotalNumberOfPosts()
        mySaves()


        return binding.root
    }

    private fun mySaves() {
        mySavesImg = ArrayList()

        val saveRef = FirebaseDatabase.getInstance().reference
            .child("Saves").child(firebaseUser.uid)

        saveRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        (mySavesImg as ArrayList<String>).add(data.key!!)
                    }
                    readSavedImagesData()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun readSavedImagesData() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){

                    (postListSaved as ArrayList<Post>).clear()
                    for (data in snapshot.children){
                        val post = data.getValue(Post::class.java)

                        for (key in mySavesImg!!){

                            if (post!!.postId == key){
                                (postListSaved as ArrayList<Post>).add(post)
                            }
                        }
                    }
                    myImagesAdapterSavedImg.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    private fun checkFollowAndFollowing() {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

      if(followingRef != null){
          followingRef.addValueEventListener(object : ValueEventListener{
              override fun onDataChange(snapshot: DataSnapshot) {

                  if(snapshot.child(profileId).exists()) {
                      binding.btnEditAccount.text = "Following"
                  }
                  else {
                      binding.btnEditAccount.text = "Follow"
                  }
              }

              override fun onCancelled(error: DatabaseError) {
              }

          })
      }

    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    binding.tvTotalFollowers.text = snapshot.childrenCount.toString()
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
    private fun getFollowings() {
        val followingsRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followingsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    binding.tvTotalFollowing.text = snapshot.childrenCount.toString()
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    private fun myPhotos(){
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    (postList as ArrayList<Post>).clear()

                    for (data in snapshot.children){
                        val post = data.getValue(Post::class.java)!!

                        if (post.publisher.equals(profileId)){
                            (postList as ArrayList<Post>).add(post)
                        }

                        Collections.reverse(postList)
                        myImagesAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(binding.profileImageFrag)
                    binding.tvProfileUsername.text=user.username
                    binding.tvFullNameProfile.text = user.fullName
                    binding.tvBioProfile.text = user.bio
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun getTotalNumberOfPosts(){
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var postCounter = 0
                    for (data in snapshot.children){
                        val post = data.getValue(Post::class.java)
                        if (post?.publisher == profileId){
                            postCounter++
                        }
                    }
                    binding.tvTotalPosts.text = " " + postCounter
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun addNotification(){
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(profileId)

        val notiMap = HashMap<String, Any>()
        notiMap["userId"] = firebaseUser.uid
        notiMap["text"] = "started following you "
        notiMap["postId"] = ""
        notiMap["isPost"] = false

        notiRef.push().setValue(notiMap)

    }

    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }




}