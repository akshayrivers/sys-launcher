package com.example.syslauncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class ContactsActivity : AppCompatActivity() {
    private lateinit var store: ContactStore
    private lateinit var listContainer: LinearLayout
    private var pendingCallNumber: String? = null
    private var selectedPhotoUri: String? = null
    private var pendingCameraAfterPermission = false

    private val requestCallPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val number = pendingCallNumber
            if (granted && !number.isNullOrBlank()) {
                placeCall(number)
            } else {
                Toast.makeText(this, "Phone permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestContactsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchContactPicker()
            } else {
                Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val photoPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@registerForActivityResult
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            selectedPhotoUri = uri.toString()
            Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show()
        }

    private val cameraPreviewLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap == null) return@registerForActivityResult
            selectedPhotoUri = saveBitmap(bitmap)
            Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show()
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted && pendingCameraAfterPermission) {
                pendingCameraAfterPermission = false
                cameraPreviewLauncher.launch(null)
            }
        }

    private val contactPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data ?: return@registerForActivityResult
            importSelectedContact(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        store = ContactStore(this)
        listContainer = findViewById(R.id.contactsContainer)

        findViewById<Button>(R.id.btnAddContact).setOnClickListener { showEditDialog(null) }
        findViewById<Button>(R.id.btnImportContact).setOnClickListener { importFromPhoneContacts() }
        findViewById<Button>(R.id.btnImportAllContacts).setOnClickListener { importAllContactsFromPhone() }
        renderContacts()
    }

    private fun renderContacts() {
        listContainer.removeAllViews()
        val contacts = store.loadContacts()
        contacts.forEach { contact ->
            val row = layoutInflater.inflate(R.layout.item_contact, listContainer, false)
            val img = row.findViewById<ImageView>(R.id.contactImage)
            val name = row.findViewById<TextView>(R.id.contactName)
            val phone = row.findViewById<TextView>(R.id.contactPhone)
            val role = row.findViewById<TextView>(R.id.contactRole)

            name.text = contact.name
            phone.text = contact.phone
            role.text = contact.role

            if (!contact.photoUri.isNullOrBlank()) {
                try {
                    img.setImageURI(Uri.parse(contact.photoUri))
                } catch (_: Exception) {
                    img.setImageResource(R.drawable.ic_contact_default)
                }
            } else {
                img.setImageResource(R.drawable.ic_contact_default)
            }

            row.setOnClickListener { callWithPermission(contact.phone) }
            row.setOnLongClickListener {
                showEditDialog(contact)
                true
            }
            listContainer.addView(row)
        }
    }

    private fun showEditDialog(existing: ContactItem?) {
        val view = layoutInflater.inflate(R.layout.dialog_edit_contact, null)
        val etName = view.findViewById<EditText>(R.id.etContactName)
        val etPhone = view.findViewById<EditText>(R.id.etContactPhone)
        val etRole = view.findViewById<EditText>(R.id.etContactRole)
        val tvPhoto = view.findViewById<TextView>(R.id.tvPhotoName)
        val btnPickPhoto = view.findViewById<Button>(R.id.btnPickPhoto)
        val btnTakePhoto = view.findViewById<Button>(R.id.btnTakePhoto)

        selectedPhotoUri = existing?.photoUri
        etName.setText(existing?.name.orEmpty())
        etPhone.setText(existing?.phone.orEmpty())
        etRole.setText(existing?.role.orEmpty())
        tvPhoto.text = selectedPhotoUri ?: "No photo selected"

        btnPickPhoto.setOnClickListener {
            photoPicker.launch(arrayOf("image/*"))
        }
        btnTakePhoto.setOnClickListener { takePhotoWithPermission() }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add Contact" else "Edit Contact")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val role = etRole.text.toString().trim().uppercase()
                if (name.isBlank() || phone.isBlank() || role.isBlank()) {
                    Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val contacts = store.loadContacts()
                if (existing == null) {
                    val nextId = (contacts.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1
                    contacts.add(ContactItem(nextId.toString(), role, name, phone, selectedPhotoUri))
                } else {
                    val index = contacts.indexOfFirst { it.id == existing.id }
                    if (index >= 0) {
                        contacts[index] = existing.copy(
                            role = role,
                            name = name,
                            phone = phone,
                            photoUri = selectedPhotoUri
                        )
                    }
                }
                store.saveContacts(contacts)
                renderContacts()
            }
            .setNeutralButton(if (existing == null) "Cancel" else "Delete") { _, _ ->
                if (existing != null) {
                    val contacts = store.loadContacts()
                    contacts.removeAll { it.id == existing.id }
                    store.saveContacts(contacts)
                    renderContacts()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun callWithPermission(number: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            placeCall(number)
            return
        }
        pendingCallNumber = number
        requestCallPermission.launch(Manifest.permission.CALL_PHONE)
    }

    private fun placeCall(number: String) {
        try {
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")))
        } catch (_: Exception) {
            Toast.makeText(this, "Unable to place call", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importFromPhoneContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            launchContactPicker()
            return
        }
        requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun launchContactPicker() {
        val intent = Intent(
            Intent.ACTION_PICK,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        )
        contactPicker.launch(intent)
    }

    private fun importSelectedContact(uri: Uri) {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        cursor.use { c ->
            if (c == null || !c.moveToFirst()) {
                Toast.makeText(this, "Unable to import contact", Toast.LENGTH_SHORT).show()
                return
            }
            val name = c.getString(0).orEmpty()
            val number = c.getString(1).orEmpty()
            val photo = c.getString(2)

            if (number.isBlank()) {
                Toast.makeText(this, "Selected contact has no number", Toast.LENGTH_SHORT).show()
                return
            }
            askRoleAndSaveImported(name, number, photo)
        }
    }

    private fun askRoleAndSaveImported(name: String, number: String, photoUri: String?) {
        val roles = arrayOf("SON", "DAUGHTER", "HOME", "HELP", "OTHER")
        AlertDialog.Builder(this)
            .setTitle("Select role for $name")
            .setItems(roles) { _, which ->
                val selectedRole = roles[which]
                val contacts = store.loadContacts()
                val nextId = (contacts.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1
                contacts.add(
                    ContactItem(
                        id = nextId.toString(),
                        role = selectedRole,
                        name = name,
                        phone = number,
                        photoUri = photoUri
                    )
                )
                store.saveContacts(contacts)
                renderContacts()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun importAllContactsFromPhone() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            importAllContactsInternal()
            return
        }
        requestContactsPermission.launch(Manifest.permission.READ_CONTACTS)
    }

    private fun importAllContactsInternal() {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        val contacts = store.loadContacts()
        val existingPairs = contacts.map { "${it.name}|${it.phone}" }.toMutableSet()
        var added = 0

        cursor.use { c ->
            if (c == null) {
                Toast.makeText(this, "Unable to read contacts", Toast.LENGTH_SHORT).show()
                return
            }
            while (c.moveToNext()) {
                val name = c.getString(0).orEmpty().trim()
                val number = c.getString(1).orEmpty().trim()
                val photo = c.getString(2)
                if (name.isBlank() || number.isBlank()) continue

                val key = "$name|$number"
                if (existingPairs.contains(key)) continue

                val nextId = (contacts.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1
                contacts.add(
                    ContactItem(
                        id = nextId.toString(),
                        role = "OTHER",
                        name = name,
                        phone = number,
                        photoUri = photo
                    )
                )
                existingPairs.add(key)
                added++
            }
        }

        store.saveContacts(contacts)
        renderContacts()
        Toast.makeText(this, "Imported $added contacts", Toast.LENGTH_SHORT).show()
    }

    private fun takePhotoWithPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            cameraPreviewLauncher.launch(null)
            return
        }
        pendingCameraAfterPermission = true
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun saveBitmap(bitmap: Bitmap): String {
        val file = File(filesDir, "contact_photo_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            output.flush()
        }
        return Uri.fromFile(file).toString()
    }
}
