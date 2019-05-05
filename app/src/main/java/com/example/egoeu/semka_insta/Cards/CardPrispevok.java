package com.example.egoeu.semka_insta.Cards;



public class CardPrispevok extends CardPrispevokId {



    public String emailPouzivatela, popis, image, idPouzivatela;
    public Integer countOblubene;

    public  CardPrispevok() {};

    public CardPrispevok(String emailPouzivatela, String popis, String image, String idPouzivatela, Integer countOblubene) {
        this.emailPouzivatela = emailPouzivatela;
        this.popis = popis;
        this.image = image;
        this.idPouzivatela = idPouzivatela;
        this.countOblubene = countOblubene;
    }

    public String getEmailPouzivatela() {
        return emailPouzivatela;
    }

    public void setEmailPouzivatela(String emailPouzivatela) {
        this.emailPouzivatela = emailPouzivatela;
    }

    public String getPopis() {
        return popis;
    }

    public void setPopis(String popis) {
        this.popis = popis;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getIdPouzivatela() {
        return idPouzivatela;
    }

    public void setIdPouzivatela(String idPouzivatela) {
        this.idPouzivatela = idPouzivatela;
    }

    public Integer getCountOblubene() {
        return countOblubene;
    }

    public void setCountOblubene(Integer countOblubene) {
        this.countOblubene = countOblubene;
    }
}
