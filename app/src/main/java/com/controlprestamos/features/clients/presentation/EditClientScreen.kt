package com.controlprestamos.features.clients.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.controlprestamos.core.ui.components.AppCard
import com.controlprestamos.core.ui.components.AppTextField
import com.controlprestamos.core.ui.components.AppTopBar
import com.controlprestamos.core.ui.components.EmptyState
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientExtraRepository
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
    val context = LocalContext.current
    LocalClientExtraRepository.initialize(context)
    val client = LocalClientRepository.getClientById(clientId)
    val extra = LocalClientExtraRepository.getExtra(clientId)

    var firstName by rememberSaveable(clientId) { mutableStateOf(client?.firstName.orEmpty()) }
    var lastName by rememberSaveable(clientId) { mutableStateOf(client?.lastName.orEmpty()) }
    var documentId by rememberSaveable(clientId) { mutableStateOf(client?.documentId.orEmpty()) }
    var phone by rememberSaveable(clientId) { mutableStateOf(client?.phone.orEmpty()) }
    var address by rememberSaveable(clientId) { mutableStateOf(client?.address.orEmpty()) }
    var notes by rememberSaveable(clientId) { mutableStateOf(client?.notes.orEmpty()) }

    var photoUri by rememberSaveable(clientId) { mutableStateOf(extra.photoUri) }
    var referredBy by rememberSaveable(clientId) { mutableStateOf(extra.referredBy) }
    var registeredBy by rememberSaveable(clientId) { mutableStateOf(extra.registeredBy) }
    var referenceName by rememberSaveable(clientId) { mutableStateOf(extra.referenceName) }
    var referencePhone by rememberSaveable(clientId) { mutableStateOf(extra.referencePhone) }
    var secondReferenceName by rememberSaveable(clientId) { mutableStateOf(extra.secondReferenceName) }
    var secondReferencePhone by rememberSaveable(clientId) { mutableStateOf(extra.secondReferencePhone) }
    var occupation by rememberSaveable(clientId) { mutableStateOf(extra.occupation) }
    var workplace by rememberSaveable(clientId) { mutableStateOf(extra.workplace) }
    var addressDetail by rememberSaveable(clientId) { mutableStateOf(extra.addressDetail) }
    var internalNotes by rememberSaveable(clientId) { mutableStateOf(extra.internalNotes) }

    var formError by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Editar cliente",
                subtitle = client?.fullName ?: "Cliente no encontrado",
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
                if (client == null) {
                    EmptyState(
                        title = "Cliente no encontrado",
                        description = "No pudimos encontrar el cliente solicitado.",
                        action = {
                            SecondaryButton(
                                text = "Volver",
                                onClick = onNavigateBack
                            )
                        }
                    )

                    return@Column
                }

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    bordered = true
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Text(
                            text = "Datos principales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray900
                        )

                        AppTextField(
                            value = firstName,
                            onValueChange = {
                                firstName = it
                                formError = null
                            },
                            label = "Nombre"
                        )

                        AppTextField(
                            value = lastName,
                            onValueChange = {
                                lastName = it
                                formError = null
                            },
                            label = "Apellido"
                        )

                        AppTextField(
                            value = documentId,
                            onValueChange = {
                                documentId = it
                                formError = null
                            },
                            label = "Cédula o documento"
                        )

                        AppTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                formError = null
                            },
                            label = "Teléfono",
                            keyboardType = KeyboardType.Phone
                        )

                        AppTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                formError = null
                            },
                            label = "Dirección principal",
                            singleLine = false
                        )

                        AppTextField(
                            value = notes,
                            onValueChange = {
                                notes = it
                                formError = null
                            },
                            label = "Notas visibles",
                            singleLine = false
                        )
                    }
                }

                ClientExtraForm(
                    photoUri = photoUri,
                    onPhotoUriChange = { photoUri = it },
                    referredBy = referredBy,
                    onReferredByChange = { referredBy = it },
                    registeredBy = registeredBy,
                    onRegisteredByChange = { registeredBy = it },
                    referenceName = referenceName,
                    onReferenceNameChange = { referenceName = it },
                    referencePhone = referencePhone,
                    onReferencePhoneChange = { referencePhone = it },
                    secondReferenceName = secondReferenceName,
                    onSecondReferenceNameChange = { secondReferenceName = it },
                    secondReferencePhone = secondReferencePhone,
                    onSecondReferencePhoneChange = { secondReferencePhone = it },
                    occupation = occupation,
                    onOccupationChange = { occupation = it },
                    workplace = workplace,
                    onWorkplaceChange = { workplace = it },
                    addressDetail = addressDetail,
                    onAddressDetailChange = { addressDetail = it },
                    internalNotes = internalNotes,
                    onInternalNotesChange = { internalNotes = it }
                )

                if (!formError.isNullOrBlank()) {
                    AppCard(bordered = true) {
                        Text(
                            text = formError.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.Error
                        )
                    }
                }

                PrimaryButton(
                    text = "Guardar cambios",
                    onClick = {
                        val validationInput = CreateClientInput(
                            firstName = firstName,
                            lastName = lastName,
                            documentId = documentId,
                            phone = phone,
                            address = address,
                            notes = notes
                        )

                        val validation = ClientFormValidator.validate(validationInput)

                        if (!validation.isValid) {
                            formError = validation.errorMessage
                            return@PrimaryButton
                        }

                        val duplicateClient = LocalClientRepository.findDuplicateClient(
                            documentId = documentId,
                            phone = phone,
                            ignoreClientId = client.id
                        )

                        if (duplicateClient != null) {
                            formError = LocalClientRepository.buildDuplicateClientMessage(duplicateClient)
                            return@PrimaryButton
                        }

                        val updated = LocalClientRepository.updateClient(
                            UpdateClientInput(
                                clientId = client.id,
                                firstName = firstName,
                                lastName = lastName,
                                documentId = documentId,
                                phone = phone,
                                address = address,
                                notes = notes
                            )
                        )

                        if (!updated) {
                            formError = "No se pudo actualizar el cliente."
                            return@PrimaryButton
                        }

                        LocalClientExtraRepository.saveExtra(
                            buildClientExtraInfo(
                                clientId = client.id,
                                photoUri = photoUri,
                                referredBy = referredBy,
                                registeredBy = registeredBy,
                                referenceName = referenceName,
                                referencePhone = referencePhone,
                                secondReferenceName = secondReferenceName,
                                secondReferencePhone = secondReferencePhone,
                                occupation = occupation,
                                workplace = workplace,
                                addressDetail = addressDetail,
                                internalNotes = internalNotes
                            )
                        )

                        onClientUpdated(client.id)
                    }
                )

                SecondaryButton(
                    text = "Cancelar",
                    onClick = onNavigateBack
                )
            }
        }
    }
}


