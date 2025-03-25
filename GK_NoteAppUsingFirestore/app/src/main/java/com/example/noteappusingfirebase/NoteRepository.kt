package com.example.noteappusingfirebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class NoteRepository {
    private val db = Firebase.firestore.collection("notes")

    suspend fun getNotes(): List<Note> {
        return db.get().await().documents.map {
            Note(it.id, it.getString("title") ?: "", it.getString("description") ?: "")
        }
    }

    suspend fun addNote(note: Note) {
        val newNote = hashMapOf("title" to note.title, "description" to note.description)
        db.add(newNote).await()
    }

    suspend fun updateNote(note: Note) {
        val updatedNote = hashMapOf("title" to note.title, "description" to note.description)
        db.document(note.id).set(updatedNote).await()
    }

    suspend fun deleteNote(noteId: String) {
        db.document(noteId).delete().await()
    }
}