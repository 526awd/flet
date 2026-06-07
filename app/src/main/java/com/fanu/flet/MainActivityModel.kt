package com.fanu.flet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.io.File
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val noteDao = AppDatabase.getDatabase(application).noteDao()
    
    // 当前选中的分类，null 表示“全部”
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    // 根据选中的分类过滤笔记列表
    val notes: Flow<List<Note>> = _selectedCategory.flatMapLatest { category ->
        if (category == null) {
            noteDao.getAllNotes()
        } else {
            noteDao.getNotesByCategory(category)
        }
    }

    // 获取所有分类
    val categories: Flow<List<Category>> = noteDao.getAllCategories()

    fun selectCategory(categoryName: String?) {
        _selectedCategory.value = categoryName
    }

    fun addCategory(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.upsertCategory(Category(name))
        }
    }

    // 使用 SharedFlow 处理带参数的跳转事件 (传递笔记 ID，-1 表示新建)
    private val _navigationEvent = MutableSharedFlow<Int>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onNavigateToEdit(noteId: Int = -1) {
        viewModelScope.launch {
            _navigationEvent.emit(noteId)
        }
    }
    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            // 注意：物理文件如果在这里删除了，撤销操作只能恢复数据库记录
            // 如果想要完整的“撤销”，建议先不删除物理文件，或者将其移动到临时目录
            val file = File(note.filePath)
            if (file.exists()) {
                file.delete()
            }
            noteDao.deleteNote(note)
        }
    }

    fun undoDelete(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            noteDao.upsertNote(note)
        }
    }
}
