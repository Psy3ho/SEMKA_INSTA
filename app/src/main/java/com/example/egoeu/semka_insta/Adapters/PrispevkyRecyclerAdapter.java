package com.example.egoeu.semka_insta.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.egoeu.semka_insta.Cards.CardPrispevok;
import com.example.egoeu.semka_insta.Profil;
import com.example.egoeu.semka_insta.R;
import com.example.egoeu.semka_insta.R.drawable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.egoeu.semka_insta.R.drawable.favouriteselected;


public class PrispevkyRecyclerAdapter extends RecyclerView.Adapter<PrispevkyRecyclerAdapter.ViewHolder>{

    public List<CardPrispevok> prispevok_list;
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    public PrispevkyRecyclerAdapter(List<CardPrispevok> prispevok_list){

        this.prispevok_list = prispevok_list;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prispevok_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        final String prispevokId = prispevok_list.get(position).CardPrispevokId;
        String email = prispevok_list.get(position).getEmailPouzivatela();
        String popis = prispevok_list.get(position).getPopis();
        String image = prispevok_list.get(position).getImage();
        String userid = prispevok_list.get(position).getIdPouzivatela();
        holder.setDataPrispevku(email,popis,image);

        holder.setKrizik(userid);

        holder.krizik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                firebaseFirestore.collection("prispevky").document(prispevokId).delete();
                                prispevok_list.remove(position);
                                notifyDataSetChanged();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Vymazať príspevok?").setPositiveButton("Áno", dialogClickListener)
                        .setNegativeButton("Nie", dialogClickListener).show();

            }
        });

        holder.email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profil = new Intent(context, Profil.class);
                profil.putExtra("idPouzivatela", prispevok_list.get(position).getIdPouzivatela());
                context.startActivity(profil);
            }
        });

        holder.addFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("prispevky").document(prispevokId).collection("Oblubene").document(firebaseAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.getResult().exists()) {

                            Map<String, Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("prispevky").document(prispevokId).collection("Oblubene").document(firebaseAuth.getUid()).set(likesMap);
                            holder.addFavourite.setImageResource(drawable.favouriteselected);


                        } else {

                            firebaseFirestore.collection("prispevky").document(prispevokId).collection("Oblubene").document(firebaseAuth.getUid()).delete();
                            holder.addFavourite.setImageResource(drawable.favouriteunselected);

                        }
                    }
                });
            }
        });

        firebaseFirestore.collection("prispevky").document(prispevokId).collection("Oblubene").document(firebaseAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {

                    holder.addFavourite.setImageResource(drawable.favouriteselected);

                } else {
                    holder.addFavourite.setImageResource(drawable.favouriteunselected);

                }
            }
        });

        firebaseFirestore.collection("prispevky").document(prispevokId).collection("Oblubene").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {
                    int count = documentSnapshots.size();
                    holder.count.setText(String.valueOf(count));
                    firebaseFirestore.collection("prispevky").document(prispevokId).update("countOblubene", count);

                } else {
                    holder.count.setText(String.valueOf(0));
                    firebaseFirestore.collection("prispevky").document(prispevokId).update("countOblubene", 0);

                }
            }
        });










    }

    @Override
    public int getItemCount() {
        return prispevok_list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView email;
        private TextView popis;
        private TextView count;
        private ImageView addFavourite;

        private ImageView prispevokO;
        private ImageView krizik;



        public ViewHolder(View itemView) {

            super(itemView);
            mView = itemView;

           addFavourite = mView.findViewById(R.id.addFavourite);
           count = mView.findViewById(R.id.countFavourite);



        }

        public void setDataPrispevku(String emailp, String popisp, String prispevokOp){

            email = mView.findViewById(R.id.nazovsingle);
            popis = mView.findViewById(R.id.popissingle);
            prispevokO = mView.findViewById(R.id.obrazoksingle);

            email.setText(emailp);
            popis.setText(popisp);
           // Log.d("skuska: ", emailpa+ popisp +prispevokOp);

            RequestOptions placeholderOption = new RequestOptions();
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(prispevokOp).into(prispevokO);

        }

        public void setKrizik(String prispevokUID){
            krizik = mView.findViewById(R.id.krizikO);
            if(prispevokUID.equals(firebaseAuth.getCurrentUser().getUid())){
                krizik.setVisibility(View.VISIBLE);
            }
        }



    }

}
