package com.fanu.flet

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,        // 笔记名称
    val content: String = "", // 笔记内容
    val filePath: String,     // 存放位置
    val lastModified: Long,   // 修改时间
    val categoryName: String = "未分类" // 分类名称
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val name: String
)

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY lastModified DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE categoryName = :categoryName ORDER BY lastModified DESC")
    fun getNotesByCategory(categoryName: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Int?): Note?

    @Upsert
    suspend fun upsertNote(note: Note): Long

    @Delete
    suspend fun deleteNote(note: Note)

    // 分类相关
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Upsert
    suspend fun upsertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
}
