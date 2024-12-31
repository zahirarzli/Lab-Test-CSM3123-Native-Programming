package com.example.sqlitedemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.recyclerviewsqlitedemo.R
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText
    private lateinit var resultTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var addButton: Button
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button
    private lateinit var viewButton: Button
    private lateinit var viewSortedButton: Button
    private lateinit var exportButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseHelper = DatabaseHelper(this)
        titleEditText = findViewById(R.id.et_title)
        contentEditText = findViewById(R.id.et_content)
        resultTextView = findViewById(R.id.tv_result)
        searchEditText = findViewById(R.id.et_search)
        addButton = findViewById(R.id.btn_add)
        updateButton = findViewById(R.id.btn_update)
        deleteButton = findViewById(R.id.btn_delete)
        viewButton = findViewById(R.id.btn_view)
        viewSortedButton = findViewById(R.id.btn_view_sorted)
        exportButton = findViewById(R.id.btn_export)

        addButton.setOnClickListener {
            addNote()
        }
        updateButton.setOnClickListener {
            updateNote()
        }
        deleteButton.setOnClickListener {
            deleteNote()
        }
        viewButton.setOnClickListener {
            viewNotes()
        }
        viewSortedButton.setOnClickListener {
            viewSortedNotes()
        }
        exportButton.setOnClickListener {
            checkPermissionsAndExportNotes()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterNotes(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun addNote() {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        if (validateTitle(title)) {
            val success = databaseHelper.addNote(title, content)
            Toast.makeText(this, if (success) "Note added successfully!" else "Failed to add note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNote() {
        val title = titleEditText.text.toString()
        val content = contentEditText.text.toString()
        if (validateTitle(title)) {
            val success = databaseHelper.updateNote(title, content)
            Toast.makeText(this, if (success) "Note updated successfully!" else "Failed to update note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteNote() {
        val title = titleEditText.text.toString()
        if (validateTitle(title)) {
            val success = databaseHelper.deleteNote(title)
            Toast.makeText(this, if (success) "Note deleted successfully!" else "Failed to delete note", Toast.LENGTH_SHORT).show()
        }
    }

    private fun viewNotes() {
        val notes = databaseHelper.getAllNotes()
        resultTextView.text = if (notes.isNotEmpty()) notes.joinToString("\n") else "No notes found"
    }

    private fun viewSortedNotes() {
        val notes = databaseHelper.getAllNotes(sorted = true)
        resultTextView.text = if (notes.isNotEmpty()) notes.joinToString("\n") else "No notes found"
    }

    private fun validateTitle(title: String): Boolean {
        return if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun checkPermissionsAndExportNotes() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            exportNotesToFile()
        }
    }

    private fun exportNotesToFile() {
        val notes = databaseHelper.getAllNotes()
        if (notes.isEmpty()) {
            Toast.makeText(this, "No notes to export", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "notes.txt"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            val fileWriter = FileWriter(file)
            notes.forEach {
                fileWriter.write("$it\n")
            }
            fileWriter.close()
            Toast.makeText(this, "Notes exported to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to export notes", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                exportNotesToFile()
            } else {
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterNotes(query: String) {
        val allNotes = databaseHelper.getAllNotes()
        val filteredNotes = allNotes.filter { it.contains(query, ignoreCase = true) }
        resultTextView.text = if (filteredNotes.isNotEmpty()) filteredNotes.joinToString("\n") else "No notes found"
    }
}
