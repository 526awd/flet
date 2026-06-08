package com.fanu.flet

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fanu.flet.ui.theme.FletTheme
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText

class NoteViewActivity : ComponentActivity() {
    private val viewModel: NoteEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteId = intent.getIntExtra("NOTE_ID", -1)

        enableEdgeToEdge()
        setContent {
            FletTheme {
                LaunchedEffect(Unit) {
                    if (noteId != -1) {
                        viewModel.loadNote(noteId)
                    }
                }

                NoteViewScreen(
                    title = viewModel.titleValue,
                    category = viewModel.categoryValue,
                    content = viewModel.richTextState.toMarkdown(),
                    onBackClick = { finish() },
                    onEditClick = {
                        val intent = Intent(this@NoteViewActivity, NoteEditActivity::class.java).apply {
                            putExtra("NOTE_ID", noteId)
                        }
                        startActivity(intent)
                    },
                    richTextState = viewModel.richTextState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteViewScreen(
    title: String,
    category: String,
    content: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    richTextState: com.mohamedrejeb.richeditor.model.RichTextState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = title, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${stringResource(R.string.category)}: ${if(category == "未分类") stringResource(R.string.uncategorized) else category}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            RichText(
                state = richTextState,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}