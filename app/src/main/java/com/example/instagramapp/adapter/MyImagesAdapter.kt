package com.example.instagramapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.model.Post
import com.example.instagramapp.ui.fragments.PostDetailsFragment
import com.squareup.picasso.Picasso

class MyImagesAdapter(private val mContext: Context, private val mPost: List<Post>) : RecyclerView.Adapter<MyImagesAdapter.ViewHolder>() {

//    private var mPost : List<Post>? = null
//    init {
//        this.mPost = mPost
//    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){

        var postImage: ImageView
        init {
            postImage = view.findViewById(R.id.post_img)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.images_item_layout, parent, false)

        return ViewHolder(view)

    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val post: Post = mPost[position]
        Picasso.get().load(post.postimage).into(holder.postImage)

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("FREFS", Context.MODE_PRIVATE).edit()

            editor.putString("postId", post.postId)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager
                .beginTransaction().replace(R.id.fragment_container, PostDetailsFragment()).commit()
        }

    }
    override fun getItemCount(): Int {

        return mPost.size
    }
}