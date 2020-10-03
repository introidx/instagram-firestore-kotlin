package com.introid.instagram_firestore

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.introid.instagram_firestore.models.Post
import com.introid.instagram_firestore.models.User
import kotlinx.android.synthetic.main.activity_create.*

private const val PICK_PHOTO_CODE=1

class CreateActivity : AppCompatActivity() {

    private var photoUri : Uri? = null
    private var signedInUser: User? = null
    private lateinit var firebaseDb : FirebaseFirestore
    private lateinit var storageRef : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        storageRef = FirebaseStorage.getInstance().reference

        firebaseDb = FirebaseFirestore.getInstance()
        firebaseDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)

            }.addOnFailureListener{exception ->
                Toast.makeText(this, exception.message , Toast.LENGTH_SHORT).show()
            }

        pickImage.setOnClickListener {
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if (imagePickerIntent.resolveActivity(packageManager) != null){
                startActivityForResult(imagePickerIntent , PICK_PHOTO_CODE)
            }
        }

        btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }
    }

    private fun handleSubmitButtonClick() {
        if (photoUri == null || tvDescription.text.isEmpty()){
            Toast.makeText(this, "Image And Description Cant Be blank ", Toast.LENGTH_SHORT).show()
            return
        }
        if (signedInUser == null){
            Toast.makeText(this, "No Signed In User" , Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false

        val photoUploadUri = photoUri as Uri
        val photoReference= storageRef.child("images/${System.currentTimeMillis()}-photo.jpg")

        photoReference.putFile(photoUploadUri)
            .continueWithTask{photoUploadTask ->
                photoReference.downloadUrl
            }.continueWithTask{ downloadUrlTask ->
                val post = Post(
                    tvDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser)
                    firebaseDb.collection("posts").add(post)

            }.addOnCompleteListener{ postCreationTask ->
                btnSubmit.isEnabled = true
                if (!postCreationTask.isSuccessful){
                    Toast.makeText(this, "Failed To Upload" , Toast.LENGTH_SHORT).show()

                }
                tvDescription.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this, "Success" , Toast.LENGTH_SHORT).show()

                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME , signedInUser?.username)
                startActivity(profileIntent)
                finish()


            }



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_CODE && resultCode == Activity.RESULT_OK){
            photoUri = data?.data
            imageView.setImageURI( photoUri)
        }
    }
}