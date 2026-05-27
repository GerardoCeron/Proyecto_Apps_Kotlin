package com.doctranslate.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.doctranslate.domain.model.DocumentFormat
import com.doctranslate.domain.model.supportedLanguages
import com.doctranslate.presentation.viewmodel.TranslateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(
    viewModel: TranslateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedFile by remember { mutableStateOf<Uri?>(null) }
    
    val translated by viewModel.translatedText.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedFormat by viewModel.selectedFormat.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val isGeneratingAudio by viewModel.isGeneratingAudio.collectAsState()

    var langMenuExpanded by remember { mutableStateOf(false) }
    var formatMenuExpanded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.downloadTranslatedFile(context)
            Toast.makeText(context, "Iniciando descarga...", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> selectedFile = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "DocTranslate",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Selección de Idioma
        ExposedDropdownMenuBox(
            expanded = langMenuExpanded,
            onExpandedChange = { langMenuExpanded = !langMenuExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedLanguage.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Idioma de destino") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langMenuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = langMenuExpanded,
                onDismissRequest = { langMenuExpanded = false }
            ) {
                supportedLanguages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text(language.name) },
                        onClick = {
                            viewModel.onLanguageSelected(language)
                            langMenuExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        // Selección de Formato
        ExposedDropdownMenuBox(
            expanded = formatMenuExpanded,
            onExpandedChange = { formatMenuExpanded = !formatMenuExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedFormat.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Formato de exportación") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatMenuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = formatMenuExpanded,
                onDismissRequest = { formatMenuExpanded = false }
            ) {
                DocumentFormat.entries.forEach { format ->
                    DropdownMenuItem(
                        text = { Text(format.displayName) },
                        onClick = {
                            viewModel.onFormatSelected(format)
                            formatMenuExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { filePicker.launch("*/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (selectedFile == null) "Seleccionar Archivo" else "Cambiar Archivo")
        }

        selectedFile?.let { uri ->
            Text(
                text = "Archivo seleccionado: ${uri.lastPathSegment}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Button(
                onClick = { viewModel.translateFile(uri, context) },
                enabled = !isTranslating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Traducir Archivo")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (translated.isNotBlank() && !translated.startsWith("Extrayendo") && !translated.startsWith("Traduciendo")) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Resultado:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = translated)
                    
                    if (!translated.startsWith("Error")) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.generateAndPlayAudio(context) },
                                enabled = !isGeneratingAudio,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isGeneratingAudio) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Escuchar Audio")
                                }
                            }
                            
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                        val status = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        )
                                        if (status == PackageManager.PERMISSION_GRANTED) {
                                            viewModel.downloadTranslatedFile(context)
                                            Toast.makeText(context, "Iniciando descarga...", Toast.LENGTH_SHORT).show()
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        }
                                    } else {
                                        viewModel.downloadTranslatedFile(context)
                                        Toast.makeText(context, "Iniciando descarga...", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Exportar (TXT)")
                            }
                        }
                    }
                }
            }
        }
    }
}
