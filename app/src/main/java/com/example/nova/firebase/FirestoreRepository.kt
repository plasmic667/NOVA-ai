package com.example.nova.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.example.nova.data.models.ActionRecord
import com.example.nova.data.models.ChatMessage
import com.example.nova.data.models.Conversation
import com.example.nova.data.models.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository(private val context: Context) {

    private val TAG = "FirestoreRepository"

    private val db: FirebaseFirestore by lazy {
        val databaseId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(databaseId)
    }

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated before performing Firestore operations.")
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val data = hashMapOf<String, Any?>(
            "userId" to uid,
            "displayName" to (profile.displayName ?: auth.currentUser?.displayName),
            "email" to (profile.email ?: auth.currentUser?.email),
            "voiceAutoPlay" to profile.voiceAutoPlay,
            "speechSpeed" to profile.speechSpeed,
            "agentModeEnabled" to profile.agentModeEnabled,
            "syncToCloud" to profile.syncToCloud,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        // If creating newly, attach createdAt
        if (profile.createdAt == null) {
            data["createdAt"] = FieldValue.serverTimestamp()
        }
        try {
            docRef.set(data.filterValues { it != null }).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user profile at ${docRef.path}", e)
        }
    }

    suspend fun saveConversation(conv: Conversation) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
            .collection("conversations").document(conv.id)
        val payload = hashMapOf<String, Any?>(
            "id" to conv.id,
            "userId" to uid,
            "title" to conv.title,
            "createdAt" to (conv.createdAt ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload.filterValues { it != null }).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save conversation at ${docRef.path}", e)
        }
    }

    suspend fun saveMessage(msg: ChatMessage) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
            .collection("conversations").document(msg.conversationId)
            .collection("messages").document(msg.id)
        val payload = hashMapOf<String, Any?>(
            "id" to msg.id,
            "conversationId" to msg.conversationId,
            "userId" to uid,
            "role" to msg.role,
            "content" to msg.content,
            "actionType" to msg.actionType,
            "actionStatus" to msg.actionStatus,
            "actionPayload" to msg.actionPayload,
            "createdAt" to (msg.createdAt ?: FieldValue.serverTimestamp())
        )
        try {
            docRef.set(payload.filterValues { it != null }).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save message at ${docRef.path}", e)
        }
    }

    suspend fun saveActionRecord(action: ActionRecord) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
            .collection("actions").document(action.id)
        val payload = hashMapOf<String, Any?>(
            "id" to action.id,
            "userId" to uid,
            "actionType" to action.actionType,
            "riskLevel" to action.riskLevel,
            "description" to action.description,
            "status" to action.status,
            "createdAt" to (action.createdAt ?: FieldValue.serverTimestamp())
        )
        try {
            docRef.set(payload.filterValues { it != null }).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save action record at ${docRef.path}", e)
        }
    }

    suspend fun deleteUserData(userId: String) {
        try {
            // Delete user doc and root references
            db.collection("users").document(userId).delete().await()
            Log.d(TAG, "Successfully requested deletion of user cloud document for $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user documents", e)
        }
    }
}
