package com.hawkfranklin.aura.data.openrouter

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/** Encrypted atomic file in noBackupFilesDir; no plaintext fallback. */
@Singleton
class OpenRouterCredentialStore @Inject constructor(@ApplicationContext context: Context) {
  private val file = AtomicFile(File(context.noBackupFilesDir, "openrouter_credentials.enc"))
  private val alias = "aura.openrouter.v1"
  private val gson = Gson()
  private fun key(): SecretKey {
    val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    (store.getKey(alias, null) as? SecretKey)?.let { return it }
    return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
      init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
    }.generateKey()
  }
  @Synchronized fun read(): OpenRouterSecrets {
    if (!file.baseFile.exists() && !File(file.baseFile.path + ".bak").exists()) return OpenRouterSecrets()
    val bytes = file.readFully()
    require(bytes.size in 30..65536 && bytes[0].toInt() == 1) { "Invalid credential storage." }
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(1, 13)))
    return gson.fromJson(String(cipher.doFinal(bytes.copyOfRange(13, bytes.size)), Charsets.UTF_8), OpenRouterSecrets::class.java)
  }
  @Synchronized fun write(value: OpenRouterSecrets) {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
    val bytes = byteArrayOf(1) + cipher.iv + cipher.doFinal(gson.toJson(value).toByteArray(Charsets.UTF_8))
    val output = file.startWrite()
    try { output.write(bytes); file.finishWrite(output) }
    catch (e: Exception) { file.failWrite(output); throw e }
  }
  @Synchronized fun clear() {
    file.delete()
    KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(alias) }
  }
}
