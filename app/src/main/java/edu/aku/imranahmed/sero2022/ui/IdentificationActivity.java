package edu.aku.imranahmed.sero2022.ui;

import static edu.aku.imranahmed.sero2022.core.MainApp.form;
import static edu.aku.imranahmed.sero2022.core.MainApp.randomChild;
import static edu.aku.imranahmed.sero2022.core.MainApp.selectedCluster;
import static edu.aku.imranahmed.sero2022.core.MainApp.selectedHousehold;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.validatorcrawler.aliazaz.Validator;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import edu.aku.imranahmed.sero2022.R;
import edu.aku.imranahmed.sero2022.contracts.TableContracts;
import edu.aku.imranahmed.sero2022.core.MainApp;
import edu.aku.imranahmed.sero2022.database.DatabaseHelper;
import edu.aku.imranahmed.sero2022.databinding.ActivityIdentificationBinding;
import edu.aku.imranahmed.sero2022.models.Form;
import edu.aku.imranahmed.sero2022.models.RandomHH;
import edu.aku.imranahmed.sero2022.ui.sections.ConsentActivity;


public class IdentificationActivity extends AppCompatActivity {

    private static final String TAG = "IdentificationActivity";
    ActivityIdentificationBinding bi;
    private DatabaseHelper db;
    private int c, c1;
    private ArrayList<String> childNames, childGrp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(MainApp.langRTL ? R.style.AppThemeUrdu : R.style.AppThemeEnglish1);
        bi = DataBindingUtil.setContentView(this, R.layout.activity_identification);
        db = MainApp.appInfo.dbHelper;

        bi.btnContinue.setText(R.string.open_hh_form);
        if (MainApp.superuser) bi.btnContinue.setText("Review Form");
        MainApp.form = new Form();

        bi.hh12.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                c = charSequence.length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                bi.headhh.setVisibility(View.GONE);
                bi.llchildName1.setVisibility(View.GONE);
                bi.llchildName2.setVisibility(View.GONE);
                bi.cvhh12a.setVisibility(View.GONE);
                bi.hh12a.clearCheck();
                bi.hh16a.setText("");
                bi.childName.setText("");
                bi.childgrp.setText("");
                bi.childName2.setText("");
                bi.childgrp2.setText("");
                c1 = charSequence.length();
                String txt = charSequence.toString();
                if (c1 > 1 && charSequence.charAt(1) != '-') {
                    txt = txt.charAt(0) + "-" + txt.substring(1);
                    bi.hh12.setText(txt);
                }

                if (c1 > 6 && charSequence.charAt(6) != '-') {
                    txt = txt.substring(0, 6) + "-" + txt.substring(6);
                    bi.hh12.setText(txt);
                }
                bi.hh12.setSelection(bi.hh12.getText().length());
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

        bi.hh12a.setOnCheckedChangeListener((radioGroup, i) -> {
            form.setHh12a("");
            if (bi.hh12aa.isChecked()) form.setHh12a("1");
            if (bi.hh12ab.isChecked()) form.setHh12a("2");
        });

    }


    private boolean insertNewRecord() {
        if (!form.getUid().equals("") || MainApp.superuser) return true;

        MainApp.form.populateMeta();
        setGPS();
        long rowId = 0;
        try {
            rowId = db.addForm(MainApp.form);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.db_excp_error + " FORM-add", Toast.LENGTH_SHORT).show();
            return false;
        }
        MainApp.form.setId(String.valueOf(rowId));
        if (rowId > 0) {
            MainApp.form.setUid(MainApp.form.getDeviceId() + MainApp.form.getId());
            db.updatesFormColumn(TableContracts.FormsTable.COLUMN_UID, MainApp.form.getUid());
            return true;
        } else {
            Toast.makeText(this, R.string.upd_db_error + " FORM-update", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    private boolean updateDB() {
        if (MainApp.superuser) return true;

        db = MainApp.appInfo.getDbHelper();
        long updcount = 0;
        try {
            updcount = db.updatesFormColumn(TableContracts.FormsTable.COLUMN_SHH, form.sHHtoString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, R.string.upd_db + e.getMessage());
            Toast.makeText(this, R.string.upd_db + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (updcount > 0) return true;
        else {
            Toast.makeText(this, R.string.upd_db_error, Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    private boolean formValidation() {
        return Validator.emptyCheckingContainer(this, bi.GrpName);
    }

    public void checkHousehold(View view) {
        if (!formValidation()) return;
    }

    public void btnContinue(View view) {

        if (!formValidation()) return;
        try {
            hhExists();
        } catch (JSONException e) {
            Log.d(TAG, getString(R.string.hh_exists_form) + e.getMessage());
            Toast.makeText(this, getString(R.string.hh_exists_form) + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        if (MainApp.form.getSynced().equals("1") && !MainApp.superuser) { // Do not allow synced form to be edited
            Toast.makeText(this, "This form has been locked.", Toast.LENGTH_SHORT).show();
        } else {
            randomChild = db.getRandomChildByhhid(MainApp.hhid);
            finish();
            if (MainApp.form.getUid().equals("") ? insertNewRecord() : updateDB()) {
                if (bi.hh12ab.isChecked()) {
                    Intent endingActivityIntent = new Intent(this, EndingActivity.class);
                    endingActivityIntent.putExtra("complete", false);
                    endingActivityIntent.putExtra("checkToEnable", 11);
                    startActivity(endingActivityIntent);
                } else {
                    startActivity(new Intent(this, ConsentActivity.class));
                }
            }
        }

    }


    public void btnEnd(View view) {
        finish();
    }


    private boolean hhExists() throws JSONException {
        MainApp.form = new Form();
        MainApp.form = db.getFormByhhid();
        return MainApp.form != null;
    }


    public void checkEBNumber(View view) {
        if (!formValidation()) return;

        bi.hh06.setText(null);      //  Province
        bi.hh07.setText(null);      //  District
        bi.hh08.setText(null);      //  Tehsil
        bi.hh09.setText(null);      //  City/Village
        bi.hh16a.setText(null);
        bi.childName.setText(null);
        bi.childgrp.setText(null);
        bi.childName2.setText(null);
        bi.childgrp2.setText(null);

        bi.checkHh06.setChecked(false);
        bi.checkHh07.setChecked(false);
        bi.checkHh08.setChecked(false);
        bi.checkHh09.setChecked(false);

        bi.fldGrpIdentifier.setVisibility(View.GONE);
        bi.headhh.setVisibility(View.GONE);
        bi.llchildName1.setVisibility(View.GONE);
        bi.llchildName2.setVisibility(View.GONE);
        bi.cvhh12a.setVisibility(View.GONE);
        bi.hh12a.clearCheck();

        bi.btnContinue.setBackgroundTintList(ContextCompat.getColorStateList(IdentificationActivity.this, R.color.gray));
        bi.btnContinue.setEnabled(false);

        selectedHousehold = null;

        selectedCluster = db.getClusterByEBNum(bi.hh05.getText().toString());

        if (selectedCluster != null) {
            String[] geoarea = selectedCluster.getGeoarea().split("\\|");
            bi.hh06.setText(geoarea[0]);    //  Province
            bi.hh07.setText(geoarea[1]);    //  District
            bi.hh08.setText(geoarea[2]);    //  Tehsil
            bi.hh09.setText(geoarea[3]);    //  City/Village
            bi.fldGrpIdentifier.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Enumeration Block not found", Toast.LENGTH_SHORT).show();


        }
    }

    public void checkHH(View view) {
        if (!formValidation()) return;

        bi.hh16a.setText(null);
        bi.childName.setText(null);
        bi.childgrp.setText(null);
        bi.childName2.setText(null);
        bi.childgrp2.setText(null);
        bi.headhh.setVisibility(View.GONE);
        bi.llchildName1.setVisibility(View.GONE);
        bi.llchildName2.setVisibility(View.GONE);
        bi.cvhh12a.setVisibility(View.GONE);
        bi.hh12a.clearCheck();

        bi.btnContinue.setBackgroundTintList(ContextCompat.getColorStateList(IdentificationActivity.this, R.color.gray));
        bi.btnContinue.setEnabled(false);

        MainApp.hhid = bi.hh12.getText().toString();

        selectedHousehold = db.getRandomByhhid(bi.hh12.getText().toString());
        if (selectedHousehold != null) {
            bi.hh16a.setText(selectedHousehold.getHhhead());    // Name of Head

            List<RandomHH> randomHH = db.getRandomChildByhhid(MainApp.hhid);
            childNames = new ArrayList<>();
            childGrp = new ArrayList<>();

            for (RandomHH random : randomHH) {
                childNames.add(random.getChildName());
                childGrp.add(random.getChildGrp());
            }
//            bi.childName.setText(selectedHousehold.getChildName());
            bi.childName.setText(childNames.get(0));
            bi.childgrp.setText(childGrp.get(0).equals("1") ? "6-11" : "12-23");

            if (childNames.size() > 1) {
                bi.llchildName2.setVisibility(View.VISIBLE);
                bi.childName2.setText(childNames.get(1));
                bi.childgrp2.setText(childGrp.get(1).equals("1") ? "6-11" : "12-23");
            }

            bi.headhh.setVisibility(View.VISIBLE);
            bi.llchildName1.setVisibility(View.VISIBLE);
            bi.cvhh12a.setVisibility(View.VISIBLE);
            bi.btnContinue.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorAccent));
            bi.btnContinue.setEnabled(true);
        } else {
            Toast.makeText(this, "Household not found", Toast.LENGTH_SHORT).show();
        }

    }

    public void setGPS() {
        SharedPreferences GPSPref = getSharedPreferences("GPSCoordinates", Context.MODE_PRIVATE);
        try {
            String lat = GPSPref.getString("Latitude", "0");
            String lang = GPSPref.getString("Longitude", "0");
            String acc = GPSPref.getString("Accuracy", "0");
            if (lat.equals("0") && lang.equals("0"))
                Toast.makeText(this, "Could not obtained points", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "Points set", Toast.LENGTH_SHORT).show();
            String date = DateFormat.format("dd-MM-yyyy HH:mm", Long.parseLong(GPSPref.getString("Time", "0"))).toString();
            form.setGpsLat(lat);
            form.setGpsLng(lang);
            form.setGpsAcc(acc);
            form.setGpsDT(date);
        } catch (Exception e) {
            Log.e(TAG, "setGPS: " + e.getMessage());
        }
    }


}
