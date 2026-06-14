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
import com.controlprestamos.features.clients.domain.model.UpdateClientInput
import com.controlprestamos.features.clients.domain.validation.ClientFormValidator

@Composable
fun EditClientScreen(
    clientId: String,
    onNavigateBack: () -> Unit,
    onClientUpdated: (String) -> Unit
) {
    val client = LocalClientRepository.getClientById(clientId)

    if (client == null) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Editar cliente",
                    subtitle = "Cliente no encontrado",
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
                        .padding(AppSpacing.screenHorizontal),
                    verticalArrangement = Arrangement.Center
                ) {
                    AppCard(bordered = true) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            Text(
                                text = "Cliente no encontrado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Gray900
                            )

                            Text(
                                text = "No pudimos encontrar el cliente que quieres editar.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AppColors.Gray600
                            )

                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    }
                }
            }
        }

        return
    }

    val safeClient = client
    val existingClients = LocalClientRepository.getClients()
    val hasFinancialHistory = LocalClientRepository.hasFinancialHistory(safeClient.id)

    var firstName by rememberSaveable(clientId) {
        mutableStateOf(safeClient.firstName)
    }

    var lastName by rememberSaveable(clientId) {
        mutableStateOf(safeClient.lastName)
    }

    var documentId by rememberSaveable(clientId) {
        mutableStateOf(safeClient.documentId)
    }

    var phone by rememberSaveable(clientId) {
        mutableStateOf(safeClient.phone)
    }

    var address by rememberSaveable(clientId) {
        mutableStateOf(safeClient.address)
    }

    var notes by rememberSaveable(clientId) {
        mutableStateOf(safeClient.notes)
    }

    var formError by rememberSaveable(clientId) {
        mutableStateOf<String?>(null)
    }

    val cleanFirstName = firstName.trim()
    val cleanLastName = lastName.trim()
    val cleanDocumentId = documentId.trim()
    val cleanPhone = phone.trim()
    val cleanAddress = address.trim()
    val cleanNotes = notes.trim()

    val finalFirstName = if (hasFinancialHistory) safeClient.firstName else cleanFirstName
    val finalLastName = if (hasFinancialHistory) safeClient.lastName else cleanLastName
    val finalDocumentId = if (hasFinancialHistory) safeClient.documentId else cleanDocumentId

    val fullNamePreview = listOf(
        finalFirstName,
        finalLastName
    )
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Cliente" }

    val duplicateByDocument = if (hasFinancialHistory || cleanDocumentId.isBlank()) {
        null
    } else {
        existingClients.firstOrNull { existingClient ->
            existingClient.id != safeClient.id &&
                existingClient.documentId.trim().equals(cleanDocumentId, ignoreCase = true)
        }
    }

    val duplicateByPhone = if (cleanPhone.isBlank()) {
        null
    } else {
        existingClients.firstOrNull { existingClient ->
            existingClient.id != safeClient.id &&
                existingClient.phone.trim().equals(cleanPhone, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Editar cliente",
                subtitle = safeClient.fullName,
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

                EditClientHeaderCard(
                    fullNamePreview = fullNamePreview,
                    documentId = finalDocumentId,
                    phone = cleanPhone,
                    protectedIdentity = hasFinancialHistory
                )

                if (hasFinancialHistory) {
                    ProtectedIdentityCard(
                        firstName = safeClient.firstName,
                        lastName = safeClient.lastName,
                        documentId = safeClient.documentId
                    )
                } else {
                    EditableIdentityCard(
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
                }

                EditableContactCard(
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

                EditableNotesCard(
                    notes = notes,
                    onNotesChange = {
                        notes = it
                        formError = null
                    }
                )

                if (duplicateByDocument != null || duplicateByPhone != null) {
                    DuplicateClientWarningCard(
                        duplicateByDocumentName = duplicateByDocument?.fullName.orEmpty(),
                        duplicateByPhoneName = duplicateByPhone?.fullName.orEmpty(),
                        blocksSave = duplicateByDocument != null
                    )
                }

                EditClientPreviewCard(
                    fullNamePreview = fullNamePreview,
                    documentId = finalDocumentId,
                    phone = cleanPhone,
                    address = cleanAddress,
                    notes = cleanNotes,
                    protectedIdentity = hasFinancialHistory
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
                    text = "Guardar cambios",
                    onClick = {
                        if (duplicateByDocument != null) {
                            formError = "Ya existe otro cliente con esta cédula o documento."
                            return@PrimaryButton
                        }

                        val validationInput = CreateClientInput(
                            firstName = finalFirstName,
                            lastName = finalLastName,
                            documentId = finalDocumentId,
                            phone = cleanPhone,
                            address = cleanAddress,
                            notes = cleanNotes
                        )

                        val validation = ClientFormValidator.validate(validationInput)

                        if (!validation.isValid) {
                            formError = validation.errorMessage
                            return@PrimaryButton
                        }

                        val updateInput = UpdateClientInput(
                            clientId = safeClient.id,
                            firstName = finalFirstName,
                            lastName = finalLastName,
                            documentId = finalDocumentId,
                            phone = cleanPhone,
                            address = cleanAddress,
                            notes = cleanNotes
                        )

                        LocalClientRepository.updateClient(updateInput)

                        onClientUpdated(safeClient.id)
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
private fun EditClientHeaderCard(
    fullNamePreview: String,
    documentId: String,
    phone: String,
    protectedIdentity: Boolean
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
                title = "Identidad",
                value = if (protectedIdentity) "Protegida" else "Editable",
                modifier = Modifier.weight(1f),
                highlight = protectedIdentity
            )

            InfoBox(
                title = "Contacto",
                value = "Editable",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProtectedIdentityCard(
    firstName: String,
    lastName: String,
    documentId: String
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
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Identidad protegida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Warning
            )

            Text(
                text = "Este cliente ya tiene historial financiero. Para evitar descuadres, el nombre y documento quedan protegidos.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Gray600
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                InfoBox(
                    title = "Nombre",
                    value = firstName.ifBlank { "Sin nombre" },
                    modifier = Modifier.weight(1f)
                )

                InfoBox(
                    title = "Apellido",
                    value = lastName.ifBlank { "Sin apellido" },
                    modifier = Modifier.weight(1f)
                )
            }

            InfoBox(
                title = "Documento",
                value = documentId.ifBlank { "Sin documento" },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EditableIdentityCard(
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
            text = "Puedes corregir la identidad mientras el cliente no tenga historial financiero.",
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
private fun EditableContactCard(
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
private fun EditableNotesCard(
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
            text = "Actualiza referencias, observaciones o detalles útiles para cobranza.",
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
    duplicateByPhoneName: String,
    blocksSave: Boolean
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
                text = if (blocksSave) "Duplicado bloqueado" else "Posible cliente duplicado",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Warning
            )

            if (duplicateByDocumentName.isNotBlank()) {
                Text(
                    text = "Ya existe otro cliente con este documento: $duplicateByDocumentName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            }

            if (duplicateByPhoneName.isNotBlank()) {
                Text(
                    text = "Hay otro cliente con este teléfono: $duplicateByPhoneName.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.Gray600
                )
            }
        }
    }
}

@Composable
private fun EditClientPreviewCard(
    fullNamePreview: String,
    documentId: String,
    phone: String,
    address: String,
    notes: String,
    protectedIdentity: Boolean
) {
    ReferenceCard {
        Text(
            text = "Resumen antes de guardar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Gray900
        )

        if (protectedIdentity) {
            Text(
                text = "Solo se actualizarán contacto, dirección y notas.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.Gray600
            )
        }

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
        color = if (highlight) AppColors.Warning.copy(alpha = 0.08f) else AppColors.SurfaceMuted,
        shape = RoundedCornerShape(AppRadius.card),
        border = BorderStroke(
            width = 1.dp,
            color = if (highlight) AppColors.Warning.copy(alpha = 0.25f) else AppColors.Border
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
                color = if (highlight) AppColors.Warning else AppColors.Gray900
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

