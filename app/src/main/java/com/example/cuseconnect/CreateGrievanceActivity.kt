package com.example.cuseconnect

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream
import java.io.IOException

class CreateGrievanceActivity : AppCompatActivity(), ImageUploadCallback {

    private lateinit var categorySpinner: Spinner
    private lateinit var subcategorySpinner: Spinner
    private lateinit var auth: FirebaseAuth
    private val selectedImageUris = ArrayList<Uri>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_grievance)

        Log.d("CreateGrievanceActivity", "Before button press")
        val uploadButton = findViewById<Button>(R.id.attachmentButton)
        uploadButton.setOnClickListener {
            Log.d("CreateGrievanceActivity", "before on click")
            if (!hasCameraPermission() || !hasStoragePermission()) {
                // Request permissions if not granted
                ActivityCompat.requestPermissions(
                    this@CreateGrievanceActivity,
                    arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
            // Always show the options to take photo or choose from gallery
            showPhotoSourceOptions()
        }

        FirebaseApp.initializeApp(this)

        categorySpinner = findViewById(R.id.categorySpinner)
        subcategorySpinner = findViewById(R.id.subcategorySpinner)

        auth = FirebaseAuth.getInstance()

        fetchFacilities()

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                val selectedFacility = parentView.getItemAtPosition(position) as String
                // Call this function to fetch and populate the subcategorySpinner
                fetchSubfacilitiesForFacility(selectedFacility)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Handle when nothing is selected (if needed)
            }
        }

        val submitBtn = findViewById<Button>(R.id.submitBtn)
        submitBtn.setOnClickListener {
            val titleEditText = findViewById<EditText>(R.id.titleEditText)
            val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)

            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Title and description are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // You may need to adjust the code to handle cases when no category or subcategory is selected
            var selectedFacility = categorySpinner.selectedItem
            var selectedSubfacility = subcategorySpinner.selectedItem

            if (selectedFacility.toString() == "Facility" || selectedSubfacility.toString() == "Sub-Facility" || selectedSubfacility.toString().isEmpty()) {
                Toast.makeText(this, "Facility and Sub-Facility are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selectedFacility = selectedFacility.toString()
            selectedSubfacility = selectedSubfacility.toString()

            // Assuming you have a Firebase user, you can get the UID of the current user
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

            if (currentUserUid != null) {
                // Create a new grievance object
                val grievance = hashMapOf(
                    "name" to title,
                    "description" to description,
                    "facility" to selectedFacility,
                    "subfacility" to selectedSubfacility,
                    "userId" to currentUserUid,
                    "status" to "Open"
                )

                // Add the grievance to the "grievances" collection
                val db = FirebaseFirestore.getInstance()

                // Use a reference to the "grievances" collection
                db.collection("grievances")
                    .add(grievance)
                    .addOnSuccessListener { documentReference ->
                        Log.d("CreateGrievanceActivity", "Grievance added with ID: ${documentReference.id}")
                        // Call the function to upload images
                        uploadImages(documentReference.id)
                        // After successfully adding the grievance, assign it to an admin
                        assignGrievanceToAdmin(documentReference.id, selectedSubfacility)
                        Toast.makeText(this, "Grievance Created Successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("CreateGrievanceActivity", "Error adding grievance", e)
                        Toast.makeText(this, "Error submitting grievance", Toast.LENGTH_SHORT).show()
                    }

            } else {
                // Handle the case when the user is not authenticated
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }

    }

    // ...

    private fun assignGrievanceToAdmin(grievanceId: String, grievanceSubfacility: String) {
        val db = FirebaseFirestore.getInstance()
        val adminsRef = db.collection("admins")

        adminsRef.whereEqualTo("subFacility", grievanceSubfacility).get()
            .addOnSuccessListener { querySnapshot ->
                val admins = querySnapshot.documents

                if (admins.isNotEmpty()) {
                    // Find the admin with the least assigned grievances
                    val adminWithLeastGrievances = findAdminWithLeastGrievances(admins)

                    // Assign the grievance to this admin
                    val adminId = adminWithLeastGrievances.id
                    db.collection("grievances").document(grievanceId)
                        .update("assignedAdmin", adminId)
                        .addOnSuccessListener {
                            Log.d("AssignGrievance", "Grievance assigned to admin: $adminId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("AssignGrievance", "Error assigning grievance", e)
                        }
                } else {
                    Log.e("AssignGrievance", "No admins found for subfacility: $grievanceSubfacility")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AssignGrievance", "Error fetching admins", e)
            }
    }

    private fun findAdminWithLeastGrievances(admins: List<DocumentSnapshot>): DocumentSnapshot {
        // This function assumes each admin document has a field 'assignedGrievancesCount'
        // representing the number of grievances currently assigned to them.
        return admins.minByOrNull { admin ->
            admin.getLong("assignedGrievancesCount") ?: Long.MAX_VALUE
        } ?: admins.first()
    }

// ...


    private fun uploadImages(grievanceId: String) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        // Initialize an empty list to hold all the upload tasks for concurrency
        val uploadTasks = mutableListOf<Task<Uri>>()

        // Iterate over all selected image URIs
        for (imageUri in selectedImageUris) {
            // Create a reference to the image file
            val imageRef = storageRef.child("grievance_images/$grievanceId/${imageUri.lastPathSegment}")

            // Create and start the upload task
            val uploadTask = imageRef.putFile(imageUri).continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                // Continue to the next task to get the download URL
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // We get the download URL like this
                    val downloadUrl = task.result
                    updateGrievanceWithImageUrl(grievanceId, downloadUrl.toString())
                } else {
                    // Handle failure
                    Log.e("CreateGrievanceActivity", "Image upload failed: ${task.exception}")
                }
            }

            // Add each upload task to the list
            uploadTasks.add(uploadTask)
        }

        Tasks.whenAll(uploadTasks).addOnCompleteListener {
            Log.d("CreateGrievanceActivity", "All images uploaded successfully")
            // Notify the callback that image upload is complete
            onImageUploadComplete()
        }
    }

    private fun updateGrievanceWithImageUrl(grievanceId: String, imageUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val grievanceRef = db.collection("grievances").document(grievanceId)

        // Update the document by adding the image URL to an array field called 'images'
        grievanceRef.update("images", FieldValue.arrayUnion(imageUrl))
            .addOnSuccessListener {
                Log.d("CreateGrievanceActivity", "Grievance updated with image URL: $imageUrl")
            }
            .addOnFailureListener { e ->
                Log.e("CreateGrievanceActivity", "Error updating grievance with image URL", e)
            }
    }




    private fun updateUserDatabase(userId: String, imageUrl: String) {
        // Update the user's database record with the new image URL
        // You'll need to implement this depending on your database structure
        // For example, if using Firestore:

        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)
        userRef.update("images", FieldValue.arrayUnion(imageUrl))
            .addOnSuccessListener { Log.d("Database", "Image URL added to database") }
            .addOnFailureListener { e -> Log.e("Database", "Error updating database", e) }

    }





    /* private fun uploadImages(grievanceId: String) {
         val storage = FirebaseStorage.getInstance()
         val storageRef = storage.reference

         // Create a reference to the folder in Firebase Storage where you want to store the images
         val imagesRef = storageRef.child("grievance_images/$grievanceId")

         // Upload each image in the selectedImageUris list
         for ((index, imageUri) in selectedImageUris.withIndex()) {
             // Create a reference to the image file
             val imageRef = imagesRef.child("image_$index.jpg")

             // Upload the image file to Firebase Storage
             imageRef.putFile(imageUri)
                 .addOnSuccessListener {
                     Log.d("CreateGrievanceActivity", "Image $index uploaded successfully")
                 }
                 .addOnFailureListener { e ->
                     Log.e("CreateGrievanceActivity", "Error uploading image $index", e)
                 }
         }
     }*/


    private fun fetchFacilities() {
        val facilitiesCollection = FirebaseFirestore.getInstance().collection("admins")

        facilitiesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val facilityList = mutableListOf<String>()

                    // Add a default item to the list
                    facilityList.add(0, "Facility")

                    for (document in querySnapshot) {
                        val facilityName = document.getString("facility")
                        facilityName?.let { facilityList.add(it) }
                    }

                    // Populate the categorySpinner with facilities data
                    populateSpinner(categorySpinner, facilityList)
                } else {
                    // Handle the case when there are no facilities
                    Log.d("GrievancesActivity", "No facilities found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("GrievancesActivity", "Error fetching facilities", e)
            }
    }

    private fun fetchSubfacilitiesForFacility(facility: String) {
        val subfacilitiesCollection = FirebaseFirestore.getInstance().collection("admins")

        subfacilitiesCollection.whereEqualTo("facility", facility)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val subfacilityList = mutableListOf<String>()

                    // Add a default item to the list
                    subfacilityList.add(0, "Sub-Facility")

                    for (document in querySnapshot) {
                        val subfacilityName = document.getString("subFacility")
                        subfacilityName?.let { subfacilityList.add(it) }
                    }

                    // Populate the subcategorySpinner with subfacilities data
                    populateSpinner(subcategorySpinner, subfacilityList)
                } else {
                    // Handle the case when there are no subfacilities for the selected facility
                    Log.d("GrievancesActivity", "No subfacilities found for facility: $facility")
                }
            }
            .addOnFailureListener { e ->
                Log.e("GrievancesActivity", "Error fetching subfacilities", e)
            }
    }

    private fun populateSpinner(spinner: Spinner, data: List<String>) {
        // Use a Set to store unique values
        val uniqueSet = LinkedHashSet<String>(data)

        // Convert the Set back to a List
        val uniqueList = ArrayList<String>(uniqueSet)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, uniqueList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasStoragePermission(): Boolean {
        val readPermissionCheck = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val writePermissionCheck = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return readPermissionCheck == PackageManager.PERMISSION_GRANTED && writePermissionCheck == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                Log.d("CreateGrievanceActivity", "Showing cam options")
                showPhotoSourceOptions()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPhotoSourceOptions() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Option")
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Take Photo") {
                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(
                    takePicture,
                    CAMERA_REQUEST_CODE
                )
            } else if (options[item] == "Choose from Gallery") {
                val pickPhoto =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(
                    pickPhoto,
                    GALLERY_REQUEST_CODE
                )
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    // Method to open the camera
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            // Display error message
        }
    }

    // Method to open the gallery
    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK)
        pickPhotoIntent.type = "image/*"
        pickPhotoIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(pickPhotoIntent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap?
                    imageBitmap?.let {
                        val imageUri = saveImageToGallery(it)
                        imageUri?.let { uri ->
                            selectedImageUris.add(uri)
                            addImageToLayout(uri)
                        }
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    val clipData = data?.clipData
                    if (clipData != null) {
                        // If multiple images are selected
                        for (i in 0 until clipData.itemCount) {
                            val imageUri = clipData.getItemAt(i).uri
                            selectedImageUris.add(imageUri)
                            addImageToLayout(imageUri)
                        }
                    } else {
                        // If a single image is selected
                        data?.data?.let { uri ->
                            selectedImageUris.add(uri)
                            addImageToLayout(uri)
                        }
                    }
                }
            }
        }
    }


    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        var fos: FileOutputStream? = null
        var imageUri: Uri? = null
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        try {
            contentResolver?.also { resolver ->
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) } as FileOutputStream
            }
            fos?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            fos?.flush()
            fos?.close()
        } catch (e: IOException) {
            Log.e("CreateGrievanceActivity", "Error saving image", e)
        }
        return imageUri
    }


    private fun addImageToLayout(imageUri: Uri) {
        val imageView = ImageView(this)
        imageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        imageView.setImageURI(imageUri)
        imageView.adjustViewBounds = true
        imageView.maxWidth = 200  // Set a max width if necessary

        val imagesLayout: LinearLayout = findViewById(R.id.imagesLayout)
        imagesLayout.addView(imageView)
    }

    companion object {
        // Define the request codes
        private const val CAMERA_REQUEST_CODE = 100
        private const val GALLERY_REQUEST_CODE = 101
        private const val CAMERA_PERMISSION_CODE = 100
        private const val PERMISSION_REQUEST_CODE = 102
    }

    override fun onImageUploadComplete() {
        // This method will be called when image upload is complete
        // You can navigate to the next screen or update UI here

        // Remove CreateGrievanceActivity from the stack
        finish()
        // Start GrievancesActivity
        startActivity(Intent(this, UserGrievancesActivity::class.java))
        Log.d("CreateGrievanceActivity", "Image upload complete")
    }
}

interface ImageUploadCallback {
    fun onImageUploadComplete()
}