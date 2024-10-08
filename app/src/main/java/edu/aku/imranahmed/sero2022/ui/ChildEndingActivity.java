package edu.aku.imranahmed.sero2022.ui;

import static edu.aku.imranahmed.sero2022.core.MainApp.child;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.validatorcrawler.aliazaz.Validator;

import net.sqlcipher.database.SQLiteException;

import org.json.JSONException;

import edu.aku.imranahmed.sero2022.R;
import edu.aku.imranahmed.sero2022.contracts.TableContracts;
import edu.aku.imranahmed.sero2022.core.MainApp;
import edu.aku.imranahmed.sero2022.database.DatabaseHelper;
import edu.aku.imranahmed.sero2022.databinding.ActivityChildEndingBinding;
import edu.aku.imranahmed.sero2022.models.EntryLog;
import edu.aku.imranahmed.sero2022.ui.sections.ChildSelectionActivity;

public class ChildEndingActivity extends AppCompatActivity {

    ActivityChildEndingBinding bi;
    int checkToEnable;
    private DatabaseHelper db;
    private String requestCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        MainApp.changeLanguage(this, MainApp.selectedLanguage);
        setTheme(MainApp.langRTL ? R.style.AppThemeUrdu : R.style.AppThemeEnglish1);
        bi = DataBindingUtil.setContentView(this, R.layout.activity_child_ending);
        bi.setChild(MainApp.child);
        Intent intent = getIntent();
        requestCode = intent.getStringExtra("requestCode");
        setSupportActionBar(bi.toolbar);
        //setTitle(R.string.section1mainheading);
        if (MainApp.superuser)
            bi.btnContinue.setText("End Review");

        db = MainApp.appInfo.dbHelper;
        boolean check = getIntent().getBooleanExtra("complete", false);

        checkToEnable = getIntent().getIntExtra("checkToEnable", 0);

        bi.ec2201.setEnabled(check);
        bi.ec2202.setEnabled(check);
        bi.ec2203.setEnabled(check);
        bi.ec2204.setEnabled(check);
        bi.ec2205.setEnabled(check);
        bi.ec2206.setEnabled(check);
        bi.ec2296.setEnabled(check);

        switch (checkToEnable) {
            case 1:
                bi.ec2201.setEnabled(!check);
                break;
            case 2:
                bi.ec2202.setEnabled(!check);
                break;
            case 3:
                bi.ec2203.setEnabled(!check);
                break;
            case 4:
                bi.ec2204.setEnabled(!check);
                break;
            case 5:
                bi.ec2205.setEnabled(!check);
                break;
            case 6:
                bi.ec2206.setEnabled(!check);
                break;
            case 7:
                bi.ec2296.setEnabled(!check);
                break;

            default:
                bi.ec2201.setEnabled(!check);
                bi.ec2202.setEnabled(!check);
                bi.ec2203.setEnabled(!check);
                bi.ec2204.setEnabled(!check);
                bi.ec2205.setEnabled(!check);
                bi.ec2206.setEnabled(!check);
                bi.ec2296.setEnabled(!check);

        }


    }

    private void saveDraft() {

    }


    public void btnEnd(View view) {
        bi.llbtn.setVisibility(View.GONE);
        new Handler().postDelayed(() -> bi.llbtn.setVisibility(View.VISIBLE), 5000);
        if (!formValidation()) return;
        saveDraft();
        if (UpdateDB()) {
            recordEntry();
            finish();
            if (MainApp.randomChild.size() > 0) {
                startActivity(new Intent(this, ChildSelectionActivity.class));
            } else {
                startActivity(new Intent(this, EndingActivity.class).putExtra("checkToEnable", 1));
            }
            Toast.makeText(this, "Data has been updated.", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Error in updating Database.", Toast.LENGTH_SHORT).show();
        }
    }

    private void   recordEntry() {

        EntryLog entryLog = new EntryLog();
        entryLog.populateMeta();
        Long rowId = null;
        try {
            rowId = db.addEntryLog(entryLog);
        } catch (SQLiteException e) {
            Toast.makeText(this, "SQLiteException(EntryLog)" + entryLog, Toast.LENGTH_SHORT).show();
        }
        if (rowId != -1) {
            entryLog.setId(String.valueOf(rowId));
            entryLog.setUid(entryLog.getDeviceId() + entryLog.getId());
            db.updatesEntryLogColumn(TableContracts.EntryLogTable.COLUMN_UID, entryLog.getUid(), entryLog.getId());
        } else {
            Toast.makeText(this, R.string.upd_db_error, Toast.LENGTH_SHORT).show();

        }

    }


    private boolean UpdateDB() {
        int updcount = 0;
        if (MainApp.superuser) return true;
        try {
            updcount = db.updatesChildColumn(TableContracts.ChildTable.COLUMN_SCB, child.sCBtoString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSONException(Forms): ", Toast.LENGTH_SHORT).show();
        }
        updcount = db.updatesChildColumn(TableContracts.ChildTable.COLUMN_CSTATUS, child.getCStatus());
        return updcount > 0;
    }


    private boolean formValidation() {
        return Validator.emptyCheckingContainer(this, bi.fldGrpEnd);
    }


    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Back Press Not Allowed", Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED); finish();
    }


}