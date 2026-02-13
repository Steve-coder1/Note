package com.notepadpro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

data class Note(
    val title: String,
    val snippet: String,
    val tags: List<String>,
    val timestamp: String
)

enum class LockState {
    LOCKED,
    UNLOCKED
}

private val sampleNotes = listOf(
    Note("Research Sprint", "Summarize model eval metrics and benchmark outcomes...", listOf("research", "ai"), "Today"),
    Note("API Security Checklist", "Gemini key verification and E2E encryption status...", listOf("security", "backend"), "Yesterday"),
    Note("Writing Plan", "Draft chapter outline with code snippets and references...", listOf("writing"), "2 days ago")
)

@Composable
fun NotepadProApp() {
    var authenticated by remember { mutableStateOf(false) }
    var lockState by remember { mutableStateOf(LockState.LOCKED) }

    MaterialTheme {
        if (!authenticated) {
            GeminiLoginScreen(
                onSuccess = {
                    authenticated = true
                    lockState = LockState.UNLOCKED
                }
            )
        } else {
            AppScaffold(
                lockState = lockState,
                onLockToggle = {
                    lockState = if (lockState == LockState.LOCKED) LockState.UNLOCKED else LockState.LOCKED
                },
                onLogout = {
                    authenticated = false
                    lockState = LockState.LOCKED
                }
            )
        }
    }
}

@Composable
private fun GeminiLoginScreen(onSuccess: () -> Unit) {
    var key by remember { mutableStateOf("") }
    var setupMode by remember { mutableStateOf(true) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Notepad Pro", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(if (setupMode) "Set up Gemini Key" else "Unlock with Gemini Key")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Gemini Key") },
                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onSuccess, modifier = Modifier.fillMaxWidth(), enabled = key.isNotBlank()) {
                Text(if (setupMode) "Generate & Continue" else "Verify Key")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = { setupMode = !setupMode }, modifier = Modifier.fillMaxWidth()) {
                Text(if (setupMode) "Already have a key? Verify" else "No key yet? Setup")
            }
        }
    }
}

@Composable
private fun AppScaffold(lockState: LockState, onLockToggle: () -> Unit, onLogout: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNav(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { LandingScreen(lockState, onLockToggle) }
            composable("editor") { NoteEditorScreen(lockState) }
            composable("search") { SearchScreen(lockState) }
            composable("settings") { SettingsScreen(onLogout) }
        }
    }
}

@Composable
private fun BottomNav(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar {
        listOf(
            "home" to Icons.Default.Home,
            "search" to Icons.Default.Search,
            "editor" to Icons.Default.Add,
            "settings" to Icons.Default.Settings
        ).forEach { (route, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { navController.navigate(route) },
                icon = { Icon(icon, contentDescription = route) },
                label = { Text(route.replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandingScreen(lockState: LockState, onLockToggle: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notepad Pro") },
                navigationIcon = { IconButton(onClick = {}) { Icon(Icons.Default.Menu, contentDescription = null) } },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = null) }
                    IconButton(onClick = onLockToggle) {
                        Icon(
                            if (lockState == LockState.LOCKED) Icons.Default.Lock else Icons.Default.Lock,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Quick Actions", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}) { Text("+ Create Note") }
                OutlinedButton(onClick = {}) { Text("Create Notebook") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (lockState == LockState.LOCKED) {
                DisabledBlock("Gemini key timeout. Re-authenticate to unlock note previews.")
            }
            Text("Recent Notes", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sampleNotes) { note ->
                    NoteCard(note, lockState == LockState.UNLOCKED)
                }
            }
        }
    }
}

@Composable
private fun NoteCard(note: Note, unlocked: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (unlocked) MaterialTheme.colorScheme.surfaceVariant else Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(note.title, fontWeight = FontWeight.Bold)
            Text(if (unlocked) note.snippet else "Encrypted snippet hidden", maxLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                note.tags.forEach { tag ->
                    AssistChip(onClick = {}, label = { Text(tag) }, leadingIcon = { Icon(Icons.Default.Tag, null) })
                }
            }
            Text(note.timestamp, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditorScreen(lockState: LockState) {
    var title by remember { mutableStateOf("Untitled") }
    var content by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = {}) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Save, null) }
                }
            )
        }
    ) { padding ->
        if (lockState == LockState.LOCKED) {
            DisabledBlock("Re-authenticate to continue editing encrypted content.")
            return@Scaffold
        }
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Start typing…") },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Attach") }
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Code") }
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Tags") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(lockState: LockState) {
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Encrypted search") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = { IconButton(onClick = {}) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.FilterList, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("research", "security", "today").forEach {
                    AssistChip(onClick = {}, label = { Text(it) })
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (lockState == LockState.LOCKED) {
                DisabledBlock("Gemini key timeout. Search and filters are disabled.")
            } else {
                sampleNotes.filter { it.title.contains(query, true) || it.snippet.contains(query, true) }.forEach {
                    NoteCard(it, unlocked = true)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(onLogout: () -> Unit) {
    var darkMode by remember { mutableStateOf(true) }
    var notifications by remember { mutableStateOf(true) }
    var cloudSync by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = { IconButton(onClick = {}) { Icon(Icons.Default.ArrowBack, null) } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Default.AccountCircle, null) } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                SettingsItem("Gemini Key Status", "Valid · backup recommended", Icons.Default.VpnKey)
            }
            item {
                SettingsToggle("Dark mode", darkMode, { darkMode = it }, Icons.Default.DarkMode)
            }
            item {
                SettingsToggle("Notifications", notifications, { notifications = it }, Icons.Default.Notifications)
            }
            item {
                SettingsToggle("Cloud Sync", cloudSync, { cloudSync = it }, Icons.Default.Save)
            }
            item {
                Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null)
            Spacer(Modifier.size(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle)
            }
        }
    }
}

@Composable
private fun SettingsToggle(title: String, value: Boolean, onChange: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onChange(!value) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null)
            Spacer(Modifier.size(12.dp))
            Text(title, modifier = Modifier.weight(1f))
            Switch(checked = value, onCheckedChange = onChange)
        }
    }
}

@Composable
private fun DisabledBlock(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECECEC), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(message)
    }
}
