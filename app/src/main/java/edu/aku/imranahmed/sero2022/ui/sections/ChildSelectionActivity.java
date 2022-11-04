package edu.aku.imranahmed.sero2022.ui.sections;


import static edu.aku.imranahmed.sero2022.core.MainApp.randomChild;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.validatorcrawler.aliazaz.Validator;

import java.util.ArrayList;

import edu.aku.imranahmed.sero2022.R;
import edu.aku.imranahmed.sero2022.core.MainApp;
import edu.aku.imranahmed.sero2022.database.DatabaseHelper;
import edu.aku.imranahmed.sero2022.databinding.ActivityChildSelectBinding;
import edu.aku.imranahmed.sero2022.models.RandomHH;
import edu.aku.imranahmed.sero2022.ui.EndingActivity;

public class ChildSelectionActivity extends AppCompatActivity {
    private static final String TAG = "SectionES1Activity";
    ActivityChildSelectBinding bi;
    private DatabaseHelper db;
    private ArrayList<String> childNames, childSerial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bi = DataBindingUtil.setContentView(this, R.layout.activity_child_select);
        setSupportActionBar(bi.toolbar);
        db = MainApp.appInfo.dbHelper;

        populateSpinner();
        if (MainApp.superuser)
            bi.btnContinue.setText("Review Next");
    }

    private void populateSpinner() {

        // Populate Provinces

        childNames = new ArrayList<>();
        childSerial = new ArrayList<>();
        childNames.add("...");
        childSerial.add("");

        for (RandomHH random : randomChild) {
            childNames.add(random.getChildName());
            childSerial.add(random.getChildSno());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ChildSelectionActivity.this,
                R.layout.custom_spinner, childNames);

        bi.es1resp.setAdapter(adapter);

        bi.es1resp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                bi.es1respline.setText(childSerial.get(bi.es1resp.getSelectedItemPosition()));

 /*                //  if (position == 0) return;
                try {
                    MainApp.child = db.getChildrenByUUID(childNames.get(bi.es1resp.getSelectedItemPosition()));
                    if (MainApp.child.getUid().equals("")) {
                        MainApp.child.setSnoAdol(childSerial.get(bi.es1resp.getSelectedItemPosition()));
                        MainApp.child.setEs1respline(childSerial.get(bi.es1resp.getSelectedItemPosition()));
                        MainApp.child.setEs1resp(childNames.get(bi.es1resp.getSelectedItemPosition()));
                    }
                    bi.es1respline.setText(childSerial.get(bi.es1resp.getSelectedItemPosition()));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ChildSelectionActivity.this, "JSONException(Child)" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }*/
                if (position != 0) {
                    MainApp.selectedChildName = (childNames.get(bi.es1resp.getSelectedItemPosition()));
                    MainApp.selectedChildPosition = (childSerial.get(bi.es1resp.getSelectedItemPosition()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void btnContinue(View view) {
        bi.llbtn.setVisibility(View.GONE);
        new Handler().postDelayed(() -> bi.llbtn.setVisibility(View.VISIBLE), 5000);
        if (!formValidation()) return;
        randomChild.remove(bi.es1resp.getSelectedItemPosition() - 1);
//        childListAll.remove(bi.es1resp.getSelectedItemPosition() - 1);
        startActivity(new Intent(this, SectionCBActivity.class));
        finish();


    }

    public void btnEnd(View view) {
        finish();
        startActivity(new Intent(this, EndingActivity.class).putExtra("complete", false));
    }

    private boolean formValidation() {
        return Validator.emptyCheckingContainer(this, bi.GrpName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainApp.lockScreen(this);
    }
}