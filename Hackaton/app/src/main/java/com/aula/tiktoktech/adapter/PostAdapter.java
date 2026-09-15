package com.aula.tiktoktech.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.tiktoktech.R;
import com.aula.tiktoktech.model.Post;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

/** Adapter responsável somente por apresentar posts e encaminhar votos. */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    public interface Listener {
        void onLike(Post post);
        void onDislike(Post post);
    }

    private final List<Post> posts = new ArrayList<>();
    private final Listener listener;

    public PostAdapter(Listener listener) { this.listener = listener; }

    public void setPosts(List<Post> newPosts) {
        posts.clear();
        if (newPosts != null) posts.addAll(newPosts);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override public int getItemCount() { return posts.size(); }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView image;
        private final TextView description, likes, dislikes, comments;
        private final ImageButton likeButton, dislikeButton;

        PostViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imgFoto);
            description = itemView.findViewById(R.id.txtDescricao);
            likes = itemView.findViewById(R.id.txtLikes);
            dislikes = itemView.findViewById(R.id.txtDislikes);
            comments = itemView.findViewById(R.id.txtComentarios);
            likeButton = itemView.findViewById(R.id.btnLike);
            dislikeButton = itemView.findViewById(R.id.btnDislike);
        }

        void bind(Post post) {
            description.setText(post.getDescricao());
            likes.setText(String.valueOf(post.getLikes()));
            dislikes.setText(String.valueOf(post.getDislikes()));
            comments.setText(String.valueOf(post.getComentarios()));
            Glide.with(image).load(post.getUrl()).placeholder(R.drawable.fundo_imagem).into(image);
            likeButton.setOnClickListener(view -> listener.onLike(post));
            dislikeButton.setOnClickListener(view -> listener.onDislike(post));
        }
    }
}
