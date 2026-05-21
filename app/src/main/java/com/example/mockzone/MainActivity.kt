@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.example.mockzone

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.sharp.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mockzone.ui.theme.EasyBg
import com.example.mockzone.ui.theme.EasyTxt
import com.example.mockzone.ui.theme.HardBg
import com.example.mockzone.ui.theme.HardTxt
import com.example.mockzone.ui.theme.MediumBg
import com.example.mockzone.ui.theme.MediumTxt
import com.example.mockzone.ui.theme.MockZoneBackground
import com.example.mockzone.ui.theme.MockZoneOnSurface
import com.example.mockzone.ui.theme.MockZoneOnSurfaceVariant
import com.example.mockzone.ui.theme.MockZoneOutlineVariant
import com.example.mockzone.ui.theme.MockZonePrimary
import com.example.mockzone.ui.theme.MockZoneTheme
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MockZoneTheme() {
                Mock_UzApp()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@PreviewScreenSizes
@Composable
fun Mock_UzApp() {
    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.HOME)
    }

    // Tepadagi va pastdagi panellar hamma ekranda ko'rinishi uchun asosiy Scaffold ichiga olindi
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBarSection() },
        bottomBar = {
            BottomNavBarSection(
                currentDestination = currentDestination,
                onNavigate = { currentDestination = it }
            )
        },
        containerColor = MockZoneBackground
    ) { innerPadding ->

        // innerPadding orqali ekran elementlari topBar va bottomBar tagiga kirib ketmaydi
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestinations.HOME -> MockZoneHomeScreen(
                    onNavigateToTest = { currentDestination = AppDestinations.TEST }
                )

                AppDestinations.TEST -> TestScreen(
                    onNavigate = { currentDestination = it }
                )

                AppDestinations.PROFILE -> ProfileScreen(
                    onNavigate = { currentDestination = it }
                )
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    TEST("Test", Icons.Sharp.Menu),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun MockZoneHomeScreen(onNavigateToTest: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Tarix, 1: Public Tests

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 1. Quick Join Room Card
        item { QuickJoinCard() }

        // 2. Tab Layout (Tarix vs Public Tests)
        item {
            TabSection(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }

        // 3. Dynamic Test List based on selected Tab
        if (selectedTab == 0) {
            items(getTarixTestData()) { test ->
                TestCardItem(test, onClick = onNavigateToTest)
            }
        } else {
            items(getPublicTestData()) { test ->
                TestCardItem(test, onClick = onNavigateToTest)
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarSection() {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Mock Zone",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MockZonePrimary
                )
            }
        },
        navigationIcon = {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.LightGray, CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = MockZoneOnSurfaceVariant)
            }
        },
        actions = {
            Row(modifier = Modifier.padding(end = 16.dp)) {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MockZoneOnSurfaceVariant)
                }
                Box {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = MockZoneOnSurfaceVariant)
                    }
                    // Red Notification Badge
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MockZoneBackground), // FIXED: topAppBarColors qilindi
        modifier = Modifier.border(color = MockZoneOutlineVariant, width = 1.dp) // FIXED: 1.dp berildi
    )
}

@Composable
fun QuickJoinCard() {
    var roomId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MockZonePrimary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MockZonePrimary, modifier = Modifier.size(20.dp))
                }
                Text(text = "Quick Join Test Room", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = MockZoneOnSurface)
            }

            // Room ID Input
            OutlinedTextField(
                value = roomId,
                onValueChange = { roomId = it },
                placeholder = { Text("Room ID", color = MockZoneOnSurfaceVariant.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = MockZoneOnSurfaceVariant) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F4F5),
                    unfocusedContainerColor = Color(0xFFF3F4F5),
                    focusedBorderColor = MockZonePrimary,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password", color = MockZoneOnSurfaceVariant.copy(alpha = 0.7f)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MockZoneOnSurfaceVariant) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F4F5),
                    unfocusedContainerColor = Color(0xFFF3F4F5),
                    focusedBorderColor = MockZonePrimary,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            // Join Button
            Button(
                onClick = { /* Handle Join Logic */ },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MockZonePrimary),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Join Room", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun TabSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Tarix", "Public Tests").forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MockZonePrimary else MockZoneOnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(2.dp)
                            .background(if (isSelected) MockZonePrimary else Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
fun TestCardItem(test: TestData, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, MockZoneOutlineVariant, RoundedCornerShape(16.dp))
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF3F4F5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(test.icon, contentDescription = null, tint = MockZoneOnSurfaceVariant)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = test.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MockZoneOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    val (bgColor, txtColor) = when (test.difficulty) {
                        "Oson" -> Pair(EasyBg, EasyTxt)
                        "O'rta" -> Pair(MediumBg, MediumTxt)
                        else -> Pair(HardBg, HardTxt)
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(bgColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = test.difficulty, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = txtColor)
                    }
                }

                Text(
                    text = "${test.creator} • ${test.questionsCount} savol",
                    fontSize = 14.sp,
                    color = MockZoneOnSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                if (test.showProgress) {
                    LinearProgressIndicator(
                        progress = { test.progress },
                        color = MockZonePrimary,
                        trackColor = Color(0xFFE7E8E9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = if (test.progress == 1f) "Tugatildi" else "Progress", fontSize = 11.sp, color = MockZoneOnSurfaceVariant)
                        Text(text = "${(test.progress * 100).toInt()}%", fontSize = 11.sp, color = MockZoneOnSurfaceVariant)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(14.dp), tint = MockZoneOnSurfaceVariant)
                            Text(text = "${test.questionsCount} savol", fontSize = 12.sp, color = MockZoneOnSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = MockZoneOnSurfaceVariant)
                            Text(text = "${test.durationMinutes} daqiqa", fontSize = 12.sp, color = MockZoneOnSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBarSection(
    currentDestination: AppDestinations,
    onNavigate: (AppDestinations) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier
            .height(80.dp)
            .border(color = MockZoneOutlineVariant, width = 1.dp)
    ) {
        NavigationBarItem(
            selected = currentDestination == AppDestinations.HOME,
            onClick = { onNavigate(AppDestinations.HOME) }, // FIXED: Haqiqiy navigatsiya ulandi
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MockZonePrimary,
                selectedTextColor = MockZonePrimary,
                indicatorColor = MockZonePrimary.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = currentDestination == AppDestinations.TEST,
            onClick = { onNavigate(AppDestinations.TEST) }, // FIXED: Haqiqiy navigatsiya ulandi
            icon = { Icon(Icons.Default.List, contentDescription = "Tests") },
            label = { Text("Tests", fontSize = 12.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Star, contentDescription = "Analytics") },
            label = { Text("Analytics", fontSize = 12.sp) }
        )
        NavigationBarItem(
            selected = currentDestination == AppDestinations.PROFILE,
            onClick = { onNavigate(AppDestinations.PROFILE) }, // FIXED: Haqiqiy navigatsiya ulandi
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 12.sp) }
        )
    }
}

fun getTarixTestData(): List<TestData> = listOf(
    TestData("World History Midterm", "Prof. Davis", 50, 60, "Qiyin", 0.75f, true, Icons.Default.Menu),
    TestData("Algebra Basics", "Self-Practice", 20, 30, "Oson", 1.0f, true, Icons.Default.Build),
    TestData("Chemistry Chapter 4", "Ms. Smith", 35, 45, "O'rta", 0.10f, true, Icons.Default.Share)
)

fun getPublicTestData(): List<TestData> = listOf(
    TestData("Matematika: Milliy Sertifikat", "Mock Zone", 30, 45, "Oson", 0f, false, Icons.Default.Build),
    TestData("Ingliz tili: CEFR B2", "Mock Zone", 40, 60, "O'rta", 0f, false, Icons.Default.List),
    TestData("Fizika: Mexanika", "Mock Zone", 25, 50, "Qiyin", 0f, false, Icons.Default.Share),
    TestData("O'zbekiston Tarixi: 5-9-sinf", "Mock Zone", 30, 30, "O'rta", 0f, false, Icons.Default.Menu)
)

data class TestData(
    val title: String = "",
    val creator: String = "",
    val questionsCount: Int = 0,
    val durationMinutes: Int = 0,
    val difficulty: String = "Oson", // Oson, O'rta, Qiyin
    val progress: Float = 0f,
    val showProgress: Boolean = false,
    val icon: ImageVector = Icons.Default.Menu,
    val id: String = ""
)

@SuppressLint("LocalContextResourcesRead", "UseKtx")
@Composable
fun TestScreen(onNavigate: (AppDestinations) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val answers = remember { mutableStateListOf<String>().apply { repeat(30) { add("") } } }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    val pdfView = PDFView(context, null)
                    pdfView.fromAsset("Slide.pdf")
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .load()
                    pdfView
                }
            )
        }

        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.White)) {
            items(30) { index ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${index + 1}", modifier = Modifier.width(30.dp), fontWeight = FontWeight.Bold)

                    listOf("A", "B", "C", "D").forEach { option ->
                        val selected = answers[index] == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) Color(0xFF0C5DFF) else Color.White)
                                .border(width = 1.5.dp, color = Color(0xFF0C5DFF), shape = RoundedCornerShape(10.dp))
                                .clickable { answers[index] = option }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = option, color = if (selected) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        Log.d("TEST", "Topshiruvchi malumotlari:\nIsmi: ${user?.displayName}\nTelefon raqami: ${user?.email}\n")
                        answers.forEachIndexed { i, answer -> Log.d("TEST", "Savol ${i + 1}: $answer") }
                        onNavigate(AppDestinations.HOME)
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(55.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "Testni tugatish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ProfileScreen(onNavigate: (AppDestinations) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var user by remember { mutableStateOf(auth.currentUser) }

    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F6F8)), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (user == null) {
                    Text(text = "MockUz", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MockZonePrimary)
                    Spacer(Modifier.height(6.dp))
                    Text(text = if (isLogin) "Hisobingizga kiring" else "Ro'yxatdan o'ting", color = Color.Gray)
                    Spacer(Modifier.height(25.dp))

                    if (!isLogin) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Ism") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.all { c -> c.isDigit() }) phone = it },
                        label = { Text("Telefon") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Parol") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = if (passwordVisible) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null)
                            }
                        }
                    )
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            loading = true
                            if (isLogin) {
                                auth.signInWithEmailAndPassword("$phone@mock.uz", password)
                                    .addOnSuccessListener {
                                        user = auth.currentUser
                                        loading = false
                                        onNavigate(AppDestinations.HOME)
                                    }.addOnFailureListener {
                                        loading = false
                                        Toast.makeText(context, "Login xatolik", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                auth.createUserWithEmailAndPassword("$phone@mock.uz", password)
                                    .addOnSuccessListener {
                                        val firebaseUser = auth.currentUser
                                        val profileUpdates = userProfileChangeRequest { displayName = name }
                                        firebaseUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
                                            user = auth.currentUser
                                            loading = false
                                            onNavigate(AppDestinations.HOME)
                                        }
                                    }.addOnFailureListener {
                                        loading = false
                                        Toast.makeText(context, "Register xatolik", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5DFF)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (loading) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                        else Text(if (isLogin) "Kirish" else "Ro'yxatdan o'tish", color = Color.White)
                    }

                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = { isLogin = !isLogin }) {
                        Text(if (isLogin) "Akkauntingiz yo'qmi? Ro'yxatdan o'ting" else "Akkauntingiz bormi? Kirish", color = Color(0xFF0C5DFF))
                    }
                } else {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFF0C5DFF))
                    Spacer(Modifier.height(12.dp))
                    Text(text = user?.displayName ?: "Foydalanuvchi", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(text = user?.email ?: "", color = Color.Gray)
                    Spacer(Modifier.height(25.dp))
                    Button(
                        onClick = {
                            auth.signOut()
                            user = null
                            onNavigate(AppDestinations.HOME)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0C5DFF)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}