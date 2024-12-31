package com.example.sqlitedemo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "NoteDatabase"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NOTES = "Notes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NOTES ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_TITLE TEXT, $COLUMN_CONTENT TEXT)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        onCreate(db)
    }

    fun addNote(title: String, content: String): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_CONTENT, content)
        }
        val result = db.insert(TABLE_NOTES, null, contentValues)
        db.close()
        return result != -1L
    }

    fun updateNote(title: String, content: String): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_CONTENT, content)
        }
        val result = db.update(TABLE_NOTES, contentValues, "$COLUMN_TITLE=?", arrayOf(title))
        db.close()
        return result > 0
    }

    fun deleteNote(title: String): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_NOTES, "$COLUMN_TITLE=?", arrayOf(title))
        db.close()
        return result > 0
    }

    fun getAllNotes(sorted: Boolean = false): List<String> {
        val noteList = ArrayList<String>()
        val db = readableDatabase
        val sortOrder = if (sorted) "$COLUMN_TITLE ASC" else null
        val cursor = db.query(TABLE_NOTES, null, null, null, null, null, sortOrder)
        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT))
                noteList.add("Title: $title, Content: $content")
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return noteList
    }
}
