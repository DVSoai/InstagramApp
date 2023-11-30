package com.example.instagramapp.ui

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.example.instagramapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        progressBar = binding.progressBar


        binding.btnSingInLink.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
        binding.btnSingUp.setOnClickListener {
            CreateAccount()
        }

    }

    private fun CreateAccount() {

        val fullName = binding.etFullName.text.toString()
        val username = binding.etUserName.text.toString()
        val email = binding.etEmailSignUp.text.toString()
        val password = binding.etPasswordSignUp.text.toString()

        when {
            TextUtils.isEmpty(fullName) -> Toast.makeText(this, "Nhập đầy đủ họ tên", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(username) -> Toast.makeText(this, "Nhập tên tài khoản", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Nhập địa chỉ email", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Nhập mật khẩu", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("please wait, this may  take a while ...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

//                progressBar.visibility = View.VISIBLE
//                progressBar.contentDescription = "Please wait, this may take a while..."

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            saveUserInfo(fullName, username, email, progressDialog)
                        }
                        else {
                            val message = task.exception!!.toString()
                            Toast.makeText(this, "Error: $message" ,Toast.LENGTH_LONG).show()
                            Log.d("Error", "Error: $message ")
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
//                        progressBar.visibility = View.GONE
                    }
            }
        }

    }

    private fun saveUserInfo(fullName: String, username: String, email: String, progressDialog: ProgressDialog) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserID
        userMap["fullName"] = fullName.lowercase()
        userMap["username"] =username.lowercase()
        userMap["email"] = email
        userMap["bio"] = "Soái đẹp trai"
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/instagramapp-b158c.appspot.com/o/Default%20Image%2Fprofile.png?alt=media&token=93611ae3-d6c3-496c-8e30-66dbc60e649d"

        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener {task ->
//                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Tài khoản đã được tạo thành công", Toast.LENGTH_LONG).show()

                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)

                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }

                else {
                    val message = task.exception!!.toString()
                    Toast.makeText(this, "Error: $message",Toast.LENGTH_LONG).show()
                    Log.d("Error", "Error: $message ")
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }

            }


    }

}