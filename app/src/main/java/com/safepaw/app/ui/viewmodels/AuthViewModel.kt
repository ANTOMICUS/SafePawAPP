package com.safepaw.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safepaw.app.data.models.UsuarioRegistro
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Authenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Mantener el estado de auth sincronizado con la sesión persistida de Supabase.
        viewModelScope.launch {
            supabaseClient.auth.sessionStatus.collect { status ->
                when (status) {
                    SessionStatus.LoadingFromStorage -> _authState.value = AuthState.Loading
                    is SessionStatus.Authenticated -> _authState.value = AuthState.Authenticated
                    is SessionStatus.NotAuthenticated -> _authState.value = AuthState.Idle
                    SessionStatus.NetworkError -> {
                        // Si falla red, no forzamos logout: mantenemos Idle (o el último estado) sin romper flujo.
                        if (_authState.value !is AuthState.Authenticated) {
                            _authState.value = AuthState.Idle
                        }
                    }
                }
            }
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    password = pass
                }
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error de autenticación")
            }
        }
    }

    fun signUp(nombre: String, email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    password = pass
                }
                // Nota supabase-kt: si "Confirm email" está desactivado, signUpWith() puede devolver null
                // porque te autentica directamente. En ese caso, el id viene en la sesión/usuario actual.
                val userId = user?.id
                    ?: supabaseClient.auth.currentSessionOrNull()?.user?.id
                    ?: supabaseClient.auth.currentUserOrNull()?.id
                    ?: throw IllegalStateException(
                        "No se pudo obtener el id del usuario de Supabase. " +
                            "Revisa si 'Confirm email' está activado y si el registro ha completado correctamente."
                    )
                val codigoUsuario = generateUserCode()
                val usuarioRegistro = UsuarioRegistro(
                    id_usuario = userId,
                    nombre = nombre,
                    rol = "Voluntario",
                    mail = email,
                    contrasena = hashPassword(pass),
                    codigo_usuario = codigoUsuario
                )
                supabaseClient.postgrest["usuarios"].insert(usuarioRegistro)
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error al crear la cuenta")
            }
        }
    }

    private fun generateUserCode(length: Int = 8): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun signOut() {
        viewModelScope.launch {
            supabaseClient.auth.signOut()
            _authState.value = AuthState.Idle
        }
    }
}
