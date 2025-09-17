package com.example.choresplit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.choresplit.ui.theme.ChoresplitTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// Data Models
data class Roommate(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    var points: Int = 0
)

data class Chore(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val points: Int = 1,
    var assignedTo: Roommate? = null,
    var isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var completedAt: LocalDateTime? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChoresplitTheme {
                ChoresplitApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoresplitApp() {
    var currentScreen by remember { mutableStateOf("chores") }
    
    // Sample data - in a real app, this would be from a database
    val sampleRoommates = remember {
        mutableStateListOf(
            Roommate(name = "Alex", points = 15),
            Roommate(name = "Sam", points = 12),
            Roommate(name = "Jordan", points = 8)
        )
    }
    
    val sampleChores = remember {
        mutableStateListOf(
            Chore(
                title = "Take out trash",
                description = "Trash day is Tuesday",
                points = 2,
                assignedTo = sampleRoommates[0]
            ),
            Chore(
                title = "Clean kitchen",
                description = "Dishes, counters, stove",
                points = 3,
                assignedTo = sampleRoommates[1]
            ),
            Chore(
                title = "Vacuum living room",
                points = 2,
                assignedTo = sampleRoommates[2],
                isCompleted = true,
                completedAt = LocalDateTime.now().minusHours(2)
            ),
            Chore(title = "Buy groceries", points = 2),
            Chore(title = "Clean bathroom", points = 3)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Split Chores",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Chores") },
                    label = { Text("Chores") },
                    selected = currentScreen == "chores",
                    onClick = { currentScreen = "chores" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Roommates") },
                    label = { Text("Roommates") },
                    selected = currentScreen == "roommates",
                    onClick = { currentScreen = "roommates" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Rewards") },
                    label = { Text("Rewards") },
                    selected = currentScreen == "rewards",
                    onClick = { currentScreen = "rewards" }
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            "chores" -> ChoresScreen(
                chores = sampleChores,
                roommates = sampleRoommates,
                modifier = Modifier.padding(innerPadding)
            )
            "roommates" -> RoommatesScreen(
                roommates = sampleRoommates,
                modifier = Modifier.padding(innerPadding)
            )
            "rewards" -> RewardsScreen(
                roommates = sampleRoommates,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun ChoresScreen(
    chores: MutableList<Chore>,
    roommates: List<Roommate>,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Add Chore Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Chores",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Chore")
            }
        }

        // Chores List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chores) { chore ->
                ChoreItem(
                    chore = chore,
                    roommates = roommates,
                    onChoreUpdate = { updatedChore ->
                        val index = chores.indexOfFirst { it.id == updatedChore.id }
                        if (index != -1) {
                            chores[index] = updatedChore
                        }
                    },
                    onChoreDelete = { choreToDelete ->
                        chores.remove(choreToDelete)
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddChoreDialog(
            roommates = roommates,
            onDismiss = { showAddDialog = false },
            onChoreAdded = { newChore ->
                chores.add(newChore)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoreItem(
    chore: Chore,
    roommates: List<Roommate>,
    onChoreUpdate: (Chore) -> Unit,
    onChoreDelete: (Chore) -> Unit
) {
    var showAssignDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (chore.isCompleted) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chore.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (chore.isCompleted) TextDecoration.LineThrough else null
                    )
                    if (chore.description.isNotEmpty()) {
                        Text(
                            text = chore.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${chore.points} points",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = { onChoreDelete(chore) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Assignment status
                if (chore.assignedTo != null) {
                    Text(
                        text = "Assigned to: ${chore.assignedTo!!.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "Unassigned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    if (!chore.isCompleted) {
                        TextButton(onClick = { showAssignDialog = true }) {
                            Text("Assign")
                        }
                        
                        if (chore.assignedTo != null) {
                            Button(
                                onClick = {
                                    val updatedChore = chore.copy(
                                        isCompleted = true,
                                        completedAt = LocalDateTime.now()
                                    )
                                    // Award points to assigned roommate
                                    chore.assignedTo?.points = chore.assignedTo!!.points + chore.points
                                    onChoreUpdate(updatedChore)
                                }
                            ) {
                                Text("Complete")
                            }
                        }
                    } else {
                        Text(
                            text = "✓ Completed",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            if (chore.isCompleted && chore.completedAt != null) {
                Text(
                    text = "Completed: ${chore.completedAt!!.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showAssignDialog) {
        AssignChoreDialog(
            chore = chore,
            roommates = roommates,
            onDismiss = { showAssignDialog = false },
            onAssign = { selectedRoommate ->
                val updatedChore = chore.copy(assignedTo = selectedRoommate)
                onChoreUpdate(updatedChore)
                showAssignDialog = false
            }
        )
    }
}

@Composable
fun RoommatesScreen(
    roommates: MutableList<Roommate>,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Roommates",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Roommate")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(roommates.sortedByDescending { it.points }) { roommate ->
                RoommateItem(
                    roommate = roommate,
                    rank = roommates.sortedByDescending { it.points }.indexOf(roommate) + 1,
                    onDelete = { roommates.remove(roommate) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddRoommateDialog(
            onDismiss = { showAddDialog = false },
            onRoommateAdded = { newRoommate ->
                roommates.add(newRoommate)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun RoommateItem(
    roommate: Roommate,
    rank: Int,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#$rank",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (rank) {
                        1 -> Color(0xFFFFD700) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        3 -> Color(0xFFCD7F32) // Bronze
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = roommate.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${roommate.points} points",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun RewardsScreen(
    roommates: List<Roommate>,
    modifier: Modifier = Modifier
) {
    val topPerformer = roommates.maxByOrNull { it.points }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Rewards & Recognition",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (topPerformer != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Star",
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFFFD700)
                    )
                    Text(
                        "🏆 Top Performer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        topPerformer.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "${topPerformer.points} points",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Suggested Rewards:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val rewards = listOf(
            "10 points: Choose next week's dinner",
            "15 points: Get out of one chore",
            "20 points: Movie night pick",
            "25 points: Breakfast in bed",
            "30 points: Grocery shopping paid for"
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rewards) { reward ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reward,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

// Dialog Components
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChoreDialog(
    roommates: List<Roommate>,
    onDismiss: () -> Unit,
    onChoreAdded: (Chore) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Chore") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Chore Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = points,
                    onValueChange = { points = it },
                    label = { Text("Points") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val newChore = Chore(
                            title = title.trim(),
                            description = description.trim(),
                            points = points.toIntOrNull() ?: 1
                        )
                        onChoreAdded(newChore)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoommateDialog(
    onDismiss: () -> Unit,
    onRoommateAdded: (Roommate) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Roommate") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val newRoommate = Roommate(name = name.trim())
                        onRoommateAdded(newRoommate)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AssignChoreDialog(
    chore: Chore,
    roommates: List<Roommate>,
    onDismiss: () -> Unit,
    onAssign: (Roommate) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign \"${chore.title}\"") },
        text = {
            LazyColumn {
                items(roommates) { roommate ->
                    TextButton(
                        onClick = { onAssign(roommate) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${roommate.name} (${roommate.points} points)",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ChoresplitAppPreview() {
    ChoresplitTheme {
        ChoresplitApp()
    }
}