package com.controlprestamos.features.clients.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
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
import com.controlprestamos.core.ui.components.PrimaryButton
import com.controlprestamos.core.ui.components.SecondaryButton
import com.controlprestamos.core.ui.theme.AppColors
import com.controlprestamos.core.ui.theme.AppSpacing
import com.controlprestamos.features.clients.data.LocalClientExtraRepository
import com.controlprestamos.features.clients.data.LocalClientRepository
import com.controlprestamos.features.clients.domain.model.CreateClientInput
import com.controlprestamos.features.clients.domain.validation.ClientFormValidator

@Composable
fun CreateClientScreen(
    onNavigateBack: () -> Unit,
    onClientCreated: (String) -> Unit
) {
    val context = LocalContext.current
    LocalClientExtraRepository.initialize(context)
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var documentId by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    var photoUri by rememberSaveable { mutableStateOf("") }
    var referredBy by rememberSaveable { mutableStateOf("") }
    var registeredBy by rememberSaveable { mutableStateOf("") }
    var referenceName by rememberSaveable { mutableStateOf("") }
    var referencePhone by rememberSaveable { mutableStateOf("") }
    var secondReferenceName by rememberSaveable { mutableStateOf("") }
    var secondReferencePhone by rememberSaveable { mutableStateOf("") }
    var occupation by rememberSaveable { mutableStateOf("") }
    var workplace by rememberSaveable { mutableStateOf("") }
    var addressDetail by rememberSaveable { mutableStateOf("") }
    var internalNotes by rememberSaveable { mutableStateOf("") }

    var formError by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Crear cliente",
                subtitle = "Perfil completo del cliente",
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
                    text = "Nuevo cliente",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray900
                )

                Text(
                    text = "Registra los datos principales y la información de respaldo del cliente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.Gray600
                )

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
                            label = "Nombre",
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppTextField(
                            value = lastName,
                            onValueChange = {
                                lastName = it
                                formError = null
                            },
                            label = "Apellido",
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppTextField(
                            value = documentId,
                            onValueChange = {
                                documentId = it
                                formError = null
                            },
                            label = "Cédula o documento",
                            modifier = Modifier.fillMaxWidth()
                        )

                        AppTextField(
                            value = phone,
                            onValueChange = {
                                phone = it
                                formError = null
                            },
                            label = "Teléfono",
                            modifier = Modifier.fillMaxWidth(),
                            keyboardType = KeyboardType.Phone
                        )

                        AppTextField(
                            value = address,
                            onValueChange = {
                                address = it
                                formError = null
                            },
                            label = "Dirección principal",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false
                        )

                        AppTextField(
                            value = notes,
                            onValueChange = {
                                notes = it
                                formError = null
                            },
                            label = "Notas visibles",
                            modifier = Modifier.fillMaxWidth(),
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
                    text = "Guardar cliente",
                    onClick = {
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

                        val duplicateClient = LocalClientRepository.findDuplicateClient(
                            documentId = documentId,
                            phone = phone
                        )

                        if (duplicateClient != null) {
                            formError = LocalClientRepository.buildDuplicateClientMessage(duplicateClient)
                            return@PrimaryButton
                        }

                        val client = LocalClientRepository.createClient(input)

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

                        onClientCreated(client.id)
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


