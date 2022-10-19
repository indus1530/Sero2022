package edu.aku.imranahmed.sero2022.ui.sections;

import static edu.aku.imranahmed.sero2022.core.MainApp.child;
import static edu.aku.imranahmed.sero2022.core.MainApp.selectedChildName;
import static edu.aku.imranahmed.sero2022.core.MainApp.selectedChildPosition;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.validatorcrawler.aliazaz.Validator;

import org.json.JSONException;

import java.util.Calendar;

import edu.aku.imranahmed.sero2022.R;
import edu.aku.imranahmed.sero2022.contracts.TableContracts;
import edu.aku.imranahmed.sero2022.core.MainApp;
import edu.aku.imranahmed.sero2022.database.DatabaseHelper;
import edu.aku.imranahmed.sero2022.databinding.ActivitySectionCbBinding;
import edu.aku.imranahmed.sero2022.ui.ChildEndingActivity;

public class SectionCBActivity extends AppCompatActivity {


    private static final String TAG = "SectionCBActivity";
    ActivitySectionCbBinding bi;
    private DatabaseHelper db;
    private String requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(MainApp.langRTL ? R.style.AppThemeUrdu : R.style.AppThemeEnglish1);
        bi = DataBindingUtil.setContentView(this, R.layout.activity_section_cb);
        setSupportActionBar(bi.toolbar);
        db = MainApp.appInfo.dbHelper;
        try {
            child = db.getChildByUUid(selectedChildPosition, selectedChildName);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSONException(getChildByUUid): " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        bi.setChild(child);
        child.setChildLno(child.getChildLno().isEmpty() ? MainApp.selectedChildPosition : child.getChildLno());
        child.setChildName(child.getChildName().isEmpty() ? MainApp.selectedChildName : child.getChildName());


        Intent intent = getIntent();

        requestCode = intent.getStringExtra("requestCode");

        // Set min year for 23 - 6 months
        Calendar cal = Calendar.getInstance();
        // cal.add(Calendar.MONTH, -6);
        bi.cb03yy.setMaxvalue(Float.parseFloat(String.valueOf(cal.get(Calendar.YEAR))));
        cal.add(Calendar.MONTH, +6);
        cal.add(Calendar.MONTH, -23 - 6); // 6 months buffer
        bi.cb03yy.setMinvalue(Float.parseFloat(String.valueOf(cal.get(Calendar.YEAR))));
    }


    private boolean insertNewRecord() {
        if (!MainApp.child.getUid().equals("") || MainApp.superuser) return true;

        MainApp.child.populateMeta();
        setGPS();
        long rowId = 0;
        try {
            rowId = db.addChild(MainApp.child);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.db_excp_error, Toast.LENGTH_SHORT).show();
            return false;
        }
        MainApp.child.setId(String.valueOf(rowId));
        if (rowId > 0) {
            MainApp.child.setUid(MainApp.child.getDeviceId() + MainApp.child.getId());
            db.updatesChildColumn(TableContracts.ChildTable.COLUMN_UID, MainApp.child.getUid());
            return true;
        } else {
            Toast.makeText(this, R.string.upd_db_error, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean updateDB() {
        if (MainApp.superuser) return true;

        db = MainApp.appInfo.getDbHelper();
        long updcount = 0;
        try {
            updcount = db.updatesChildColumn(TableContracts.ChildTable.COLUMN_SCB, child.sCBtoString());
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

    public void btnContinue(View view) {
        bi.llbtn.setVisibility(View.GONE);
        new Handler().postDelayed(() -> bi.llbtn.setVisibility(View.VISIBLE), 5000);
        if (!formValidation()) return;
        /*if (!insertNewRecord()) return;*/
        // saveDraft();
        if (child.getUid().equals("") ? insertNewRecord() : updateDB()) {
            if (child.getEc21().equals("1")) {
                finish();
                startActivity(new Intent(this, SectionIM1Activity.class));
            } else {
                Intent forwardIntent = new Intent(this, ChildEndingActivity.class).putExtra("complete", false);
                forwardIntent.putExtra("requestCode", requestCode);
                forwardIntent.putExtra("checkToEnable", 3);
                forwardIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                setResult(RESULT_OK, forwardIntent);
                startActivity(forwardIntent);
                finish();
            }
        } else {
            Toast.makeText(this, R.string.fail_db_upd, Toast.LENGTH_SHORT).show();
        }

    }


    public void btnEnd(View view) {

        Intent returnIntent = new Intent();
        returnIntent.putExtra("requestCode", requestCode);
        setResult(RESULT_CANCELED, returnIntent);
        //startActivity(new Intent(this, EndingActivity.class).putExtra("complete", false));
        finish();
    }

    private boolean formValidation() {

        if (!Validator.emptyCheckingContainer(this, bi.GrpName)) {
            return false;
        }

        if (child.getCb01a().equals("77")) {
            if (!child.getCb01b().equals("77") && !child.getCb01b().equals("88")) {
                return Validator.emptyCustomTextBox(this, bi.cb01b, "Incorrect value, Only 77 or 88 is allowed.");
            }
        }
        if (child.getCb02a().equals("77")) {
            if (!child.getCb02b().equals("77") && !child.getCb02b().equals("88")) {
                return Validator.emptyCustomTextBox(this, bi.cb02b, "Incorrect value, Only 77 or 88 is allowed.");
            }
        }


        return true;

    }


    @Override
    public void onBackPressed() {
        //Toast.makeText(this, "Back Press Not Allowed", Toast.LENGTH_SHORT).show();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("requestCode", requestCode);
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    public void setGPS() {
        SharedPreferences GPSPref = getSharedPreferences("GPSCoordinates", Context.MODE_PRIVATE);
        try {
            String lat = GPSPref.getString("Latitude", "0");
            String lang = GPSPref.getString("Longitude", "0");
            String acc = GPSPref.getString("Accuracy", "0");

            if (lat == "0" && lang == "0") {
                Toast.makeText(this, "Could not obtained points", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Points set", Toast.LENGTH_SHORT).show();
            }

            String date = DateFormat.format("dd-MM-yyyy HH:mm", Long.parseLong(GPSPref.getString("Time", "0"))).toString();

            child.setGpsLat(lat);
            child.setGpsLng(lang);
            child.setGpsAcc(acc);
            child.setGpsDT(date); // Timestamp is converted to date above

//            Toast.makeText(this, "GPS set", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "setGPS: " + e.getMessage());
        }

    }
}