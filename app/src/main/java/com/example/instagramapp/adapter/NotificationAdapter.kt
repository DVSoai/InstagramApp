package com.example.instagramapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.model.Notification
import com.example.instagramapp.model.Post
import com.example.instagramapp.model.User
import com.example.instagramapp.ui.fragments.PostDetailsFragment
import com.example.instagramapp.ui.fragments.ProfileFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter(private val mContext: Context, private var mNotification: List<Notification>) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view:View) : RecyclerView.ViewHolder(view){

        var postImage: ImageView
        var profileImage: CircleImageView
        var userName: TextView
        var text: TextView
        init {
            postImage = view.findViewById(R.id.notification_post_img)
            profileImage = view.findViewById(R.id.notification_profile_img)
            userName = view.findViewById(R.id.username_notification)
            text = view.findViewById(R.id.comment_notification)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notifications_item_layout, parent ,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  mNotification.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val notification = mNotification[position]

        if (notification.text.equals("started following you ")){
            holder.text.text = "started following you "
        }else if (notification.text.equals("liked your post ")){
            holder.text.text = "liked your post "
        }else if (notification.text.contains("commented: ")){
            holder.text.text = notification.text.replace("commented:", "commented: ")
        }else{
            holder.text.text = notification.text
        }

        userInfo(holder.profileImage, holder.userName, notification.userId)

        if(!notification.isPost){
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage, notification.postId)

        }
        else{
            holder.postImage.visibility = View.GONE

        }

        holder.itemView.setOnClickListener {
            if (!notification.isPost){

                val editor = mContext.getSharedPreferences("FREFS", Context.MODE_PRIVATE).edit()

                editor.putString("postId", notification.postId)
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction().replace(R.id.fragment_container, PostDetailsFragment()).commit()

            }
            if (notification.isPost){
                val editor1 = mContext.getSharedPreferences("FREFS", Context.MODE_PRIVATE).edit()
                editor1.putString("profileId ", notification.userId)
                editor1.apply()
                val fragment = ProfileFragment()
                (mContext as FragmentActivity).supportFragmentManager
                    .beginTransaction().replace(R.id.fragment_container, fragment).commit()
            }
//            else{
//                val editor = mContext.getSharedPreferences("FREFS", Context.MODE_PRIVATE).edit()
//                editor.putString("profileId", notification.userId)
//                editor.apply()
//                (mContext as FragmentActivity).supportFragmentManager
//                    .beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
//            }
        }

    }
    private fun userInfo(imageView: ImageView, userName: TextView, publisherId: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(publisherId)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(imageView)
                    userName.text = user.username
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
    private fun getPostImage(imageView: ImageView, postId: String) {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
            .child(postId)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()) {

                    val post = snapshot.getValue<Post>(Post::class.java)
                    if ( !post!!.postimage.isNullOrEmpty()) {
                        Picasso.get().load(post.postimage).placeholder(R.drawable.profile).into(imageView)
                    }

//                    Picasso.get().load(post!!.postimage).placeholder(R.drawable.profile).into(imageView)

                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}