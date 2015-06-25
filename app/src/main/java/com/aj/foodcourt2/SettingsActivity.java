package com.aj.foodcourt2;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingsActivity extends AppCompatActivity {

    RadioGroup radioGroup;
    RadioButton rbQueuing, rbLocalization, rbJoost, rbJork, rbWillem, rbAlexander;
    private final static String PREF_NAME = "foodcourtPreferenceFile";
    private final static String STEP_MODE_NAME = "prefStepMode";
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

        radioGroup = (RadioGroup)findViewById(R.id.rg_mode_selection);

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
