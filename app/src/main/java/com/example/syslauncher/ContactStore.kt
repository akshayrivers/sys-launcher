package com.example.syslauncher

import android.content.Context

data class ContactItem(
    val id: String,
    val role: String,
    val name: String,
    val phone: String,
    val photoUri: String?
)

class ContactStore(context: Context) {
    private val prefs = context.getSharedPreferences("senior_launcher_contacts", Context.MODE_PRIVATE)

    fun loadContacts(): MutableList<ContactItem> {
        val raw = prefs.getString(KEY_CONTACTS, "").orEmpty()
        if (raw.isBlank()) return mutableListOf()

        return raw.split(RECORD_SEP)
            .mapNotNull { decode(it) }
            .toMutableList()
    }

    fun saveContacts(contacts: List<ContactItem>) {
        val encoded = contacts.joinToString(RECORD_SEP) { encode(it) }
        prefs.edit().putString(KEY_CONTACTS, encoded).apply()
    }

    fun seedDefaultsIfEmpty(son: String, daughter: String, home: String, help: String) {
        if (loadContacts().isNotEmpty()) return
        saveContacts(
            listOf(
                ContactItem("1", "SON", "Son", son, null),
                ContactItem("2", "DAUGHTER", "Daughter", daughter, null),
                ContactItem("3", "HOME", "Home", home, null),
                ContactItem("4", "HELP", "Help", help, null)
            )
        )
    }

    fun findByRole(role: String): ContactItem? {
        return loadContacts().firstOrNull { it.role == role }
    }

    fun findById(id: String): ContactItem? {
        return loadContacts().firstOrNull { it.id == id }
    }

    private fun encode(item: ContactItem): String {
        return listOf(item.id, item.role, item.name, item.phone, item.photoUri.orEmpty())
            .joinToString(FIELD_SEP) { it.replace(FIELD_SEP, " ").replace(RECORD_SEP, " ") }
    }

    private fun decode(raw: String): ContactItem? {
        val parts = raw.split(FIELD_SEP)
        if (parts.size < 5) return null
        return ContactItem(
            id = parts[0],
            role = parts[1],
            name = parts[2],
            phone = parts[3],
            photoUri = parts[4].ifBlank { null }
        )
    }

    companion object {
        private const val KEY_CONTACTS = "contacts_v1"
        private const val FIELD_SEP = "|~|"
        private const val RECORD_SEP = "|#|"
    }
}
