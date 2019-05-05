package com.example.egoeu.semka_insta;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.egoeu.semka_insta.Adapters.PrispevkyRecyclerAdapter;
import com.example.egoeu.semka_insta.Cards.CardPrispevok;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class fragmentOblubene extends Fragment {

    private List<CardPrispevok> oblubene_list;
    private RecyclerView prispevky_view_list;
    private PrispevkyRecyclerAdapter prispevkyRecyclerAdapter;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public fragmentOblubene() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_prispevky, container, false);

        oblubene_list = new ArrayList<>();
        prispevky_view_list = view.findViewById(R.id.prispevkylist);

        firebaseAuth = FirebaseAuth.getInstance();

        prispevkyRecyclerAdapter = new PrispevkyRecyclerAdapter(oblubene_list);
        prispevky_view_list.setLayoutManager(new LinearLayoutManager(container.getContext()));
        prispevky_view_list.setAdapter(prispevkyRecyclerAdapter);
        prispevky_view_list.setHasFixedSize(true);

        if (firebaseAuth.getCurrentUser() != null) {

            firebaseFirestore = FirebaseFirestore.getInstance();


            /*
             * prechadzame objekt za objektom a pri kazdom
             * znova volame do databazky kde uz ideme podla jednotliveho prispevku
             */
            Query firstQuery = firebaseFirestore.collection("prispevky").orderBy("countOblubene", Query.Direction.DESCENDING);
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        for (final DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                final String cardPodnikId = doc.getDocument().getId();
                                final CardPrispevok cardPrispevok = doc.getDocument().toObject(CardPrispevok.class).withId(cardPodnikId);

                                firebaseFirestore.collection("prispevky")
                                        .document(cardPodnikId)
                                        .collection("Oblubene")
                                        .document(firebaseAuth.getUid())
                                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                                if (documentSnapshot.exists()) {
                                                    oblubene_list.add(cardPrispevok);



                                                }
                                            }

                                        });




                                prispevkyRecyclerAdapter.notifyDataSetChanged();
                            }
                        }

                    }
                }
            });
        }
        return view;
    }


}
