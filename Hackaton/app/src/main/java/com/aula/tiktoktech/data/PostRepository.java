package com.aula.tiktoktech.data;

import com.aula.tiktoktech.model.Post;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Collections;
import java.util.List;

/** Único ponto de acesso à coleção de posts do feed. */
public class PostRepository {
    public interface PostsListener {
        void onPostsChanged(List<Post> posts);
        void onError(String message);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public ListenerRegistration listenPosts(PostsListener listener) {
        return firestore.collection("POSTS_2E")
                .orderBy("criadoEm", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        listener.onError(error.getMessage());
                        return;
                    }
                    listener.onPostsChanged(snapshot == null
                            ? Collections.emptyList()
                            : snapshot.toObjects(Post.class));
                });
    }

    public void savePost(String url, String descricao, OperationCallback callback) {
        firestore.collection("POSTS_2E")
                .add(new Post(url, descricao))
                .addOnSuccessListener(document -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onError(error.getMessage()));
    }

    public void incrementLike(String postId, boolean like, OperationCallback callback) {
        String field = like ? "likes" : "dislikes";
        firestore.collection("POSTS_2E").document(postId)
                .update(field, FieldValue.increment(1))
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(error -> callback.onError(error.getMessage()));
    }
}
