package com.fanu.flet

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

class NoteWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val noteDao = database.noteDao()

        provideContent {
            GlanceTheme {
                val notes = noteDao.getAllNotes().collectAsState(initial = emptyList())
                
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(8.dp),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Text(
                        text = "我的笔记",
                        style = TextStyle(color = ColorProvider(Color.White))
                    )
                    
                    LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                        items(notes.value) { note ->
                            Text(
                                text = note.title,
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .clickable(actionStartActivity<MainActivity>()),
                                style = TextStyle(color = ColorProvider(Color.LightGray))
                            )
                        }
                    }
                }
            }
        }
    }
}
