package com.example.weathersimulator.data.remote.firebase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String,
    val email: String,
    val firstName: String,
    val lastName: String
)

interface FirebaseUserService {
    suspend fun createUserProfile(profile: UserProfile)
    suspend fun getUserProfile(uid: String): UserProfile?
}

class FirebaseUserServiceImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FirebaseUserService {

    override suspend fun createUserProfile(profile: UserProfile) {
        db.collection("users")
            .document(profile.uid)
            .set(profile)
            .await()
    }

    override suspend fun getUserProfile(uid: String): UserProfile? {
        val snap = db.collection("users").document(uid).get().await()
        return if (snap.exists()) snap.toObject(UserProfile::class.java) else null
    }
}
