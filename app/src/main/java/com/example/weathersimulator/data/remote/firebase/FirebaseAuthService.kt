package com.example.weathersimulator.data.remote.firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

interface FirebaseAuthService {
    suspend fun register(email: String, password: String): String // return uid
    suspend fun login(email: String, password: String)
    fun currentUid(): String?
    fun currentEmail(): String?
    suspend fun setDisplayName(fullName: String)
    fun logout()
}

class FirebaseAuthServiceImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : FirebaseAuthService {

    override suspend fun register(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: error("UID lipsă după înregistrare")
    }

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override fun currentUid(): String? = auth.currentUser?.uid
    override fun currentEmail(): String? = auth.currentUser?.email

    override suspend fun setDisplayName(fullName: String) {
        val user = auth.currentUser ?: return
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(fullName)
            .build()
        user.updateProfile(request).await()
    }

    override fun logout() {
        auth.signOut()
    }
}
