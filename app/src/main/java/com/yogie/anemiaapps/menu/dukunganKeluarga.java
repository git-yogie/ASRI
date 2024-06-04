package com.yogie.anemiaapps.menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;
import com.yogie.anemiaapps.R;
import com.yogie.anemiaapps.helper.UserPref;
import com.yogie.anemiaapps.helper.loadingDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class dukunganKeluarga extends AppCompatActivity {

    AutoCompleteTextView autoCompleteTextView;
    RadioGroup radioGroup;

    TextInputLayout nama,usia, orangtua, jurusan;
    TextInputEditText nama_editText,usia_editText,orangtua_editText;

    UserPref userPref;
    FirebaseFirestore db;

    MaterialButton button_simpan;

    loadingDialog loadingDialog;
    FloatingActionButton fabCalender;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dukungan_keluarga);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        jurusan = findViewById(R.id.jurusan_textField);
        nama = findViewById(R.id.nama_anak_textField);
        usia = findViewById(R.id.usia_textField);
        orangtua = findViewById(R.id.nama_orangtua_textField);

        nama_editText = findViewById(R.id.nama_anak_editText);
        usia_editText = findViewById(R.id.usia_editText);
        orangtua_editText = findViewById(R.id.nama_orangtua_editText);

        autoCompleteTextView = findViewById(R.id.jurusan_editText);


        radioGroup = findViewById(R.id.radioGroup);
        fabCalender = findViewById(R.id.fabCalendar);
        fabCalender.setOnClickListener(v->{
            Intent intent = new Intent(this, com.yogie.anemiaapps.menu.calendar_activity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        loadingDialog = new loadingDialog(this);
        loadingDialog.setMessage("menyimpan data...");

        userPref = new UserPref(this);
        if(userPref.isLogin()){
            nama_editText.setText(userPref.getUserData().get("nama").toString());
            usia_editText.setText(userPref.getUserData().get("usia").toString());
        }


        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("Dukungan keluarga");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        radioGroup.setOnCheckedChangeListener(this::checkedKelas);

        button_simpan = findViewById(R.id.simpan_button);
        button_simpan.setOnClickListener(this::simpan);

        getAndReplace();

        autoCompleteTextView.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onSupportNavigateUp() {
        finishAfterTransition();
        return true;
    }
    public void simpan(View view){

        String nama = nama_editText.getText().toString();
        String usia = usia_editText.getText().toString();
        String orangtua = orangtua_editText.getText().toString();
        String jurusan = autoCompleteTextView.getText().toString();

        if(nama.isEmpty()){
            this.nama.setError("Nama tidak boleh kosong");
            return;
        } else {
            this.nama.setError(null);
        }

        if(usia.isEmpty()){
            this.usia.setError("Usia tidak boleh kosong");
            return;
        } else {
            this.usia.setError(null);
        }

        if(orangtua.isEmpty()){
            this.orangtua.setError("Nama orangtua tidak boleh kosong");
            return;
        } else {
            this.orangtua.setError(null);
        }

        if(jurusan.isEmpty()){
            this.jurusan.setError("Jurusan tidak boleh kosong");
            return;
        } else {
            this.jurusan.setError(null);
        }

        String namaString = nama_editText.getText().toString();
        String usiaString = usia_editText.getText().toString();
        String orangtuaString = orangtua_editText.getText().toString();
        String jurusanString = autoCompleteTextView.getText().toString();
        String kelasString = radioGroup.getCheckedRadioButtonId() == R.id.kelasX ? "X" : "XI";



        if(!nama.isEmpty() && !usia.isEmpty() && !orangtua.isEmpty() && !jurusan.isEmpty()) {
            this.nama.setError(null);
            this.usia.setError(null);
            this.orangtua.setError(null);
            this.jurusan.setError(null);


            Map<String, Object> data = new HashMap<>();
            data.put("nama", namaString);
            data.put("usia", usiaString);
            data.put("orangtua", orangtuaString);
            data.put("jurusan", jurusanString);
            data.put("kelas", kelasString);

            db.collection("dukunganKeluarga").document(userPref.getUserData().get("documentId").toString()).set(data, SetOptions.merge()).addOnCompleteListener(task -> {
                loadingDialog.show();
                if (task.isSuccessful()) {
                    loadingDialog.dismiss();
                    Snackbar.make(view, "Data berhasil disimpan", Snackbar.LENGTH_SHORT).show();
                } else {
                    loadingDialog.dismiss();
                    Snackbar.make(view, "Data gagal disimpan,", Snackbar.LENGTH_SHORT).show();
                    Log.d("error", "simpan: " + Objects.requireNonNull(task.getException()).getMessage());
                }
            });
        }

    }

    public void checkedKelas(RadioGroup group, int checkedId){
        if(checkedId == R.id.kelasX){
            String[] kelas = {"teknik kimia industri","rekayasa perangkat lunak 1","rekayasa perangkat lunak 2 ","usaha layanan wisata","perhotelan","tata kecantikan kulit dan rambut","desain dan produk busana","akuntansi 1 ","akuntansi 2"};
            setJurusan(kelas);

        } else if(checkedId == R.id.KelasXI){
            String[] kelas = {"kimia industri","rekayasa perangkat lunak 1","rekayasa perangkat lunak 2","usaha perjalanan wisata","perhotelan","tata kecantikan kulit dan rambut","tata busana","akuntansi dan keuangan lembaga 1","akuntansi dan keuangan lembaga 2"};
            setJurusan(kelas);
        }
    }

    public void setJurusan (String[] kelas){
        autoCompleteTextView.setVisibility(View.VISIBLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kelas);
        autoCompleteTextView.setAdapter(adapter);
    }

    private void getAndReplace(){
        db.collection("dukunganKeluarga").document(userPref.getUserData().get("documentId").toString()).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Map<String, Object> data = task.getResult().getData();
                if (data != null) {
                    nama_editText.setText(data.get("nama").toString());
                    usia_editText.setText(data.get("usia").toString());
                    orangtua_editText.setText(data.get("orangtua").toString());
                    autoCompleteTextView.setText(data.get("jurusan").toString());

                    if (data.get("kelas").toString().equals("X")) {
                        radioGroup.check(R.id.kelasX);
                    } else if(data.get("kelas").toString().equals("XI")){
                        radioGroup.check(R.id.KelasXI);
                    }
                }
            }else{

            }

            });
    }

}