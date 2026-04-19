package de.shopme.test.fakes

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk

fun fakeAuth(uid: String): FirebaseAuth {
    val auth = mockk<FirebaseAuth>(relaxed = true)
    val user = mockk<FirebaseUser>(relaxed = true)

    every { user.uid } returns uid
    every { auth.currentUser } returns user

    return auth
}