package bangla.English.bekaron.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bangla.English.bekaron.engine.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BekaronApp(engine: OfflineGrammarEngine) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "bekaron (ব্যাকরণ)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "POS Analyzer") },
                    label = { Text("POS Analyzer") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Spellcheck, contentDescription = "Gap Fill") },
                    label = { Text("Gap Fill") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedTab == 0) {
                PosAnalyzerScreen(engine = engine)
            } else {
                GapFillScreen(engine = engine)
            }
        }
    }
}

@Composable
fun PosAnalyzerScreen(engine: OfflineGrammarEngine) {
    var inputText by remember { mutableStateOf("") }
    var detectedLanguage by remember { mutableStateOf<SupportedLanguage?>(null) }
    var posResults by remember { mutableStateOf<List<PosResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { newText ->
                inputText = newText
                detectedLanguage = if (newText.isNotBlank()) engine.detectLanguage(newText) else null
            },
            label = { Text("Enter text (Bangla, English, or Arabic)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            trailingIcon = {
                if (inputText.isNotEmpty()) {
                    IconButton(onClick = {
                        inputText = ""
                        detectedLanguage = null
                        posResults = emptyList()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            detectedLanguage?.let { lang ->
                AssistChip(
                    onClick = {},
                    label = { Text("Detected: ${lang.displayName}") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            } ?: Spacer(modifier = Modifier.width(1.dp))

            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        isLoading = true
                        coroutineScope.launch {
                            posResults = engine.getPOS(inputText)
                            isLoading = false
                        }
                    }
                },
                enabled = inputText.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Analyze POS")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (posResults.isNotEmpty()) {
            Text(
                text = "Part of Speech Tokens:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(posResults) { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = result.word,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = result.tag,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GapFillScreen(engine: OfflineGrammarEngine) {
    var sentence by remember { mutableStateOf("The sun ___ in the east.") }
    var optionsInput by remember { mutableStateOf("rises, sets, shines") }
    var gapResult by remember { mutableStateOf<GapFillResult?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val detectedLang = engine.detectLanguage(sentence)
    val isArabic = detectedLang == SupportedLanguage.ARABIC

    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = sentence,
                onValueChange = { sentence = it },
                label = { Text("Sentence with ___ gap") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = optionsInput,
                onValueChange = { optionsInput = it },
                label = { Text("Options (comma separated)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("Language: ${detectedLang.displayName}") }
                )

                Button(
                    onClick = {
                        val optionsList = optionsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        if (sentence.isNotBlank() && optionsList.isNotEmpty()) {
                            isLoading = true
                            coroutineScope.launch {
                                gapResult = engine.fillGap(sentence, optionsList)
                                isLoading = false
                            }
                        }
                    },
                    enabled = sentence.contains("___") && optionsInput.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Fill Gap")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            gapResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Best Prediction:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.filledSentence,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Candidate Option Ranking:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(result.options) { optionResult ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (optionResult.isBest)
                                    MaterialTheme.colorScheme.tertiaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = optionResult.option,
                                    fontWeight = if (optionResult.isBest) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Score: ${"%.2f".format(optionResult.score)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
