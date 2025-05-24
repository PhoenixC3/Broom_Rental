package com.example.hogwartshoppers.viewmodels

import com.example.hogwartshoppers.model.Posts
import com.example.hogwartshoppers.model.Replies
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase

sealed interface ForumUIState {
    data class Success(val posts: List<Posts>) : ForumUIState
    object Error : ForumUIState
    object Loading : ForumUIState
}

class ForumViewModel: ViewModel() {

    private val db: FirebaseDatabase = FirebaseDatabase.getInstance()
    val postsRef = db.reference.child("Posts")
    val repliesRef = db.reference.child("Replies")

    init {

    }

    // criar um post os titulos
    fun createPost(userEmail: String, title: String, text: String) {
        postsRef.get().addOnSuccessListener { snapshot ->
            // Create a new user entry
            val post = Posts(
                userEmail = userEmail,
                title = title,
                text = text
            )
            postsRef.push().setValue(post)
        }
    }

    fun deletePost(userEmail: String,title: String) {
        postsRef.get().addOnSuccessListener { snapshot ->
            for (allPosts in snapshot.children) {
                val post = Posts(
                    userEmail = allPosts.child("userEmail").value as? String ?: "",
                    title = allPosts.child("title").value as? String ?: "",
                    text = allPosts.child("text").value as? String ?: ""
                )
                if (post.userEmail == userEmail && post.title == title) {
                    allPosts.ref.removeValue()
                }
            }
        }
    }

    // funcao para buscar todos os posts
    fun getPosts(callback: (List<Posts>?) -> Unit) {
        postsRef.get().addOnSuccessListener { snapshot ->
            val postsList = mutableListOf<Posts>()
            for (allPosts in snapshot.children) {
                val post = Posts(
                    userEmail = allPosts.child("userEmail").value as? String ?: "",
                    title = allPosts.child("title").value as? String ?: "",
                    text = allPosts.child("text").value as? String ?: ""
                )
                postsList.add(post) // Add the post to the list
            }
            callback(postsList) // Return the list via callback
        }
    }

    fun getPostsByUser(userEmail: String, callback: (List<Posts>?) -> Unit) {
        postsRef.get().addOnSuccessListener { snapshot ->
            val postsList = mutableListOf<Posts>()
            for (allPosts in snapshot.children) {
                val post = Posts(
                    userEmail = allPosts.child("userEmail").value as? String ?: "",
                    title = allPosts.child("title").value as? String ?: "",
                    text = allPosts.child("text").value as? String ?: ""
                )
                if (post.userEmail == userEmail) {
                    postsList.add(post)
                }
            }
            callback(postsList)
        }
    }

    // funcao para criar uma reply
    fun createReply(userEmail: String, title: String, text: String) {
        repliesRef.get().addOnSuccessListener { snapshot ->
            // Create a new user entry
            val reply = Replies(
                userEmail = userEmail,
                title = title,
                text = text
            )
            repliesRef.push().setValue(reply)
        }
    }

    // funcao para dar get das replies de um title
    fun getReplies(title: String, callback: (List<Replies>?) -> Unit) {
        repliesRef.get().addOnSuccessListener { snapshot ->
            val repliesList = mutableListOf<Replies>()
            for (allReplies in snapshot.children) {
                val reply = Replies(
                    userEmail = allReplies.child("userEmail").value as? String ?: "",
                    title = allReplies.child("title").value as? String ?: "",
                    text = allReplies.child("text").value as? String ?: ""
                )
                if (reply.title == title) {
                    repliesList.add(reply)
                }
            }
            callback(repliesList)
        }
    }

    // funcao para dar delete das replies de um title
    fun deleteReplies(title: String) {
        repliesRef.get().addOnSuccessListener { snapshot ->
            for (allReplies in snapshot.children) {
                val reply = Replies(
                    userEmail = allReplies.child("userEmail").value as? String ?: "",
                    title = allReplies.child("title").value as? String ?: "",
                    text = allReplies.child("text").value as? String ?: ""
                )
                if (reply.title == title) {
                    allReplies.ref.removeValue()
                }
            }
        }
    }

    // funcao para dar retrieve de TODAS as replies
    fun getAllReplies(callback: (List<Replies>?) -> Unit) {
        repliesRef.get().addOnSuccessListener { snapshot ->
            val repliesList = mutableListOf<Replies>()
            for (allReplies in snapshot.children) {
                val reply = Replies(
                    userEmail = allReplies.child("userEmail").value as? String ?: "",
                    title = allReplies.child("title").value as? String ?: "",
                    text = allReplies.child("text").value as? String ?: ""
                )
                repliesList.add(reply)
            }
            callback(repliesList)
        }
    }
}