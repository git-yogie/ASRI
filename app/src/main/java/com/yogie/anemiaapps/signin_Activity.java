package com.yogie.anemiaapps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yogie.anemiaapps.helper.loadingDialog;
import com.yogie.anemiaapps.menu.notif_activity;

import org.w3c.dom.Text;

public class signin_Activity extends AppCompatActivity {

    TextView daftar;
    TextInputEditText username,password;
    TextInputLayout usernameLayout, passwordLayout;

    MaterialButton loginButton;

    FirebaseFirestore db;

    loadingDialog loading;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        db = FirebaseFirestore.getInstance();
        daftar = (TextView) findViewById(R.id.daftar_textView);
        daftar.setOnClickListener(v -> daftarAct());

        username = (TextInputEditText) findViewById(R.id.username_editText);
        password = (TextInputEditText) findViewById(R.id.password_editText);

        usernameLayout = (TextInputLayout) findViewById(R.id.username_textField);
        passwordLayout = (TextInputLayout) findViewById(R.id.password_textField);

        loginButton = (MaterialButton) findViewById(R.id.signIn_button);
        loginButton.setOnClickListener(v -> loginAct(v));

        loading = new loadingDialog(this);
        loading.setMessage("Loading...");
    }

    protected void daftarAct(){
        Intent intent = new Intent(this, signup_Activity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    protected  void loginAct(View view) {

        String usernameInput = username.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();

        if (usernameInput.isEmpty()) {
            usernameLayout.setError("Username tidak boleh kosong");
        } else {
            usernameLayout.setError(null);
        }

        if (passwordInput.isEmpty()) {
            passwordLayout.setError("Password tidak boleh kosong");
        } else {
            passwordLayout.setError(null);
        }

        if (!usernameInput.isEmpty() && !passwordInput.isEmpty()) {
            //login
            loading.show();

            db.collection("users").whereEqualTo("username", usernameInput).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        //user ditemukan
                        if (task.getResult().getDocuments().get(0).get("password").equals(passwordInput)) {
                            //password benar
                            Snackbar snackbar = Snackbar.make(view, "Login Berhasil", Snackbar.LENGTH_SHORT);

                            DocumentSnapshot userData = task.getResult().getDocuments().get(0);
                            String documentId = userData.getId();

                            SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            editor.putString("documentId", documentId);
                            editor.putString("username", userData.getString("username"));
                            editor.putString("nama", userData.getString("nama"));
                            editor.putString("usia", userData.getString("usia"));
                            editor.putString("role", userData.getString("role"));
                            editor.apply();

                            loading.dismiss();
                            Intent main = new Intent(this, notif_activity.class);
                            startActivity(main);
                            finish();
                        } else {
                            //password salah
                            Snackbar snackbar = Snackbar.make(view, "Password Salah", Snackbar.LENGTH_SHORT);
                            snackbar.show();
                            loading.dismiss();
                        }
                    } else {
                        //user tidak ditemukan
                        Snackbar snackbar = Snackbar.make(view, "Username tidak ditemukan", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        loading.dismiss();
                    }
                } else {
                    //error
                    Snackbar snackbar = Snackbar.make(view, "Terjadi kesalahan", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    loading.dismiss();
                }
            });
        }
    }
}