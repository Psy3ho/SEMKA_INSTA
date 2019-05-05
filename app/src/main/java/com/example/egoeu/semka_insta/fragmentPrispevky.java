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


public class fragmentPrispevky extends Fragment {

    private List<CardPrispevok> oblubene_list;
    private RecyclerView prispevky_view_list;
    private PrispevkyRecyclerAdapter prispevkyRecyclerAdapter;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    private DocumentSnapshot lastVisible;

    private Boolean isFirstPageFirstLoad = true;

    public fragmentPrispevky() {
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

            //scrollovanie
            prispevky_view_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom) {

                        loadMorePost();

                    }

                }
            });

            /*
             * prechadzame objekt za objektom a pri kazdom
             * znova volame do databazky kde uz ideme podla jednotliveho prispevku
             */
            Query firstQuery = firebaseFirestore.collection("prispevky").orderBy("countOblubene", Query.Direction.DESCENDING).limit(10);
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {
                        if (isFirstPageFirstLoad) {

                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            oblubene_list.clear();

                        }
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String cardPodnikId = doc.getDocument().getId();
                                CardPrispevok cardPrispevok = doc.getDocument().toObject(CardPrispevok.class).withId(cardPodnikId);


                                if (isFirstPageFirstLoad) {

                                    oblubene_list.add(cardPrispevok);
                                    //Toast.makeText(getActivity(), "Nacitava"+ cardPrispevok.getNazov(), Toast.LENGTH_SHORT).show();

                                } else {

                                    oblubene_list.add(0, cardPrispevok);

                                }

                                prispevkyRecyclerAdapter.notifyDataSetChanged();
                            }
                        }

                        isFirstPageFirstLoad = false;
                    }
                }
            });
        }
        return view;
    }

    public void loadMorePost(){

        if(firebaseAuth.getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("prispevky").orderBy("countOblubene", Query.Direction.DESCENDING).startAfter(lastVisible).limit(10);
            nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String cardPrispevokId = doc.getDocument().getId();
                                CardPrispevok cardPrispevok = doc.getDocument().toObject(CardPrispevok.class).withId(cardPrispevokId);

                                oblubene_list.add(cardPrispevok);

                                prispevkyRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
        }
    }
}
