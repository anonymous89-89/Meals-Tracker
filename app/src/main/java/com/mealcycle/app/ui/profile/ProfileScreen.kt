package com.mealcycle.app.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mealcycle.app.R
import com.mealcycle.app.data.datastore.UserPreferences
import com.mealcycle.app.data.model.User
import com.mealcycle.app.ui.navigation.AnimatedNavigationBar
import com.mealcycle.app.ui.navigation.NavItem
import com.mealcycle.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToStats: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddUserDialog.collectAsStateWithLifecycle()
    val snackMessage by viewModel.snackMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Snackbar feedback
    LaunchedEffect(snackMessage) {
        if (snackMessage != null) {
            snackbarHostState.showSnackbar(snackMessage!!)
            viewModel.clearSnack()
        }
    }

    // SAF launchers for backup export/import
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> uri?.let { viewModel.exportData(it) } }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? -> uri?.let { viewModel.importData(it) } }

    // ── Meal export (PDF/JSON) state ──
    var showExportDialog by remember { mutableStateOf(false) }
    var pendingExportStart by remember { mutableStateOf(java.time.LocalDate.now().minusDays(30)) }
    var pendingExportEnd by remember { mutableStateOf(java.time.LocalDate.now()) }

    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportToJson(it, pendingExportStart, pendingExportEnd) }
    }

    val pdfExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportToPdf(it, pendingExportStart, pendingExportEnd) }
    }

    // Edit user dialog state
    var editingUser by remember { mutableStateOf<User?>(null) }

    // ── Image crop state ──
    var cropSourceUri by remember { mutableStateOf<Uri?>(null) }
    var cropTarget by remember { mutableStateOf<String?>(null) } // userId to update after crop

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            AnimatedNavigationBar(
                items = listOf(
                    NavItem(Icons.Filled.Home, "Home", onNavigateToHome),
                    NavItem(Icons.Filled.BarChart, "Stats", onNavigateToStats),
                    NavItem(Icons.Filled.History, "History", onNavigateToHistory),
                    NavItem(Icons.Filled.Person, "Profile") {}
                ),
                selectedIndex = 3
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddUserDialog() },
                containerColor = Primary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Filled.PersonAdd, "Add user", tint = OnPrimary)
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Loading state ─────────────────────────────────────────────────
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // ── Empty state ──────────────────────────────────────────────────
            if (!uiState.isLoading && uiState.users.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.GroupAdd, null,
                                    tint = Primary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.no_users),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── User cards ───────────────────────────────────────────────────
            items(uiState.users, key = { it.userId }) { user ->
                UserCard(
                    user = user,
                    isActive = user.userId == uiState.activeUserId,
                    onSwitch = { viewModel.switchUser(user.userId) },
                    onEdit = { editingUser = user },
                    onDelete = { viewModel.deleteUser(user.userId) }
                )
            }

            // ── Data Management Section ──────────────────────────────────────
            if (uiState.users.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Data Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Export button
                        OutlinedButton(
                            onClick = { exportLauncher.launch("meal_tracker_backup.json") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Icon(
                                Icons.Filled.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Export", color = Primary, fontWeight = FontWeight.SemiBold)
                        }

                        // Import button
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Import", color = Primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                item {
                    Text(
                        "Export your meal data as a backup file. Import to restore from a previous session.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ── Meal Export (PDF / JSON) ──
                item {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.Filled.FileDownload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Export Meals (PDF / JSON)", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Plan & History Management ────────────────────────────
            item {
                var showResetConfirm by remember { mutableStateOf(false) }
                var showClearHistoryConfirm by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Reset Plan", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { showClearHistoryConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.DeleteSweep, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Clear History", fontWeight = FontWeight.SemiBold)
                    }
                }

                if (showResetConfirm) {
                    AlertDialog(
                        onDismissRequest = { showResetConfirm = false },
                        title = { Text("Reset Plan", fontWeight = FontWeight.Bold) },
                        text = { Text("This will delete all meals and holidays for the active user. This cannot be undone.") },
                        confirmButton = {
                            Button(
                                onClick = { showResetConfirm = false; viewModel.resetPlan() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Reset") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
                        }
                    )
                }

                if (showClearHistoryConfirm) {
                    AlertDialog(
                        onDismissRequest = { showClearHistoryConfirm = false },
                        title = { Text("Clear History", fontWeight = FontWeight.Bold) },
                        text = { Text("This will delete all plan history records for the active user. This cannot be undone.") },
                        confirmButton = {
                            Button(
                                onClick = { showClearHistoryConfirm = false; viewModel.clearHistory() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Clear") }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { showClearHistoryConfirm = false }) { Text("Cancel") }
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // ── Add user dialog ──────────────────────────────────────────────────────
    if (showAddDialog) {
        AddUserDialog(
            onDismiss = { viewModel.dismissAddUserDialog() },
            onConfirm = { name, photoUri -> viewModel.addUser(name, photoUri) }
        )
    }

    // ── Edit user dialog ─────────────────────────────────────────────────────
    editingUser?.let { user ->
        EditUserDialog(
            user = user,
            onDismiss = { editingUser = null },
            onSave = { newName, newPhotoUri ->
                viewModel.updateUser(user.userId, newName, newPhotoUri)
                editingUser = null
            }
        )
    }

    // ── Export dialog (PDF / JSON with date range) ───────────────────────────
    if (showExportDialog) {
        ExportDialog(
            mealCount = 0, // placeholder — actual count shown from date range
            onDismiss = { showExportDialog = false },
            onExportJson = { start, end ->
                pendingExportStart = start
                pendingExportEnd = end
                showExportDialog = false
                jsonExportLauncher.launch("meal_export_${start}_${end}.json")
            },
            onExportPdf = { start, end ->
                pendingExportStart = start
                pendingExportEnd = end
                showExportDialog = false
                pdfExportLauncher.launch("meal_export_${start}_${end}.pdf")
            }
        )
    }

    // ── Image Crop Dialog ───────────────────────────────────────────────────
    cropSourceUri?.let { uri ->
        ImageCropDialog(
            sourceUri = uri,
            onDismiss = { cropSourceUri = null; cropTarget = null },
            onCropComplete = { croppedUri ->
                cropTarget?.let { userId ->
                    viewModel.updatePhoto(userId, croppedUri.toString())
                }
                cropSourceUri = null
                cropTarget = null
            }
        )
    }
}

// ─── User Card ───────────────────────────────────────────────────────────────

@Composable
private fun UserCard(
    user: User,
    isActive: Boolean,
    onSwitch: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = if (isActive) Primary else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300), label = "card_bg"
    )
    // Text colors that always contrast with the background
    val nameColor = if (isActive) OnPrimary else MaterialTheme.colorScheme.onSurface
    val statusColor = if (isActive) OnPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isActive) Modifier.clickable(onClick = onSwitch) else Modifier),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isActive) 6.dp else 2.dp
        ),
        colors = CardDefaults.elevatedCardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(if (isActive) 4.dp else 0.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        if (isActive) Brush.linearGradient(listOf(Primary, Delivered))
                        else Brush.linearGradient(listOf(Undelivered, SurfaceVariant))
                    )
                    .then(if (isActive) Modifier.border(2.dp, Primary, CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                if (user.photoUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(user.photoUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = user.name,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = user.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimary,
                        fontSize = 20.sp
                    )
                }
            }

            // Name + status — ensure visibility with text overflow
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = nameColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isActive) stringResource(R.string.active_user) else stringResource(R.string.tap_to_switch),
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }

            // Active badge
            if (isActive) {
                Icon(Icons.Filled.CheckCircle, null, tint = if (isActive) OnPrimary else Delivered, modifier = Modifier.size(22.dp))
            }

            // Edit button
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, "Edit", tint = if (isActive) OnPrimary.copy(alpha = 0.9f) else Primary, modifier = Modifier.size(20.dp))
            }

            // Delete button
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Filled.DeleteOutline, null, tint = Error, modifier = Modifier.size(20.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_user), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_user_message, user.name)) },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text(stringResource(R.string.delete), color = OnPrimary) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

// ─── Edit User Dialog ────────────────────────────────────────────────────────

@Composable
private fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var photoUri by remember { mutableStateOf(user.photoUri) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> uri?.let { photoUri = it.toString() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Profile", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainer)
                        .clickable {
                            launcher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photoUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Filled.CameraAlt, null,
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    // Camera overlay badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = null,
                            tint = OnPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    "Tap photo to change",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.user_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, photoUri) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = name.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

// ─── Add User Dialog ─────────────────────────────────────────────────────────

@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> photoUri = uri?.toString() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_user), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.user_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedButton(
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Image, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (photoUri != null) stringResource(R.string.photo_selected) else stringResource(R.string.pick_photo))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, photoUri) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.add)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

// ─── Theme Mode Chip ─────────────────────────────────────────────────────────

@Composable
private fun ThemeModeChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "chip_bg_$label"
    )
    val contentColor = if (isSelected) OnPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = contentColor
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}
