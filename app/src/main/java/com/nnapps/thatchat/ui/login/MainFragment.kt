package com.nnapps.thatchat.ui.login

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import com.nnapps.thatchat.R
import com.nnapps.thatchat.databinding.MainFragmentBinding
import com.nnapps.thatchat.models.User

const val TAG = "FirstFragment"
const val RC_SIGN_IN = 123
const val RC_ONE_TAP = 124

class MainFragment : Fragment() {


    private lateinit var loginBinding: MainFragmentBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private var currentUser: FirebaseUser? = null
    private lateinit var database: CollectionReference

    // Control whether user declined One Tap UI
    private var userDeclinedOneTap = false

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        createFirebaseUserObject()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mFirestore = FirebaseFirestore.getInstance()
        mFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        database = mFirestore.collection("Users")
        loginBinding = MainFragmentBinding.inflate(layoutInflater)
        return loginBinding.root
    }



    private fun createFirebaseUserObject() {
        if (currentUser != null) {
            storeUser()
            //If currentUser != null, let's go to the next screen
            //TODO: Write your own logic to update your UI or go to a second Activity/Fragment
        } else {
            //If the user hasn't already declined to use One Tap sign-in
            if (!userDeclinedOneTap) {
                oneTapClient.beginSignIn(signInRequest)
                    .addOnSuccessListener { result ->
                        try {
                            startIntentSenderForResult(
                                result.pendingIntent.intentSender, RC_ONE_TAP,
                                null, 0, 0, 0, null
                            )
                        } catch (e: IntentSender.SendIntentException) {
                            Log.e("LOGIN", "Couldn't start One Tap UI: ${e.localizedMessage}")
                        }


                    }.addOnFailureListener { e ->
                        oneTapClient
                        //No saved credentials found. Launch the One Tap sign-up flow, or
                        //do nothing and continue presenting the signed-out UI.
                        Log.e("LOGIN", "No saved credentials: ${e.localizedMessage}")
                    }
            }
        }
    }

    private fun storeUser() {
        val user = User(currentUser!!.photoUrl!!,
            currentUser!!.displayName!!, currentUser!!.email!!, null, null, null,null)
        val id = currentUser!!.uid
        database.document(id).set({user}).addOnSuccessListener {
            val navController = findNavController()
            navController.navigate(R.id.action_firstFragment_to_chatList)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            RC_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("LOGIN", "Google sign in failed", e)
                    // ...
                }
            }
            // Result returned from launching the Intent from startIntentSenderForResult(...)
            RC_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    // This credential contains a googleIdToken which
                    // we can use to authenticate with FirebaseAuth
                    credential.googleIdToken?.let {
                        firebaseAuthWithGoogle(it)
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            // The user closed the dialog
                            userDeclinedOneTap = true
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            // No Internet connection ?
                        }
                        else -> {
                            // Some other error
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Initialize lateinit vars
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        auth = Firebase.auth

        oneTapClient = Identity.getSignInClient(requireContext())
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()

        // Click listeners for the buttons
        loginBinding.btnSignInGl.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(googleIdToken: String) {
        val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    currentUser = auth.currentUser!!
                    createFirebaseUserObject()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LOGIN", "signInWithCredential:failure", task.exception)
                    Snackbar.make(requireView(),
                        "Authentication Failed.",
                        Snackbar.LENGTH_SHORT).show()
                }
            }
    }
}