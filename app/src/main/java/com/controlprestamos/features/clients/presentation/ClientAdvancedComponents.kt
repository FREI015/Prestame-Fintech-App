package com.controlprestamos.features.clients.presentation

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.ClientExtraInfo
import com.controlprestamos.features.clients.data.LocalClientExtraRepository

@Composable
fun ClientPhotoPicker(
    photoUri: String,
    onPhotoUriChange: (String) -> Unit
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            persistReadPermission(
                context = context,
                uri = uri
            )

            onPhotoUriChange(uri.toString())
        }
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ClientPhotoView(
                photoUri = photoUri,
                size = 96
            )

            Text(
                text = if (photoUri.isBlank()) "Sin foto del cliente" else "Foto cargada",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            SecondaryButton(
                text = if (photoUri.isBlank()) "Cargar foto" else "Cambiar foto",
                onClick = {
                    launcher.launch(arrayOf("image/*"))
                }
            )

            if (photoUri.isNotBlank()) {
                SecondaryButton(
                    text = "Quitar foto",
                    onClick = {
                        onPhotoUriChange("")
                    }
                )
            }
        }
    }
}

@Composable
fun ClientPhotoView(
    photoUri: String,
    size: Int = 72
) {
    val context = LocalContext.current

    val bitmap = remember(photoUri) {
        if (photoUri.isBlank()) {
            null
        } else {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(photoUri))?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }.getOrNull()
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Foto del cliente",
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
        )
    } else {
        com.controlprestamos.core.ui.components.ClientAvatar(
            fullName = "",
            modifier = Modifier.size(size.dp)
        )
    }
}

@Composable
fun ClientExtraForm(
    photoUri: String,
    onPhotoUriChange: (String) -> Unit,
    referredBy: String,
    onReferredByChange: (String) -> Unit,
    registeredBy: String,
    onRegisteredByChange: (String) -> Unit,
    referenceName: String,
    onReferenceNameChange: (String) -> Unit,
    referencePhone: String,
    onReferencePhoneChange: (String) -> Unit,
    secondReferenceName: String,
    onSecondReferenceNameChange: (String) -> Unit,
    secondReferencePhone: String,
    onSecondReferencePhoneChange: (String) -> Unit,
    occupation: String,
    onOccupationChange: (String) -> Unit,
    workplace: String,
    onWorkplaceChange: (String) -> Unit,
    addressDetail: String,
    onAddressDetailChange: (String) -> Unit,
    internalNotes: String,
    onInternalNotesChange: (String) -> Unit
) {
    ClientPhotoPicker(
        photoUri = photoUri,
        onPhotoUriChange = onPhotoUriChange
    )

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Información avanzada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            AppTextField(
                value = registeredBy,
                onValueChange = onRegisteredByChange,
                label = "Registrado por"
            )

            AppTextField(
                value = referredBy,
                onValueChange = onReferredByChange,
                label = "Referido por"
            )

            AppTextField(
                value = occupation,
                onValueChange = onOccupationChange,
                label = "Ocupación"
            )

            AppTextField(
                value = workplace,
                onValueChange = onWorkplaceChange,
                label = "Lugar de trabajo"
            )

            AppTextField(
                value = addressDetail,
                onValueChange = onAddressDetailChange,
                label = "Dirección ampliada",
                singleLine = false
            )

            AppTextField(
                value = referenceName,
                onValueChange = onReferenceNameChange,
                label = "Referencia personal 1"
            )

            AppTextField(
                value = referencePhone,
                onValueChange = onReferencePhoneChange,
                label = "Teléfono referencia 1"
            )

            AppTextField(
                value = secondReferenceName,
                onValueChange = onSecondReferenceNameChange,
                label = "Referencia personal 2"
            )

            AppTextField(
                value = secondReferencePhone,
                onValueChange = onSecondReferencePhoneChange,
                label = "Teléfono referencia 2"
            )

            AppTextField(
                value = internalNotes,
                onValueChange = onInternalNotesChange,
                label = "Notas internas",
                singleLine = false
            )
        }
    }
}

@Composable
fun ClientAdvancedInfoCard(
    clientId: String
) {
    val context = LocalContext.current
    LocalClientExtraRepository.initialize(context)

    val extra = LocalClientExtraRepository.getExtra(clientId)

    if (
        extra.photoUri.isBlank() &&
        extra.referredBy.isBlank() &&
        extra.registeredBy.isBlank() &&
        extra.referenceName.isBlank() &&
        extra.referencePhone.isBlank() &&
        extra.secondReferenceName.isBlank() &&
        extra.secondReferencePhone.isBlank() &&
        extra.occupation.isBlank() &&
        extra.workplace.isBlank() &&
        extra.addressDetail.isBlank() &&
        extra.internalNotes.isBlank()
    ) {
        return
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Información avanzada",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Gray900
            )

            if (extra.photoUri.isNotBlank()) {
                ClientPhotoView(photoUri = extra.photoUri)
            }

            ClientExtraLine("Registrado por", extra.registeredBy)
            ClientExtraLine("Referido por", extra.referredBy)
            ClientExtraLine("Ocupación", extra.occupation)
            ClientExtraLine("Lugar de trabajo", extra.workplace)
            ClientExtraLine("Dirección ampliada", extra.addressDetail)
            ClientExtraLine("Referencia 1", extra.referenceName)
            ClientExtraLine("Teléfono ref. 1", extra.referencePhone)
            ClientExtraLine("Referencia 2", extra.secondReferenceName)
            ClientExtraLine("Teléfono ref. 2", extra.secondReferencePhone)
            ClientExtraLine("Notas internas", extra.internalNotes)
        }
    }
}

@Composable
private fun ClientExtraLine(
    label: String,
    value: String
) {
    if (value.isBlank()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.AccentTeal
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray900
        )
    }
}

fun buildClientExtraInfo(
    clientId: String,
    photoUri: String,
    referredBy: String,
    registeredBy: String,
    referenceName: String,
    referencePhone: String,
    secondReferenceName: String,
    secondReferencePhone: String,
    occupation: String,
    workplace: String,
    addressDetail: String,
    internalNotes: String
): ClientExtraInfo {
    return ClientExtraInfo(
        clientId = clientId,
        photoUri = photoUri.trim(),
        referredBy = referredBy.trim(),
        registeredBy = registeredBy.trim(),
        referenceName = referenceName.trim(),
        referencePhone = referencePhone.trim(),
        secondReferenceName = secondReferenceName.trim(),
        secondReferencePhone = secondReferencePhone.trim(),
        occupation = occupation.trim(),
        workplace = workplace.trim(),
        addressDetail = addressDetail.trim(),
        internalNotes = internalNotes.trim()
    )
}

private fun persistReadPermission(
    context: Context,
    uri: Uri
) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}



