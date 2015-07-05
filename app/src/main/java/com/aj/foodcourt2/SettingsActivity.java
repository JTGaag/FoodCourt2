package com.aj.foodcourt2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    RadioGroup radioGroup, rgLocations;
    RadioButton rbQueuing, rbLocalization, rbJoost, rbJork, rbWillem, rbAlexander, rbEWI, rbRDW;
    Switch sDebugMode, sLocationMode;
    TextView tvAutoLocationName;
    EditText etStrideLength;
    private final static String PREF_NAME = "foodcourtPreferenceFile";
    private final static String STEP_MODE_NAME = "prefStepMode";
    private final static String DEBUG_MODE_NAME = "prefDebugMode";
    private final static String LOCATION_MODE_NAME = "prefLocationMode";
    private final static String LOCATION_MANUAL_NAME = "prefLocationManual";
    private final static String LOCATION_AUTO_NAME = "prefLocationAuto";
    private final static String STRIDE_LENGTH_NAME = "prefStrideLength";
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settings = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        rbQueuing = (RadioButton)findViewById(R.id.rb_mode_queuing);
        rbLocalization = (RadioButton)findViewById(R.id.rb_mode_localization);
        rbJoost = (RadioButton)findViewById(R.id.rb_mode_joost);
        rbJork = (RadioButton)findViewById(R.id.rb_mode_jork);
        rbWillem = (RadioButton)findViewById(R.id.rb_mode_willem);
        rbAlexander = (RadioButton)findViewById(R.id.rb_mode_alexander);

        sDebugMode = (Switch)findViewById(R.id.switch_debug_mode);
        sLocationMode = (Switch)findViewById(R.id.switch_manual_location);

        radioGroup = (RadioGroup)findViewById(R.id.rg_mode_selection);
        rgLocations = (RadioGroup)findViewById(R.id.rg_location_selection);

        tvAutoLocationName = (TextView)findViewById(R.id.tv_auto_location);

        etStrideLength = (EditText)findViewById(R.id.et_stride_length);

        etStrideLength.setText(Integer.toString(settings.getInt(STRIDE_LENGTH_NAME,65)));

        switch (settings.getInt(STEP_MODE_NAME, 1)){
            case 1:
                radioGroup.check(rbQueuing.getId());
                break;
            case 2:
                radioGroup.check(rbLocalization.getId());
                break;
            case 3:
                radioGroup.check(rbJoost.getId());
                break;
            case 4:
                radioGroup.check(rbJork.getId());
                break;
            case 5:
                radioGroup.check(rbWillem.getId());
                break;
            case 6:
                radioGroup.check(rbAlexander.getId());
                break;
            default:
                break;
        }

        switch (settings.getInt(LOCATION_MANUAL_NAME, 1)){
            case 1:
                rgLocations.check(R.id.rb_location_ewi);
                break;
            case 2:
                rgLocations.check(R.id.rb_location_rdw);
                break;
            default:
                break;
        }

        switch (settings.getInt(LOCATION_AUTO_NAME, 1)){
            case 1:
                tvAutoLocationName.setText("Automatic location: EWI");
                break;
            case 2:
                tvAutoLocationName.setText("Automatic location: RDW");
                break;
            default:
                break;
        }

        sDebugMode.setChecked(settings.getBoolean(DEBUG_MODE_NAME, false));
        sLocationMode.setChecked(settings.getBoolean(LOCATION_MODE_NAME, true));


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SharedPreferences.Editor editor = settings.edit();
                switch(checkedId){
                    case R.id.rb_mode_queuing:
                        editor.putInt(STEP_MODE_NAME, 1);
                        break;
                    case R.id.rb_mode_localization:
                        editor.putInt(STEP_MODE_NAME, 2);
                        break;
                    case R.id.rb_mode_joost:
                        editor.putInt(STEP_MODE_NAME, 3);
                        break;
                    case R.id.rb_mode_jork:
                        editor.putInt(STEP_MODE_NAME, 4);
                        break;
                    case R.id.rb_mode_willem:
                        editor.putInt(STEP_MODE_NAME, 5);
                        break;
                    case R.id.rb_mode_alexander:
                        editor.putInt(STEP_MODE_NAME, 6);
                        break;
                    default:
                        editor.putInt(STEP_MODE_NAME, 1);
                        break;
                }
                editor.commit();
            }
        });

        rgLocations.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                SharedPreferences.Editor editor = settings.edit();
                switch(checkedId){
                    case R.id.rb_location_ewi:
                        editor.putInt(LOCATION_MANUAL_NAME, 1);
                        break;
                    case R.id.rb_location_rdw:
                        editor.putInt(LOCATION_MANUAL_NAME, 2);
                        break;
                    default:
                        editor.putInt(LOCATION_MANUAL_NAME, 1);
                        break;
                }
                editor.commit();
            }
        });

        sDebugMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(DEBUG_MODE_NAME, isChecked);
                editor.commit();
            }
        });

        sLocationMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(LOCATION_MODE_NAME, isChecked);
                editor.commit();
            }
        });

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(STRIDE_LENGTH_NAME, (int)Math.round(Double.parseDouble(etStrideLength.getText().toString())));
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
