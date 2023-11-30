package com.example.instagramapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.model.Story
import com.example.instagramapp.model.User
import com.example.instagramapp.ui.AddStoryActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter(private val mContext: Context, private val mStory: List<Story>): RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        //StoryItem
        var story_img_seen: CircleImageView ?= null
        var story_img: CircleImageView?= null
        var story_username: TextView?= null

        //AddStoryItem
        var story_plus_btn : ImageView? =null
        var addStory_text: TextView?= null
        init {
            story_img_seen = view.findViewById(R.id.story_img_seen)
            story_img = view.findViewById(R.id.story_img)
            story_username = view.findViewById(R.id.story_username)

            story_plus_btn = view.findViewById(R.id.story_add)
            addStory_text = view.findViewById(R.id.add_story_text)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

      return if (viewType == 0){
          val view = LayoutInflater.from(mContext)
              .inflate(R.layout.add_story_item, parent, false)
          ViewHolder(view)
        }
         else{
          val view = LayoutInflater.from(mContext)
              .inflate(R.layout.story_item, parent, false)
          ViewHolder(view)
      }

    }

    override fun getItemCount(): Int {
       return mStory.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = mStory[position]

        userInfo(holder, story.userId, position)

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, AddStoryActivity::class.java)
            intent.putExtra("userId", story.storyId)
            mContext.startActivity(intent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position ==0){
            return  0
        }
        return  1
    }

    private fun userInfo(viewHolder: ViewHolder, userId: String, position: Int){

        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId)

        usersRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    user?.let {
                        // Ensure the image view is not null before loading the image
                        if (position != 0 && viewHolder.story_img_seen != null) {
                            Picasso.get().load(it.image).placeholder(R.drawable.profile).into(viewHolder.story_img_seen!!)
                            viewHolder.story_username?.text = it.username
                        } else if (viewHolder.story_img != null) {
                            Picasso.get().load(it.image).placeholder(R.drawable.profile).into(viewHolder.story_img!!)
                        }
                    }
//
//                        Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(viewHolder.story_img)
//
//                    if (position!=0){
//                        Picasso.get().load(user?.image).placeholder(R.drawable.profile).into(viewHolder.story_img_seen)
//                        viewHolder.story_username!!.text = user?.username
//                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
}