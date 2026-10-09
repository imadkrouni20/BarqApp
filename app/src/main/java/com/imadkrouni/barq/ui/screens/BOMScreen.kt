package com.imadkrouni.barq.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.imadkrouni.barq.data.ComponentType
import com.imadkrouni.barq.engine.AutoRouter
import com.imadkrouni.barq.viewmodel.ProjectViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BOMScreen(
    projectId: Long,
    onBack: () -> Unit,
    vm: ProjectViewModel = viewModel()
) {
    LaunchedEffect(projectId) { vm.selectProject(projectId) }
    val components by vm.components.collectAsStateWithLifecycle()

    // حساب الكميات
    val grouped = components.groupingBy { it.type }.eachCount()
    val wires = remember(components) {
        val routes = AutoRouter.computeRoutes(components, 100f, 100f)
        AutoRouter.totalWireLength(routes)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 جدول الكميات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("🔌 القطع الكهربائية", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }

            if (grouped.isEmpty()) {
                item { Text("لا توجد قطع بعد", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(grouped.entries.toList()) { (typeName, count) ->
                    val ct = ComponentType.values().firstOrNull { it.name == typeName }
                    ListItem(
                        headlineContent = { Text("${ct?.emoji ?: ""} ${ct?.displayName ?: typeName}") },
                        trailingContent = { Text("× $count", fontWeight = FontWeight.Bold) }
                    )
                    HorizontalDivider()
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Text("🔗 الأسلاك المطلوبة", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
            }

            if (wires.isEmpty()) {
                item { Text("لا توجد أسلاك — اضغط ✨ في المخطط للتركيب التلقائي",
                    color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(wires.entries.toList()) { (gauge, length) ->
                    ListItem(
                        headlineContent = { Text("سلك نحاس $gauge مم²") },
                        trailingContent = { Text("%.1f م".format(length), fontWeight = FontWeight.Bold) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
