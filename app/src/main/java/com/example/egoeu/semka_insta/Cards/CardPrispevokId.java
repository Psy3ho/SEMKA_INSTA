package com.example.egoeu.semka_insta.Cards;

import android.support.annotation.NonNull;

public class CardPrispevokId {
    public String CardPrispevokId;

    public <T extends CardPrispevokId> T withId(@NonNull final String id) {
        this.CardPrispevokId = id;
        return (T) this;
    }
}
