package com.example.instagramapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.adapter.CommentsAdapter
import com.example.instagramapp.databinding.ActivityCommentsBinding
import com.example.instagramapp.model.Comment
import com.example.instagramapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class CommentsActivity : AppCompatActivity() {

     var postId  =""
     var publisherId=""
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var binding: ActivityCommentsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var commentAdapter: CommentsAdapter
    private var commentList: MutableList<Comment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val intent = intent
        postId = intent.getStringExtra("postId").toString()
        publisherId = intent.getStringExtra("publisherId").toString()

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        recyclerView= binding.recyclerViewComments

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        commentList = ArrayList()
        commentAdapter = CommentsAdapter(this, commentList as ArrayList<Comment>)
        recyclerView.adapter = commentAdapter
        userInfo()
        readComments()
        getPostImage()

        binding.postComment.setOnClickListener{
            if (binding.addComment.text.toString() == ""){
                Toast.makeText(this, "Nhập comment ", Toast.LENGTH_LONG).show()
            }
            else{
                addComment()
            }
        }
    }

    private fun addComment() {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments").child(postId)

        val commentsMap = HashMap<String, Any>()
        commentsMap["comments"] = binding.addComment.text.toString()
        commentsMap["publisher"] = firebaseUser.uid


        addNotification()
        commentsRef.push().setValue(commentsMap)

        binding.addComment.text.clear()

    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(binding.profileImgComment)

                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun getPostImage() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
            .child(postId).child("postimage")

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()) {


                   val image = snapshot.value.toString()
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(binding.postImgComment)

                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


    private fun readComments() {
        val commentsRef = FirebaseDatabase.getInstance().reference.child("Comments")
            .child(postId)
        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    commentList!!.clear()
                    for (data in snapshot.children){
                        val comment = data.getValue(Comment::class.java)

                        commentList!!.add(comment!!)
                    }

                    commentAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun addNotification(){
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(publisherId)

        val notiMap = HashMap<String, Any>()
        notiMap["userId"] = firebaseUser.uid
        notiMap["text"] = "commented: " + binding.addComment.text.toString()
        notiMap["postId"] = postId
        notiMap["isPost"] = true

        notiRef.push().setValue(notiMap)

    }
}