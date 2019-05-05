package com.example.egoeu.semka_insta;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private fragmentPrispevky fragmentPrispevky;
    private fragmentOblubene fragmentOblubene;
    private fragmentMojePrispevky fragmentMojePrispevky;
    private BottomNavigationView mainbottomNav;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        if (!isConnected(MainActivity.this)) buildDialog(MainActivity.this).show();
        else {
            //Log.d("internet","stale sme na sieti");
            setContentView(R.layout.activity_main);
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();

            mainbottomNav = findViewById(R.id.mainBottomNav);

            //testujeme ci je pouzivatel prihlaseny
            if (currentUser == null) {
                startActivity(new Intent(MainActivity.this,Login.class));
            } else {
                fragmentPrispevky = new fragmentPrispevky();
                fragmentOblubene = new fragmentOblubene();
                fragmentMojePrispevky = new fragmentMojePrispevky();


                initializeFragment();
            }


            //menu na spodku na prepinanie fragmentov
            mainbottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.prispevky_container);

                    switch (item.getItemId()) {

                        case R.id.main:

                            replaceFragment(fragmentPrispevky, currentFragment);
                            return true;

                        case R.id.profile:

                            replaceFragment(fragmentMojePrispevky, currentFragment);
                            return true;

                        case R.id.favourite:

                            replaceFragment(fragmentOblubene, currentFragment);
                            return true;

                        default:
                            return false;

                    }

                }
            });
        }
    }

    //odhlasenie
    public void ButtonClick(View view) {
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this,Login.class));

    }


    //ci je mobil pruipojeny k internetu
     public boolean isConnected(MainActivity context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnectedOrConnecting()) {

            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
        else return false;
        } else
        return false;
    }

    //vystrazny dialog
    public AlertDialog.Builder buildDialog(MainActivity c) {

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Žiadne internetové pripojenie");
        builder.setMessage("Potrebujete wi-fi alebo internetovú mobilnú sieť.");

        builder.setPositiveButton("Zavrieť", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                finish();
                return;
            }
        });

        return builder;

    }


    //hlavne menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this,Login.class));
                finish();
                return true;
            case R.id.action_add:
                startActivity(new Intent(MainActivity.this, Pridaj.class));
                return true;
            case R.id.profil:
                Intent profil = new Intent(this, Profil.class);
                profil.putExtra("idPouzivatela", currentUser.getUid());
                this.startActivity(profil);

                return true;

            default:
                return false;
        }
    }


    //inicializacia fragmentov
    private void initializeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.prispevky_container, fragmentPrispevky);
        fragmentTransaction.add(R.id.prispevky_container, fragmentOblubene);
        fragmentTransaction.add(R.id.prispevky_container, fragmentMojePrispevky);
        Bundle bundle =new Bundle();
        bundle.putString("userId",currentUser.getUid());
        fragmentMojePrispevky.setArguments(bundle);
        Log.d("bundle odoslany", currentUser.getUid());
        fragmentTransaction.hide(fragmentMojePrispevky);
        fragmentTransaction.hide(fragmentOblubene);
        fragmentTransaction.detach(fragmentPrispevky);
        fragmentTransaction.attach(fragmentPrispevky);
        fragmentTransaction.commit();

    }

    //prehodenie zobrazovaneho fragmentu
    private void replaceFragment(Fragment fragment, Fragment currentFragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == fragmentPrispevky){
            fragmentTransaction.hide(fragmentMojePrispevky);
            fragmentTransaction.hide(fragmentOblubene);
            fragmentTransaction.show(fragment);
        }

        if(fragment == fragmentMojePrispevky){
            fragmentTransaction.hide(fragmentPrispevky);
            fragmentTransaction.hide(fragmentOblubene);
            fragmentTransaction.show(fragment);
        }

        if(fragment == fragmentOblubene){

            fragmentTransaction.hide(fragmentMojePrispevky);
            fragmentTransaction.hide(fragmentPrispevky);
            fragmentTransaction.show(fragment);
        }
        fragmentTransaction.commit();

    }

}
