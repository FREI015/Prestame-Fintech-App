package com.controlprestamos.features.backup.presentation

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.backup.data.LocalBackupRepository

@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var pendingBackupContent by remember {
        mutableStateOf("")
    }

    var pendingBackupFileName by remember {
        mutableStateOf("control_prestamos_backup.json")
    }

    var importedBackupContent by remember {
        mutableStateOf("")
    }

    var message by rememberSaveable {
        mutableStateOf("")
    }

    var restorePreview by rememberSaveable {
        mutableStateOf("")
    }

    var confirmRestore by rememberSaveable {
        mutableStateOf(false)
    }

    var backupReady by rememberSaveable {
        mutableStateOf(false)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) {
            message = "Guardado cancelado."
            return@rememberLauncherForActivityResult
        }

        if (pendingBackupContent.isBlank()) {
            message = "Primero crea una copia de seguridad."
            return@rememberLauncherForActivityResult
        }

        val saved = writeTextToUri(
            context = context,
            uri = uri,
            text = pendingBackupContent
        )

        message = if (saved) {
            "Copia guardada correctamente."
        } else {
            "No se pudo guardar la copia."
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            message = "Restauración cancelada."
            return@rememberLauncherForActivityResult
        }

        val content = readTextFromUri(
            context = context,
            uri = uri
        )

        if (content.isBlank()) {
            message = "No se pudo leer el archivo seleccionado."
            confirmRestore = false
            importedBackupContent = ""
            restorePreview = ""
            return@rememberLauncherForActivityResult
        }

        val validationReport = LocalBackupRepository.inspectBackup(content)

        if (!validationReport.isValid) {
            importedBackupContent = ""
            restorePreview = ""
            confirmRestore = false
            message = validationReport.message
            return@rememberLauncherForActivityResult
        }

        importedBackupContent = content
        restorePreview = validationReport.toUserMessage()
        confirmRestore = true
        message = "Archivo cargado y validado. Confirma la restauración para reemplazar los datos actuales."
    }

    val lastBackupLabel = remember(message, backupReady) {
        LocalBackupRepository.getLastBackupLabel(context)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Copia de seguridad",
                subtitle = "Guardar y restaurar datos",
                showBack = true,
                onBack = onNavigateBack
            )
        },
        containerColor = AppColors.Background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = AppColors.Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                Text(
                    text = "Protección de datos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Guarda una copia de seguridad para poder recuperar clientes, préstamos, pagos, reportes, preferencias y seguridad.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )

                if (message.isNotBlank()) {
                    AppCard(bordered = true) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                message.contains("correctamente", ignoreCase = true) -> AppColors.Success
                                message.contains("cargado", ignoreCase = true) -> AppColors.Success
                                message.contains("cancel", ignoreCase = true) -> AppColors.Warning
                                message.contains("No se pudo", ignoreCase = true) -> AppColors.Error
                                else -> AppColors.Gray900
                            }
                        )
                    }
                }

                AppCard(bordered = true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Crear copia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Última copia: $lastBackupLabel",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        Text(
                            text = "Crea un archivo de respaldo y guárdalo en Google Drive, Telegram, WhatsApp, correo o computadora.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        PrimaryButton(
                            text = "Crear copia ahora",
                            onClick = {
                                val result = LocalBackupRepository.createBackup(context)

                                pendingBackupContent = result.json
                                pendingBackupFileName = result.fileName
                                backupReady = result.json.isNotBlank()

                                message = if (backupReady) {
                                    "Copia creada correctamente. Ahora puedes guardarla o compartirla."
                                } else {
                                    "No se pudo crear la copia."
                                }
                            }
                        )

                        if (backupReady) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                SecondaryButton(
                                    text = "Guardar archivo",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        exportLauncher.launch(pendingBackupFileName)
                                    }
                                )

                                SecondaryButton(
                                    text = "Compartir archivo",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val file = BackupShareUtils.createBackupFile(
                                            context = context,
                                            fileName = pendingBackupFileName,
                                            content = pendingBackupContent
                                        )

                                        BackupShareUtils.shareBackupFile(
                                            context = context,
                                            file = file
                                        )

                                        message = "Abriendo opciones para compartir la copia."
                                    }
                                )
                            }
                        }
                    }
                }

                AppCard(bordered = true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Restaurar copia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Selecciona un archivo de copia de seguridad guardado anteriormente. Esta acción reemplaza los datos actuales.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )

                        PrimaryButton(
                            text = "Seleccionar archivo",
                            onClick = {
                                importLauncher.launch(
                                    arrayOf(
                                        "application/json",
                                        "text/plain",
                                        "*/*"
                                    )
                                )
                            }
                        )

                        if (confirmRestore) {
                            AppCard(bordered = true) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                                ) {
                                    Text(
                                        text = "Confirmar restauración",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.Error
                                    )

                                    Text(
                                        text = "Esta acción reemplazará los datos actuales por los datos del archivo seleccionado.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.Gray600
                                    )

                                    if (restorePreview.isNotBlank()) {
                                        AppCard(bordered = true) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                                            ) {
                                                Text(
                                                    text = "Datos detectados en la copia",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AppColors.Gray900
                                                )

                                                Text(
                                                    text = restorePreview,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AppColors.Gray600
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                                    ) {
                                        SecondaryButton(
                                            text = "Cancelar",
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                importedBackupContent = ""
                                                restorePreview = ""
                                                confirmRestore = false
                                                message = "Restauración cancelada."
                                            }
                                        )

                                        PrimaryButton(
                                            text = "Restaurar",
                                            modifier = Modifier.weight(1f),
                                            onClick = {
                                                val result = LocalBackupRepository.restoreBackup(
                                                    context = context,
                                                    backupJson = importedBackupContent
                                                )

                                                message = result.message
                                                confirmRestore = false

                                                if (result.success) {
                                                    importedBackupContent = ""
                                                    restorePreview = ""
                                                    backupReady = false
                                                    pendingBackupContent = ""
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AppCard(bordered = true) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        Text(
                            text = "Recomendación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        Text(
                            text = "Haz una copia cada vez que cierres una jornada de cobranza o antes de cambiar de teléfono.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Gray600
                        )
                    }
                }
            }
        }
    }
}

private fun writeTextToUri(
    context: Context,
    uri: Uri,
    text: String
): Boolean {
    return runCatching {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(text.toByteArray(Charsets.UTF_8))
        } ?: return false

        true
    }.getOrDefault(false)
}

private fun readTextFromUri(
    context: Context,
    uri: Uri
): String {
    return runCatching {
        context.contentResolver.openInputStream(uri)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            .orEmpty()
    }.getOrDefault("")
}

