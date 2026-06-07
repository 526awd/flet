package com.fanu.flet

import com.fanu.flet.R
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fanu.flet.ui.theme.FletTheme
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FletTheme {
                val viewModel: MainActivityViewModel by viewModels()
                LaunchedEffect(Unit) {
                    viewModel.navigationEvent.collect { noteId ->
                        val destination = if (noteId == -1) NoteEditActivity::class.java else NoteViewActivity::class.java
                        val intent = Intent(this@MainActivity, destination).apply {
                            putExtra("NOTE_ID", noteId)
                        }
                        startActivity(intent)
                    }
                }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    val notes by viewModel.notes.collectAsState(initial = emptyList())
                    val categories by viewModel.categories.collectAsState(initial = emptyList())
                    val selectedCategory by viewModel.selectedCategory.collectAsState()
                    val context = LocalContext.current

                    Greeting(
                        modifier = Modifier.padding(innerPadding),
                        notes = notes,
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) },
                        onAddCategory = { viewModel.addCategory(it) },
                        onNavigateToEdit = { noteId ->
                            viewModel.onNavigateToEdit(noteId)
                        },
                        onDelete = { note ->
                            viewModel.delete(note)
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.deleted_note, note.title),
                                    actionLabel = context.getString(R.string.undo),
                                    withDismissAction = true
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoDelete(note)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting(
    modifier: Modifier = Modifier,
    notes: List<Note> = emptyList(),
    categories: List<Category> = emptyList(),
    selectedCategory: String? = null,
    onCategorySelected: (String?) -> Unit,
    onAddCategory: (String) -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onDelete: (Note) -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    
    // Snackbar resources
    val deletedMsg = stringResource(R.string.deleted_note)
    val undoLabel = stringResource(R.string.undo)
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(stringResource(R.string.category), modifier = Modifier.padding(16.dp), fontSize = 20.sp)
                
                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.all_notes)) },
                    selected = selectedCategory == null,
                    onClick = {
                        onCategorySelected(null)
                        scope.launch { drawerState.close() }
                    }
                )
                
                categories.forEach { category ->
                    NavigationDrawerItem(
                        label = { Text(text = category.name) },
                        selected = selectedCategory == category.name,
                        onClick = {
                            onCategorySelected(category.name)
                            scope.launch { drawerState.close() }
                        }
                    )
                }

                androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.add_category)) },
                    selected = false,
                    onClick = { showAddCategoryDialog = true }
                )
                
                androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    label = { Text(text = stringResource(R.string.settings))},
                    selected = false,
                    onClick = {
                        scope.launch { 
                            drawerState.close()
                            val intent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(text = selectedCategory ?: stringResource(R.string.app_name)) 
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                )
            },
            floatingActionButton = { 
                OutlinedButton(onClick = { onNavigateToEdit(-1) }) { // -1 表示新建
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = stringResource(R.string.add))
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                if (notes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = if (selectedCategory == null) stringResource(R.string.no_notes_all) else stringResource(R.string.no_notes_category))
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        items(
                            items = notes,
                            key = { it.id } // 性能优化：添加 Key 以支持高效列表更新
                        ) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNavigateToEdit(note.id) },
                                onDelete = { onDelete(note) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        onAddCategory(newCategoryName)
                        newCategoryName = ""
                        showAddCategoryDialog = false
                    }
                }) {
                    Text(stringResource(R.string.add))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = { Text(stringResource(R.string.new_category)) },
            text = {
                androidx.compose.material3.TextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    placeholder = { Text(stringResource(R.string.category_name)) },
                    singleLine = true
                )
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(note: Note, onClick: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showProperties by remember { mutableStateOf(false) }

    val richTextState = rememberRichTextState()
    LaunchedEffect(note.content) {
        richTextState.setMarkdown(note.content)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { expanded = true }
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            RichText(
                state = richTextState,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "${stringResource(R.string.category)}: ${note.categoryName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false } // 点击菜单外区域关闭
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.edit)) },
                onClick = {
                    expanded = false
                    onClick()
                }
            )
            DropdownMenuItem(text = {Text(text = stringResource(R.string.note_properties))},onClick = {
                expanded = false
                showProperties = true
            })
            DropdownMenuItem(
                text = { Text(stringResource(R.string.del), color = MaterialTheme.colorScheme.error) },
                onClick = {
                    expanded = false
                    onDelete()
                }
            )
            
        }
    }

    if (showProperties) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showProperties = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showProperties = false }) {
                    Text(stringResource(R.string.close))
                }
            },
            title = { Text(stringResource(R.string.note_properties)) },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    PropertyItem(stringResource(R.string.note_title), note.title)
                    PropertyItem(stringResource(R.string.word_count), note.content.length.toString())
                    PropertyItem(stringResource(R.string.Modified_Time), SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(note.lastModified))
                    PropertyItem(stringResource(R.string.file_path), note.filePath)
                }
            }
        )
    }
}




@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FletTheme {
        Greeting(
            notes = emptyList(),
            onNavigateToEdit = {},
            onDelete = {},
            onCategorySelected = {},
            onAddCategory = {}
        )
    }
}
@Preview(name="设备演示", showSystemUi = true, showBackground = true)
@Composable
fun device(){
    FletTheme {
        Greeting(
            notes = emptyList(),
            onNavigateToEdit = {},
            onDelete = {},
            onCategorySelected = {},
            onAddCategory = {}
        )
    }
}