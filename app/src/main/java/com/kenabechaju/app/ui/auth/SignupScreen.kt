package com.kenabechaju.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kenabechaju.app.data.remote.dto.DepartmentDto
import com.kenabechaju.app.data.remote.dto.HallDto
import com.kenabechaju.app.data.remote.dto.SessionOptionDto

/**
 * A plain dropdown `Box` + `DropdownMenu`, not `ExposedDropdownMenuBox` for
 * the actual popup — sidesteps that component's `menuAnchor()` signature,
 * which has shifted across recent Compose BOM releases (a real, previously
 * hit instability on this exact toolchain, not a hypothetical).
 */
@Composable
private fun <T> LabeledDropdown(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected?.let(optionLabel) ?: label)
        }
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun SignupScreen(
    onSignedUp: (email: String) -> Unit,
    onGoToLogin: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.signedUpEmail) {
        state.signedUpEmail?.let(onSignedUp)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("Join KenaBecha JU", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
        Text(
            "Selling needs your full JU details so buyers know who they're dealing with.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )

        if (!state.showFullForm) {
            Button(onClick = viewModel::showFullForm, modifier = Modifier.fillMaxWidth()) {
                Text("Sign up with full JU details")
            }
        } else {
            OutlinedTextField(
                value = state.fullName,
                onValueChange = { v -> viewModel.update { it.copy(fullName = v) } },
                label = { Text("Full name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = { v -> viewModel.update { it.copy(email = v) } },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = { v -> viewModel.update { it.copy(password = v) } },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { v -> viewModel.update { it.copy(confirmPassword = v) } },
                label = { Text("Confirm password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.phone,
                onValueChange = { v -> viewModel.update { it.copy(phone = v) } },
                label = { Text("Phone (e.g. 01712345678)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.studentId,
                    onValueChange = { v -> viewModel.update { it.copy(studentId = v) } },
                    label = { Text("Student ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
                OutlinedTextField(
                    value = state.registrationNo,
                    onValueChange = { v -> viewModel.update { it.copy(registrationNo = v) } },
                    label = { Text("Registration No") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            LabeledDropdown(
                label = "Hall",
                options = state.halls,
                selected = state.halls.find { it.id == state.hallId },
                optionLabel = HallDto::name,
                onSelect = { h -> viewModel.update { it.copy(hallId = h.id) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            LabeledDropdown(
                label = "Department",
                options = state.departments,
                selected = state.departments.find { it.id == state.departmentId },
                optionLabel = DepartmentDto::name,
                onSelect = { d -> viewModel.update { it.copy(departmentId = d.id) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            LabeledDropdown(
                label = "Session",
                options = state.sessions,
                selected = state.sessions.find { it.session == state.session },
                optionLabel = SessionOptionDto::session,
                onSelect = { s -> viewModel.update { it.copy(session = s.session) } },
                modifier = Modifier.fillMaxWidth(),
            )
            state.batch?.let {
                Text(
                    "Batch $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
            }

            Button(
                onClick = viewModel::submit,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text(if (state.isLoading) "Creating account…" else "Create account")
            }
        }

        TextButton(onClick = onGoToLogin, modifier = Modifier.padding(top = 12.dp)) {
            Text("Already have an account? Log in")
        }
    }
}
