package com.example.firebase_database

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    private lateinit var edit_name: EditText
    private lateinit var edit_email: EditText
    private lateinit var submit: Button
    private lateinit var logout: Button
    private lateinit var username_textview:TextView



    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            Log.d("neel" , user.toString())
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Log.d("neel" , "sign in failed")
        }
    }



    // See: https://developer.android.com/training/basics/intents/result
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }


    private fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
        // [END auth_fui_create_intent]
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        createSignInIntent()

        Firebase.database.setPersistenceEnabled(true)

        val presenceRef = Firebase.database.getReference("disconnect-message")
        // Write a string when this client loses connection
        presenceRef.onDisconnect().setValue("I disconnected!")

        presenceRef.onDisconnect().removeValue { error, reference ->
            error?.let {
                Log.d("neel", "could not establish onDisconnect event: ${error.message}")
            }
        }




        edit_name = findViewById(R.id.name)
        edit_email = findViewById(R.id.email)
        submit = findViewById(R.id.submitButton)
        logout = findViewById(R.id.logout)
        username_textview = findViewById(R.id.textView)

        database = Firebase.database("https://zendatabase-dfa16-default-rtdb.firebaseio.com/").getReference()
//         database = Firebase.database.reference


        val userListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val post = dataSnapshot.getValue<User>()

                Log.d("neel" , "ValueEventListener : $post")


                if (post != null) {
                    username_textview.setText(post.username.toString())
                }
                // ...
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }


        submit.setOnClickListener(){

                val name = edit_name.text.toString()
                val email = edit_email.text.toString()
                val userid = (0..100).random()

            val user = User(userid,name, email)
            val key = database.child("users").push().key

            if (key == null) {
                Log.w("neel", "Couldn't get push key for posts")
            }

            if (key != null) {
                database.child("users").child(key).setValue(user)
            }


            if (key != null) {
                database.child("users").child(key).addValueEventListener(userListener)
            }

            Toast.makeText(this@MainActivity, "User entered successfully...", Toast.LENGTH_SHORT).show()
        }



          database.child("message").setValue("Hello! Neel Patel")


        val childEventListener = object : ChildEventListener {

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

//                Log.d("neel", "onChildAdded:" + dataSnapshot.key!!)

                // A new User has been added, add it to the displayed list
                val user = dataSnapshot.getValue<User>()
                Log.d("neel", "onChildAdded:$user")
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {

                Log.d("neel", "onChildChanged: ${dataSnapshot.key}")

                // A user has changed, use the key to determine if we are displaying this
                // User and if so displayed the changed comment.
                val newUser = dataSnapshot.getValue<User>()
                val userKey = dataSnapshot.key

                Log.d("neel", "onChildChanged: $newUser")
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d("neel", "onChildRemoved: ${dataSnapshot.getValue<User>()}")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("neel", "onChildRemoved")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "postComments:onCancelled", error.toException())
                Toast.makeText(this@MainActivity,
                    "Failed to load comments.",
                    Toast.LENGTH_SHORT,
                ).show()
            }


        }

        database.child("users").orderByChild("userid").addChildEventListener(childEventListener)



        val connectedRef = Firebase.database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d("neel", "connected")
                } else {
                    Log.d("neel", "not connected")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("neel", "Listener was cancelled")
            }
        })

//        val userLastOnlineRef = Firebase.database.getReference("users/lastOnline")
//        userLastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP)


        logout.setOnClickListener(){

            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    // ...
                    Log.d("neel" , it.toString())
                    onDestroy()
                }

        }


    }
}