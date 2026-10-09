package com.imadkrouni.barq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imadkrouni.barq.data.Project
import com.imadkrouni.barq.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenProject: (Long) -> Unit,
    vm: ProjectViewModel = viewModel()
) {
    val projects by vm.projects.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚡ بَرْق — مشاريعي", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("مشروع جديد") }
            )
        }
    ) { padding ->
        if (projects.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏗️", fontSize = 72.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("لا توجد مشاريع بعد", fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("اضغط + لإنشاء مشروعك الأول", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(projects, key = { it.id }) { p ->
                    ProjectCard(p) { onOpenProject(p.id) }
                }
            }
        }
    }

    if (showDialog) {
        NewProjectDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, client, area ->
                vm.createProject(Project(name = name, clientName = client, area = area)) { id ->
                    showDialog = false
                    onOpenProject(id)
                }
            }
        )
    }
}

@Composable
fun ProjectCard(p: Project, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(p.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            if (p.clientName.isNotBlank()) Text("👤 ${p.clientName}", fontSize = 14.sp)
            if (p.area > 0) Text("📐 ${p.area} م²", fontSize = 14.sp)
        }
    }
}

@Composable
fun NewProjectDialog(onDismiss: () -> Unit, onConfirm: (String, String, Float) -> Unit) {
    var name by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🏗️ مشروع جديد") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("اسم المشروع") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = client, onValueChange = { client = it },
                    label = { Text("اسم الزبون") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = area, onValueChange = { area = it },
                    label = { Text("المساحة (م²)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) onConfirm(name, client, area.toFloatOrNull() ?: 0f)
            }) { Text("إنشاء") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
