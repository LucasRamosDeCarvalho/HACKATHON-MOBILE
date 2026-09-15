package com.aula.tiktoktech;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static boolean cloudinaryInicializado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FloatingActionButton fabNovaFoto = findViewById(R.id.fabNovaFoto);
        ProgressBar progress = findViewById(R.id.progress);
        TextView txtVazio = findViewById(R.id.txtVazio);

        if (!cloudinaryInicializado) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", getString(R.string.cloudinary_cloud_name));
            config.put("secure", true);
            MediaManager.init(getApplicationContext(), config);
            cloudinaryInicializado = true;
        }

        ActivityResultLauncher<PickVisualMediaRequest> selecionarFoto = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(), uri -> uploadPhoto(uri, txtVazio, progress, fabNovaFoto));

        fabNovaFoto.setOnClickListener(view -> selecionarFoto.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
    }

    private void uploadPhoto(Uri uri, TextView txtVazio, ProgressBar progress, FloatingActionButton fabNovaFoto) {
        if (uri == null) return;
        txtVazio.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        fabNovaFoto.setEnabled(false);

        try {
            MediaManager.get().upload(uri)
                    .unsigned(getString(R.string.cloudinary_upload_preset))
                    .option("folder", getString(R.string.cloudinary_folder))
                    .callback(new UploadCallback() {
                        @Override public void onStart(String requestId) { }
                        @Override public void onProgress(String requestId, long bytes, long totalBytes) { }
                        @Override public void onReschedule(String requestId, ErrorInfo error) { }
                        @Override public void onSuccess(String requestId, Map resultData) {
                            Object value = resultData == null ? null : resultData.get("secure_url");
                            String url = value instanceof String ? (String) value : "";
                            runOnUiThread(() -> finishUpload(txtVazio, progress, fabNovaFoto,
                                    url.startsWith("https://") ? url : getString(R.string.msg_url_invalida)));
                        }
                        @Override public void onError(String requestId, ErrorInfo error) {
                            runOnUiThread(() -> finishUpload(txtVazio, progress, fabNovaFoto,
                                    getString(R.string.msg_erro_upload, error == null ? "" : error.getDescription())));
                        }
                    }).dispatch();
        } catch (RuntimeException error) {
            finishUpload(txtVazio, progress, fabNovaFoto, getString(R.string.msg_erro_upload, error.getMessage()));
        }
    }

    private void finishUpload(TextView text, ProgressBar progress, FloatingActionButton button, String message) {
        text.setText(message);
        text.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        button.setEnabled(true);
    }
}
