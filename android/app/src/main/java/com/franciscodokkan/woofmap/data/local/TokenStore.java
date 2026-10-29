package com.franciscodokkan.woofmap.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenStore {

    private static final String NOMBRE_FICHERO = "woofmap_sesion";
    private static final String CLAVE_TOKEN = "token";
    private static final String CLAVE_NOMBRE = "nombre";
    private static final String CLAVE_EMAIL = "email";

    private static TokenStore instancia;

    private final SharedPreferences preferencias;

    public TokenStore(SharedPreferences preferencias) {
        this.preferencias = preferencias;
    }

    public static synchronized TokenStore getInstance(Context contexto) {
        if (instancia == null) {
            instancia = new TokenStore(crearPreferencias(contexto.getApplicationContext()));
        }
        return instancia;
    }

    private static SharedPreferences crearPreferencias(Context contexto) {
        try {
            MasterKey claveMaestra = new MasterKey.Builder(contexto)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    contexto,
                    NOMBRE_FICHERO,
                    claveMaestra,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("No se pudo abrir el almacén de sesión", e);
        }
    }

    public void guardarSesion(String token, String nombre, String email) {
        preferencias.edit()
                .putString(CLAVE_TOKEN, token)
                .putString(CLAVE_NOMBRE, nombre)
                .putString(CLAVE_EMAIL, email)
                .apply();
    }

    public String getToken() {
        return preferencias.getString(CLAVE_TOKEN, null);
    }

    public String getNombre() {
        return preferencias.getString(CLAVE_NOMBRE, null);
    }

    public boolean haySesion() {
        return getToken() != null;
    }

    public void limpiar() {
        preferencias.edit().clear().apply();
    }
}
