package com.example.noteappusingfirebase.ui.theme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.noteappusingfirebase.Note
import com.example.noteappusingfirebase.NoteViewModel

@Composable
fun NoteScreen(viewModel: NoteViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }

    val notes by viewModel.notes.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            viewModel.addNote(title, description)
            title = ""
            description = ""
        }) {
            Text("Add Note")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(notes) { note ->
                Card(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(note.title, style = MaterialTheme.typography.titleLarge)
                            Text(note.description)
                        }
                        Button(onClick = {
                            selectedNote = note
                            title = note.title
                            description = note.description
                            showEditDialog = true
                        }) {
                            Text("Edit")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { viewModel.deleteNote(note.id) }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog && selectedNote != null) {
        Dialog(onDismissRequest = { showEditDialog = false }) {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Edit Note", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        viewModel.updateNote(selectedNote!!.copy(title = title, description = description))
                        showEditDialog = false
                        title = ""
                        description = ""
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}