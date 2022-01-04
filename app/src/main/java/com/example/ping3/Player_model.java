package com.example.ping3;

public class Player_model {

    private String pseudo;
    private String player_id;
    private String email;
    private double x;
    private double y;

    public Player_model(String pseudo, String player_id) {
        this.pseudo = pseudo;
        this.player_id = player_id;
    }


    public Player_model() {
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(String player_id) {
        this.player_id = player_id;
    }

    @Override
    public String toString() {
        return "Player_model{" +
                "pseudo='" + pseudo + '\'' +
                ", player_id=" + player_id +
                '}';
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
