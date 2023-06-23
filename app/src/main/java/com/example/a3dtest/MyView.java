package com.example.a3dtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class MyView extends View {
    private Paint myPaint; //paint object for drawing the lines
    private Coordinate[] cube_vertices, draw_cube_vertices;//the vertices of a 3D cube

    public void setRotationMode(boolean rotationMode) {
        this.rotationMode = rotationMode;
    }

    private volatile boolean rotationMode = true; //true = Affine, false = Quaternion
    private double[][] matrix;
    private volatile int[] quaternionRotationAxes;
    private volatile int rotX;
    private volatile int rotY;
    private volatile int rotZ;
    private volatile int trX;
    private volatile int trY;
    private volatile int trZ;
    private volatile int scX;
    private volatile int scY;
    private volatile int scZ;
    private volatile int shX;
    private volatile int shY;

    public int getQuaternionRotationAngle() {
        return quaternionRotationAngle;
    }

    private volatile int quaternionRotationAngle;

    public MyView(Context context) {
        super(context, null);
        init();
    }

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        //create the paint object
        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setStyle(Paint.Style.STROKE);//Stroke
        myPaint.setColor(Color.BLACK);
        myPaint.setStrokeWidth(5);

        //create a 3D cube
        draw_cube_vertices = new Coordinate[8];
        cube_vertices = new Coordinate[8];
        cube_vertices[0] = new Coordinate(-1, -1, -1, 1);
        cube_vertices[1] = new Coordinate(-1, -1, 1, 1);
        cube_vertices[2] = new Coordinate(-1, 1, -1, 1);
        cube_vertices[3] = new Coordinate(-1, 1, 1, 1);
        cube_vertices[4] = new Coordinate(1, -1, -1, 1);
        cube_vertices[5] = new Coordinate(1, -1, 1, 1);
        cube_vertices[6] = new Coordinate(1, 1, -1, 1);
        cube_vertices[7] = new Coordinate(1, 1, 1, 1);

        rotX = rotY = rotZ = shX = shY = 0;
        trX = trY = 400; trZ = 0;
        scX = scY = scZ = 100;
        quaternionRotationAxes = new int[3];
        ApplyEffects();
    }

    private void ApplyEffects() {
        if (rotationMode) { //Affine transforms
            draw_cube_vertices = rotateX(cube_vertices, rotX);
            draw_cube_vertices = rotateY(draw_cube_vertices, rotY);
            draw_cube_vertices = rotateZ(draw_cube_vertices, rotZ);
        }
        else
            draw_cube_vertices = rotateQuaternion(cube_vertices, quaternionRotationAngle, quaternionRotationAxes);

        draw_cube_vertices = scale(draw_cube_vertices, scX, scY, scZ);
        draw_cube_vertices = translate(draw_cube_vertices, trX, trY, trZ);
        draw_cube_vertices = shear(draw_cube_vertices, shX, shY);

        invalidate();
    }

    private Coordinate[] rotateQuaternion(Coordinate[] vertices, int quaternionRotationAngle, int[] quaternionRotationAxes) {
        //q = (w, v vector)
        double w = Math.cos(Math.toRadians(quaternionRotationAngle/2.0));
        double[]v = new double[3];

        for (int i = 0; i < quaternionRotationAxes.length; i++)
            v[i] = Math.sin(Math.toRadians(quaternionRotationAngle/2.0))*quaternionRotationAxes[i];

        ResetIdentityMatrix();
        matrix[0][0] = w*w + v[0]*v[0] - v[1]*v[1] - v[2]*v[2];
        matrix[0][1] = 2*(v[0]*v[1] - w*v[2]);
        matrix[0][2] = 2*(v[0]*v[2] + w*v[1]);

        matrix[1][0] = 2*(v[0]*v[1] + w*v[2]);
        matrix[1][1] = w*w - v[0]*v[0] + v[1]*v[1] - v[2]*v[2];
        matrix[1][2] = 2*(v[1]*v[2] - w*v[0]);

        matrix[2][0] = 2*(v[0]*v[2] - w*v[1]);
        matrix[2][1] = 2*(v[1]*v[2] + w*v[0]);
        matrix[2][2] = w*w + v[2]*v[2] - v[0]*v[0] - v[1]*v[1];

        return Transformation(vertices, matrix);
    }

    private void DrawLinePairs(Canvas canvas, Coordinate[] vertices, int start, int end) {
        //draw a line connecting 2 points
        //canvas - canvas of the view
        //points - array of points
        //start - index of the starting point
        //end - index of the ending point
        //paint - the paint of the line
        canvas.drawLine((int) vertices[start].x, (int) vertices[start].y, (int) vertices[end].x, (int) vertices[end].y, myPaint);
    }

    private void DrawCube(Canvas canvas) {
        //draw a cube on the screen
        DrawLinePairs(canvas, draw_cube_vertices, 0, 1);
        DrawLinePairs(canvas, draw_cube_vertices, 1, 3);
        DrawLinePairs(canvas, draw_cube_vertices, 3, 2);
        DrawLinePairs(canvas, draw_cube_vertices, 2, 0);
        DrawLinePairs(canvas, draw_cube_vertices, 4, 5);
        DrawLinePairs(canvas, draw_cube_vertices, 5, 7);
        DrawLinePairs(canvas, draw_cube_vertices, 7, 6);
        DrawLinePairs(canvas, draw_cube_vertices, 6, 4);
        DrawLinePairs(canvas, draw_cube_vertices, 0, 4);
        DrawLinePairs(canvas, draw_cube_vertices, 1, 5);
        DrawLinePairs(canvas, draw_cube_vertices, 2, 6);
        DrawLinePairs(canvas, draw_cube_vertices, 3, 7);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //draw objects on the screen
        super.onDraw(canvas);
        DrawCube(canvas);//draw a cube onto the screen
    }

    //*********************************
    //matrix and transformation functions
    public void ResetIdentityMatrix() {
        //return an 4x4 identity matrix
        if (matrix == null)
            matrix = new double[4][4];

        matrix[0][0] = 1;
        matrix[0][1] = 0;
        matrix[0][2] = 0;
        matrix[0][3] = 0;
        matrix[1][0] = 0;
        matrix[1][1] = 1;
        matrix[1][2] = 0;
        matrix[1][3] = 0;
        matrix[2][0] = 0;
        matrix[2][1] = 0;
        matrix[2][2] = 1;
        matrix[2][3] = 0;
        matrix[3][0] = 0;
        matrix[3][1] = 0;
        matrix[3][2] = 0;
        matrix[3][3] = 1;

    }

    public Coordinate Transformation(Coordinate vertex, double[][] matrix) {
        //affine transformation with homogeneous coordinates
        //i.e. a vector (vertex) multiply with the transformation matrix
        // vertex - vector in 3D
        // matrix - transformation matrix
        Coordinate result = new Coordinate();
        result.x = matrix[0][0] * vertex.x + matrix[0][1] * vertex.y + matrix[0][2] * vertex.z + matrix[0][3];
        result.y = matrix[1][0] * vertex.x + matrix[1][1] * vertex.y + matrix[1][2] * vertex.z + matrix[1][3];
        result.z = matrix[2][0] * vertex.x + matrix[2][1] * vertex.y + matrix[2][2] * vertex.z + matrix[2][3];
        result.w = matrix[3][0] * vertex.x + matrix[3][1] * vertex.y + matrix[3][2] * vertex.z + matrix[3][3];
        return result;
    }

    public Coordinate[] Transformation(Coordinate[] vertices, double[][] matrix) {
        //Affine transform a 3D object with vertices
        // vertices - vertices of the 3D object.
        // matrix - transformation matrix
        Coordinate[] result = new Coordinate[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            result[i] = Transformation(vertices[i], matrix);
            result[i].Normalise();
        }
        return result;
    }

    //***********************************************************
    //Affine transformation
    public Coordinate[] translate(Coordinate[] vertices, double tx, double ty, double tz) {
        ResetIdentityMatrix();
        matrix[0][3] = tx;
        matrix[1][3] = ty;
        matrix[2][3] = tz;
        return Transformation(vertices, matrix);
    }

    private Coordinate[] scale(Coordinate[] vertices, double sx, double sy, double sz) {
        ResetIdentityMatrix();
        matrix[0][0] = sx;
        matrix[1][1] = sy;
        matrix[2][2] = sz;
        return Transformation(vertices, matrix);
    }

    private Coordinate[] shear(Coordinate[] vertices, int sx, int sy) {
        ResetIdentityMatrix();
        matrix[0][2] = sx/100.0;
        matrix[1][2] = sy/100.0;
        return Transformation(vertices, matrix);
    }
    public Coordinate[] rotateX(Coordinate[] vertices, int deg) {

        ResetIdentityMatrix();
        matrix[1][1] = Math.cos(Math.toRadians(deg));
        matrix[2][2] = matrix[1][1];
        matrix[2][1] = Math.sin(Math.toRadians(deg));
        matrix[1][2] = -matrix[2][1];
        return Transformation(vertices, matrix);
    }

    public Coordinate[] rotateY(Coordinate[] vertices, int deg) {

        ResetIdentityMatrix();
        matrix[0][0] = Math.cos(Math.toRadians(deg));
        matrix[2][2] = matrix[0][0];
        matrix[0][2] = Math.sin(Math.toRadians(deg));
        matrix[2][0] = -matrix[0][2];
        return Transformation(vertices, matrix);
    }

    public Coordinate[] rotateZ(Coordinate[] vertices, int deg) {

        ResetIdentityMatrix();
        matrix[0][0] = Math.cos(Math.toRadians(deg));
        matrix[1][1] = matrix[0][0];
        matrix[1][0] = Math.sin(Math.toRadians(deg));
        matrix[0][1] = -matrix[1][0];
        return Transformation(vertices, matrix);
    }

    public void setXYZRotation(int x, int y, int z) {
        rotX = x; rotY = y; rotZ = z;
        ApplyEffects();
    }

    public void setScale(int x, int y, int z) {
        scX = x;
        scY = y;
        scZ = z;
        ApplyEffects();
    }

    public void setTranslation(int x, int y, int z) {
        trX = x;
        trY = y;
        trZ = z;
        ApplyEffects();
    }

    public void setShear(int x, int y) {
        shX = x;
        shY = y;
        ApplyEffects();
    }

    public void quaternionRotate(int l, int[] axes) {
        quaternionRotationAngle = l;
        quaternionRotationAxes[0] = axes[0];
        quaternionRotationAxes[1] = axes[1];
        quaternionRotationAxes[2] = axes[2];

        ApplyEffects();
    }
}