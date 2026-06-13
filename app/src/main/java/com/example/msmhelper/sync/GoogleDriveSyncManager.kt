package com.example.msmhelper.sync

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.client.http.ByteArrayContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Collections

class GoogleDriveSyncManager(private val context: Context) {

    private val googleSignInClient: GoogleSignInClient
    private val appDataScope = "https://www.googleapis.com/auth/drive.appdata"
    
    // For handling recoverable auth screens (consent UI)
    var recoverableIntent: Intent? = null
    
    // For diagnostic error tracking
    var lastError: String? = null

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(appDataScope))
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    suspend fun silentSignIn(): GoogleSignInAccount? = withContext(Dispatchers.IO) {
        lastError = null
        try {
            val task = googleSignInClient.silentSignIn()
            // Wait for the task to complete asynchronously on the background thread
            Tasks.await(task)
            if (task.isSuccessful) {
                task.result
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            lastError = "${e.javaClass.simpleName}: ${e.message}"
            null
        }
    }

    suspend fun signOut(): Boolean = withContext(Dispatchers.IO) {
        lastError = null
        return@withContext try {
            val task = googleSignInClient.signOut()
            // Wait for signout task to complete asynchronously on the background thread
            Tasks.await(task)
            task.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            lastError = "${e.javaClass.simpleName}: ${e.message}"
            false
        }
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(appDataScope)
        )
        // Using email is much more reliable and avoids dangerous permission GET_ACCOUNTS
        credential.selectedAccountName = account.email
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("MSM Helper")
            .build()
    }

    suspend fun uploadBackup(account: GoogleSignInAccount, jsonContent: String): Boolean = withContext(Dispatchers.IO) {
        recoverableIntent = null
        lastError = null
        try {
            val drive = getDriveService(account)
            
            // 1. Check if backup file already exists in AppDataFolder
            val filesList = drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = 'msm_helper_backup.json'")
                .execute()
            
            val driveFile = filesList.files?.firstOrNull()
            
            val metadata = File().apply {
                name = "msm_helper_backup.json"
                parents = Collections.singletonList("appDataFolder")
            }
            
            val contentStream = ByteArrayContent.fromString("application/json", jsonContent)
            
            if (driveFile != null) {
                // Update existing file
                drive.files().update(driveFile.id, null, contentStream).execute()
            } else {
                // Create new file
                drive.files().create(metadata, contentStream).execute()
            }
            true
        } catch (e: UserRecoverableAuthIOException) {
            e.printStackTrace()
            recoverableIntent = e.intent
            lastError = "User consent required (UserRecoverableAuthIOException)"
            false
        } catch (e: Exception) {
            e.printStackTrace()
            lastError = "${e.javaClass.simpleName}: ${e.message}"
            false
        }
    }

    suspend fun downloadBackup(account: GoogleSignInAccount): String? = withContext(Dispatchers.IO) {
        recoverableIntent = null
        lastError = null
        try {
            val drive = getDriveService(account)
            
            // Locating the file
            val filesList = drive.files().list()
                .setSpaces("appDataFolder")
                .setQ("name = 'msm_helper_backup.json'")
                .execute()
            
            val driveFile = filesList.files?.firstOrNull() ?: return@withContext null
            
            // Downloading content
            val outputStream = ByteArrayOutputStream()
            drive.files().get(driveFile.id).executeMediaAndDownloadTo(outputStream)
            outputStream.toString("UTF-8")
        } catch (e: UserRecoverableAuthIOException) {
            e.printStackTrace()
            recoverableIntent = e.intent
            lastError = "User consent required (UserRecoverableAuthIOException)"
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
