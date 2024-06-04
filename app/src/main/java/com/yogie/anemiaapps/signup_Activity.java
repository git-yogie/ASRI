package com.yogie.anemiaapps;

import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yogie.anemiaapps.helper.loadingDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class signup_Activity extends AppCompatActivity {

    TextInputLayout namaLayout, usiaLayout , usernameLayout, passwordLayout;
    TextInputEditText nama, usia, username, password;

    TextView masuk;
    MaterialButton daftarButton;
    loadingDialog loading;
    FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = FirebaseFirestore.getInstance();

        nama = (TextInputEditText) findViewById(R.id.nama_editText);
        usia = (TextInputEditText) findViewById(R.id.usia_editText);
        username = (TextInputEditText) findViewById(R.id.username_editText);
        password = (TextInputEditText) findViewById(R.id.password_editText);


        namaLayout = (TextInputLayout) findViewById(R.id.nama_textField);
        usiaLayout = (TextInputLayout) findViewById(R.id.usia_textField);
        usernameLayout = (TextInputLayout) findViewById(R.id.username_textField);
        passwordLayout = (TextInputLayout) findViewById(R.id.password_textField);

        masuk = (TextView) findViewById(R.id.masuk_textView);
        masuk.setOnClickListener(v -> masukAct());

        daftarButton = (MaterialButton) findViewById(R.id.signUp_button);
        daftarButton.setOnClickListener(v -> daftarAct(v));

        loading = new loadingDialog(this);
        loading.setMessage("Mendaftarkan Akun....");

    }

    protected void masukAct(){
        finish();

    }
    protected void daftarAct(View view){

        String namaInput = nama.getText().toString().trim();
        String usiaInput = usia.getText().toString().trim();
        String usernameInput = username.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();

        if(namaInput.isEmpty()){
            namaLayout.setError("Nama tidak boleh kosong");
        }
        if(usiaInput.isEmpty()) {
            usiaLayout.setError("Usia tidak boleh kosong");
        }
        if(usernameInput.isEmpty()) {
            usernameLayout.setError("Username tidak boleh kosong");
        }
        if(passwordInput.isEmpty()){
            passwordLayout.setError("Password tidak boleh kosong");
        }

        if(!namaInput.isEmpty() && !usiaInput.isEmpty() && !usernameInput.isEmpty() && !passwordInput.isEmpty()) {
            loading.show();
            //daftar
            Map<String, Object> userData = new HashMap<>();
            userData.put("nama", namaInput);
            userData.put("usia", usiaInput);
            userData.put("username", usernameInput);
            userData.put("password", passwordInput);
            userData.put("role", "user");

            db.collection("users").add(userData).addOnSuccessListener(documentReference -> {
                loading.dismiss();
                Snackbar snackbar = Snackbar.make(view, "Pendaftaran Berhasil", Snackbar.LENGTH_SHORT);
                snackbar.show();
                finish();
            }).addOnFailureListener(e -> {
                loading.dismiss();
                Snackbar snackbar = Snackbar.make(view, "Pendaftaran Gagal", Snackbar.LENGTH_SHORT);
                snackbar.show();
            });
        }

    }


}