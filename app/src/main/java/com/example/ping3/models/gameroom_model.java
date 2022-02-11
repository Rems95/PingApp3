package com.example.ping3.models;

import java.util.ArrayList;

public class gameroom_model {


    private ArrayList<Player_model> Players= new ArrayList<Player_model>();
    private String creator;
    private Integer roomId;
    private Integer status;
    private Integer nbPlayers;
    private Integer time;
    private String level;
    private Integer refreshTime;

    public gameroom_model(ArrayList<Player_model> players, String creator, Integer roomId, Integer status, Integer nbPlayers, Integer time, String level) {
        Players = players;
        this.creator = creator;
        this.roomId = roomId;
        this.status = status;
        this.nbPlayers = nbPlayers;
        this.time = time;
        this.level = level;
    }


    public gameroom_model() {
    }

    public Integer getNbPlayers() {
        return nbPlayers;
    }

    public void setNbPlayers(Integer nbPlayers) {
        this.nbPlayers = nbPlayers;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCreator() {
        return creator;
    }



    public void setCreator(String creator) {
        this.creator = creator;
    }



    public Integer getRoomId() {
        return roomId;
    }



    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }



    public Integer getStatus() {
        return status;
    }



    public void setStatus(Integer status) {
        this.status = status;
    }



    public ArrayList<Player_model> getPlayers() {
        return Players;
    }

    public void addPlayers(Player_model object) {
        Players.add(object);
    }
    public void setPlayers(ArrayList<Player_model> players) {
        Players = players;
    }

    @Override
    public String toString() {
        return "gameroom_model{" +
                "Players=" + Players +
                ", creator='" + creator + '\'' +
                ", roomId=" + roomId +
                ", status=" + status +
                '}';
    }

    public Integer getRefreshTime() {
        return refreshTime;
    }

    public void setRefreshTime(Integer refreshTime) {
        this.refreshTime = refreshTime;
    }
}
