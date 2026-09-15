package com.aula.tiktoktech;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.tiktoktech.adapter.PostAdapter;
import com.aula.tiktoktech.data.PostRepository;
import com.aula.tiktoktech.model.Post;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static boolean cloudinaryInicializado;
    private PostRepository postRepository;
    private ListenerRegistration postsListener;
    private PostAdapter postAdapter;
    private ProgressBar progress;
    private TextView emptyText;
    private FloatingActionButton newPhotoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progress = findViewById(R.id.progress);
        emptyText = findViewById(R.id.txtVazio);
        newPhotoButton = findViewById(R.id.fabNovaFoto);
        postRepository = new PostRepository();

        setupToolbar();
        setupFeed();
        initializeCloudinary();

        ActivityResultLauncher<PickVisualMediaRequest> photoPicker = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) showCaptionDialog(uri);
                });
        newPhotoButton.setOnClickListener(view -> photoPicker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.getMenu().add(R.string.acao_sair).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() != android.R.id.home) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupFeed() {
        RecyclerView recyclerView = findViewById(R.id.recyclerPosts);
        postAdapter = new PostAdapter(new PostAdapter.Listener() {
            @Override public void onLike(Post post) { updateVote(post, true); }
            @Override public void onDislike(Post post) { updateVote(post, false); }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);
        observePosts();
    }

    private void observePosts() {
        progress.setVisibility(View.VISIBLE);
        postsListener = postRepository.listenPosts(new PostRepository.PostsListener() {
            @Override public void onPostsChanged(List<Post> posts) {
                runOnUiThread(() -> {
                    postAdapter.setPosts(posts);
                    emptyText.setVisibility(posts == null || posts.isEmpty() ? View.VISIBLE : View.GONE);
                    progress.setVisibility(View.GONE);
                });
            }

            @Override public void onError(String message) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    showError(getString(R.string.msg_erro_feed, message));
                });
            }
        });
    }

    private void showCaptionDialog(Uri imageUri) {
        EditText captionInput = new EditText(this);
        captionInput.setHint(R.string.hint_legenda);
        captionInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        captionInput.setMinLines(2);
        captionInput.setPadding(48, 8, 48, 8);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_titulo_legenda)
                .setView(captionInput)
                .setNegativeButton(R.string.acao_cancelar, null)
                .setPositiveButton(R.string.acao_publicar, (dialog, which) -> {
                    String caption = captionInput.getText() == null
                            ? ""
                            : captionInput.getText().toString().trim();
                    if (caption.isEmpty()) {
                        showError(getString(R.string.msg_legenda_vazia));
                    } else {
                        uploadPhoto(imageUri, caption);
                    }
                })
                .show();
    }

    private void uploadPhoto(Uri imageUri, String caption) {
        progress.setVisibility(View.VISIBLE);
        newPhotoButton.setEnabled(false);
        try {
            MediaManager.get().upload(imageUri)
                    .unsigned(getString(R.string.cloudinary_upload_preset))
                    .option("folder", getString(R.string.cloudinary_folder))
                    .callback(new UploadCallback() {
                        @Override public void onStart(String requestId) { }
                        @Override public void onProgress(String requestId, long bytes, long totalBytes) { }
                        @Override public void onReschedule(String requestId, ErrorInfo error) { }

                        @Override public void onSuccess(String requestId, Map resultData) {
                            Object value = resultData == null ? null : resultData.get("secure_url");
                            String url = value instanceof String ? (String) value : "";
                            if (!url.startsWith("https://")) {
                                runOnUiThread(() -> finishOperation(getString(R.string.msg_url_invalida)));
                                return;
                            }
                            postRepository.savePost(url, caption, new PostRepository.OperationCallback() {
                                @Override public void onSuccess() {
                                    runOnUiThread(() -> finishOperation(getString(R.string.msg_post_salvo)));
                                }
                                @Override public void onError(String message) {
                                    runOnUiThread(() -> finishOperation(getString(R.string.msg_erro_salvar, message)));
                                }
                            });
                        }

                        @Override public void onError(String requestId, ErrorInfo error) {
                            runOnUiThread(() -> finishOperation(getString(
                                    R.string.msg_erro_upload,
                                    error == null ? "" : error.getDescription())));
                        }
                    }).dispatch();
        } catch (RuntimeException error) {
            finishOperation(getString(R.string.msg_erro_upload, error.getMessage()));
        }
    }

    private void updateVote(Post post, boolean like) {
        if (post.getId() == null) return;
        postRepository.incrementLike(post.getId(), like, new PostRepository.OperationCallback() {
            @Override public void onSuccess() { }
            @Override public void onError(String message) {
                runOnUiThread(() -> showError(getString(R.string.msg_erro_voto, message)));
            }
        });
    }

    private void initializeCloudinary() {
        if (cloudinaryInicializado) return;
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", getString(R.string.cloudinary_cloud_name));
        config.put("secure", true);
        MediaManager.init(getApplicationContext(), config);
        cloudinaryInicializado = true;
    }

    private void finishOperation(String message) {
        progress.setVisibility(View.GONE);
        newPhotoButton.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (postsListener != null) postsListener.remove();
        super.onDestroy();
    }
}
