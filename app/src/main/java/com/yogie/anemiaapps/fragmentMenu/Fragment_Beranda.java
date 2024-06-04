package com.yogie.anemiaapps.fragmentMenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.yogie.anemiaapps.R;
import com.yogie.anemiaapps.helper.UserPref;
import com.yogie.anemiaapps.helper.fyi_activity;
import com.yogie.anemiaapps.menu.calendar_activity;
import com.yogie.anemiaapps.menu.remajaPutri_Activity;
import com.yogie.anemiaapps.menu.dukunganKeluarga;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Fragment_Beranda extends Fragment {

    MaterialCardView remajaPutri,fyi,kalender, dukunganKeluargas;

    TextView gretter;
    Context context;

    UserPref userPref;

    ImageView signOut;

    public Fragment_Beranda(Context context) {
        // Required empty public constructor
        this.context = context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment__beranda, container, false);
        userPref = new UserPref(context);

        gretter = view.findViewById(R.id.textView_Gretter);
        gretter.setText("Hai, " + userPref.getUserData().get("nama").toString() + "!");


        remajaPutri = view.findViewById(R.id.remajaPutri);
        fyi = view.findViewById(R.id.fyi);
        kalender = view.findViewById(R.id.kalender);
        dukunganKeluargas = view.findViewById(R.id.dukunganKeluarga);
        signOut = view.findViewById(R.id.logOut);

        remajaPutri.setOnClickListener(this::setRemajaPutri);
        fyi.setOnClickListener(this::setfyi);
        kalender.setOnClickListener(this::setKalender);
        dukunganKeluargas.setOnClickListener(this::setDukunganKeluarga);

        signOut.setOnClickListener(v -> {
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

        return view;
    }

    private void setRemajaPutri(View view){
        Intent intent = new Intent(context, remajaPutri_Activity.class);
        context.startActivity(intent);
    }

    private void setfyi(View view){
        Intent intent = new Intent(context, fyi_activity.class);
        context.startActivity(intent);
    }

    private void setKalender(View view){
        Intent intent = new Intent(context, calendar_activity.class);
        context.startActivity(intent);
    }

    private void setDukunganKeluarga(View view){
        Intent intent = new Intent(context, dukunganKeluarga.class);
        context.startActivity(intent);
    }
}