package edu.aku.imranahmed.sero2022.ui.sections;

import static edu.aku.imranahmed.sero2022.core.MainApp.form;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.validatorcrawler.aliazaz.Validator;

import org.json.JSONException;

import edu.aku.imranahmed.sero2022.R;
import edu.aku.imranahmed.sero2022.contracts.TableContracts;
import edu.aku.imranahmed.sero2022.core.MainApp;
import edu.aku.imranahmed.sero2022.database.DatabaseHelper;
import edu.aku.imranahmed.sero2022.databinding.ActivityConsentBinding;
import edu.aku.imranahmed.sero2022.ui.EndingActivity;

public class ConsentActivity extends AppCompatActivity {


    private static final String TAG = "ConsentActivity";
    ActivityConsentBinding bi;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(MainApp.langRTL ? R.style.AppThemeUrdu : R.style.AppThemeEnglish1);
        bi = DataBindingUtil.setContentView(this, R.layout.activity_consent);
        setSupportActionBar(bi.toolbar);
        db = MainApp.appInfo.dbHelper;
        bi.setForm(form);
        bi.consentTextView.setText(getString(R.string.hh18t, MainApp.user.getFullname()));
    }


    private boolean insertNewRecord() {
        if (!form.getUid().equals("") || MainApp.superuser) return true;

        MainApp.form.populateMeta();

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

    public void btnContinue(View view) {
        bi.llbtn.setVisibility(View.GONE);
        new Handler().postDelayed(() -> bi.llbtn.setVisibility(View.VISIBLE), 5000);
        if (!formValidation()) return;
        if (updateDB()) {
            finish();
            if (form.getHh18().equals("1")) {
                startActivity(new Intent(this, SectionRIActivity.class));
            } else {
                Intent endingActivityIntent = new Intent(this, EndingActivity.class);
                endingActivityIntent.putExtra("complete", false);
                endingActivityIntent.putExtra("checkToEnable", 4);
                startActivity(endingActivityIntent);
            }
        } else
            Toast.makeText(this, R.string.fail_db_upd, Toast.LENGTH_SHORT).show();
    }


    public void btnEnd(View view) {
        finish();
        startActivity(new Intent(this, EndingActivity.class).putExtra("complete", false));
    }

    private boolean formValidation() {
        return Validator.emptyCheckingContainer(this, bi.GrpName);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "Back Press Not Allowed", Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
    }
}