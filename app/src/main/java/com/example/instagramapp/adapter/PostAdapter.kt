package com.example.instagramapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.model.Post
import com.example.instagramapp.model.User
import com.example.instagramapp.ui.CommentsActivity
import com.example.instagramapp.ui.MainActivity
import com.example.instagramapp.ui.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private var mContext: Context, private var mPost: List<Post>): RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var firebaseUser: FirebaseUser? = null


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        var profileImage: CircleImageView
        var postImage: ImageView
        var likeButton: ImageView
        var commentButton: ImageView
        var saveButton: ImageView
        var username: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView

        init {

            profileImage = view.findViewById(R.id.user_profile_image_post)
            postImage = view.findViewById(R.id.post_image_home)
            likeButton = view.findViewById(R.id.post_image_like_btn)
            commentButton = view.findViewById(R.id.post_image_comment_btn)
            saveButton= view.findViewById(R.id.post_save_comment_btn)
            username = view.findViewById(R.id.user_name_post)
            likes = view.findViewById(R.id.likes)
            publisher = view.findViewById(R.id.publisher)
            description = view.findViewById(R.id.description)
            comments = view.findViewById(R.id.comments)

        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PostAdapter.ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.post_item_layout, viewGroup, false)

        return PostAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val post  = mPost[position]

        if (!post.postimage.isNullOrEmpty()) {
            Picasso.get().load(post.postimage).into(holder.postImage)
        } else {
        }
//        Picasso.get().load(post.postimage).into(holder.postImage)

        if(post.description == ""){
            holder.description.visibility = View.GONE
        }

        else{
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.description
        }

        publisherInfo(holder.profileImage, holder.username, holder.publisher, post.publisher)
        isLikes(post.postId, holder.likeButton)
        numberOfLike(holder.likes, post.postId)
        getTotalComments(holder.comments, post.postId)
        checkSavedStatus(post.postId, holder.saveButton)
        holder.likeButton.setOnClickListener {

            if(holder.likeButton.tag == "Like"){
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.postId)
                    .child(firebaseUser!!.uid)
                    .setValue(true)

                addNotification(post.publisher, post.postId)
            } else{
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.postId)
                    .child(firebaseUser!!.uid)
                    .removeValue()
                val intent = Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)
            }



        }

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id", post.postId)
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }

        holder.commentButton.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId", post.postId).toString()
            intentComment.putExtra("publisherId", post.publisher).toString()
            mContext.startActivity(intentComment)
        }

        holder.comments.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId", post.postId).toString()
            intentComment.putExtra("publisherId", post.publisher).toString()
            mContext.startActivity(intentComment)
        }
        holder.saveButton.setOnClickListener {
            if(holder.saveButton.tag == "Save"){
                FirebaseDatabase.getInstance().reference
                    .child("Saves")
                    .child(firebaseUser!!.uid).child(post.postId)
                    .setValue(true)
            }
            else{
                FirebaseDatabase.getInstance().reference
                    .child("Saves")
                    .child(firebaseUser!!.uid).child(post.postId)
                    .removeValue()
            }
        }

    }
    private fun getTotalComments(comments: TextView, postId: String) {

        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postId)
        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    comments.text = "view all " + snapshot.childrenCount.toString() + " comments"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun numberOfLike(likes: TextView, postId: String) {

        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postId)
        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               if(snapshot.exists()){
                   likes.text = snapshot.childrenCount.toString() + " likes"
               }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun isLikes(postId: String, likeButton: ImageView) {

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val likesRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(firebaseUser!!.uid).exists()){

                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                }else{
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun publisherInfo(profileImage: CircleImageView, username: TextView, publisher: TextView, publisherId: String) {

        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)

        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)


                    // Check if the image path is not empty or null before loading with Picasso
                    if (!user?.image.isNullOrEmpty()) {
                        Picasso.get().load(user?.image).placeholder(R.drawable.profile).into(profileImage)
                    } else {
                        // Handle the case where the image path is empty or null
                        // For example, set a default profile image
                        profileImage.setImageResource(R.drawable.profile) // Set a default profile image
                    }
//                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(profileImage)
                    username.text = user?.username
                    publisher.text = user?.fullName
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkSavedStatus(postId: String, imageView: ImageView){
        val saveRef = FirebaseDatabase.getInstance().reference
            .child("Saves")
            .child(firebaseUser!!.uid)
        saveRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(postId).exists()){
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag = "Saved"
                }
                else{
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag = "Save"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    private fun addNotification(userId: String, postId: String){
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(userId)

        val notiMap = HashMap<String, Any>()
        notiMap["userId"] = firebaseUser!!.uid
        notiMap["text"] = "liked your post "
        notiMap["postId"] = postId
        notiMap["isPost"] = true

        notiRef.push().setValue(notiMap)

    }
}