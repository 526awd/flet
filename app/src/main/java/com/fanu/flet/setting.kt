package com.fanu.flet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fanu.flet.ui.theme.FletTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FletTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SettingsScreen(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(name: String, modifier: Modifier = Modifier) {
    var markdownEnabled by remember { mutableStateOf(true) }
    
    // 获取当前的主题设定
    val currentDarkMode = ThemeConfig.darkModeState ?: isSystemInDarkTheme()

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = stringResource(R.string.settings), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        SettingItem(
            label = stringResource(R.string.markdown_support),
            checked = markdownEnabled,
            onCheckedChange = { markdownEnabled = it }
        )
        
        SettingItem(
            label = stringResource(R.string.manual_dark_mode),
            checked = currentDarkMode,
            onCheckedChange = { isDark ->
                // 手动设定主题
                ThemeConfig.darkModeState = isDark
            }
        )
        
        // 可选：添加一个恢复跟随系统的按钮
        if (ThemeConfig.darkModeState != null) {
            androidx.compose.material3.TextButton(
                onClick = { ThemeConfig.darkModeState = null },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.follow_system))
            }
        }
    }
}

@Composable
fun SettingItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    FletTheme {
        SettingsScreen("Android")
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun h(){
    FletTheme() {
        SettingsScreen("和一位")
    }
}