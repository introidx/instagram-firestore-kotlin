package com.introid.instagram_firestore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.collection.LLRBBlackValueNode
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.introid.instagram_firestore.models.Post
import com.introid.instagram_firestore.models.User
import kotlinx.android.synthetic.main.activity_posts.*

const val TAG = "ProfileActivity"
public const val EXTRA_USERNAME = "EXTRA_USERNAME"

open class PostsActivity : AppCompatActivity() {

    private var signedInUser: User? = null
    private lateinit var firebaseDb : FirebaseFirestore
    private lateinit var posts : MutableList<Post>
    private lateinit var adapter : PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posts)

        posts = mutableListOf()
        adapter = PostAdapter(this, posts)
        rvPosts.adapter = adapter
        rvPosts.layoutManager = LinearLayoutManager(this)

        firebaseDb = FirebaseFirestore.getInstance()
        firebaseDb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)

            }.addOnFailureListener{exception ->
                Toast.makeText(this, exception.message , Toast.LENGTH_SHORT).show()
            }


        var postReference = firebaseDb
            .collection("posts")
            .limit(20)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        if (username != null){
            supportActionBar?.title = username
            postReference = postReference.whereEqualTo("user.username" , username)
        }

        postReference.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null){
                Log.d(TAG, "onCreate: ", exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()

            for (post in postList){
                Log.d(TAG, "onCreate: Post : $post")

            }
        }

        fabCreate.setOnClickListener{
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_profile){
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

}