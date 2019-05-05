package com.example.egoeu.semka_insta;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Pridaj extends AppCompatActivity {

    private ImageView imageView;
    private Uri imageUri = null;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private String user_id;
    private String currentUserEmail;
    private FirebaseFirestore firebaseFirestore;

    private EditText popis;
    private Button pridajPrispevok;
    ProgressDialog progress;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pridaj);

        //nech nam nezobrazi hned klavesnicu
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        currentUserEmail = firebaseAuth.getCurrentUser().getEmail().toString();

        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        imageView = findViewById(R.id.imageView);
        popis = findViewById(R.id.popis);

        pridajPrispevok =findViewById(R.id.pridajPrispevok);

        progress = new ProgressDialog(Pridaj.this);
        progress.setTitle("Nahráva príspevok");
        progress.setMessage("počkajte prosím...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog



        //pridanie podniku
        pridajPrispevok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


// To dismiss the dialog


                final String popisF = popis.getText().toString();



                    if(!TextUtils.isEmpty(popisF)) {

                        progress.show();


                                //nahranie obrazku do uloziska
                                final String randomName = UUID.randomUUID().toString();
                                if( imageUri != null) {
                                    StorageReference imagePath = storageReference.child("fotkaPrispevku").child(randomName + ".jpg");
                                    imagePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                            if (task.isSuccessful()) {

                                                storeFirestore(task, popisF);
                                                Toast.makeText(Pridaj.this, "Obrázok je nahratý ", Toast.LENGTH_LONG).show();

                                            } else {

                                                String error = task.getException().getMessage();
                                                Toast.makeText(Pridaj.this, "(IMAGE Error) : " + error, Toast.LENGTH_LONG).show();


                                            }
                                        }
                                    });
                                }else {
                                    Task task =null;
                                    storeFirestore(task, popisF);
                                }


                    } else {

                        Toast.makeText(Pridaj.this, "Popis príspevku nieje vyplnený", Toast.LENGTH_LONG).show();

                    }



            }
        });



        //kliknutie na obrazok a pridanie noveho z mobilu volame imagepicker
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

                    if(ContextCompat.checkSelfPermission(Pridaj.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(Pridaj.this, "Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(Pridaj.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {

                        BringImagePicker();

                    }

                } else {

                    BringImagePicker();

                }

            }
        });
    }

    //option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pridaj_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.spat:
                startActivity(new Intent(Pridaj.this, MainActivity.class));
                return true;
            default:
                return false;
        }
    }

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String popisS) {

        Uri download_uri;
        int countOblubene =0;
        Map<String, Object> prispevokMap = new HashMap<>();

        if(task != null) {
            download_uri = task.getResult().getDownloadUrl();
            prispevokMap.put("image", download_uri.toString());
        } else {
            String url = "https://firebasestorage.googleapis.com/v0/b/semka-insta.appspot.com/o/-wLyC7pw.png?alt=media&token=ac72f7cf-d308-44ca-9973-5eb82b15f0b6";
            prispevokMap.put("image", url);
        }
        prispevokMap.put("idPouzivatela", user_id);
        prispevokMap.put("emailPouzivatela", currentUserEmail);
        prispevokMap.put("popis", popisS);
        prispevokMap.put("countOblubene", countOblubene);


        //pridanie mapy podniku do databazi aj s udajmi
        firebaseFirestore.collection("prispevky").add(prispevokMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(task.isSuccessful()){

                    Toast.makeText(Pridaj.this, "Príspevok bol pridaný", Toast.LENGTH_LONG).show();
                    Intent mainIntent = new Intent(Pridaj.this, MainActivity.class);
                    startActivity(mainIntent);
                    progress.dismiss();
                    finish();

                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(Pridaj.this, "(FIRESTORE Chyba) : " + error, Toast.LENGTH_LONG).show();

                }
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

                imageUri = result.getUri();
                imageView.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }
}

