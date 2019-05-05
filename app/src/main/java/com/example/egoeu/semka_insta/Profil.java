package com.example.egoeu.semka_insta;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.UUID;

public class Profil extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    private ImageView profilovka;
    private Uri imageUri = null;
    private TextView meno;
    private TextView priezvisko;
    private TextView email;
    private TextView popis;

    private StorageReference storage;
    private DatabaseReference databaseRef, mDatabaseUsers;
    private FirebaseUser mCurrentUser;

    private String userId;
    private String currentUserEmail;
    private fragmentMojePrispevky fragmentMojePrispevky;

    private Button ulozit,zrusit;
    private TextView zmenaDialog;
    private EditText editDialog;
    private Dialog dialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance().getReference();

        profilovka = findViewById(R.id.profilovka);
        meno = findViewById(R.id.meno);
        priezvisko = findViewById(R.id.priezvisko);
        email = findViewById(R.id.email);
        popis = findViewById(R.id.popis);

        final String currentUserId =  getIntent().getStringExtra("idPouzivatela");;
        userId= currentUserId;

        currentUserEmail = mAuth.getCurrentUser().getEmail().toString();


        //osetrenie pre bundle ci je nejaky pouzivatel prihlaseny
        if(mAuth.getCurrentUser() != null) {

            fragmentMojePrispevky = new fragmentMojePrispevky();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Bundle bundle =new Bundle();
            bundle.putString("userId",currentUserId);
            fragmentTransaction.add(R.id.prispevky_container, fragmentMojePrispevky);
            fragmentMojePrispevky.setArguments(bundle);
            fragmentTransaction.commit();

        }


        //nacietanie udajov o uzivatelovi z databazi
        firebaseFirestore.collection("udaje").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {

                    String meno = task.getResult().getString("name");
                    String priezvisko = task.getResult().getString("subname");
                    String popis = task.getResult().getString("popis");
                    String obrazok = task.getResult().getString("image");
                    setProfil(meno, priezvisko, popis, obrazok);


                } else {
                    //Firebase Exception
                }
            }
        });


        //zmena progfilovky profilovky mena popisu a priezviska len pre pouzivatela ktory zobrazuje svoj vlastny profil
        if(mAuth.getCurrentUser().getUid().equals(currentUserId)) {
            profilovka.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        if (ContextCompat.checkSelfPermission(Profil.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                            Toast.makeText(Profil.this, "Permission Denied", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(Profil.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                        } else {

                            BringImagePicker();
                        }

                    } else {

                        BringImagePicker();


                    }

                }
            });


            meno.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog = new Dialog(Profil.this);
                    dialog.setContentView(R.layout.dialog_template);
                    ulozit = dialog.findViewById(R.id.save);
                    zrusit = dialog.findViewById(R.id.cancel);
                    editDialog = dialog.findViewById(R.id.changeText);

                    dialog.show();
                    dialog.setTitle("Zadajte nové meno");

                    ulozit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            firebaseFirestore.collection("udaje").document(currentUserId).update("name", editDialog.getText().toString());
                            meno.setText(editDialog.getText().toString());
                            dialog.cancel();
                        }
                    });
                    zrusit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });


                }
            });

            priezvisko.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog = new Dialog(Profil.this);
                    dialog.setContentView(R.layout.dialog_template);
                    ulozit = dialog.findViewById(R.id.save);
                    zrusit = dialog.findViewById(R.id.cancel);
                    editDialog = dialog.findViewById(R.id.changeText);

                    dialog.show();

                    dialog.setTitle("Zadajte nové priezvisko");

                    ulozit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            firebaseFirestore.collection("udaje").document(currentUserId).update("subname", editDialog.getText().toString());
                            priezvisko.setText(editDialog.getText().toString());
                            dialog.cancel();
                        }
                    });
                    zrusit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });


                }
            });

            popis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog = new Dialog(Profil.this);
                    dialog.setContentView(R.layout.dialog_template);
                    dialog.setTitle("");
                    ulozit = dialog.findViewById(R.id.save);
                    zrusit = dialog.findViewById(R.id.cancel);
                    editDialog = dialog.findViewById(R.id.changeText);
                    dialog.setTitle("Zadajte nový popis");

                    dialog.show();

                    ulozit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            firebaseFirestore.collection("udaje").document(currentUserId).update("popis", editDialog.getText().toString());
                            popis.setText(editDialog.getText().toString());
                            dialog.cancel();
                        }
                    });
                    zrusit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.cancel();
                        }
                    });


                }
            });
        }


    }


    //nastavenie atriubutov profilu
    public void setProfil(String menop,String priezviskop,String popisp,String obrazokp){
        meno.setText(menop);
        priezvisko.setText(priezviskop);
        popis.setText(popisp);

        RequestOptions placeholderOption = new RequestOptions();
        placeholderOption = placeholderOption.placeholder(R.drawable.image_placeholder);

        Glide.with(this).applyDefaultRequestOptions(placeholderOption).load(obrazokp).into(profilovka);

    }

    //option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profil_menu, menu);
        return true;
    }



    //menu spet
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.spat:
                final String randomName = UUID.randomUUID().toString();
                if( imageUri != null) {
                    StorageReference imagePath = storage.child("fotkaPrispevku").child(randomName + ".jpg");
                    imagePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {
                                firebaseFirestore.collection("udaje").document(userId).update("image", imageUri.toString());

                                Toast.makeText(Profil.this, "Obrázok je upravený ", Toast.LENGTH_LONG).show();

                            } else {

                                String error = task.getException().getMessage();
                                Toast.makeText(Profil.this, "(IMAGE Error) : " + error, Toast.LENGTH_LONG).show();


                            }
                        }
                    });
                }
                startActivity(new Intent(this, MainActivity.class));
                return true;
            default:
                return false;
        }
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
                profilovka.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }
    }



}
