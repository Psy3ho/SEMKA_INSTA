package com.example.egoeu.semka_insta;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.egoeu.semka_insta.Cards.CardPrispevok;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VyplnProfil extends AppCompatActivity {
    private EditText meno, priezvisko, popis;
    private Uri urlObrazok = null;
    private ImageView profilObrazok;


    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vypln_profil);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        meno = findViewById(R.id.name);
        priezvisko = findViewById(R.id.subname);
        popis = findViewById(R.id.popis);
        profilObrazok = findViewById(R.id.profilObrazok);

        progress = new ProgressDialog(VyplnProfil.this);
        progress.setTitle("Registruje sa používateľ");
        progress.setMessage("počkajte prosím...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        firebaseFirestore = FirebaseFirestore.getInstance();


        Query firstQuery = firebaseFirestore.collection("udaje");
        firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {

                    for (final DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            final String profilId = doc.getDocument().getId();
                            firebaseFirestore.collection("udaje").document(profilId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if (task.isSuccessful()) {

                                        String uid = task.getResult().getString("idPouzivatela");
                                        if(uid.equals(user_id)){
                                            Intent mainIntent = new Intent(VyplnProfil.this, MainActivity.class);
                                            startActivity(mainIntent);
                                        }


                                    } else {
                                        //Firebase Exception
                                    }
                                }
                            });

                        }
                    }

                }
            }
        });



        //kliknutie na obrazok a pridanie noveho z mobilu volame imagepicker
        profilObrazok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(VyplnProfil.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(VyplnProfil.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(VyplnProfil.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        BringImagePicker();

                    }

                } else {

                    BringImagePicker();

                }

            }
        });
    }

//registracia pouzivatela aj s jeho vyplnenými udajmi
    public void registracia(View view) {
        final String menoF = meno.getText().toString();
        final String priezviskoF = priezvisko.getText().toString();
        final String popisF = popis.getText().toString();

        if(!TextUtils.isEmpty(menoF)) {

            if(!TextUtils.isEmpty(priezviskoF)) {

            if(!TextUtils.isEmpty(popisF)) {

                progress.show();

                //nahranie obrazku do uloziska
                final String randomName = UUID.randomUUID().toString();
                if( urlObrazok != null) {
                    StorageReference imagePath = storageReference.child("fotka_profilu").child(randomName + ".jpg");
                    imagePath.putFile(urlObrazok).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {

                                storeFirestore(task, menoF,priezviskoF, popisF);
                                Toast.makeText(VyplnProfil.this, "Obrázok je nahratý ", Toast.LENGTH_LONG).show();

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(VyplnProfil.this, "(IMAGE Error) : " + error, Toast.LENGTH_LONG).show();


                            }
                        }
                    });
                }else {
                    Task task =null;
                    storeFirestore(task, menoF,priezviskoF, popisF);
                }


            } else {

                Toast.makeText(VyplnProfil.this, "Vyplňte popis svojho profilu", Toast.LENGTH_LONG).show();

            }
            } else {

                Toast.makeText(VyplnProfil.this, "Zadajte svoje priezvisko", Toast.LENGTH_LONG).show();

            }

        } else {

            Toast.makeText(VyplnProfil.this, "Zadajte svoje meno", Toast.LENGTH_LONG).show();

        }
    }



    //nahratie obrazku pouzivatela na ulozisko
    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String nameS,String subnameS, String popisS) {

        Uri download_uri;
        int countOblubene =0;
        Map<String, Object> personalMap = new HashMap<>();

        if(task != null) {
            download_uri = task.getResult().getDownloadUrl();
            personalMap.put("image", download_uri.toString());
        } else {
            String url = "https://firebasestorage.googleapis.com/v0/b/semka-insta.appspot.com/o/-wLyC7pw.png?alt=media&token=ac72f7cf-d308-44ca-9973-5eb82b15f0b6";
            personalMap.put("image", url);
        }
        personalMap.put("idPouzivatela",user_id);
        personalMap.put("name", nameS);
        personalMap.put("subname", subnameS);
        personalMap.put("popis", popisS);


        firebaseFirestore.collection("udaje").document(user_id).set(personalMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(VyplnProfil.this, "pouzivatel bol pridaný", Toast.LENGTH_LONG).show();
                Intent mainIntent = new Intent(VyplnProfil.this, MainActivity.class);
                startActivity(mainIntent);
                progress.dismiss();
                finish();
            }

        });



    }


    // nahranie obrazku
    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                urlObrazok = result.getUri();
                profilObrazok.setImageURI(urlObrazok);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }

}
