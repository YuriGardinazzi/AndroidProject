package com.example.maze3dopengl;


import android.os.Handler;
import android.os.Message;
import android.util.Log;

class MovementHandler extends Handler {

    private Maze game;

    public MovementHandler(Maze game) {
        Log.d("MOVEMENT_HANDLER","istanziato");
        this.game = game;
    }

    public void dispose(){
        game = null;
    }

    @Override
    public void handleMessage(Message msg) {

        if(game==null) return;

        if(msg!=null & msg.getData()!=null){
            Log.d("MOVEMENT_HANDLER","callingupdate visual");
            game.updateVisual(msg.getData().getFloat("cX"),
                              msg.getData().getFloat("cZ"),
                              msg.getData().getFloat("lX"),
                              msg.getData().getFloat("lZ"),
                              msg.getData().getInt("tA"));
        }
    }
}
