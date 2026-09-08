package com.controlprestamos.features.clients.presentation.form.identity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp


@Composable
fun IdentityStep(

    name: String,

    lastName: String,

    document: String,

    onNameChange: (String) -> Unit,

    onLastNameChange: (String) -> Unit,

    onDocumentChange: (String) -> Unit

) {

    val nameError =
        name.isNotBlank() &&
        name.length < 2

    val lastNameError =
        lastName.isNotBlank() &&
        lastName.length < 2

    val documentError =
        document.isNotBlank() &&
        document.length < 5


    Card(

        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors()

    ) {

        Column(

            modifier = Modifier.padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(16.dp)

        ) {

            Text(

                text = "IDENTIDAD DEL CLIENTE",

                style =
                    MaterialTheme.typography.titleMedium

            )


            OutlinedTextField(

                value = name,

                onValueChange = { value ->

                    val clean =
                        value.filter {

                            it.isLetter() || it == ' '

                        }

                    onNameChange(clean)

                },

                label = {

                    Text("Nombre")

                },

                supportingText = {

                    if (nameError) {

                        Text(
                            "Ingrese un nombre válido"
                        )

                    }

                },

                isError = nameError,

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()

            )


            OutlinedTextField(

                value = lastName,

                onValueChange = { value ->

                    val clean =
                        value.filter {

                            it.isLetter() || it == ' '

                        }

                    onLastNameChange(clean)

                },

                label = {

                    Text("Apellido")

                },

                supportingText = {

                    if (lastNameError) {

                        Text(
                            "Ingrese un apellido válido"
                        )

                    }

                },

                isError = lastNameError,

                singleLine = true,

                modifier =
                    Modifier.fillMaxWidth()

            )


            OutlinedTextField(

                value = document,

                onValueChange = { value ->

                    val clean =
                        value.filter {

                            it.isLetterOrDigit()

                        }

                    onDocumentChange(clean)

                },

                label = {

                    Text("Documento de identidad")

                },

                supportingText = {

                    if (documentError) {

                        Text(
                            "Documento demasiado corto"
                        )

                    }

                },

                isError = documentError,

                singleLine = true,

                keyboardOptions =
                    KeyboardOptions(

                        keyboardType =
                            KeyboardType.Text

                    ),

                modifier =
                    Modifier.fillMaxWidth()

            )

        }

    }

}
