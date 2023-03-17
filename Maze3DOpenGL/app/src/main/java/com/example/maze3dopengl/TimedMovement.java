package com.example.maze3dopengl;

import java.util.TimerTask;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class TimedMovement extends TimerTask {

    private float currentX,currentZ,lookingX,lookingZ;
    private float fCurrentX,fCurrentZ,fLookingX,fLookingZ;
    private int currentAngle = 0;
    private int futureAngle;
    private Handler mesHandler;
    private boolean sveglio;
    private Object lock;

    private final int NUM_STEP = 45;
    private int stepCounter = 0;
    private int fullAngle = 0;
    float deltaX = 0;
    float deltaZ = 0;

    public TimedMovement(float cX, float cZ, float lX,float lZ,Handler mesHandler){
        currentX = cX;
        currentZ = cZ;
        lookingX = lX;
        lookingZ = lZ;

        fCurrentX = cX;
        fCurrentZ = cZ;
        fLookingX = lX;
        fLookingZ = lZ;

        futureAngle = 0;
        currentAngle = 0;
        fullAngle = 0;
        deltaX = Math.abs(fCurrentX - currentX);
        deltaZ = Math.abs(fCurrentZ -currentZ);
        this.mesHandler = mesHandler;
        lock = new Object();
        sveglio = true;
    }

    private boolean mustMove(){
        //Identifico i casi in cui non mi muovo, cioè quando le posizioni future e attuali sono uguali
        //O differiscono al più di un epsilon
        float absDiffCurrentX = Math.abs(fCurrentX-currentX);
        float absDiffCurrentZ = Math.abs(fCurrentZ-currentZ);
        float absDiffLookingX = Math.abs(fLookingX - lookingX);
        float absDiffLookingZ = Math.abs(fLookingZ - lookingZ);
        float absDiffAngle = Math.abs(futureAngle - currentAngle);
        float epsilon = 0.01f;
        //Log.v("STATUSMOVEMENT", absDiffCurrentX +" " + absDiffLookingZ + " " + absDiffLookingX + " " + absDiffLookingZ + " " + absDiffAngle);


        if(absDiffCurrentX < epsilon && absDiffCurrentZ < epsilon && absDiffLookingX < epsilon && absDiffLookingZ < epsilon && absDiffAngle < epsilon){
            //Log.v("STATUSMOVEMENT", "Sending message to not move");
            return false;
        }
        Log.v("STATUSMOVEMENT", "Sending message to MOVE");
        return true;
    }

    public void updateFuturePos(float fX, float fZ, float fLX, float fLZ, int fAngle)
    {
        Log.v(" UPDATEFUTUREPOS", "angolo: " + fAngle);
        stepCounter = 0;

        fCurrentX = fX;
        fCurrentZ = fZ;
        fLookingX = fLX;
        fLookingZ = fLZ;
        futureAngle = fAngle;
        fullAngle = futureAngle - currentAngle;
        deltaX = Math.abs(fCurrentX - currentX);
        deltaZ = Math.abs(fCurrentZ -currentZ);
    }
    public void updateCurrentPos(float X, float Z, float LX, float LZ, int Angle){
        Log.v("UPDATECURRENTPOS", "angolo: " + Angle);
        currentX = X;
        currentZ = Z;
        lookingX = LX;
        lookingZ = LZ;
        currentAngle = Angle;
    }

    @Override
    public void run() {

        if(sveglio) {

            if (mustMove() && stepCounter < NUM_STEP-1) {


                //CALCOLO DELLA DIREZIONE
                //Movimento in Avanti/Indietro
                stepCounter +=1;
                //Log.v("STEPCOUNTER"," "+ stepCounter);

                if (deltaX >0.000001f){ //Movimento sull'asseX
                    Log.v("TIMEDMOVEMENT","movimento sull'asseX");
                    if(fCurrentX > currentX){
                        currentX += deltaX/NUM_STEP;
                        lookingX += deltaX/NUM_STEP;
                    }else{
                        currentX -= deltaX/NUM_STEP;
                        lookingX -= deltaX/NUM_STEP;

                    }
                    Log.v("ASSEX",currentX + "  " + lookingX);
                }
                if(deltaZ >  0.000001f){ //Movimento sull'asseZ
                    Log.v("TIMEDMOVEMENT","movimento sull'asseZ");
                    if(fCurrentZ > currentZ){
                        currentZ += deltaZ/NUM_STEP;
                        lookingZ += deltaZ/NUM_STEP;
                    }else{
                        currentZ -= deltaZ/NUM_STEP;
                        lookingZ -= deltaZ/NUM_STEP;

                    }
                    Log.v("ASSEZ",currentZ + "  " + lookingZ + " future " + fLookingZ);
                }
                //Rotazione
                if(Math.abs(futureAngle-currentAngle) > Math.abs(futureAngle)/NUM_STEP){
                    Log.v("TIMEDMOVEMENT","movimento angolare");
                    Log.v("MOVIMENTOANGOLO","PRE: "+currentAngle + " - "+ futureAngle);
                    currentAngle +=Math.ceil(fullAngle/NUM_STEP);
                    rotatePoint(fullAngle/NUM_STEP);

                }

                if(stepCounter == NUM_STEP-1){
                    Log.v("FINE_STEP_COUNTER", "raggiunto il massimo PRE "+ stepCounter + " "+futureAngle+" " + currentAngle);
                    currentAngle = futureAngle;
                    currentX  = fCurrentX;
                    currentZ = fCurrentZ;
                    lookingX = fLookingX;
                    lookingZ = fLookingZ;
                    Log.v("FINE_STEP_COUNTER", "raggiunto il massimo  POST "+ stepCounter + " "+futureAngle+" " + currentAngle);
                }


                Message m = mesHandler.obtainMessage();
                Bundle bundle = m.getData();
                bundle.putFloat("cX",currentX);
                bundle.putFloat("cZ",currentZ);
                bundle.putFloat("lX",lookingX);
                bundle.putFloat("lZ",lookingZ);
                bundle.putInt("tA", currentAngle);

                m.setData(bundle);
                mesHandler.sendMessage(m);

            }
        }else {
            Log.d("TIMEDMOVEMENT","Sleeping");
            synchronized(lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d("TAG","Waking up");
        }


    }

    public void addormenta(){
        sveglio = false;
        mesHandler.removeCallbacksAndMessages(null); //svuota coda di messaggi
        fCurrentZ = currentZ;
        fCurrentX = currentX;
        fLookingX = lookingX;
        fLookingZ = lookingZ;
        futureAngle = currentAngle;
        //posFutura.x = posFutura.y = -1;
    }

    public void dispose() { mesHandler = null; }

    public void sveglia(){
        sveglio = true;
        mesHandler.removeCallbacksAndMessages(null); //svuota la coda di messaggi
        fCurrentZ = currentZ;
        fCurrentX = currentX;
        fLookingX = lookingX;
        fLookingZ = lookingZ;
        futureAngle = currentAngle;
        synchronized(lock){
            lock.notify();
        }
    }

    public void rotatePoint(float angle){
        float x = lookingX;
        float z = lookingZ;
        float  newX,newZ;

        float radiantAngle =  angle  * (float) Math.PI/180;
        float s = (float) Math.sin(radiantAngle);
        float c = (float) Math.cos(radiantAngle);
        //translate to origin
        x = lookingX - currentX;
        z = lookingZ -currentZ;

        //rotate point
        newX = x * c - z * s;
        newZ = x * s + z * c;

        //translate point back
        newX += currentX;
        newZ += currentZ;

        Log.v("ANGOLO_TIMED", "PRE: ("+ lookingX + "," + lookingZ+")");

        lookingX = newX;
        lookingZ = newZ;

        Log.v("ANGOLO_TIMED", "POST ccw: ("+ newX + "," + newZ+")");
    }

}
