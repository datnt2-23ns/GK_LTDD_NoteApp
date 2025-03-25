package com.example.noteapp.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.noteapp.model.Note

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "NoteApp.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NOTES = "notes"
        private const val TABLE_USERS = "users"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createNotesTable = """
            CREATE TABLE $TABLE_NOTES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                filePath TEXT
            )
        """.trimIndent()
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createNotesTable)
        db.execSQL(createUsersTable)

        // Thêm user admin mặc định
        val values = ContentValues().apply {
            put("email", "admin@example.com")
            put("password", "admin123")
            put("role", "admin")
        }
        val rowId = db.insert(TABLE_USERS, null, values)
        Log.d("DatabaseHelper", "Inserted default admin user, rowId: $rowId")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    fun checkLogin(email: String, password: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT role FROM $TABLE_USERS WHERE email = ? AND password = ?",
            arrayOf(email, password)
        )
        return if (cursor.moveToFirst()) {
            val role = cursor.getString(0)
            Log.d("DatabaseHelper", "Login success, role: $role")
            cursor.close()
            role
        } else {
            Log.d("DatabaseHelper", "Login failed, no user found for email: $email, password: $password")
            cursor.close()
            null
        }
    }

    // Các hàm khác cho Note
    fun addNote(note: Note): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", note.title)
            put("description", note.description)
            put("filePath", note.filePath)
        }
        return db.insert(TABLE_NOTES, null, values)
    }

    fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NOTES", null)
        if (cursor.moveToFirst()) {
            do {
                val note = Note(
                    id = cursor.getInt(0),
                    title = cursor.getString(1),
                    description = cursor.getString(2),
                    filePath = cursor.getString(3)
                )
                notes.add(note)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notes
    }

    fun deleteNote(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_NOTES, "id = ?", arrayOf(id.toString()))
    }

    fun updateNote(note: Note): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("title", note.title)
            put("description", note.description)
            put("filePath", note.filePath)
        }
        return db.update(TABLE_NOTES, values, "id = ?", arrayOf(note.id.toString()))
    }
}