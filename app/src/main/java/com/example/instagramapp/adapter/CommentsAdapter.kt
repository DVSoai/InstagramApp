package com.example.instagramapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.instagramapp.R
import com.example.instagramapp.model.Comment
import com.example.instagramapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter(private val mContext: Context, private var mComment: MutableList<Comment>?): RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? =null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var imgProfile: CircleImageView
        var userNameTV: TextView
        var commentTV: TextView
        init {
            imgProfile = view.findViewById(R.id.user_profile_image_comment)
            userNameTV = view.findViewById(R.id.user_name_comment)
            commentTV = view.findViewById(R.id.tv_comment)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comments_item_layout, parent, false)

        return  CommentsAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val comment = mComment!![position]

        holder.commentTV.text = comment.comments
        getUserInfo(holder.imgProfile, holder.userNameTV,comment.publisher)

    }

    private fun getUserInfo(imgProfile: CircleImageView, userNameTV: TextView, publisher: String) {

        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
            .child(publisher)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)

                    // Check if the image path is not empty or null before loading with Picasso
                    if (!user?.image.isNullOrEmpty()) {
                        Picasso.get().load(user?.image).placeholder(R.drawable.profile).into(imgProfile)
                    } else {
                        // Handle the case where the image path is empty or null
                        // For example, set a default profile image
                        imgProfile.setImageResource(R.drawable.profile) // Set a default profile image
                    }
//                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(imgProfile)
                    userNameTV.text = user?.username
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


    override fun getItemCount(): Int {
        return  mComment!!.size
    }
}