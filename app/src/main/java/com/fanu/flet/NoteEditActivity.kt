package com.fanu.flet

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row

import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fanu.flet.ui.theme.FletTheme
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.model.RichSpanStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor

// Extension functions for RichTextState in version 1.0.0-rc14
fun RichTextState.addImage(
    model: Any,
    width: TextUnit = 200.sp,
    height: TextUnit = 200.sp,
    contentDescription: String? = null,
) {
    this.addTextAtIndex(
        index = selection.min,
        text = " ",
    )
    this.addRichSpan(
        spanStyle = RichSpanStyle.Image(
            model = model,
            width = width,
            height = height,
            contentDescription = contentDescription,
        ),
        textRange = TextRange(selection.min - 1, selection.min)
    )
}
fun RichTextState.toggleBold() = toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
val RichTextState.isBold: Boolean get() = currentSpanStyle.fontWeight == FontWeight.Bold

fun RichTextState.toggleItalic() = toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
val RichTextState.isItalic: Boolean get() = currentSpanStyle.fontStyle == FontStyle.Italic

fun RichTextState.toggleUnderline() = toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
val RichTextState.isUnderline: Boolean get() = currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true

fun RichTextState.toggleStrikethrough() = toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
val RichTextState.isStrikethrough: Boolean get() = currentSpanStyle.textDecoration?.contains(TextDecoration.LineThrough) == true

val QuoteSpanStyle = SpanStyle(
    background = Color.LightGray.copy(alpha = 0.2f),
    fontStyle = FontStyle.Italic
)

fun RichTextState.toggleQuote() = toggleSpanStyle(QuoteSpanStyle)
val RichTextState.isQuote: Boolean get() = currentSpanStyle.background == QuoteSpanStyle.background

fun RichTextState.toggleUnorderedList() = toggleUnorderedList()
val RichTextState.isUnorderedList: Boolean get() = isUnorderedList

fun RichTextState.toggleOrderedList() = toggleOrderedList()
val RichTextState.isOrderedList: Boolean get() = isOrderedList

class NoteEditActivity : ComponentActivity() {
    private val viewModel: NoteEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val noteId = intent.getIntExtra("NOTE_ID", -1)
        
        enableEdgeToEdge()
        setContent {
            FletTheme {
                // 只在首次进入时加载数据
                LaunchedEffect(Unit) {
                    if (noteId != -1) {
                        viewModel.loadNote(noteId)
                    }
                }

                NoteEditScreen(
                    titleValue = viewModel.titleValue,
                    onTitleChange = { viewModel.titleValue = it },
                    categoryValue = viewModel.categoryValue,
                    onCategoryChange = { viewModel.categoryValue = it },
                    categories = viewModel.categories.collectAsState(initial = emptyList()).value,
                    richTextState = viewModel.richTextState,
                    onSaveClick = { viewModel.saveNote() },
                    onDeleteClick = {
                        viewModel.deleteNote {
                            finish()
                        }
                    },
                    onBackClick = { finish() },
                    lastModified = viewModel.lastModified,
                    filePath = viewModel.filePath,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteEditScreen(
    titleValue: String,
    onTitleChange: (String) -> Unit,
    categoryValue: String,
    onCategoryChange: (String) -> Unit,
    categories: List<Category>,
    richTextState: RichTextState,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
    lastModified: Long,
    filePath: String
) {
    
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var expandedQ by remember { mutableStateOf(false) }
    var showProperties by remember { mutableStateOf(false) }

    val documentLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { selectedUri ->
                try {
                    // 创建内部存储路径: files/images/
                    val imagesDir = java.io.File(context.filesDir, "images")
                    if (!imagesDir.exists()) imagesDir.mkdirs()

                    // 生成唯一文件名防止冲突
                    val fileName = "img_${System.currentTimeMillis()}.png"
                    val destFile = java.io.File(imagesDir, fileName)

                    // 复制文件流
                    context.contentResolver.openInputStream(selectedUri)?.use { input ->
                        destFile.outputStream().use { output -> input.copyTo(output) }
                    }

                    // 使用内部文件的路径，以 Markdown 语法插入到编辑器
                    val markdownImagePath = "![](file://${destFile.absolutePath})"
                    richTextState.addTextAtIndex(richTextState.selection.min, markdownImagePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "图片导入失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    // Prepare strings outside of onClick to avoid context warnings in some environments
    val savedMessage = stringResource(R.string.saved)

    if (showProperties) {
        NotePropertyDialog(
            title = titleValue,
            wordCount = richTextState.annotatedString.text.length,
            lastModified = lastModified,
            filePath = filePath,
            onDismiss = { showProperties = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        TextField(
                            value = titleValue,
                            onValueChange = onTitleChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.INPUT_TITLE)) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.titleLarge,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Box {
                            Text(
                                text = "${stringResource(R.string.category)}: ${if(categoryValue == "未分类") stringResource(R.string.uncategorized) else categoryValue}",
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .combinedClickable(onClick = { expanded = true }),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.uncategorized)) },
                                    trailingIcon = {
                                        RadioButton(
                                            selected = categoryValue == "未分类",
                                            onClick = null
                                        )
                                    },
                                    onClick = {
                                        onCategoryChange("未分类")
                                        expanded = false
                                    }
                                )
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        trailingIcon = {
                                            RadioButton(
                                                selected = categoryValue == category.name,
                                                onClick = null
                                            )
                                        },
                                        onClick = {
                                            onCategoryChange(category.name)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
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
                    IconButton(onClick = {
                        onSaveClick()
                        Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = stringResource(R.string.save))
                    }

                    IconButton(onClick = {
                        expandedQ = true
                    }) {
                        Icon(imageVector = Icons.Rounded.MoreHoriz, contentDescription = stringResource(R.string.app_name))
                    }
                    DropdownMenu(
                        expanded = expandedQ,
                        onDismissRequest = { expandedQ = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.del)) },
                            onClick = {
                                expandedQ = false
                                onDeleteClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.note_properties)) },
                            onClick = {
                                expandedQ = false
                                showProperties = true
                            }
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
        Column(modifier = Modifier.padding(innerPadding)) {
            EditorControls(state = richTextState, documentLauncher = documentLauncher)
            val scrollState = rememberScrollState()
            RichTextEditor(
                state = richTextState,
                modifier = Modifier
                    .fillMaxSize()

                    .verticalScroll(scrollState)
                    .semantics {
                        contentDescription = "note_content_field"
                    },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun EditorControls(
    state: RichTextState,
    documentLauncher: androidx.activity.compose.ManagedActivityResultLauncher<String, Uri?>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ControlIcon(
            imageVector = Icons.Default.FormatBold,
            contentDescription = "Bold",
            active = state.isBold,
            onClick = { state.toggleBold() }
        )
        ControlIcon(
            imageVector = Icons.Default.FormatItalic,
            contentDescription = "Italic",
            active = state.isItalic,
            onClick = { state.toggleItalic() }
        )
        ControlIcon(
            imageVector = Icons.Default.FormatUnderlined,
            contentDescription = "Underline",
            active = state.isUnderline,
            onClick = { state.toggleUnderline() }
        )
        ControlIcon(
            imageVector = Icons.Default.FormatStrikethrough,
            contentDescription = "Strikethrough",
            active = state.isStrikethrough,
            onClick = { state.toggleStrikethrough() }
        )
        ControlIcon(
            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
            contentDescription = "Bullet List",
            active = state.isUnorderedList,
            onClick = { state.toggleUnorderedList() }
        )
        ControlIcon(
            imageVector = Icons.Default.FormatListNumbered,
            contentDescription = "Ordered List",
            active = state.isOrderedList,
            onClick = { state.toggleOrderedList() }
        )
        ControlIcon(
            imageVector = Icons.Default.FormatQuote,
            contentDescription = "Quote",
            active = state.isQuote,
            onClick = { state.toggleQuote() }
        )
        ControlIcon(
            imageVector = Icons.Default.Image,
            contentDescription = "图像",
            active = true,
            onClick = { documentLauncher.launch("image/*") }
        )
    }
}

@Composable
fun ControlIcon(
    imageVector: ImageVector,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "ControlIconBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.onPrimaryContainer else LocalContentColor.current,
        label = "ControlIconContent"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        color = backgroundColor,
        contentColor = contentColor,
        modifier = modifier
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun NotePropertyDialog(
    title: String,
    wordCount: Int,
    lastModified: Long,
    filePath: String,
    onDismiss: () -> Unit
) {
    val sdf = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()) }
    val dateStr = if (lastModified > 0) sdf.format(java.util.Date(lastModified)) else "-"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.note_properties)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PropertyItem(stringResource(R.string.note_title), title)
                PropertyItem(stringResource(R.string.word_count), wordCount.toString())
                PropertyItem(stringResource(R.string.Modified_Time), dateStr)
                PropertyItem(stringResource(R.string.file_path), filePath)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun PropertyItem(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun NoteEditScreenPre() {
    FletTheme {
        NoteEditScreen(
            titleValue = "示例标题",
            onTitleChange = {},
            categoryValue = "工作",
            onCategoryChange = {},
            categories = emptyList(),
            richTextState = rememberRichTextState(),
            onSaveClick = {},
            onDeleteClick = {},
            onBackClick = {},
            lastModified = System.currentTimeMillis(),
            filePath = "/sample/path"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditScreenPreOutsytemui() {
    FletTheme {
        NoteEditScreen(
            titleValue = "示例标题",
            onTitleChange = {},
            categoryValue = "生活",
            onCategoryChange = {},
            categories = emptyList(),
            richTextState = rememberRichTextState(),
            onSaveClick = {},
            onDeleteClick = {},
            onBackClick = {},
            lastModified = System.currentTimeMillis(),
            filePath = "/sample/path"
        )
    }
}
