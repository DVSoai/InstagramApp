package com.example.instagramapp.model

data class Notification(
    var userId: String ="",
    var text: String="",
    var postId: String="",
    var isPost: Boolean = false
)
