package com.fanu.flet

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedrejeb.richeditor.model.RichTextState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class NoteEditViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao = AppDatabase.getDatabase(application).noteDao()

    var currentNoteId: Int? = null
    var titleValue by mutableStateOf("")
    var categoryValue by mutableStateOf("未分类")
    var lastModified by mutableStateOf(0L)
    var filePath by mutableStateOf("")
    
    val richTextState = RichTextState()

    val categories: Flow<List<Category>> = noteDao.getAllCategories()

    fun loadNote(id: Int) {
        if (id == -1) return
        currentNoteId = id
        viewModelScope.launch {
            val note = withContext(Dispatchers.IO) {
                noteDao.getNoteById(id)
            }
            note?.let {
                titleValue = it.title
                categoryValue = it.categoryName
                richTextState.setMarkdown(it.content)
                lastModified = it.lastModified
                filePath = it.filePath
            }
        }
    }

    fun saveNote() {
        val content = richTextState.toMarkdown()
        val currentTitle = titleValue
        val currentCategory = categoryValue
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileName = if (currentNoteId != null) {
                    "note_$currentNoteId.txt"
                } else {
                    "note_${System.currentTimeMillis()}.txt"
                }
                val file = File(getApplication<Application>().filesDir, fileName)
                file.writeText(content)

                val note = Note(
                    id = currentNoteId ?: 0,
                    title = if (currentTitle.isBlank()) "未命名笔记" else currentTitle,
                    content = content,
                    filePath = file.absolutePath,
                    lastModified = System.currentTimeMillis(),
                    categoryName = currentCategory
                )
                // 使用 upsert 自动处理插入或更新逻辑
                val id = noteDao.upsertNote(note)
                
                // 在主线程更新 ID，确保 UI 状态一致性
                withContext(Dispatchers.Main) {
                    currentNoteId = id.toInt()
                    lastModified = note.lastModified
                    filePath = note.filePath
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteNote(onComplete: () -> Unit) {
        val noteId = currentNoteId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val note = noteDao.getNoteById(noteId)
                if (note != null) {
                    noteDao.deleteNote(note)
                    // Optionally delete the file too
                    val file = File(note.filePath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
