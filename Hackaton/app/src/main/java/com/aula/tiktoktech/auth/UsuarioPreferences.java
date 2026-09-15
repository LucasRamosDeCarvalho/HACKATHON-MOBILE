package com.aula.tiktoktech.auth;

import android.content.Context;
import android.content.SharedPreferences;

public final class UsuarioPreferences {
    private static final String NOME_PREFERENCIAS = "usuario_local";
    private static final String CHAVE_USUARIO = "usuario_atual";

    private final SharedPreferences preferencias;

    public UsuarioPreferences(Context context) {
        preferencias = context.getApplicationContext()
                .getSharedPreferences(NOME_PREFERENCIAS, Context.MODE_PRIVATE);
    }

    public String getUsuarioAtual() {
        String usuario = preferencias.getString(CHAVE_USUARIO, null);
        if (usuario == null) {
            return null;
        }

        usuario = usuario.trim();
        return usuario.isEmpty() ? null : usuario;
    }

    public boolean estaIdentificado() {
        return getUsuarioAtual() != null;
    }

    public boolean salvarUsuario(String usuario) {
        if (usuario == null) {
            return false;
        }

        String usuarioNormalizado = usuario.trim();
        if (usuarioNormalizado.isEmpty()) {
            return false;
        }

        preferencias.edit()
                .putString(CHAVE_USUARIO, usuarioNormalizado)
                .apply();
        return true;
    }

    public void removerUsuario() {
        preferencias.edit()
                .remove(CHAVE_USUARIO)
                .apply();
    }
}
