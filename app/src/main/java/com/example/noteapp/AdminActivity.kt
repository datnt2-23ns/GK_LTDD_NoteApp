package com.example.noteapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.adapter.NoteAdapter
import com.example.noteapp.database.DatabaseHelper
import com.example.noteapp.model.Note
import java.io.File
import java.io.FileOutputStream

class AdminActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: NoteAdapter
    private var fileUri: Uri? = null
    private var selectedNote: Note? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            fileUri = uri
            Log.d("AdminActivity", "Image selected: $uri")
            Toast.makeText(this, "Image selected: $uri", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("AdminActivity", "No image selected")
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }

        dbHelper = DatabaseHelper(this)

        val titleEditText = findViewById<EditText>(R.id.titleEditText)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val uploadFileButton = findViewById<Button>(R.id.uploadFileButton)
        val addButton = findViewById<Button>(R.id.addButton)
        val updateButton = findViewById<Button>(R.id.updateButton)
        val recyclerView = findViewById<RecyclerView>(R.id.noteRecyclerView)
        val logoutButton = findViewById<Button>(R.id.logoutButton)

        adapter = NoteAdapter(
            onDelete = { note -> deleteNote(note) },
            onEdit = { note -> editNote(note) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadNotes()

        uploadFileButton.setOnClickListener {
            Log.d("AdminActivity", "Upload Image button clicked")
            try {
                pickImageLauncher.launch("image/*")
            } catch (e: Exception) {
                Log.e("AdminActivity", "Error launching image picker: ${e.message}")
                Toast.makeText(this, "Error opening image picker", Toast.LENGTH_SHORT).show()
            }
        }

        addButton.setOnClickListener {
            Log.d("AdminActivity", "Add Note button clicked")
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val filePath = fileUri?.let {
                Log.d("AdminActivity", "Attempting to save fileUri: $it")
                saveFileToStorage(it)
            }
            val note = Note(title = title, description = description, filePath = filePath)
            val noteId = dbHelper.addNote(note)
            Log.d("AdminActivity", "Note added with ID: $noteId, filePath: $filePath")
            Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show()
            loadNotes()
            clearInputs()
        }

        updateButton.setOnClickListener {
            selectedNote?.let { note ->
                Log.d("AdminActivity", "Update Note button clicked for note ID: ${note.id}")
                val title = titleEditText.text.toString()
                val description = descriptionEditText.text.toString()
                val filePath = fileUri?.let { saveFileToStorage(it) } ?: note.filePath
                val updatedNote = Note(id = note.id, title = title, description = description, filePath = filePath)
                dbHelper.updateNote(updatedNote)
                Log.d("AdminActivity", "Note updated with filePath: $filePath")
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
                loadNotes()
                clearInputs()
                updateButton.visibility = Button.GONE
                addButton.visibility = Button.VISIBLE
            }
        }

        logoutButton.setOnClickListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun saveFileToStorage(uri: Uri): String? {
        val fileName = "note_image_${System.currentTimeMillis()}.jpg"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
        try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            val path = file.absolutePath
            Log.d("AdminActivity", "Saved image to: $path")
            return path
        } catch (e: Exception) {
            Log.e("AdminActivity", "Error saving image: ${e.message}")
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun loadNotes() {
        val notes = dbHelper.getAllNotes()
        Log.d("AdminActivity", "Loaded notes: ${notes.size}")
        adapter.submitList(notes)
    }

    private fun deleteNote(note: Note) {
        dbHelper.deleteNote(note.id)
        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
        loadNotes()
    }

    private fun editNote(note: Note) {
        val titleEditText = findViewById<EditText>(R.id.titleEditText)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        titleEditText.setText(note.title)
        descriptionEditText.setText(note.description)
        fileUri = note.filePath?.let { Uri.fromFile(File(it)) }
        selectedNote = note

        findViewById<Button>(R.id.addButton).visibility = Button.GONE
        findViewById<Button>(R.id.updateButton).visibility = Button.VISIBLE
    }

    private fun clearInputs() {
        findViewById<EditText>(R.id.titleEditText).text.clear()
        findViewById<EditText>(R.id.descriptionEditText).text.clear()
        fileUri = null
        selectedNote = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}