package com.example.noteapp.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteapp.R
import com.example.noteapp.model.Note
import java.io.File

class NoteAdapter(
    private val onDelete: (Note) -> Unit,
    private val onEdit: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note)
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView = itemView.findViewById<TextView>(R.id.titleTextView)
        private val descriptionTextView = itemView.findViewById<TextView>(R.id.descriptionTextView)
        private val noteImageView = itemView.findViewById<ImageView>(R.id.noteImageView)
        private val deleteButton = itemView.findViewById<Button>(R.id.deleteButton)

        fun bind(note: Note) {
            titleTextView.text = note.title
            descriptionTextView.text = note.description
            note.filePath?.let { path ->
                val file = File(path)
                Log.d("NoteAdapter", "Loading image from: $path, exists: ${file.exists()}")
                if (file.exists()) {
                    noteImageView.setImageURI(Uri.fromFile(file))
                } else {
                    Log.w("NoteAdapter", "Image file does not exist: $path")
                    noteImageView.setImageDrawable(null)
                }
            } ?: run {
                Log.d("NoteAdapter", "No filePath for note ID: ${note.id}")
                noteImageView.setImageDrawable(null)
            }
            itemView.setOnClickListener { onEdit(note) }
            deleteButton.setOnClickListener { onDelete(note) }
        }
    }
}

class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem == newItem
}