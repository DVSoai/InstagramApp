package com.example.instagramapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.adapter.UserAdapter
import com.example.instagramapp.databinding.ActivityShowUsersBinding
import com.example.instagramapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowUsersBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var recyclerView: RecyclerView

    var id : String =""
    var title: String =""
    var userList : List<User>? = null
    var idList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent

        id = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()

        val toolbar : Toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener{
            finish()
        }

        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        userList = ArrayList()
        userAdapter = UserAdapter(this, userList as ArrayList<User>, false)
        recyclerView.adapter = userAdapter

        idList = ArrayList()

        when(title) {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }
    }

    private fun getViews() {
        TODO("Not yet implemented")
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()

                for (data in snapshot.children){
                    (idList as ArrayList<String>).add(data.key!!)
                }
                showUsers()

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun getFollowing() {

        val followingsRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id)
            .child("Following")

        followingsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (idList as ArrayList<String>).clear()

                for (data in snapshot.children){
                    (idList as ArrayList<String>).add(data.key!!)
                }
                showUsers()

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun getLikes() {
       val likeRef = FirebaseDatabase.getInstance().reference.child("Likes")
           .child(id)

        likeRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    (idList as ArrayList<String>).clear()


                    for (data in snapshot.children){
                        (idList as ArrayList<String>).add(data.key!!)
                    }
                    showUsers()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun showUsers() {

        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users")

        usersRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (userList as ArrayList<User>).clear()

                for (data in snapshot.children){
                    val user = data.getValue(User::class.java)

                   for (id in idList!!){
                       if (user!!.uid == id){
                           (userList as ArrayList<User>).add(user)
                       }
                   }
                }

                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}