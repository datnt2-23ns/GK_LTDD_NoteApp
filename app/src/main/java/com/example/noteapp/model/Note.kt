package com.example.noteapp.model

data class Note(
    val id: Int = 0,
    val title: String = "",
    val description: String = "",
    val filePath: String? = null // Đường dẫn file lưu trong bộ nhớ thiết bị
)