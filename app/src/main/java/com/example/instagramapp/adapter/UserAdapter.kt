package com.example.instagramapp.adapter

import android.content.Context
import android.content.Intent
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity

import androidx.recyclerview.widget.RecyclerView
import com.example.instagramapp.R
import com.example.instagramapp.model.User
import com.example.instagramapp.ui.MainActivity
import com.example.instagramapp.ui.fragments.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter( private var mContext: Context,
                   private var mUser: List<User>,
                   private var isFragment: Boolean = false) : RecyclerView.Adapter<UserAdapter.ViewHolder>()
{


    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var userName: TextView
        var userFullame: TextView
        var userProfile: CircleImageView
        var btnFollow: Button


        init {
            userName = view.findViewById(R.id.user_name_search)
            userFullame = view.findViewById(R.id.user_full_name_search)
            userProfile = view.findViewById(R.id.user_profile_img_search)
            btnFollow = view.findViewById(R.id.btn_follow_search)

        }
    }
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.user_item_layout, viewGroup, false)

            return ViewHolder(view)
        }

        // Thay thế nội dung chế độ xem (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            val user = mUser[position]
            viewHolder.userName.text = user.username
            viewHolder.userFullame.text = user.fullName
            Picasso.get().load(user.image).placeholder(R.drawable.profile).into(viewHolder.userProfile)

            checkFollowingStatus(user.uid.toString(), viewHolder.btnFollow)

            viewHolder.itemView.setOnClickListener(View.OnClickListener {
               if (isFragment){
                   val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                   pref.putString("profileId", user.uid)
                   pref.apply()

                   (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                       .replace(R.id.fragment_container, ProfileFragment()).commit()


               }else{
                   val intent = Intent(mContext, MainActivity::class.java)
                   intent.putExtra("publisherId", user.uid)
                   mContext.startActivity(intent)


               }
            })

            viewHolder.btnFollow.setOnClickListener {
                if (viewHolder.btnFollow.text.toString() == "Follow") {

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(user.uid.toString())
                            .setValue(true).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    firebaseUser?.uid.let { it ->
                                        FirebaseDatabase.getInstance().reference
                                            .child("Follow").child(user.uid.toString())
                                            .child("Followers").child(it.toString())
                                            .setValue(true).addOnCompleteListener { task ->
                                                if (task.isSuccessful) {

                                                }
                                            }
                                    }
                                }
                            }
                    }

                    addNotification(user.uid.toString())

                }
                else {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(user.uid.toString())
                            .removeValue().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    firebaseUser?.uid.let { it1 ->
                                        FirebaseDatabase.getInstance().reference
                                            .child("Follow").child(user.uid.toString())
                                            .child("Followers").child(it1.toString())
                                            .removeValue().addOnCompleteListener { task ->
                                                if (task.isSuccessful) {

                                                }
                                            }
                                    }
                                }
                            }
                    }
                }
            }
        }

    private fun checkFollowingStatus(uid: String, btnFollow: Button) {

        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        followingRef.addValueEventListener( object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(uid).exists()) {
                    btnFollow.text = "Following"
                } else {
                    btnFollow.text = "Follow"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
        }

    // Trả về kích thước của tập dữ liệu của bạn (invoked by the layout manager)
        override fun getItemCount(): Int {
            return mUser.size
        }

    private fun addNotification(userId: String){
        val notiRef = FirebaseDatabase.getInstance().reference
            .child("Notifications")
            .child(userId)

        val notiMap = HashMap<String, Any>()
        notiMap["userId"] = firebaseUser!!.uid
        notiMap["text"] = "started following you "
        notiMap["postId"] = ""
        notiMap["isPost"] = false

        notiRef.push().setValue(notiMap)

    }

}