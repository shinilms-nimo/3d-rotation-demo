package com.example.a3dtest;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import br.com.forusers.heinsinputdialogs.HeinsInputDialog;
import br.com.forusers.heinsinputdialogs.interfaces.OnInputLongListener;

public class MainActivity extends AppCompatActivity {
    private MyView mMyView;//a custom view for drawing
    private SeekBar sbScaleX, sbScaleY, sbScaleZ,
            sbTrX, sbTrY, sbTrZ,
            sbRotX, sbRotY, sbRotZ,
            sbShearX, sbShearY,

    sbQuatRotAngle;

    private TextView tvScaleX, tvScaleY, tvScaleZ,
            tvTrX, tvTrY, tvTrZ,
            tvRotX, tvRotY, tvRotZ,
            tvShearX, tvShearY,

    tvQuatRotAngle;

    private RadioGroup radioGroup;

    private CheckBox cbQuatX, cbQuatY, cbQuatZ;

    Timer t;
    TimerTask task;
    int screenMinPaddingDp = 100;
    volatile int rotationAxis = 0;
    int[] quatRotationAxes = {0,0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControls();
        radioGroup.check(R.id.rb_affine);
        t = new Timer(true);
        task = new TimerTask() {
            int angle = 0;
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (rotationAxis == 0) return;
                    if (rotationAxis == 1) sbRotX.setProgress(angle);
                    else if (rotationAxis == 2) sbRotY.setProgress(angle);
                    else if (rotationAxis == 3) sbRotZ.setProgress(angle);
                    else if (rotationAxis == 4) sbQuatRotAngle.setProgress(angle);
                    else return;
                    angle++;
                    angle %= 360;
                });
            }
        };
        t.scheduleAtFixedRate(task, 100, 8);
    }

    private void initControls() {
        mMyView = findViewById(R.id.polyView);

        radioGroup = findViewById(R.id.rg_select);

        cbQuatX = findViewById(R.id.cb_x);
        cbQuatY = findViewById(R.id.cb_y);
        cbQuatZ = findViewById(R.id.cb_z);
        sbQuatRotAngle = findViewById(R.id.sb_quatRotationAngle);
        sbScaleX = findViewById(R.id.sb_scaleX);
        sbScaleY = findViewById(R.id.sb_scaleY);
        sbScaleZ = findViewById(R.id.sb_scaleZ);

        sbTrX = findViewById(R.id.sb_translationX);
        sbTrY = findViewById(R.id.sb_translationY);
        sbTrZ = findViewById(R.id.sb_translationZ);

        sbRotX = findViewById(R.id.sb_rotationX);
        sbRotY = findViewById(R.id.sb_rotationY);
        sbRotZ = findViewById(R.id.sb_rotationZ);

        sbShearX = findViewById(R.id.sb_shearX);
        sbShearY = findViewById(R.id.sb_shearY);

        tvScaleX = findViewById(R.id.tv_scaleX);
        tvScaleY = findViewById(R.id.tv_scaleY);
        tvScaleZ = findViewById(R.id.tv_scaleZ);

        tvTrX = findViewById(R.id.tv_trx);
        tvTrY = findViewById(R.id.tv_try);
        tvTrZ = findViewById(R.id.tv_trz);

        tvRotX = findViewById(R.id.tv_rotX);
        tvRotY = findViewById(R.id.tv_rotY);
        tvRotZ = findViewById(R.id.tv_rotZ);

        tvShearX = findViewById(R.id.tv_shx);
        tvShearY = findViewById(R.id.tv_shy);

        tvQuatRotAngle = findViewById(R.id.tv_quatRotAngle);

        ViewTreeObserver vto = mMyView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mMyView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mMyView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                sbTrX.setMax(mMyView.getMeasuredWidth() - screenMinPaddingDp);
                sbTrY.setMax(mMyView.getMeasuredHeight()- screenMinPaddingDp);

                sbTrZ.setMax(1000);
            }
        });
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            setAffineControlsEnabled(checkedId == R.id.rb_affine);
            setQuaternionControlsEnabled(checkedId == R.id.rb_quaternion);

            if (checkedId != R.id.rb_quaternion)
                rotationAxis = 0;
        });
        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar.getId() == R.id.sb_rotationX || seekBar.getId() == R.id.sb_rotationY || seekBar.getId() == R.id.sb_rotationZ) {
                    mMyView.setXYZRotation(sbRotX.getProgress(), sbRotY.getProgress(), sbRotZ.getProgress());
                    tvRotX.setText("RTX: (" + String.format("%03d", sbRotX.getProgress()) + "°) ");
                    tvRotY.setText("RTY: (" + String.format("%03d", sbRotY.getProgress()) + "°) ");
                    tvRotZ.setText("RTZ: (" + String.format("%03d", sbRotZ.getProgress()) + "°) ");
                } else if (seekBar.getId() == R.id.sb_scaleX || seekBar.getId() == R.id.sb_scaleY || seekBar.getId() == R.id.sb_scaleZ) {
                    mMyView.setScale(sbScaleX.getProgress(), sbScaleY.getProgress(), sbScaleZ.getProgress());
                    tvScaleX.setText("SLX: (" + String.format("%1.2f", sbScaleX.getProgress() / 100.0) + ") ");
                    tvScaleY.setText("SLY: (" + String.format("%1.2f", sbScaleY.getProgress() / 100.0) + ") ");
                    tvScaleZ.setText("SLZ: (" + String.format("%1.2f", sbScaleZ.getProgress() / 100.0) + ") ");
                } else if (seekBar.getId() == R.id.sb_translationX || seekBar.getId() == R.id.sb_translationY || seekBar.getId() == R.id.sb_translationZ) {
                    mMyView.setTranslation(sbTrX.getProgress(), sbTrY.getProgress(), sbTrZ.getProgress());
                    tvTrX.setText("TRX: (" + String.format("%04d", screenMinPaddingDp + sbTrX.getProgress()) + ") ");
                    tvTrY.setText("TRY: (" + String.format("%04d", screenMinPaddingDp + sbTrY.getProgress()) + ") ");
                    tvTrZ.setText("TRZ: (" + String.format("%04d", sbTrZ.getProgress()) + ") ");
                } else if (seekBar.getId() == R.id.sb_shearX || seekBar.getId() == R.id.sb_shearY) {
                    mMyView.setShear(sbShearX.getProgress(), sbShearY.getProgress());
                    tvShearX.setText("SHX: (" + String.format("%1.2f", sbShearX.getProgress() / 100f) + ") ");
                    tvShearY.setText("SHY: (" + String.format("%1.2f", sbShearY.getProgress() / 100f) + ") ");
                } else if (seekBar.getId() == R.id.sb_quatRotationAngle) {
                    if (cbQuatX.isChecked()) {
                        quatRotationAxes[0] = 1;
                    } else {
                        quatRotationAxes[0] = 0;
                    }

                    if (cbQuatY.isChecked()) {
                        quatRotationAxes[1] = 1;
                    } else {
                        quatRotationAxes[1] = 0;
                    }

                    if (cbQuatZ.isChecked()) {
                        quatRotationAxes[2] = 1;
                    } else {
                        quatRotationAxes[2] = 0;
                    }

                    tvQuatRotAngle.setText("ANGL: (" + String.format("%03d", sbQuatRotAngle.getProgress()) + "°) ");
                    mMyView.quaternionRotate(progress, quatRotationAxes);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        View.OnClickListener onClickListener = v -> {
            HeinsInputDialog dialog = new HeinsInputDialog(this);
            if (v.getId() == R.id.tv_rotX) {
                dialog.setPositiveButton((OnInputLongListener) (alertDialog, aLong) -> {
                    int l = aLong.intValue();
                    sbRotX.setProgress(l);
                    return false;
                });
                dialog.setTitle("Set X Rotation");
                dialog.setHint("Input Value");
                dialog.setValue("" + sbRotX.getProgress());
                dialog.show();
            } else if (v.getId() == R.id.tv_rotY) {
                dialog = new HeinsInputDialog(this);
                dialog.setPositiveButton((OnInputLongListener) (alertDialog, aLong) -> {
                    int l = aLong.intValue();
                    sbRotY.setProgress(l);
                    return false;
                });
                dialog.setTitle("Set Y Rotation");
                dialog.setHint("Input Value");
                dialog.setValue("" + sbRotY.getProgress());
                dialog.show();
            } else if (v.getId() == R.id.tv_rotZ) {
                dialog = new HeinsInputDialog(this);
                dialog.setPositiveButton((OnInputLongListener) (alertDialog, aLong) -> {
                    int l = aLong.intValue();
                    sbRotZ.setProgress(l);
                    return false;
                });
                dialog.setTitle("Set Z Rotation");
                dialog.setHint("Input Value");
                dialog.setValue("" + sbRotZ.getProgress());
                dialog.show();
            } else if (v.getId() == R.id.tv_quatRotAngle) {
                if (!cbQuatX.isChecked() && !cbQuatY.isChecked() && !cbQuatZ.isChecked())
                    return;

                dialog = new HeinsInputDialog(this);
                dialog.setPositiveButton((OnInputLongListener) (alertDialog, aLong) -> {
                    int l = aLong.intValue();
                    sbQuatRotAngle.setProgress(l);
                    return false;
                });
                dialog.setTitle("Set Quaternion Rotation Angle:");
                dialog.setHint("Input Value");
                dialog.setValue("" + sbQuatRotAngle.getProgress());
                dialog.show();
            } else if (v.getId() == R.id.polyView) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.rb_affine) {
                    rotationAxis++;
                    rotationAxis %= 4;
                } else if (sbQuatRotAngle.isEnabled()) {
                    if (rotationAxis == 4) {
                        rotationAxis = 0;
                    } else {
                        rotationAxis = 4;
                    }
                } else {
                    rotationAxis = 0;
                }
            }

        };
        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> {
            if (isChecked) sbQuatRotAngle.setEnabled(true);
            else if (!cbQuatX.isChecked() && !cbQuatY.isChecked() && !cbQuatZ.isChecked()) {
                sbQuatRotAngle.setEnabled(false);
                rotationAxis = 0;
            }
            else rotationAxis = 4;
        };
        cbQuatX.setOnCheckedChangeListener(onCheckedChangeListener);
        cbQuatY.setOnCheckedChangeListener(onCheckedChangeListener);
        cbQuatZ.setOnCheckedChangeListener(onCheckedChangeListener);

        sbShearX.setOnSeekBarChangeListener(onSeekBarChangeListener);
        sbShearY.setOnSeekBarChangeListener(onSeekBarChangeListener);

        sbTrX.setOnSeekBarChangeListener(onSeekBarChangeListener);
        sbTrY.setOnSeekBarChangeListener(onSeekBarChangeListener);
        sbTrZ.setOnSeekBarChangeListener(onSeekBarChangeListener);

        sbScaleX.setOnSeekBarChangeListener(onSeekBarChangeListener);
        sbScaleY.setOnSeekBarChangeListener(onSeekBarChangeListener);
        sbScaleZ.setOnSeekBarChangeListener(onSeekBarChangeListener);

        sbRotX.setOnSeekBarChangeListener(onSeekBarChangeListener);
        sbRotY.setOnSeekBarChangeListener(onSeekBarChangeListener);
        sbRotZ.setOnSeekBarChangeListener(onSeekBarChangeListener);

        sbQuatRotAngle.setOnSeekBarChangeListener(onSeekBarChangeListener);

        tvRotX.setOnClickListener(onClickListener);
        tvRotY.setOnClickListener(onClickListener);
        tvRotZ.setOnClickListener(onClickListener);
        tvQuatRotAngle.setOnClickListener(onClickListener);

        mMyView.setOnClickListener(onClickListener);

    }

    private void setQuaternionControlsEnabled(boolean b) {
        cbQuatX.setEnabled(b);
        cbQuatY.setEnabled(b);
        cbQuatZ.setEnabled(b);

        sbQuatRotAngle.setEnabled(cbQuatX.isChecked() || cbQuatY.isChecked() || cbQuatZ.isChecked());
    }

    private void setAffineControlsEnabled(boolean isChecked) {
        rotationAxis = 0;
        mMyView.setRotationMode(isChecked);

        sbRotX.setEnabled(isChecked);
        sbRotY.setEnabled(isChecked);
        sbRotZ.setEnabled(isChecked);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        task.cancel();
        t.purge();
    }
}
