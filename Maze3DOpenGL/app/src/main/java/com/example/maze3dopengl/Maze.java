package com.example.maze3dopengl;


import static android.opengl.GLES20.GL_TEXTURE4;
import static android.opengl.GLES30.GL_CCW;
import static android.opengl.GLES30.GL_ARRAY_BUFFER;
import static android.opengl.GLES30.GL_BACK;
import static android.opengl.GLES30.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES30.GL_CULL_FACE;
import static android.opengl.GLES30.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES30.GL_DEPTH_TEST;
import static android.opengl.GLES30.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES30.GL_FLOAT;
import static android.opengl.GLES30.GL_LEQUAL;
import static android.opengl.GLES30.GL_STATIC_DRAW;
import static android.opengl.GLES30.GL_TEXTURE0;
import static android.opengl.GLES30.GL_TEXTURE_2D;
import static android.opengl.GLES30.GL_TRIANGLES;
import static android.opengl.GLES30.GL_UNSIGNED_INT;
import static android.opengl.GLES30.glActiveTexture;
import static android.opengl.GLES30.glBindBuffer;
import static android.opengl.GLES30.glBindTexture;
import static android.opengl.GLES30.glBufferData;
import static android.opengl.GLES30.glClear;
import static android.opengl.GLES30.glCullFace;
import static android.opengl.GLES30.glDepthFunc;
import static android.opengl.GLES30.glDrawElements;
import static android.opengl.GLES30.glEnable;
import static android.opengl.GLES30.glEnableVertexAttribArray;
import static android.opengl.GLES30.glFrontFace;
import static android.opengl.GLES30.glGenBuffers;
import static android.opengl.GLES30.glGetUniformLocation;
import static android.opengl.GLES30.glUniformMatrix4fv;
import static android.opengl.GLES30.glUseProgram;
import static android.opengl.GLES30.glVertexAttribPointer;

import static javax.microedition.khronos.opengles.GL10.GL_TEXTURE9;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.testingcapo10.utils.PlyObject;
import com.example.testingcapo10.utils.ShaderCompiler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.Timer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Maze extends BasicRenderer {

    private int VAO[];
    private int shaderHandle;
    private int shaderHandle2D;
    private int MVPloc;
    private int MVPSquareLoc;

    private float viewM[];
    private float viewMSquare[];

    private float modelM[];
    private float modelMSquare[];
    private float projM[];
    private float projMSquare[];
    private float MVP[];
    private float MVPSquare[];
    private float temp[];
    private float tempSquare[];
    private int[] texObjId;
    private int[] texObjIdPlane;
    private int[] texObjId2D;
    private int texUnit;
    private int texUnitSquare;
    private int texSelectorLoc;
    private int texScalingLoc;
    private int texScalingLocSquare;
    private int drawMode;
    private float dimXSquare;
    private float dimYSquare;
    private float dimZSquare;
    private int triangleAngle;
    private int countFacesToElement;
    private int countFacesToElementPlane;
    private int countFacesToElementSquare;
    private int countFacesToElementTriangle;


    private float swipeX1, swipeX2, swipeY1, swipeY2;

    private float currentX, currentY, currentZ;
    private float lookingX, lookingY, lookingZ;

    private float startingXMap = 5.95f;
    private float startingYMap = 0.05f;
    private float startingZMap = 0f;
    private float posSquareX, posSquareY, posSquareZ;
    static final int MIN_DISTANCE = 150;
    static final int DIM_MAZE = 20;
    static final float STEP = 2f;
    private float initialAngle = 0f;


    private int [][]maze;

    private TimedMovement timedMov;
    private MovementHandler handler;
    private Timer timer;

    private float futureCurrentX, futureCurrentZ;
    private float futureLookingX, futureLookingZ;
    private int futureAngle;



    //public volatile float mAngle;
    public Maze() {
        //super(1,1,1);
        drawMode = GL_TRIANGLES;
        viewM = new float[16];
        modelM = new float[16];
        projM = new float[16];
        MVP = new float[16];
        temp = new float[16];


        viewMSquare = new float[16];
        modelMSquare = new float[16];
        projMSquare = new float[16];
        tempSquare = new float[16];
        MVPSquare = new float[16];

        currentX = 6.0f;
        currentY = 0.0f;
        currentZ = 6.0f;

        lookingX = 6.0f;
        lookingY = 0.0f;
        lookingZ = 6.5f;

        dimXSquare = 0f;
        dimYSquare = 0f;
        dimZSquare = 0f;

        triangleAngle = 0;

        futureCurrentX = currentX;
        futureCurrentZ = currentZ;
        futureLookingX = lookingX;
        futureCurrentZ = lookingZ;
        futureAngle = triangleAngle;

        Matrix.setIdentityM(viewM, 0);
        Matrix.setIdentityM(modelM, 0);
        Matrix.setIdentityM(projM, 0);
        Matrix.setIdentityM(MVP, 0);

        Matrix.setIdentityM(viewMSquare, 0);
        Matrix.setIdentityM(modelMSquare, 0);
        Matrix.setIdentityM(projMSquare, 0);
        Matrix.setIdentityM(MVPSquare, 0);


        //SETTING TIMER THREAD
        timer = new Timer();
        handler = new MovementHandler(this);
        timedMov = new TimedMovement(currentX,currentZ,lookingX,lookingZ,handler);
        timer.scheduleAtFixedRate(timedMov,10,16);
        generateMaze();

    }

    @SuppressLint("ClickableViewAccessibility") //Non viene fatto l'ovveride del OnClick event
    @Override
    public void setContextAndSurface(Context context, GLSurfaceView surface) {
        super.setContextAndSurface(context, surface);

        this.surface.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                //float x = event.getX();
                //float y = event.getY();
                float absDiffCurrentX = Math.abs(futureCurrentX-currentX);
                float absDiffCurrentZ = Math.abs(futureCurrentZ-currentZ);
                float absDiffLookingX = Math.abs(futureLookingX - lookingX);
                float absDiffLookingZ = Math.abs(futureLookingZ - lookingZ);
                float absDiffAngle = Math.abs(futureAngle - triangleAngle);

                switch(event.getAction())
                {

                    case MotionEvent.ACTION_DOWN:
                        swipeX1 = event.getX();
                        swipeY1 = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        swipeX2 = event.getX();
                        swipeY2 = event.getY();
                        float deltaX = swipeX2 - swipeX1;
                        float deltaY = swipeY2 - swipeY1;
                        if(absDiffCurrentX > 0 || absDiffCurrentZ > 0 || absDiffLookingX > 0 || absDiffLookingZ > 0 || absDiffAngle > 0) {
                            Log.v("ONTOUCH","BLOCKED");
                            break;
                        }else{
                            Log.v("ONTOUCH","NON SONO ENTRATO NEL'IF :( " + absDiffCurrentX + " " +
                                    absDiffLookingZ + " "+ absDiffCurrentZ + " " + absDiffAngle);
                        }

                        //considero in caso di swipe obliquo la direzione con delta più grande
                        if (Math.abs(deltaX) > Math.abs(deltaY)){ //direzione orizzontale
                            if (Math.abs(deltaX) > MIN_DISTANCE){
                                // Left to Right swipe action
                                if (swipeX2 > swipeX1)
                                {
                                    Log.v("ONTOUCH","Left to Right");
                                    rotatePoint(90);

                                }else{ //Right to left
                                    Log.v("ONTOUCH","Right to Left");
                                    rotatePoint(-90);

                                }

                            }

                        }else{ //direzione verticale
                            if (Math.abs(deltaY) > MIN_DISTANCE)
                            {
                                // Left to Right swipe action
                                if (swipeY1 > swipeY2)
                                {
                                    Log.v("ONTOUCH","Bottom to Top");
                                    move(true);

                                }else{ //Right to left
                                    Log.v("ONTOUCH","Top to Bottom");
                                    move(false);

                                }

                            }
                        }

                        break;

                }
                Log.v("POSITION","("+currentX+","+currentZ+")"+"=> ("+lookingX+","+lookingZ+")");
                return true;
            }

        });
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        super.onSurfaceChanged(gl10, w, h);
        float aspect = ((float) w) / ((float) (h == 0 ? 1 : h));
        //Matrix.frustumM(projM, 0, -aspect, aspect, -1, 1f, 0.1f, 1);

        Matrix.perspectiveM(projM, 0, 45f, aspect, 0.1f, 100f);
        //Matrix.perspectiveM(projMSquare, 0, 45f, aspect, 0.1f, 100f);
        Matrix.setLookAtM(viewM, 0, currentX, currentY, currentZ,
                lookingX, lookingY, lookingZ,
                0, 1, 0);

        Matrix.orthoM(projMSquare,0,-1f*aspect,1f*aspect,-1,1,-0.1f,100f);

        Matrix.setLookAtM(viewMSquare, 0, 5, 0,  5,
                5f, 0, 4f,
                0, 1, 0);

        dimXSquare =  (1.0f/2.0f)/(DIM_MAZE-1);
        dimYSquare = (1.0f/5.0f)/(DIM_MAZE-1);
        dimZSquare = (1.0f/2.0f)/(DIM_MAZE-1);
        Log.v("DIM_SCREEN", "("+w+","+h+")");
        Log.v("DIMENSIONI"," (" + Float.toString(dimXSquare) + " " + Float.toString(dimYSquare)+ " "+ Float.toString(dimZSquare));
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        super.onSurfaceCreated(gl10, eglConfig);
        Log.v("ONCREATE","INIZIO");
        String vertexSrc = "#version 300 es\n" +
                "\n" +
                "layout(location = 1) in vec3 vPos;\n" +
                "layout(location = 2) in vec2 texCoord;\n" +
                "uniform mat4 MVP;\n" +
                "uniform vec2 texScaling;\n" +
                "out vec2 varyingTexCoord;\n" +
                "\n" +
                "void main(){\n" +
                "varyingTexCoord = texCoord * texScaling;\n" +
                "gl_Position = MVP * vec4(vPos,1);\n" +
                "}";

        String fragmentSrc = "#version 300 es\n" +
                "\n" +
                "precision mediump float;\n" +
                "\n" +
                "uniform sampler2D tex;\n" +
                "in vec2 varyingTexCoord;\n" +
                "out vec4 fragColor;\n" +
                "\n" +
                "void main() {\n" +
                "fragColor = texture(tex,varyingTexCoord);\n" +
                "}";
        String vertexSquareSrc = "#version 300 es\n" +
                "\n" +
                "layout(location = 1) in vec2 vPos;\n" +
                "layout(location = 2) in vec2 texCoord;\n" +
                "uniform mat4 MVPSquareLoc;\n" +
                "uniform vec2 texScaling;\n" +
                "out vec2 varyingTexCoord;\n" +
                "\n" +
                "void main(){\n" +
                "varyingTexCoord = texCoord * texScaling;\n" +
                "gl_Position = MVPSquareLoc * vec4(vPos,0,1);\n" +
                //"gl_Position =  vec4(vPos,0,1);\n" +
                "}";

        String fragmentSquaretSrc ="#version 300 es\n" +
                "\n" +
                "precision mediump float;\n" +
                "\n" +
                "in vec2 varyingTexCoord;\n" +
                "uniform sampler2D tex;\n" +
                "out vec4 fragColor;\n" +
                "\n" +
                "void main() {\n" +
                "fragColor = texture(tex,varyingTexCoord);\n" +
                "}\n";



        Log.v("ONCREATE","PRE PRIMO SHADER");
        shaderHandle = ShaderCompiler.createProgram(vertexSrc, fragmentSrc);
        Log.v("ONCREATE","PRIMO SHADER");
        shaderHandle2D = ShaderCompiler.createProgram(vertexSquareSrc,fragmentSquaretSrc);
        Log.v("ONCREATE"," SHADERS: " + shaderHandle + "  -  " + shaderHandle2D);
        //if(shaderHandle < 0){
        if(shaderHandle < 0|| shaderHandle2D < 0) {
            Log.v(TAG,"Error in shader(s) compile. Check SHADER_COMPILER logcat tag. Exiting");
            System.exit(-1);
        }
       // Log.v("ONCREATE"," SHADERS: " + shaderHandle + "  -  " + shaderHandle2D);
        InputStream is;
        float[] vertices=null;
        int[] indices=null;

        try {
            is = context.getAssets().open("cube.ply");
            PlyObject po = new PlyObject(is);
            po.parse();
            //Log.v("TAG",po.toString());
            vertices = po.getVertices();
            indices = po.getIndices();

        }catch(IOException | NumberFormatException e){
            e.printStackTrace();
        }



        FloatBuffer vertexData =
                ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
        vertexData.put(vertices);
        vertexData.position(0);

        IntBuffer indexData =
                ByteBuffer.allocateDirect(indices.length * Integer.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asIntBuffer();
        indexData.put(indices);
        indexData.position(0);

        countFacesToElement = indices.length;

        int VBO[] = new int[2]; //0: vpos, 1: indices

        glGenBuffers(2, VBO, 0);

        VAO = new int[4]; //one VAO to bind both vpos and color
        // 0: Cubo
        // 1: Piano
        // 2: Quadrato
        // 3: triangolo

        GLES30.glGenVertexArrays(4, VAO, 0);

        GLES30.glBindVertexArray(VAO[0]);
        glBindBuffer(GL_ARRAY_BUFFER, VBO[0]);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * vertexData.capacity(),
                vertexData, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 5*Float.BYTES, 0); //vpos
        //glVertexAttribPointer(2,3,GL_FLOAT, false, 5*Float.BYTES, 3*Float.BYTES); //color/normal
        glVertexAttribPointer(2, 2, GL_FLOAT, false, Float.BYTES*5, 3*Float.BYTES); //texcoord
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBO[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indexData.capacity(), indexData,
                GL_STATIC_DRAW);

        GLES30.glBindVertexArray(0);




        // PIANO
            //mapping vertices to s,t texture coordinates
        float[] verticesPlane = new float[]{
                -1,0,-1, 0,10,
                1,0,1, 10,0,
                1,0,-1, 10,10,
                -1,0,1, 0,0
            };

        int[] indicesPlane=new int[]{
                0, 1, 2,
                0, 3, 1
        };
       
        //Log.v("VERTICES_PLANE", verticesPlane.toString());
       // Log.v("INDICES_PLANE", indicesPlane.toString());
        //plane
        FloatBuffer vertexDataPlane =
                ByteBuffer.allocateDirect(verticesPlane.length * Float.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
        vertexDataPlane.put(verticesPlane);
        vertexDataPlane.position(0);

        IntBuffer indexDataPlane =
                ByteBuffer.allocateDirect(indicesPlane.length * Integer.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asIntBuffer();
        indexDataPlane.put(indicesPlane);
        indexDataPlane.position(0);

        countFacesToElementPlane = indicesPlane.length;

        int VBOPlane[] = new int[2]; //0: vpos, 1: indices

        glGenBuffers(2, VBOPlane, 0);


        GLES30.glBindVertexArray(VAO[1]);
                glBindBuffer(GL_ARRAY_BUFFER, VBOPlane[0]);
                glBufferData(GL_ARRAY_BUFFER, Float.BYTES * vertexDataPlane.capacity(),
                        vertexDataPlane, GL_STATIC_DRAW);
                glVertexAttribPointer(1, 3, GL_FLOAT, false, Float.BYTES*5, 0); //vpos
                glVertexAttribPointer(2, 2, GL_FLOAT, false, Float.BYTES*5, 3*Float.BYTES); //texcoord
                glEnableVertexAttribArray(1);
                glEnableVertexAttribArray(2);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBOPlane[1]);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indexDataPlane.capacity(), indexDataPlane,
                    GL_STATIC_DRAW);

        GLES30.glBindVertexArray(0);
        Log.v("ONCREATE"," PRE SQUARE");

        //SQUARE MINIMAP
        float[] verticesSquare = new float[]{
                -1f, -1f, 1f, 1f,
                1f, -1f,  1f, 0f,
                1f, 1f,   0f, 0f,
                -1f,1f,  0f, 1f
        };

        int[] indicesSquare=new int[]{
                0, 1, 2, //first triangle
                0, 2, 3 //second triangle
        };
            FloatBuffer vertexDataSquare =
                    ByteBuffer.allocateDirect(verticesPlane.length * Float.BYTES)
                            .order(ByteOrder.nativeOrder())
                            .asFloatBuffer();
            vertexDataSquare.put(verticesSquare);
            vertexDataSquare.position(0);

            IntBuffer indexDataSquare =
                    ByteBuffer.allocateDirect(indicesSquare.length * Integer.BYTES)
                            .order(ByteOrder.nativeOrder())
                            .asIntBuffer();
            indexDataSquare.put(indicesSquare);
            indexDataSquare.position(0);

            countFacesToElementSquare = indicesSquare.length;

            int VBOSquare[] = new int[2]; //0: vpos, 1: indices

            glGenBuffers(2, VBOSquare, 0);


            GLES30.glBindVertexArray(VAO[2]);
            glBindBuffer(GL_ARRAY_BUFFER, VBOSquare[0]);
            glBufferData(GL_ARRAY_BUFFER, Float.BYTES * vertexDataSquare.capacity(),
                    vertexDataSquare, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, Float.BYTES*4, 0); //vpos
            glVertexAttribPointer(2, 2, GL_FLOAT, false, Float.BYTES*4, 2*Float.BYTES); //texcoord
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBOSquare[1]);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indexDataSquare.capacity(), indexDataSquare,
                    GL_STATIC_DRAW);

        GLES30.glBindVertexArray(0);

        //END SQUARE

        // TRIANGLE  (PLAYER)

        float[] verticesTriangle = new float[]{
                -0.25f, -1f, 1f, 1f,
                +0.25f, -1f,  1f, 0f,
                +0.25f, -0.75f,   0f, 0f,
                -0.25f, -0.75f,  0f, 1f,

                -0.5f, -0.75f, 1f, 1f,
                0.5f, -0.75f,  1f, 0f,
                -0.5f, -0.25f,   0f, 0f,
                0.5f, -0.25f,   0f, 1f,


                0.25f, -0.25f, 1f, 1f,
                -0.25f, -0.25f,  1f, 0f,
                -0.25f, 0.75f,   0f, 0f,
                0.25f,  0.75f,   0f, 1f,
                 0f, 1f,   0f, 0f,
        };

        int[] indicesTriangle=new int[]{
                0, 1, 3, //first triangle
                1, 2, 3,
                4, 5, 6,
                5, 7, 6,
                9, 8, 10,
                8, 11, 10,
                10, 11, 12,
        };
        FloatBuffer vertexDataTriangle =
                ByteBuffer.allocateDirect(verticesTriangle.length * Float.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asFloatBuffer();
        vertexDataTriangle.put(verticesTriangle);
        vertexDataTriangle.position(0);

        IntBuffer indexDataTriangle =
                ByteBuffer.allocateDirect(indicesTriangle.length * Integer.BYTES)
                        .order(ByteOrder.nativeOrder())
                        .asIntBuffer();
        indexDataTriangle.put(indicesTriangle);
        indexDataTriangle.position(0);

        countFacesToElementTriangle = indicesTriangle.length;

        int VBOTriangle[] = new int[2]; //0: vpos, 1: indices

        glGenBuffers(2, VBOTriangle, 0);


        GLES30.glBindVertexArray(VAO[3]);
        glBindBuffer(GL_ARRAY_BUFFER, VBOTriangle[0]);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * vertexDataTriangle.capacity(),
                vertexDataTriangle, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, Float.BYTES*4, 0); //vpos
        glVertexAttribPointer(2, 2, GL_FLOAT, false, Float.BYTES*4, 2*Float.BYTES); //texcoord
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VBOTriangle[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indexDataTriangle.capacity(), indexDataTriangle,
                GL_STATIC_DRAW);

        GLES30.glBindVertexArray(0);

        //END TRIANGLE

        MVPloc = glGetUniformLocation(shaderHandle, "MVP");
        texUnit = glGetUniformLocation(shaderHandle, "tex");
        texScalingLoc = glGetUniformLocation(shaderHandle,"texScaling");
        Log.v("ONCREATE"," FINE TEX UNIT PLANE0");

        MVPSquareLoc = glGetUniformLocation(shaderHandle2D, "MVPSquareLoc");
        texUnitSquare = glGetUniformLocation(shaderHandle2D, "tex");
        texScalingLocSquare = glGetUniformLocation(shaderHandle2D,"texScaling");
        Log.v("ONCREATE"," FINE TEX UNIT PLANE");
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        BitmapFactory.Options opts = new BitmapFactory.Options(); //Get a bitmap configuration data structure
        opts.inScaled=false; //set to false the “scaling” flag: prevents android to tamper with image size
        //while loading...
        Bitmap bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.dirt,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());

        texObjId = new int[3];
        GLES30.glGenTextures(3, texObjId, 0);
        glBindTexture(GL_TEXTURE_2D, texObjId[0]);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0); // unbinding

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjId[0]);
        glUseProgram(shaderHandle);
        GLES30.glUniform1i(texUnit, 0); //0 because active texture is GL_TEXTURE0.
        GLES30.glUniform2f(texScalingLoc, 1,1); //No scaling...
        glBindTexture(GL_TEXTURE_2D, 0);

        glUseProgram(0);

        bitmap.recycle();

        //DOOR
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.door,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());
        glBindTexture(GL_TEXTURE_2D, texObjId[1]);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0); // unbinding

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjId[1]);
        glUseProgram(shaderHandle);
        GLES30.glUniform1i(texUnit, 1); //0 because active texture is GL_TEXTURE0.
        //glActiveTexture(GL_TEXTURE0+0) and glUniform1i(texUnit,GL_TEXTURE0+0) would be more correct
        GLES30.glUniform2f(texScalingLoc, 1,1); //No scaling...
        glBindTexture(GL_TEXTURE_2D, 0);

        glUseProgram(0);

        bitmap.recycle();

        // EXIT DOOR
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.exit,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());
        //GLES30.glGenTextures(2, texObjId, 0);
        glBindTexture(GL_TEXTURE_2D, texObjId[2]);
        //tex filtering try both "nearest"
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        //try other params "i" for wrapping
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0); // unbinding

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjId[2]);
        glUseProgram(shaderHandle);
        GLES30.glUniform1i(texUnit, 2); //0 because active texture is GL_TEXTURE0.
        //glActiveTexture(GL_TEXTURE0+0) and glUniform1i(texUnit,GL_TEXTURE0+0) would be more correct
        GLES30.glUniform2f(texScalingLoc, 1,1); //No scaling...
        glBindTexture(GL_TEXTURE_2D, 0);

        glUseProgram(0);

        bitmap.recycle();


        //FLOOR
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.grass,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());

        texObjIdPlane = new int[2];
        GLES30.glGenTextures(2, texObjIdPlane, 0);
        glBindTexture(GL_TEXTURE_2D, texObjIdPlane[0]);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0); // unbinding

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjIdPlane[0]);
        glUseProgram(shaderHandle);
        GLES30.glUniform1i(texUnit, 3); //0 because active texture is GL_TEXTURE0.
        //glActiveTexture(GL_TEXTURE0+0) and glUniform1i(texUnit,GL_TEXTURE0+0) would be more correct
        GLES30.glUniform2f(texScalingLoc, 1,1); //No scaling...
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);


        bitmap.recycle();


        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.sky,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());
        Log.v("ONCREATE",Integer.toString(countFacesToElement) + " " + Integer.toString(countFacesToElementPlane));

        glBindTexture(GL_TEXTURE_2D, texObjIdPlane[1]);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0); // unbinding

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjIdPlane[1]);
        glUseProgram(shaderHandle);
        GLES30.glUniform1i(texUnit, 4); //0 because active texture is GL_TEXTURE2.
        //glActiveTexture(GL_TEXTURE0+0) and glUniform1i(texUnit,GL_TEXTURE0+0) would be more correct
        GLES30.glUniform2f(texScalingLoc, 1,1); //No scaling...
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);


        bitmap.recycle();


        //SQUARE 1 MINIMAP
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.sand,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());
        Log.v("ONCREATE",Integer.toString(countFacesToElementSquare) + " " + Integer.toString(countFacesToElementSquare));

        texObjId2D = new int[5];
        GLES30.glGenTextures(5, texObjId2D, 0);

        glBindTexture(GL_TEXTURE_2D, texObjId2D[0]);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0); // unbinding

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjId2D[0]);
        glUseProgram(shaderHandle2D);
        GLES30.glUniform1i(texUnit, 5); //0 because active texture is GL_TEXTURE3.
        //glActiveTexture(GL_TEXTURE0+0) and glUniform1i(texUnit,GL_TEXTURE0+0) would be more correct
        GLES30.glUniform2f(texScalingLoc, 1,1); //No scaling...
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);


        bitmap.recycle();

        // SQUARE 2 MINIMAP
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.gravel,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());

        glBindTexture(GL_TEXTURE_2D, texObjId2D[1]);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0); // unbinding

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjId2D[1]);
        glUseProgram(shaderHandle2D);
        // glUseProgram(shaderHandle2D);
        GLES30.glUniform1i(texUnit, 6); //0 because active texture is GL_TEXTURE3.
        //glActiveTexture(GL_TEXTURE0+0) and glUniform1i(texUnit,GL_TEXTURE0+0) would be more correct
        GLES30.glUniform2f(texScalingLoc, 1,1); //No scaling...
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
        bitmap.recycle();

        // DOOR SQUARE MINIMAP
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.door_minimap,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());

        glBindTexture(GL_TEXTURE_2D, texObjId2D[2]);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0); // unbinding

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjId2D[2]);
        glUseProgram(shaderHandle2D);
        GLES30.glUniform1i(texUnit, 7); //0 because active texture is GL_TEXTURE3.
        //glActiveTexture(GL_TEXTURE0+0) and glUniform1i(texUnit,GL_TEXTURE0+0) would be more correct
        GLES30.glUniform2f(texScalingLoc, 1,1); //No scaling...
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
        bitmap.recycle();

        // EXIT SQUARE MINIMAP
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.exit_minimap,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());

        glBindTexture(GL_TEXTURE_2D, texObjId2D[3]);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0); // unbinding

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjId2D[3]);
        glUseProgram(shaderHandle2D);

        GLES30.glUniform1i(texUnit, 8); //0 because active texture is GL_TEXTURE3.
        //glActiveTexture(GL_TEXTURE0+0) and glUniform1i(texUnit,GL_TEXTURE0+0) would be more correct
        GLES30.glUniform2f(texScalingLoc, 1,1); //No scaling...
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);
        bitmap.recycle();




        // PLAYER TEXTURE
        bitmap = BitmapFactory. decodeResource(context.getResources(),R.drawable.arrow,opts);

        if (bitmap != null)
            Log.v(TAG, "bitmap of size " + bitmap.getWidth() + "x" + bitmap.getHeight() + " loaded " +
                    "with format " + bitmap.getConfig().name());

        glBindTexture(GL_TEXTURE_2D, texObjId2D[4]);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
        GLES30.glTexParameteri(GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        GLES30.glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0); // unbinding

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texObjId2D[4]);
        glUseProgram(shaderHandle2D);
        GLES30.glUniform1i(texUnit, 9); //0 because active texture is GL_TEXTURE3.
        //glActiveTexture(GL_TEXTURE0+0) and glUniform1i(texUnit,GL_TEXTURE0+0) would be more correct
        GLES30.glUniform2f(texScalingLoc, 1,1); //No scaling...
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);

        bitmap.recycle();

        Log.v("DEBUGGING:", "Son arrivato qua");

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //Matrix.setLookAtM(viewM, 0, currentX, currentY, currentZ,  lookingX, lookingY, lookingZ, 0f, 1.0f, 0.0f);
        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, texObjIdPlane[0]);  //FLOOR

        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, texObjIdPlane[1]); // CEILING

        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, texObjId[0]);   //WALL

        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, texObjId[1]); //DOOR

        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, texObjId[2]); // EXIT




        //FLOOR
        glBindTexture(GL_TEXTURE_2D, texObjIdPlane[0]);
        Matrix.multiplyMM(temp, 0, projM, 0, viewM, 0);
        Matrix.setIdentityM(modelM,0);
        Matrix.translateM(modelM,0,DIM_MAZE-1,-1,DIM_MAZE-1);
        Matrix.scaleM(modelM,0,DIM_MAZE,1,DIM_MAZE);
        Matrix.multiplyMM(MVP, 0, temp, 0, modelM, 0);



        glUseProgram(shaderHandle);
        GLES30.glBindVertexArray(VAO[1]);
        glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
        glDrawElements(GL_TRIANGLES, countFacesToElementPlane, GL_UNSIGNED_INT, 0);
        GLES30.glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D,0);
        glUseProgram(0);


        //CEILING
        glBindTexture(GL_TEXTURE_2D, texObjIdPlane[1]);
        Matrix.multiplyMM(temp, 0, projM, 0, viewM, 0);
        Matrix.setIdentityM(modelM,0);
        Matrix.translateM(modelM,0,DIM_MAZE-1,1,DIM_MAZE-1);
        Matrix.rotateM(modelM,0,180,1,0,0);
        Matrix.scaleM(modelM,0,DIM_MAZE,1,DIM_MAZE);
        Matrix.multiplyMM(MVP, 0, temp, 0, modelM, 0);


        glUseProgram(shaderHandle);
        GLES30.glBindVertexArray(VAO[1]);
        glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
        glDrawElements(GL_TRIANGLES, countFacesToElementPlane, GL_UNSIGNED_INT, 0);
        GLES30.glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D,0);
        glUseProgram(0);



        //Maze
        for(int x = 0; x < DIM_MAZE; x+=1){
            for(int y = 0; y < DIM_MAZE; y+=1){
                Matrix.multiplyMM(temp, 0, projM, 0, viewM, 0);

                Matrix.setIdentityM(modelM,0);
                Matrix.translateM(modelM,0,x*2,0,y*2);

                //Matrix.scaleM(modelM,0,1.5f,1,2);
                Matrix.multiplyMM(MVP, 0, temp, 0, modelM, 0);
                if(maze[x][y] == 1){

                    glBindTexture(GL_TEXTURE_2D, texObjId[0]); //WALL
                    glUseProgram(shaderHandle);
                    GLES30.glBindVertexArray(VAO[0]);

                    glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
                    glDrawElements(drawMode, countFacesToElement, GL_UNSIGNED_INT, 0);
                    GLES30.glBindVertexArray(0);
                    glBindTexture(GL_TEXTURE_2D,0);
                    glUseProgram(0);
                }else if(maze[x][y] == 2){

                    glBindTexture(GL_TEXTURE_2D, texObjId[1]); //DOOR

                    glUseProgram(shaderHandle);
                    GLES30.glBindVertexArray(VAO[0]);

                    glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
                    glDrawElements(drawMode, countFacesToElement, GL_UNSIGNED_INT, 0);
                    GLES30.glBindVertexArray(0);
                    glBindTexture(GL_TEXTURE_2D,0);
                    glUseProgram(0);
                }else if(maze[x][y] == 3){

                    glBindTexture(GL_TEXTURE_2D, texObjId[2]); // EXIT

                    glUseProgram(shaderHandle);
                    GLES30.glBindVertexArray(VAO[0]);

                    glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
                    glDrawElements(drawMode, countFacesToElement, GL_UNSIGNED_INT, 0);
                    GLES30.glBindVertexArray(0);
                    glBindTexture(GL_TEXTURE_2D,0);
                    glUseProgram(0);
                }
            }
        }

        glActiveTexture(GL_TEXTURE9);
        glBindTexture(GL_TEXTURE_2D, texObjId2D[1]); //WALL MINIMAP
        glActiveTexture(GL_TEXTURE9);
        glBindTexture(GL_TEXTURE_2D, texObjId2D[2]);// DOOR MINIMAP
        glActiveTexture(GL_TEXTURE9);
        glBindTexture(GL_TEXTURE_2D, texObjId2D[3]); //EXIT MINIMAP
        glActiveTexture(GL_TEXTURE9);
        glBindTexture(GL_TEXTURE_2D, texObjId2D[0]); //FLOOR MINIMAP

        //MiniMap
        for(int x = 0; x < DIM_MAZE; x+=1){
            for(int y = 0; y < DIM_MAZE; y+=1){
                Matrix.multiplyMM(tempSquare, 0, projMSquare, 0, viewMSquare, 0);
                Matrix.setIdentityM(modelMSquare,0);
                Matrix.translateM(modelMSquare,0,startingXMap+(dimXSquare*x*2) ,startingYMap - (dimXSquare*y*2),startingZMap);
                Matrix.scaleM(modelMSquare,0,dimXSquare,dimXSquare,dimXSquare);

                Matrix.multiplyMM(MVPSquare, 0, tempSquare, 0, modelMSquare, 0);
                //Log.v("MINIMAP","Dentro il for");
                if(maze[x][y] == 1){
                    glBindTexture(GL_TEXTURE_2D, texObjId2D[1]); //WALL MINIMAP
                }else if(maze[x][y] == 2){
                    glBindTexture(GL_TEXTURE_2D, texObjId2D[2]);// DOOR MINIMAP
                }else if(maze[x][y]==3){
                    glBindTexture(GL_TEXTURE_2D, texObjId2D[3]); //EXIT MINIMAP
                }else{

                    glBindTexture(GL_TEXTURE_2D, texObjId2D[0]); //FLOOR MINIMAP
                }
                glUseProgram(shaderHandle2D);

                GLES30.glBindVertexArray(VAO[2]);

                glUniformMatrix4fv(MVPSquareLoc, 1, false, MVPSquare, 0);
                glDrawElements(GL_TRIANGLES, countFacesToElementSquare,GL_UNSIGNED_INT,0); //num of indices, not vertices!

                glBindTexture(GL_TEXTURE_2D,0);
                GLES30.glBindVertexArray(0);
                glUseProgram(0);

            }
        }
        //PLAYER
        Matrix.multiplyMM(tempSquare, 0, projMSquare, 0, viewMSquare, 0);
        Matrix.setIdentityM(modelMSquare,0);
        Matrix.translateM(modelMSquare,0,startingXMap+(dimXSquare*currentX) ,startingYMap - (dimXSquare*currentZ),startingZMap);
        Matrix.rotateM(modelMSquare,0,initialAngle - triangleAngle,0,0,1);
        Matrix.scaleM(modelMSquare,0,dimXSquare*2,dimXSquare*1.5f,dimXSquare*1.5f);
        Matrix.multiplyMM(MVPSquare, 0, tempSquare, 0, modelMSquare, 0);

        glActiveTexture(GL_TEXTURE9);
        glBindTexture(GL_TEXTURE_2D, texObjId2D[4]);

        glUseProgram(shaderHandle2D);

        GLES30.glBindVertexArray(VAO[3]);

        glUniformMatrix4fv(MVPSquareLoc, 1, false, MVPSquare, 0);
        glDrawElements(GL_TRIANGLES, countFacesToElementTriangle,GL_UNSIGNED_INT,0); //num of indices, not vertices!

        glBindTexture(GL_TEXTURE_2D,0);
        GLES30.glBindVertexArray(0);
        glUseProgram(0);

    }




    public void rotatePoint(int angle){
        Log.d("ROTATEPOINT","begin function");

        float x = lookingX;
        float z = lookingZ;
        float  newX,newZ;

        //Rotation of 90°
        if(angle>0){
            newX = (currentX+currentZ-lookingZ);
            newZ = (lookingX+currentZ-currentX);

        }else{
            newX = (lookingZ-currentZ) + currentX;
            newZ = -(lookingX-currentX)+currentZ;

        }


        Log.v("ANGOLO_TIMED", "PRE: ("+ lookingX + "," + lookingZ+")");

        futureLookingX = newX;
        futureLookingZ = newZ;

        Log.v("ANGOLO_TIMED", "POST ccw: ("+ newX + "," + newZ+")");


        futureAngle = triangleAngle + angle;
        timedMov.updateCurrentPos(currentX,currentZ,lookingX,lookingZ,triangleAngle);
        timedMov.updateFuturePos(futureCurrentX,futureCurrentZ,futureLookingX,futureLookingZ,futureAngle);
        Log.d("ROTATEPOINT","future Angle: " + futureAngle +" from " + triangleAngle);
    }

    public void move(boolean forward){
        float deltaX = lookingX - currentX;
        float deltaZ = lookingZ - currentZ;
        float predictedZ = Float.MAX_VALUE;
        float predictedX = Float.MAX_VALUE;
        float old_lookingX = lookingX;
        float old_lookingZ = lookingZ;

        if (deltaX != 0){ //X axis
            if (deltaX > 0){ //watching in the right direction
                if(forward){
                    predictedX = currentX + STEP;
                    futureLookingX = lookingX + STEP;
                }else{
                    predictedX = currentX - STEP;
                    futureLookingX = lookingX -  STEP;
                }

            }else{ //watching in the left direction
                if(forward){
                    predictedX = currentX - STEP;
                    futureLookingX = lookingX - STEP;
                }else{
                    predictedX = currentX + STEP;
                    futureLookingX = lookingX + STEP;
                }
            }
        }else if(deltaZ != 0){ //Z axis
            if(deltaZ > 0){ //looking down
                if(forward){
                    predictedZ = currentZ + STEP;
                    futureLookingZ = lookingZ+ STEP;
                }else{
                    predictedZ = currentZ- STEP;
                    futureLookingZ = lookingZ - STEP;}
            }else{ //looking up
                if(forward){
                    predictedZ = currentZ- STEP;
                    futureLookingZ = lookingZ - STEP;
                }else{
                    predictedZ = currentZ + STEP;
                    futureLookingZ = lookingZ + STEP;
                }
            }
        }
        Log.v("COLLISION","collisione detection");
        // COLLISION DETECTION
        boolean end_game = false;
        if (predictedX != Float.MAX_VALUE){
            Log.v("COLLISION","primo if");
            //Only X or Z changes in this function
            if(maze[(int) (predictedX/2)][(int) (currentZ/2)] != 0 ){
                if(maze[(int) (predictedX/2)][(int) (currentZ/2)] == 3 ){
                    end_game = true;
                }
                Log.v("OBSTACLE", "ostacolo sull'asse X, " + (int) Math.floor(predictedX/2) + " " +  currentZ/2 + " " + maze[(int) Math.floor(predictedX/2)][(int) currentZ/2]);

                //reset looking variable there's an obstacle!!
                futureLookingX = old_lookingX;
            }else{
                //No obstacle the character can move
                futureCurrentX = predictedX;
                Log.v("ASSE_MOVIMENTO", "Movimento lungo l'asseX, " + (int) Math.floor(predictedX/2) + " " +  currentZ/2 + " " + maze[(int) Math.floor(predictedX/2)][(int) currentZ/2]);

                timedMov.updateCurrentPos(currentX,currentZ,lookingX,lookingZ,triangleAngle);
                timedMov.updateFuturePos(futureCurrentX,futureCurrentZ,futureLookingX,futureLookingZ,futureAngle);
            }
        }else if(predictedZ != Float.MAX_VALUE){
            Log.v("COLLISION","secondo if");
            if(maze[(int) (currentX/2)][(int) (predictedZ/2)] != 0){
                Log.v("COLLISION","secondo if - true");
                if(maze[(int) (currentX/2)][(int) (predictedZ/2)] == 3 ){
                    end_game = true;
                }
                //reset looking variable there's an obstacle!!
                //Log.v("OBSTACLE", "ostacolo sull'asse Z, " + currentX/2 + " " + Math.ceil(predictedZ/2) + " " + maze[(int) currentX/2][(int) Math.ceil(predictedZ/2)/2]);
                futureLookingZ = old_lookingZ;
            }else{
                Log.v("COLLISION","secondo if - else");
                //No obstacle the character can move
                futureCurrentZ = predictedZ;
                Log.v("COLLISION","secondo if - post assegnazione");
                //Log.v("COLLISION", "Movimento lungo l'asseZ, " + (int) Math.floor(predictedX/2) + " " +  currentZ/2 + " " + maze[(int) Math.floor(predictedX/2)][(int) currentZ/2]);
                timedMov.updateCurrentPos(currentX,currentZ,lookingX,lookingZ,triangleAngle);
                timedMov.updateFuturePos(futureCurrentX,futureCurrentZ,futureLookingX,futureLookingZ,futureAngle);

            }
        }
        if (end_game){
            generateMaze();
        }
    }
    public void generateMaze(){
        maze = new int[DIM_MAZE][DIM_MAZE];
        Random  rn = new Random();
        int startingPositionY = 0,startingPositionX = 0, positionExit = 0;
        boolean leftSide = rn.nextBoolean();
        boolean topSide = rn.nextBoolean();
        int side = rn.nextInt(4)+1;
        positionExit = rn.nextInt(DIM_MAZE-2) +1 ;
        switch (side){
            case 1: //LEFT
                startingPositionX = 0;
                startingPositionY =rn.nextInt(DIM_MAZE-2) +1 ;
                break;
            case 2: //TOP
                startingPositionX = rn.nextInt(DIM_MAZE-2) +1 ;
                startingPositionY = 0;
                break;
            case 3: // RIGHT
                startingPositionX =  DIM_MAZE-1;
                startingPositionY = rn.nextInt(DIM_MAZE-2) +1;
                break;
            case 4: // BOTTOM
                startingPositionX = rn.nextInt(DIM_MAZE-2) +1;
                startingPositionY = DIM_MAZE-1;
                break;

        }


        for(int x = 0; x < DIM_MAZE; x+=1){
            for(int y = 0; y < DIM_MAZE; y+=1){
                 if(rn.nextBoolean()){  //random generation of the maze
                    maze[x][y] = 1;
                }else{
                    maze[x][y] = 0;
                }
            }
        }

        //Border construction and exit way
        int middleTurnPoint = rn.nextInt(DIM_MAZE-2) +1 ;

        for(int x = 0; x < DIM_MAZE; x+=1) {
            for (int y = 0; y < DIM_MAZE; y += 1) {
                //adding the borders to the maze
                if ((x == 0 || x == (DIM_MAZE - 1)) || (y == 0 || y == (DIM_MAZE - 1))) {
                    maze[x][y] = 1;
                }
                //creating a solution for the exit
                switch(side){
                    case 1: //LEFT
                        if( x <= middleTurnPoint && y == startingPositionY ||
                                x >= middleTurnPoint && y == positionExit ||
                                x == middleTurnPoint && y >= startingPositionY && y <= positionExit || //exit higher than entrance
                                x == middleTurnPoint && y <= startingPositionY && y >= positionExit){//exit lower than entrance
                            maze[x][y] = 0;
                        }
                        break;
                    case 2:  // TOP
                        if( y <= middleTurnPoint && x == startingPositionX ||
                                y >= middleTurnPoint && x == positionExit ||
                                y == middleTurnPoint && x >= startingPositionX && x <= positionExit || //exit higher than entrance
                                y == middleTurnPoint && x <= startingPositionX && x >= positionExit){//exit lower than entrance
                            maze[x][y] = 0;
                        }
                        break;
                    case 3:
                        if( x >= middleTurnPoint && y == startingPositionY ||
                                x <= middleTurnPoint && y == positionExit ||
                                x == middleTurnPoint && y >= startingPositionY && y <= positionExit || //exit higher than entrance
                                x == middleTurnPoint && y <= startingPositionY && y >= positionExit){//exit lower than entrance
                            maze[x][y] = 0;
                        }
                        break;
                    case 4:
                        // TOP
                        if( y >= middleTurnPoint && x == startingPositionX ||
                                y <= middleTurnPoint && x == positionExit ||
                                y == middleTurnPoint && x >= startingPositionX && x <= positionExit || //exit higher than entrance
                                y == middleTurnPoint && x <= startingPositionX && x >= positionExit){//exit lower than entrance
                            maze[x][y] = 0;
                        }
                        break;
                }

            }
        }

        //Setting right starting positions
        switch (side){
            case 1: //LEFT
                currentX = (startingPositionX)*2+2;
                currentY = 0.0f;
                currentZ = startingPositionY*2;

                lookingX = (startingPositionX)*2+2.5f;
                lookingY = 0.0f;
                lookingZ = (startingPositionY*2);
                initialAngle = -90;
                maze[0][startingPositionY]  = 2;
                maze[DIM_MAZE-1][positionExit]  = 3;
                break;
            case 2: //TOP
                initialAngle = 180;
                currentX = (startingPositionX)*2;
                currentY = 0.0f;
                currentZ = startingPositionY*2+2;

                lookingX = (startingPositionX)*2f;
                lookingY = 0.0f;
                lookingZ = (startingPositionY*2)+2.5f;

                maze[startingPositionX][0]  = 2;
                maze[positionExit][DIM_MAZE-1]  = 3;
                break;
            case 3: // RIGHT

                currentX = (startingPositionX)*2-2;
                currentY = 0.0f;
                currentZ = startingPositionY*2;

                lookingX = (startingPositionX)*2-2.5f;
                lookingY = 0.0f;
                lookingZ = (startingPositionY*2);
                initialAngle = 90;
                maze[DIM_MAZE-1][startingPositionY]  = 2;
                maze[0][positionExit]  = 3;
                break;
            case 4: // BOTTOM
                initialAngle = 0;
                currentX = (startingPositionX)*2;
                currentY = 0.0f;
                currentZ = startingPositionY*2-2;

                lookingX = (startingPositionX)*2f;
                lookingY = 0.0f;
                lookingZ = (startingPositionY*2)-2.5f;

                maze[startingPositionX][DIM_MAZE-1]  = 2;
                maze[positionExit][0]  = 3;
                break;
        }




        futureCurrentX = currentX;
        futureCurrentZ = currentZ;
        futureLookingX = lookingX;
        futureLookingZ = lookingZ;

        futureAngle  = 0;
        triangleAngle = 0;
        timedMov.updateCurrentPos(currentX,currentZ,lookingX,lookingZ,triangleAngle);
        timedMov.updateFuturePos(futureCurrentX,futureCurrentZ,futureLookingX,futureLookingZ,futureAngle);
        Matrix.setLookAtM(viewM, 0, currentX, currentY, currentZ, lookingX, lookingY, lookingZ, 0f, 1.0f, 0.0f);


    }


    public void updateVisual(float cX, float cZ, float lX, float lZ, int tA){
        currentX = cX;
        currentZ = cZ;
        lookingX = lX;
        lookingZ = lZ;
        if(tA == 360 || tA == -360){ //resetting the angle
            Log.v("UPDATEVISUAL", "reset angolo");
            tA = 0;
            futureAngle = 0;
        }
        triangleAngle = tA;
        Matrix.setLookAtM(viewM, 0, currentX, currentY, currentZ, lookingX, lookingY, lookingZ, 0f, 1.0f, 0.0f);
    }
}
