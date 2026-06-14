package com.controlprestamos.features.clients.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppRadius
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.clients.domain.model.CreateClientInput
import com.controlprestamos.features.clients.domain.validation.ClientFormValidator

@Composable
fun CreateClientScreen(
    onNavigateBack: () -> Unit,
    onClientCreated: (String) -> Unit
) {
    val existingClients = LocalClientRepository.getClients()

    var firstName by rememberSaveable {
        mutableStateOf("")
    }

    var lastName by rememberSaveable {
        mutableStateOf("")
    }

    var documentId by rememberSaveable {
        mutableStateOf("")
    }

    var phone by rememberSaveable {
        mutableStateOf("")
    }

    var address by rememberSaveable {
        mutableStateOf("")
    }

    var notes by rememberSaveable {
        mutableStateOf("")
    }

    var formError by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val cleanFirstName = firstName.trim()
    val cleanLastName = lastName.trim()
    val cleanDocumentId = documentId.trim()
    val cleanPhone = phone.trim()
    val cleanAddress = address.trim()
    val cleanNotes = notes.trim()

    val fullNamePreview = listOf(
        cleanFirstName,
        cleanLastName
    )
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Nuevo cliente" }

    val duplicateByDocument = if (cleanDocumentId.isBlank()) {
        null
    } else {
        existingClients.firstOrNull { client ->
            client.documentId.trim().equals(cleanDocumentId, ignoreCase = true)
        }
    }

    val duplicateByPhone = if (cleanPhone.isBlank()) {
        null
    } else {
        existingClients.firstOrNull { client ->
            client.phone.trim().equals(cleanPhone, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Crear cliente",
                subtitle = "Nuevo cliente",
                showBack = true,
                showMore = false,
                showMenu = false,
                showNotifications = false,
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
                Spacer(modifier = Modifier.height(AppSpacing.xs))

                CreateClientHeaderCard(
                    fullNamePreview = fullNamePreview,
                    documentId = cleanDocumentId,
                    phone = cleanPhone
                )

                ClientIdentityCard(
                    firstName = firstName,
                    onFirstNameChange = {
                        firstName = it
                        formError = null
                    },
                    lastName = lastName,
                    onLastNameChange = {
                        lastName = it
                        formError = null
                    },
                    documentId = documentId,
                    onDocumentIdChange = {
                        documentId = it
                        formError = null
                    }
                )

                ClientContactCard(
                    phone = phone,
                    onPhoneChange = {
                        phone = it
                        formError = null
                    },
                    address = address,
                    onAddressChange = {
                        address = it
                        formError = null
                    }
                )

                ClientNotesCard(
                    notes = notes,
                    onNotesChange = {
                        notes = it
                        formError = null
                    }
                )

                if (duplicateByDocument != null || duplicateByPhone != null) {
                    DuplicateClientWarningCard(
                        duplicateByDocumentName = duplicateByDocument?.fullName.orEmpty(),
                        duplicateByPhoneName = duplicateByPhone?.fullName.orEmpty()
                    )
                }

                CreateClientPreviewCard(
                    fullNamePreview = fullNamePreview,
                    documentId = cleanDocumentId,
                    phone = cleanPhone,
                    address = cleanAddress,
                    notes = cleanNotes
                )

                if (!formError.isNullOrBlank()) {
                    AppCard(bordered = true) {
                        Text(
                            text = formError.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Error
                        )
                    }
                }

                PrimaryButton(
                    text = "Crear cliente",
                    onClick = {
                        if (duplicateByDocument != null) {
                            formError = "Ya existe un cliente con esta cédula o documento."
                            return@PrimaryButton
                        }

                        val input = CreateClientInput(
                            firstName = firstName,
                            lastName = lastName,
                            documentId = documentId,
                            phone = phone,
                            address = address,
                            notes = notes
                        )

                        val validation = ClientFormValidator.validate(input)

                        if (!validation.isValid) {
                            formError = validation.errorMessage
                            return@PrimaryButton
                        }

                        val createdClient = LocalClientRepository.createClient(input)

                        onClientCreated(createdClient.id)
                    }
                )

                SecondaryButton(
                    text = "Cancelar",
                    onClick = onNavigateBack
                )

                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
        }
    }
}

@Composable
private fun CreateClientHeaderCard(
    fullNamePreview: String,
    documentId: String,
    phone: String
) {
    ReferenceCard {
        Text(
            text = "Ficha del cliente",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Text(
            text = fullNamePreview,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.PrimaryDark
        )

        Text(
            text = buildSubtitle(
                documentId = documentId,
                phone = phone
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.Gray600
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Estado inicial",
                value = "Activo",
                modifier = Modifier.weight(1f),
                highlight = true
            )

            InfoBox(
                title = "Historial",
                value = "Nuevo",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ClientIdentityCard(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    documentId: String,
    onDocumentIdChange: (String) -> Unit
) {
    ReferenceCard {
        Text(
            text = "Identificación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Text(
            text = "Estos datos ayudan a reconocer al cliente y evitar duplicados.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )

        AppTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            label = "Nombre"
        )

        AppTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            label = "Apellido"
        )

        AppTextField(
            value = documentId,
            onValueChange = onDocumentIdChange,
            label = "Cédula o documento"
        )
    }
}

@Composable
private fun ClientContactCard(
    phone: String,
    onPhoneChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit
) {
    ReferenceCard {
        Text(
            text = "Contacto",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        AppTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = "Teléfono",
            keyboardType = KeyboardType.Phone
        )

        AppTextField(
            value = address,
            onValueChange = onAddressChange,
            label = "Dirección",
            singleLine = false
        )
    }
}

@Composable
private fun ClientNotesCard(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    ReferenceCard {
        Text(
            text = "Notas internas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        Text(
            text = "Puedes agregar referencias, comportamiento de pago o detalles útiles.",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.Gray600
        )

        AppTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = "Notas",
            singleLine = false
        )
    }
}

@Composable
private fun DuplicateClientWarningCard(
    duplicateByDocumentName: String,
    duplicateByPhoneName: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Warning.copy(alpha = 0.08f),
        shape = RoundedCornerShape(AppRadius.cardLarge),
        border = BorderStroke(
            width = 1.dp,
            color = AppColors.Warning.copy(alpha = 0.28f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.cardPaddingLarge),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Text(
                text = "Posible cliente duplicado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Warning
            )

            if (duplicateByDocumentName.isNotBlank()) {
                Text(
                    text = "Ya existe un cliente con este documento: $duplicateByDocumentName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            }

            if (duplicateByPhoneName.isNotBlank()) {
                Text(
                    text = "Hay un cliente con este teléfono: $duplicateByPhoneName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            }
        }
    }
}

@Composable
private fun CreateClientPreviewCard(
    fullNamePreview: String,
    documentId: String,
    phone: String,
    address: String,
    notes: String
) {
    ReferenceCard {
        Text(
            text = "Resumen antes de guardar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        InfoBox(
            title = "Nombre completo",
            value = fullNamePreview,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            InfoBox(
                title = "Documento",
                value = documentId.ifBlank { "Sin documento" },
                modifier = Modifier.weight(1f)
            )

            InfoBox(
                title = "Teléfono",
                value = phone.ifBlank { "Sin teléfono" },
                modifier = Modifier.weight(1f)
            )
        }

        InfoBox(
            title = "Dirección",
            value = address.ifBlank { "Sin dirección" },
            modifier = Modifier.fillMaxWidth()
        )

        if (notes.isNotBlank()) {
            InfoBox(
                title = "Notas",
                value = notes,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InfoBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Surface(
        modifier = modifier.heightIn(min = 68.dp),
        color = if (highlight) AppColors.AccentTeal.copy(alpha = 0.10f) else AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = if (highlight) AppColors.AccentTeal.copy(alpha = 0.30f) else AppColors.Border
        )
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.Gray500
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (highlight) AppColors.AccentTeal else AppColors.Gray900
            )
        }
    }
}

@Composable
private fun ReferenceCard(
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        bordered = true
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            content = content
        )
    }
}

private fun buildSubtitle(
    documentId: String,
    phone: String
): String {
    return listOf(
        documentId.ifBlank { "Sin documento" },
        phone.ifBlank { "Sin teléfono" }
    ).joinToString(" · ")
}
