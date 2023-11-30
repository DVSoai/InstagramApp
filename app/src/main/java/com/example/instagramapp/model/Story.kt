package com.example.instagramapp.model

data class Story(
    var imageUrl: String = "",
    var timeStart: Long = 0,
    var timeEnd: Long =0,
    var storyId: String ="",
    var userId: String =""
)
