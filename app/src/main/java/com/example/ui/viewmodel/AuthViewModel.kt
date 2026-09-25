package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val currentUser: UserSession? = null
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, generalError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, generalError = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login(): Boolean {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        var hasError = false
        var emailErr: String? = null
        var passErr: String? = null

        if (email.isEmpty()) {
            emailErr = "Email address is required."
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailErr = "Please enter a valid clinical/academic email address."
            hasError = true
        }

        if (password.isEmpty()) {
            passErr = "Password is required."
            hasError = true
        } else if (password.length < 6) {
            passErr = "Password must be at least 6 characters long."
            hasError = true
        }

        if (hasError) {
            _uiState.update { it.copy(emailError = emailErr, passwordError = passErr) }
            return false
        }

        val name = email.substringBefore("@").replace(".", " ")
            .split(" ")
            .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

        val user = UserSession(
            email = email,
            name = if (name.isNotBlank()) "Dr. $name" else "Dr. Alex Morgan",
            role = "Clinical Researcher",
            hospitalDept = "Renal Medicine & Diagnostic Imaging",
            isDemoAccount = true
        )

        _uiState.update { it.copy(currentUser = user, emailError = null, passwordError = null, generalError = null) }
        return true
    }

    fun quickDemoLogin() {
        _uiState.update {
            it.copy(
                email = "dr.alex.morgan@nephrology.org",
                password = "ClinicalAI2026!",
                emailError = null,
                passwordError = null,
                generalError = null,
                currentUser = UserSession(
                    email = "dr.alex.morgan@nephrology.org",
                    name = "Dr. Alex Morgan, MD",
                    role = "Lead Nephrology Imaging Specialist",
                    hospitalDept = "Renal Diagnostic & AI Research Lab",
                    isDemoAccount = true
                )
            )
        }
    }

    fun logout() {
        _uiState.update {
            AuthUiState()
        }
    }
}
