/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.vrtoolkit.cardboard.samples.treasurehunt;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.google.vrtoolkit.cardboard.audio.CardboardAudioEngine;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;

import jp.co.altec.openingactionsample.DataControl;
import jp.co.altec.openingactionsample.DeviceInfo;
import jp.co.altec.openingactionsample.Point;

/**
 * A Cardboard sample application.
 */
public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer {

    private static final String TAG = "MainActivity";
    private static final String Check = "0.0.0.0";
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static float CAMERA_X = 0.0f;
    private static float CAMERA_Y = 0.5f;
    private static float CAMERA_Z = 0.01f;

    private static float Z_info = CAMERA_Z;


    private float HeadPointX = 0.0f;
    private float HeadPointY = 0.0f;
    private float HeadPointZ = 0.0f;
    private float model_x, model_y, model_z;
    private float Gmodel_x, Gmodel_y, Gmodel_z;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final int COORDS_PER_VERTEX = 3;

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[]{0.0f, 2.0f, 0.0f, 1.0f};

    private final float[] lightPosInEyeSpace = new float[4];

    private static final String SOUND_FILE = "cube_sound.wav";

    private FloatBuffer floorVertices;
    private FloatBuffer floorColors;
    private FloatBuffer floorNormals;

    private FloatBuffer cubeVertices;
    private FloatBuffer cubeColors;
    private FloatBuffer cubeFoundColors;
    private FloatBuffer cubeNormals;

    private int cubeProgram;
    private int floorProgram;

    private int cubePositionParam;
    private int cubeNormalParam;
    private int cubeColorParam;
    private int cubeModelParam;
    private int cubeModelViewParam;
    private int cubeModelViewProjectionParam;
    private int cubeLightPosParam;

    private int floorPositionParam;
    private int floorNormalParam;
    private int floorColorParam;
    private int floorModelParam;
    private int floorModelViewParam;
    private int floorModelViewProjectionParam;
    private int floorLightPosParam;

    private float[] modelCube;
    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;
    private float[] modelFloor;
    private float[] modelTreasure;
    private float[] mPerspective;

    private float[] headRotation;

    private int score = 0;
    private float objectDistance = 12f;
    private float floorDepth = 20f;
    private String name, ipAddress;
    private String CheckFight = null;
    NetWorkMgr mNetWorkMgr = NetWorkMgr.getInstance();
    private Vibrator vibrator;
    private CardboardOverlayView overlayView;

    private float PositiveCatchObjectEye_X, PositiveCatchObjectEye_Z, NegativeCatchObjectEye_X, NegativeCatchObjectEye_Z;

    private CardboardAudioEngine cardboardAudioEngine;
    private volatile int soundId = CardboardAudioEngine.INVALID_ID;

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
     *
     * @param type  The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The shader object handler.
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }


    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.common_ui);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRestoreGLStateEnabled(false);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        modelCube = new float[16];
        modelTreasure = new float[16];
        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        modelFloor = new float[16];
        headView = new float[16];
        mPerspective = new float[16];
        headRotation = new float[4];

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        overlayView = (CardboardOverlayView) findViewById(R.id.overlay);
        overlayView.show3DToast("オレンジ色のCubeを探せ");

        // Initialize 3D audio engine.
        cardboardAudioEngine =
                new CardboardAudioEngine(getAssets(), CardboardAudioEngine.RenderingQuality.HIGH);
    }

    @Override
    public void onPause() {
        cardboardAudioEngine.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        cardboardAudioEngine.resume();
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     * <p/>
     * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        ByteBuffer bbVertices = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        cubeVertices = bbVertices.asFloatBuffer();
        cubeVertices.put(WorldLayoutData.CUBE_COORDS);
        cubeVertices.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_COLORS.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        cubeColors = bbColors.asFloatBuffer();
        cubeColors.put(WorldLayoutData.CUBE_COLORS);
        cubeColors.position(0);

        ByteBuffer bbFoundColors = ByteBuffer.allocateDirect(
                WorldLayoutData.CUBE_FOUND_COLORS.length * 4);
        bbFoundColors.order(ByteOrder.nativeOrder());
        cubeFoundColors = bbFoundColors.asFloatBuffer();
        cubeFoundColors.put(WorldLayoutData.CUBE_FOUND_COLORS);
        cubeFoundColors.position(0);

        ByteBuffer bbNormals = ByteBuffer.allocateDirect(WorldLayoutData.CUBE_NORMALS.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        cubeNormals = bbNormals.asFloatBuffer();
        cubeNormals.put(WorldLayoutData.CUBE_NORMALS);
        cubeNormals.position(0);

        // make a floor
        ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COORDS.length * 4);
        bbFloorVertices.order(ByteOrder.nativeOrder());
        floorVertices = bbFloorVertices.asFloatBuffer();
        floorVertices.put(WorldLayoutData.FLOOR_COORDS);
        floorVertices.position(0);

        ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_NORMALS.length * 4);
        bbFloorNormals.order(ByteOrder.nativeOrder());
        floorNormals = bbFloorNormals.asFloatBuffer();
        floorNormals.put(WorldLayoutData.FLOOR_NORMALS);
        floorNormals.position(0);

        ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(WorldLayoutData.FLOOR_COLORS.length * 4);
        bbFloorColors.order(ByteOrder.nativeOrder());
        floorColors = bbFloorColors.asFloatBuffer();
        floorColors.put(WorldLayoutData.FLOOR_COLORS);
        floorColors.position(0);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.light_vertex);
        int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.grid_fragment);
        int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.passthrough_fragment);

        cubeProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(cubeProgram, vertexShader);
        GLES20.glAttachShader(cubeProgram, passthroughShader);
        GLES20.glLinkProgram(cubeProgram);
        GLES20.glUseProgram(cubeProgram);

        checkGLError("Cube program");

        cubePositionParam = GLES20.glGetAttribLocation(cubeProgram, "a_Position");
        cubeNormalParam = GLES20.glGetAttribLocation(cubeProgram, "a_Normal");
        cubeColorParam = GLES20.glGetAttribLocation(cubeProgram, "a_Color");

        cubeModelParam = GLES20.glGetUniformLocation(cubeProgram, "u_Model");
        cubeModelViewParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVMatrix");
        cubeModelViewProjectionParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVP");
        cubeLightPosParam = GLES20.glGetUniformLocation(cubeProgram, "u_LightPos");

        GLES20.glEnableVertexAttribArray(cubePositionParam);
        GLES20.glEnableVertexAttribArray(cubeNormalParam);
        GLES20.glEnableVertexAttribArray(cubeColorParam);

        checkGLError("Cube program params");

        floorProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(floorProgram, vertexShader);
        GLES20.glAttachShader(floorProgram, gridShader);
        GLES20.glLinkProgram(floorProgram);
        GLES20.glUseProgram(floorProgram);

        checkGLError("Floor program");

        floorModelParam = GLES20.glGetUniformLocation(floorProgram, "u_Model");
        floorModelViewParam = GLES20.glGetUniformLocation(floorProgram, "u_MVMatrix");
        floorModelViewProjectionParam = GLES20.glGetUniformLocation(floorProgram, "u_MVP");
        floorLightPosParam = GLES20.glGetUniformLocation(floorProgram, "u_LightPos");

        floorPositionParam = GLES20.glGetAttribLocation(floorProgram, "a_Position");
        floorNormalParam = GLES20.glGetAttribLocation(floorProgram, "a_Normal");
        floorColorParam = GLES20.glGetAttribLocation(floorProgram, "a_Color");

        GLES20.glEnableVertexAttribArray(floorPositionParam);
        GLES20.glEnableVertexAttribArray(floorNormalParam);
        GLES20.glEnableVertexAttribArray(floorColorParam);

        checkGLError("Floor program params");
        CAMERA_X = (float) Math.random() * 140 - 70;
        CAMERA_Z = (float) Math.random() * 140 - 70;
        Z_info = CAMERA_Z;

        // Object first appears directly in front of user.
        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, -15, -15, 0);

        Matrix.setIdentityM(modelFloor, 0);
        Matrix.translateM(modelFloor, 0, 0, -floorDepth, 0); // Floor appears below user.
        // Avoid any delays during start-up due to decoding of sound files.
        new Thread(
                new Runnable() {
                    public void run() {
                        // Start spatial audio playback of SOUND_FILE at the model postion. The returned
                        //soundId handle is stored and allows for repositioning the sound object whenever
                        // the cube position changes.
                        cardboardAudioEngine.preloadSoundFile(SOUND_FILE);
                        soundId = cardboardAudioEngine.createSoundObject(SOUND_FILE);
                        cardboardAudioEngine.setSoundObjectPosition(soundId, -1, -1, -1);
                        cardboardAudioEngine.playSound(soundId, true /* looped playback */);
                    }
                })
                .start();
        // Update the sound location to match it with the new cube position.
        if (soundId != CardboardAudioEngine.INVALID_ID) {
            cardboardAudioEngine.setSoundObjectPosition(
                    soundId, -1, -1, -1);
        }
        checkGLError("onSurfaceCreated");
    }

    /**
     * Converts a raw text file into a string.
     *
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The context of the text file, or null in case of error.
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        // Build the Model part of the ModelView matrix.
        Matrix.rotateM(modelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);
        HeadPointX = headView[8] / 10;
        HeadPointZ = headView[10] / 10;
        PositiveCatchObjectEye_X = CAMERA_X + 1.0f;
        NegativeCatchObjectEye_X = CAMERA_X - 1.0f;
        PositiveCatchObjectEye_Z = CAMERA_Z + 1.0f;
        NegativeCatchObjectEye_Z = CAMERA_Z - 1.0f;
        float Wall_Z = 150.0f;
        float Wall_X = 150.0f;

        boolean PositiveObjectInView_X = CAMERA_X < modelCube[12] && modelCube[12] < PositiveCatchObjectEye_X;
        boolean NegativeObjectInView_X = NegativeCatchObjectEye_X < modelCube[12] && modelCube[12] < CAMERA_X;
        boolean PositiveObjectInView_Z = CAMERA_Z < modelCube[14] && modelCube[14] < PositiveCatchObjectEye_Z;
        boolean NegativeObjectInView_Z = NegativeCatchObjectEye_Z < modelCube[14] && modelCube[14] < CAMERA_Z;
        boolean PositiveWall_Z = CAMERA_Z < Wall_Z && Wall_Z < PositiveCatchObjectEye_Z;
        boolean NegativeWall_Z = NegativeCatchObjectEye_Z < -Wall_Z && -Wall_Z < CAMERA_Z;
        boolean PositiveWall_X = CAMERA_X < Wall_X && Wall_X < PositiveCatchObjectEye_X;
        boolean NegativeWall_X = NegativeCatchObjectEye_X < -Wall_X && -Wall_X < CAMERA_X;
        boolean culcX = (HeadPointX < 0) ? PositiveObjectInView_X : NegativeObjectInView_X;
        boolean culcZ = (HeadPointZ > 0) ? PositiveObjectInView_Z : NegativeObjectInView_Z;
        boolean BumpWallZ = (HeadPointZ > 0) ? PositiveWall_Z : NegativeWall_Z;
        boolean BumpWallX = (HeadPointX < 0) ? PositiveWall_X : NegativeWall_X;
        if ((culcX && culcZ) || BumpWallX || BumpWallZ) {
            CAMERA_Z = Z_info;
        } else {
            CAMERA_Y = -floorDepth + 5.0f;
            CAMERA_X = CAMERA_X - HeadPointX;
            CAMERA_Z = Z_info + HeadPointZ;
            Z_info = Z_info + HeadPointZ;
        }
        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, CAMERA_X, CAMERA_Y, CAMERA_Z, 0.0f, 0.0f, 200.0f, 0.0f, 1.0f, 0.0f);
        String x = String.valueOf(CAMERA_X);
        String y = String.valueOf(CAMERA_Y);
        String z = String.valueOf(CAMERA_Z);
        DeviceInfo deviceInfo = mNetWorkMgr.getDeviceInfo();
        if (deviceInfo != null) {
            deviceInfo.setPoint(new Point(x, y, z));
        }
        headTransform.getHeadView(headView, 0);

        // Update the 3d audio engine with the most recent head rotation.
        headTransform.getQuaternion(headRotation, 0);
        cardboardAudioEngine.setHeadRotation(headRotation[0], headRotation[1], headRotation[2], headRotation[3]);

        if (!mNetWorkMgr.getCheckInfo().getKeyIP().equals(null)) {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (!mNetWorkMgr.getCheckInfo().getKeyIP().equals(Check)) {
                        if (CheckFight == null) {
                            CheckFight = mNetWorkMgr.getCheckInfo().getKeyIP();
                            Log.d("CheckFight = ", CheckFight);
                        }
                        if (CheckFight != null) {
                            if (CheckFight.equals(mNetWorkMgr.getDeviceInfo().getIpAddress())) {
                                overlayView.show3DToast("Found it! Conguraturation! Winner :" + mNetWorkMgr.getDeviceInfo().getName());
                            } else if (!CheckFight.equals(mNetWorkMgr.getDeviceInfo().getIpAddress())) {
                                overlayView.show3DToast("Don't mind! Loser :" + mNetWorkMgr.getDeviceInfo().getName());
                            }
                            for (int count = 0; count <= 500; count++) {
                                if (count > 450) {
                                    CheckFight = null;
                                    System.out.print(CheckFight);
                                }
                            }
                        }
                    }
                }
            });
        }
        checkGLError("onReadyToDraw");
    }


    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        checkGLError("colorParam");
        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);
        // Set the position of the light
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        for (Map.Entry<String, DeviceInfo> e : DataControl.mDeviceInfos.entrySet()) {
            String MapKey = e.getKey();
            model_x = Float.valueOf(e.getValue().getPoint().x);
            model_z = Float.valueOf(e.getValue().getPoint().z);
            if (e.getValue().getName().equals("GOAL")) {
                Cube Tr = new Cube(Float.valueOf(e.getValue().getPoint().x), -15f, Float.valueOf(e.getValue().getPoint().z));
                CubeControl.mCubeInfos.put(MapKey, Tr);
                modelCube = Tr.getDrawCube();
                Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
                Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
                drawCube(e.getValue().getName());
            } else {
                if (CubeControl.mCubeInfos.containsKey(MapKey)) {
                    Cube cube = CubeControl.mCubeInfos.get(MapKey);
                    cube.setPoint(model_x, -20f, model_z);
                    modelCube = cube.getDrawCube();
                } else {
                    Cube cube = new Cube(model_x, -20f, model_z);
                    CubeControl.mCubeInfos.put(MapKey, cube);
                    modelCube = cube.getDrawCube();
                }
                Matrix.scaleM(modelCube, 0, 1.0f, 6.0f, 1.0f);
                Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
                Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
                drawCube(e.getValue().getName());
            }
        }
        // Set modelView for the floor, so we draw floor in the correct location
        Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0,
                modelView, 0);
        drawFloor();


    }


    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    /**
     * Draw the cube.
     * <p/>
     * <p>We've set all of our transformation matrices. Now we simply pass them into the shader.
     */
    public void drawCube(String CheckName) {
        GLES20.glUseProgram(cubeProgram);

        GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

        // Set the Model in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube, 0);

        // Set the ModelView in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);

        // Set the position of the cube
        GLES20.glVertexAttribPointer(cubePositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, cubeVertices);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);

        // Set the normal positions of the cube, again for shading
        GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
        if (CheckName.equals("GOAL")) {
            GLES20.glVertexAttribPointer(cubeColorParam, 4, GLES20.GL_FLOAT, false, 0, cubeFoundColors);
        } else {
            GLES20.glVertexAttribPointer(cubeColorParam, 4, GLES20.GL_FLOAT, false, 0, cubeColors);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        checkGLError("Drawing cube");
    }

    /**
     * Draw the floor.
     * <p/>
     * <p>This feeds in data for the floor into the shader. Note that this doesn't feed in data about
     * position of the light, so if we rewrite our code to draw the floor first, the lighting might
     * look strange.
     */
    public void drawFloor() {
        GLES20.glUseProgram(floorProgram);
        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(floorLightPosParam, 1, lightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(floorModelParam, 1, false, modelFloor, 0);
        GLES20.glUniformMatrix4fv(floorModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(floorModelViewProjectionParam, 1, false,
                modelViewProjection, 0);
        GLES20.glVertexAttribPointer(floorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, floorVertices);
        GLES20.glVertexAttribPointer(floorNormalParam, 3, GLES20.GL_FLOAT, false, 0,
                floorNormals);
        GLES20.glVertexAttribPointer(floorColorParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        checkGLError("drawing floor");
    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        Log.i(TAG, "onCardboardTrigger");

        if (isTouchingAtObject()) {
            score++;
            //overlayView.setText("message");
            overlayView.show3DToast("Found it! Look around for another one.\nScore = " + score);
            hideObject();
        }
        // Always give user feedback.
        vibrator.vibrate(50);
    }

    /**
     * Find a new random position for the object.
     * <p/>
     * <p>We'll rotate it around the Y-axis so it's out of sight, and then up or down by a little bit.
     */
    private void hideObject() {
        float[] rotationMatrix = new float[16];
        float[] posVec = new float[4];

        // First rotate in XZ plane, between 90 and 270 deg away, and scale so that we vary
        // the object's distance from the user.
        float angleXZ = (float) Math.random() * 180 + 90;
        Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
        float oldObjectDistance = objectDistance;
        objectDistance = (float) Math.random() * 15 + 5;
        float objectScalingFactor = objectDistance / oldObjectDistance;
        Matrix.scaleM(rotationMatrix, 0, objectScalingFactor, objectScalingFactor,
                objectScalingFactor);
        Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelCube, 12);

        // Now get the up or down angle, between -20 and 20 degrees.
        float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane, between -40 and 40.
        angleY = (float) Math.toRadians(angleY);
        float newY = (float) Math.tan(angleY) * objectDistance;

        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, posVec[0], newY, posVec[2]);
    }


    private boolean isTouchingAtObject() {
        float[] initVec = {0, 0, 0, 1.0f};
        float[] objPositionVec = new float[4];
        boolean CheckTouchX, CheckTouchZ, CheckTouch;
        CheckTouchX = CAMERA_X - 3.0f < modelCube[12] && modelCube[12] < CAMERA_X + 3.0f;
        CheckTouchZ = CAMERA_Z - 3.0f < modelCube[14] && modelCube[14] < CAMERA_Z + 3.0f;
        CheckTouch = CheckTouchX && CheckTouchZ;
        return CheckTouch;
    }

}
