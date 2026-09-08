package com.controlprestamos.features.backup.presentation

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object BackupShareUtils {

    fun createBackupFile(
        context: Context,
        fileName: String,
        content: String
    ): File {
        val safeName = fileName
            .ifBlank { "control_prestamos_backup.json" }
            .replace(" ", "_")
            .replace("/", "-")
            .replace("\\", "-")

        val directory = File(context.cacheDir, "backups").apply {
            mkdirs()
        }

        val file = File(directory, safeName)

        file.writeText(
            text = content,
            charset = Charsets.UTF_8
        )

        return file
    }

    fun shareBackupFile(
        context: Context,
        file: File
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Copia de seguridad Control Préstamos")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(
            intent,
            "Compartir copia de seguridad"
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooser)
    }
}

