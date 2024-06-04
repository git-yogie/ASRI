package com.yogie.anemiaapps.fragmentMenu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yogie.anemiaapps.R;
import com.yogie.anemiaapps.helper.UserPref;
import com.yogie.anemiaapps.helper.loadingDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class Fragment_pengaturan extends Fragment {

   Context context;
    TextInputLayout namaLayout, usiaLayout , usernameLayout, passwordLayout;
    TextInputEditText nama, usia, username, password;

    MaterialButton daftarButton, logoutButton;
    FirebaseFirestore db;

    loadingDialog loading;

    UserPref userPref;
    public Fragment_pengaturan(Context context) {

        this.context = context;
        db = FirebaseFirestore.getInstance();
        userPref = new UserPref(context);
        loading = new loadingDialog(context);
        loading.setMessage("Menyimpan pengaturan akun!");


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pengaturan, container, false);


        nama = (TextInputEditText) view.findViewById(R.id.nama_editText);
        usia = (TextInputEditText) view.findViewById(R.id.usia_editText);
        username = (TextInputEditText) view.findViewById(R.id.username_editText);
        password = (TextInputEditText) view.findViewById(R.id.password_editText);


        namaLayout = (TextInputLayout) view.findViewById(R.id.nama_textField);
        usiaLayout = (TextInputLayout) view.findViewById(R.id.usia_textField);
        usernameLayout = (TextInputLayout) view.findViewById(R.id.username_textField);
        passwordLayout = (TextInputLayout) view.findViewById(R.id.password_textField);

        getData();

        daftarButton = (MaterialButton) view.findViewById(R.id.signUp_button);
        daftarButton.setOnClickListener(v -> simpan(view));

        logoutButton = (MaterialButton) view.findViewById(R.id.signOut_button);
        logoutButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Keluar Akun")
                    .setMessage("Anda dapat login lagi lain kali!")
                    .setNegativeButton("Batalkan", (dialog, which) -> {
                        // Respond to negative button press
                    })
                    .setPositiveButton("Keluar Akun", (dialog, which) -> {
                        // Respond to positive button press
                        userPref.logout();
                    }).show();
        });


        // Inflate the layout for this fragment
        return view;
    }

    protected  void getData(){
        db.collection("users").document(Objects.requireNonNull(userPref.getUserData().get("documentId")).toString()).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                nama.setText(task.getResult().getString("nama"));
                usia.setText(task.getResult().getString("usia"));
                username.setText(task.getResult().getString("username"));
            }
        });
    }

    protected void simpan(View view){

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

        if(!namaInput.isEmpty() && !usiaInput.isEmpty() && !usernameInput.isEmpty()) {

            //daftar
            Map<String, Object> userData = new HashMap<>();
            userData.put("nama", namaInput);
            userData.put("usia", usiaInput);
            userData.put("username", usernameInput);
            if(!passwordInput.isEmpty()){
                userData.put("password", passwordInput);
                new AlertDialog.Builder(context)
                        .setTitle("Ubah Password")
                        .setMessage("Anda yakin ingin mengubah password?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                save(userData, view);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }else{
                save(userData, view);
            }

        }

    }

    private void save(Map<String,Object> userData, View view){
        loading.show();
        db.collection("users").document(userPref.getUserData().get("documentId").toString()).update(userData).addOnSuccessListener(documentReference -> {
            loading.dismiss();
            Snackbar snackbar = Snackbar.make(view, "Pengaturan disimpan", Snackbar.LENGTH_SHORT);
            snackbar.show();
            getData();
        }).addOnFailureListener(e -> {
            loading.dismiss();
            Snackbar snackbar = Snackbar.make(view, "Gagal disimpan", Snackbar.LENGTH_SHORT);
            snackbar.show();
        });
    }
}