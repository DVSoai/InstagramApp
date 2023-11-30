package com.example.instagramapp.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast

import com.example.instagramapp.R
import com.example.instagramapp.databinding.ActivityAccountSettingsBinding
import com.example.instagramapp.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView


class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountSettingsBinding
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")
        binding.btnLogout.setOnClickListener {

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.changImgTextBtn.setOnClickListener {

            checker = "clicked"

            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@AccountSettingsActivity)

        }

        binding.imgSaveProfile.setOnClickListener{

            if (checker == "clicked")
            {
                uploadImageAndUpdateInfo()
            }
            else
            {
                updateUserInfoOnly()
            }
        }

        userInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE  &&  resultCode == Activity.RESULT_OK  &&  data != null)
        {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
           binding.profileImgViewProfileFrag.setImageURI(imageUri)
        }
    }

    private fun uploadImageAndUpdateInfo() {


        when {
            imageUri == null -> Toast.makeText(this, "Vui lòng chọn hình ảnh", Toast.LENGTH_LONG).show()

            TextUtils.isEmpty(binding.fullNameProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Nhập  họ tên", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.usernameProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Nhập tên tài khoản", Toast.LENGTH_LONG).show()
            }

            TextUtils.isEmpty(binding.bioProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Nhập bio", Toast.LENGTH_LONG).show()
            }

            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                val fileRef = storageProfilePicRef!!.child(firebaseUser.uid + ".jpg")
                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->

                    if (!task.isSuccessful){
                        task.exception?.let {
                            throw  it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl

                }).addOnCompleteListener ( OnCompleteListener<Uri> {task ->
                    if (task.isSuccessful) {

                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullName"] = binding.fullNameProfileFrag.text.toString().lowercase()
                        userMap["username"] =binding.usernameProfileFrag.text.toString().lowercase()
                        userMap["bio"] = binding.bioProfileFrag.text.toString().lowercase()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this, "Thay đổi ảnh thành công", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else {
                        progressDialog.dismiss()
                    }
                })
            }
        }
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
//            && requestCode == Activity.RESULT_OK && data != null) {
//            val result = CropImage.getActivityResult(data)
//            imageUri = result.uri
//            binding.profileImgViewProfileFrag.setImageURI(imageUri)
//        }
//    }



    private fun updateUserInfoOnly() {

        when {
            TextUtils.isEmpty(binding.fullNameProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Nhập  họ tên", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(binding.usernameProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Nhập tên tài khoản", Toast.LENGTH_LONG).show()
            }
            TextUtils.isEmpty(binding.bioProfileFrag.text.toString()) -> {
                Toast.makeText(this, "Nhập bio", Toast.LENGTH_LONG).show()
            }
            else -> {

                val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
                val userMap = HashMap<String, Any>()
                userMap["fullName"] = binding.fullNameProfileFrag.text.toString().lowercase()
                userMap["username"] =binding.usernameProfileFrag.text.toString().lowercase()
                userMap["bio"] = binding.bioProfileFrag.text.toString().lowercase()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this, "Thay đổi thông tin tài khoản thành công", Toast.LENGTH_LONG).show()
                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)

                startActivity(intent)
                finish()
            }
        }


    }


    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()) {
                    val user = snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(binding.profileImgViewProfileFrag)
                    binding.usernameProfileFrag.setText(user.username)
                    binding.fullNameProfileFrag.setText(user.fullName)
                    binding.bioProfileFrag.setText(user.bio)

                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

}