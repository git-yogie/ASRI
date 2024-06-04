package com.yogie.anemiaapps.helper;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.yogie.anemiaapps.R;

import java.util.Objects;

import javax.annotation.Nullable;

public class bottomsheet_fragment extends BottomSheetDialogFragment {

    DocumentSnapshot document;
    TextView waktu_minum,jadwal;
    ImageButton close;
    ImageView bukti,check;
    StorageReference ref;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    String jadwal_minum;
    public bottomsheet_fragment(DocumentSnapshot document,String user_id,String jadwal) {
        // Required empty public constructor
        this.document = document;


        this.jadwal_minum = jadwal;
        ref = storage.getReference().child("images/"+user_id+"/"+document.get("foto"));




    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        bukti = view.findViewById(R.id.bukti);

        ref.getDownloadUrl().addOnSuccessListener(uri -> {
            // Got the download URL for the image
            Picasso.get().load(uri).into(bukti);
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(getContext(), "Gagal memuat gambar!", Toast.LENGTH_SHORT).show();
            }
        });

        waktu_minum = view.findViewById(R.id.waktu_minum);
        close = view.findViewById(R.id.close);
        jadwal = view.findViewById(R.id.jadwal);
        check = view.findViewById(R.id.check);

        check.setAlpha(0.5f);

        waktu_minum.setText(document.getString("created_at_day") + " " + document.getString("created_at_time"));
        jadwal.setText(jadwal_minum);

        close.setOnClickListener(v -> dismiss());
        return view;
    }

}
