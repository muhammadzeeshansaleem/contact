package com.example.contact

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ContactList()
        }
    }
}

@Composable
fun ContactList() {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Fetch contacts when permission is granted
                contacts = fetchContacts(context)
            }
        }

    Column {
        Button(onClick = {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }) {
            Text("Get Contacts")
        }
        LazyColumn {
            items(contacts) { contact ->
                ContactItem(contact)
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Image(
            painter = contact.image,
            contentDescription = "Contact Image",
            modifier = Modifier
                .size(48.dp)
                .padding(end = 16.dp)
        )
        Column {
            Text(text = contact.name)
            Text(text = contact.number)
        }
    }
}

data class Contact(
    val name: String,
    val number: String,
    val image: Painter
)


@Composable
fun fetchContacts(context: Context): List<Contact> {
    val contacts = mutableListOf<Contact>()

    if (androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoUriIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)
                val number = cursor.getString(numberIndex)
                val photoUri = cursor.getString(photoUriIndex)

                val image = if (!photoUri.isNullOrEmpty()) {
                    val bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        Uri.parse(photoUri)
                    )
                    BitmapPainter(bitmap.asImageBitmap())
                } else {
                    painterResource(id = R.drawable.image_d)
                }

                contacts.add(Contact(name = name, number = number, image = image))
            }
        }
    }

    return contacts
}