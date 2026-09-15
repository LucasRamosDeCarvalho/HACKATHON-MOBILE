package com.aula.tiktoktech;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextInputEditText campoEmail;
    private TextInputEditText campoSenha;
    private TextView textoModo;
    private TextView textoAlternar;
    private MaterialButton botaoEntrar;
    private ProgressBar progresso;
    private boolean modoCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            abrirFeed();
            return;
        }

        campoEmail = findViewById(R.id.campoEmail);
        campoSenha = findViewById(R.id.campoSenha);
        textoModo = findViewById(R.id.textoModo);
        textoAlternar = findViewById(R.id.textoAlternar);
        botaoEntrar = findViewById(R.id.botaoEntrar);
        progresso = findViewById(R.id.progressoLogin);

        botaoEntrar.setOnClickListener(view -> autenticar());
        textoAlternar.setOnClickListener(view -> alternarModo());
    }

    private void autenticar() {
        String email = valor(campoEmail);
        String senha = valor(campoSenha);
        campoEmail.setError(null);
        campoSenha.setError(null);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            campoEmail.setError(getString(R.string.erro_email_invalido));
            campoEmail.requestFocus();
            return;
        }
        if (senha.length() < 6) {
            campoSenha.setError(getString(R.string.erro_senha_curta));
            campoSenha.requestFocus();
            return;
        }

        alterarCarregamento(true);
        if (modoCadastro) {
            auth.createUserWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(result -> abrirFeed())
                    .addOnFailureListener(error -> falhaAutenticacao(error.getMessage()));
        } else {
            auth.signInWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(result -> abrirFeed())
                    .addOnFailureListener(error -> falhaAutenticacao(error.getMessage()));
        }
    }

    private void alternarModo() {
        modoCadastro = !modoCadastro;
        textoModo.setText(modoCadastro ? R.string.titulo_cadastro : R.string.titulo_login);
        botaoEntrar.setText(modoCadastro ? R.string.acao_cadastrar : R.string.acao_entrar);
        textoAlternar.setText(modoCadastro ? R.string.acao_voltar_login : R.string.acao_criar_conta);
    }

    private void alterarCarregamento(boolean carregando) {
        progresso.setVisibility(carregando ? View.VISIBLE : View.GONE);
        botaoEntrar.setEnabled(!carregando);
        textoAlternar.setEnabled(!carregando);
    }

    private void falhaAutenticacao(String mensagem) {
        alterarCarregamento(false);
        Toast.makeText(this, getString(R.string.msg_erro_login), Toast.LENGTH_LONG).show();
    }

    private void abrirFeed() {
        startActivity(new android.content.Intent(this, MainActivity.class));
        finish();
    }

    private String valor(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
