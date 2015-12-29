package com.google.vrtoolkit.cardboard.samples.treasurehunt;

import android.opengl.Matrix;

/**
 * Created by 2015295 on 2015/12/25.
 */
public class Cube {
    private float Model_x,Model_y,Model_z;

    public Cube(float model_x, float model_y, float model_z){

        Model_x = model_x;
        Model_y = model_y;
        Model_z = model_z;
    }

    public void setPoint(float x, float y, float z){
        this.Model_x = x;
        this.Model_y = y;
        this.Model_z = z;
    }
    public float[] getDrawCube(){
        float[] modelCube = new float[16];
        Matrix.setIdentityM(modelCube,0);
        Matrix.translateM(modelCube, 0, Model_x, Model_y, Model_z);
        return modelCube;
    }

}
