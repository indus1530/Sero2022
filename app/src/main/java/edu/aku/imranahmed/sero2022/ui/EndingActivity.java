package edu.aku.imranahmed.sero2022.ui;

import static edu.aku.imranahmed.sero2022.core.MainApp.form;

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
import edu.aku.imranahmed.sero2022.databinding.ActivityEndingBinding;
import edu.aku.imranahmed.sero2022.models.EntryLog;


public class EndingActivity extends AppCompatActivity {

    ActivityEndingBinding bi;
    int checkToEnable;
    private DatabaseHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(MainApp.langRTL ? R.style.AppThemeUrdu : R.style.AppThemeEnglish1);
        bi = DataBindingUtil.setContentView(this, R.layout.activity_ending);
        bi.setForm(form);

        setSupportActionBar(bi.toolbar);
        //setTitle(R.string.section1mainheading);
        if (MainApp.superuser)
            bi.btnContinue.setText("End Review");

        db = MainApp.appInfo.dbHelper;
        boolean check = getIntent().getBooleanExtra("complete", false);

        checkToEnable = getIntent().getIntExtra("checkToEnable", 0);

        bi.istatusa.setEnabled(check);
        bi.istatusb.setEnabled(check);
        bi.istatusc.setEnabled(check);
        bi.istatusd.setEnabled(check);
        bi.istatuse.setEnabled(check);
        bi.istatusf.setEnabled(check);
        bi.istatusg.setEnabled(check);
        bi.istatush.setEnabled(check);
        bi.istatusi.setEnabled(check);
        bi.istatus96.setEnabled(check);

        switch (checkToEnable) {
            case 1:
                bi.istatusa.setEnabled(!check);
                break;
            case 2:
                bi.istatusb.setEnabled(!check);
                break;
            case 3:
                bi.istatusc.setEnabled(!check);
                break;
            case 4:
                bi.istatusd.setEnabled(!check);
                break;
            case 5:
                bi.istatuse.setEnabled(!check);
                break;
            case 6:
                bi.istatusf.setEnabled(!check);
                break;
            case 7:
                bi.istatusg.setEnabled(!check);
                break;
            case 8:
                bi.istatush.setEnabled(!check);
                break;
            case 9:
                bi.istatusi.setEnabled(!check);
                break;
            case 10:
                bi.istatus96.setEnabled(!check);
                break;
            case 11:
                bi.istatusg.setEnabled(!check);
                bi.istatush.setEnabled(!check);
                break;
            default:
                bi.istatusa.setEnabled(check);
                bi.istatusb.setEnabled(!check);
                bi.istatusc.setEnabled(!check);
                bi.istatusd.setEnabled(!check);
                bi.istatuse.setEnabled(!check);
                bi.istatusf.setEnabled(!check);
                bi.istatusg.setEnabled(!check);
                bi.istatush.setEnabled(check);
                bi.istatusi.setEnabled(check);
                bi.istatus96.setEnabled(!check);

        }

    }

    private void saveDraft() {
        form.setiStatus(bi.istatusa.isChecked() ? "1"
                : bi.istatusb.isChecked() ? "2"
                : bi.istatusc.isChecked() ? "3"
                : bi.istatusd.isChecked() ? "4"
                : bi.istatuse.isChecked() ? "5"
                : bi.istatusf.isChecked() ? "6"
                : bi.istatusg.isChecked() ? "7"
                : bi.istatush.isChecked() ? "8"
                : bi.istatusi.isChecked() ? "9"
                : bi.istatus96.isChecked() ? "96"
                : "-1");
        form.setiStatus96x(bi.istatus96x.getText().toString());
        // form.setEndTime(new SimpleDateFormat("dd-MM-yy HH:mm", Locale.ENGLISH).format(new Date().getTime()));
    }


    public void btnEnd(View view) {
        bi.llbtn.setVisibility(View.GONE);
        new Handler().postDelayed(() -> bi.llbtn.setVisibility(View.VISIBLE), 5000);
        if (!formValidation()) return;
        saveDraft();
        if (UpdateDB()) {

            recordEntry();
            finish();
            setResult(RESULT_OK);
           /* Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
           */
            Toast.makeText(this, "Data has been updated.", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Error in updating Database.", Toast.LENGTH_SHORT).show();
        }
    }


    private void recordEntry() {

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
        if (MainApp.superuser) return true;
        try {
            db.updatesFormColumn(TableContracts.FormsTable.COLUMN_SHH, form.sHHtoString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "JSONException(Forms): ", Toast.LENGTH_SHORT).show();
        }
        int updcount = db.updatesFormColumn(TableContracts.FormsTable.COLUMN_ISTATUS, form.getiStatus());
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
