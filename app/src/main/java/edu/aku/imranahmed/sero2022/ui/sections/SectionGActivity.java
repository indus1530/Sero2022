package edu.aku.imranahmed.sero2022.ui.sections;

import static edu.aku.imranahmed.sero2022.core.MainApp.child;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.validatorcrawler.aliazaz.Validator;

import org.json.JSONException;

import edu.aku.imranahmed.sero2022.R;
import edu.aku.imranahmed.sero2022.contracts.TableContracts;
import edu.aku.imranahmed.sero2022.core.MainApp;
import edu.aku.imranahmed.sero2022.database.DatabaseHelper;
import edu.aku.imranahmed.sero2022.databinding.ActivitySectionGBinding;
import edu.aku.imranahmed.sero2022.ui.ChildEndingActivity;



public class SectionGActivity extends AppCompatActivity {


    private static final String TAG = "SectionGActivity";
    ActivitySectionGBinding bi;
    private DatabaseHelper db;
    private String requestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(MainApp.langRTL ? R.style.AppThemeUrdu : R.style.AppThemeEnglish1);
        bi = DataBindingUtil.setContentView(this, R.layout.activity_section_g);
        setSupportActionBar(bi.toolbar);
        db = MainApp.appInfo.dbHelper;

        child.setEc13cline(child.getEc13());
        child.setEc14cname(child.getEc14());
        bi.setChild(child);

        Intent intent = getIntent();
        requestCode = intent.getStringExtra("requestCode");
    }

    private boolean updateDB() {
        if (MainApp.superuser) return true;

        db = MainApp.appInfo.getDbHelper();
        long updcount = 0;
        try {
            updcount = db.updatesChildColumn(TableContracts.ChildTable.COLUMN_SG, child.sGtoString());
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
        if (!formValidation()) return;
        // saveDraft();
        if (updateDB()) {

                Intent forwardIntent = new Intent(this, ChildEndingActivity.class).putExtra("complete", false);
            forwardIntent.putExtra("requestCode", requestCode);
            forwardIntent.putExtra("complete", true);
                //forwardIntent.putExtra("checkToEnable", 3);
                forwardIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                setResult(RESULT_OK, forwardIntent);
                startActivity(forwardIntent);
                finish();
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
        return Validator.emptyCheckingContainer(this, bi.GrpName);
    }


    @Override
    public void onBackPressed() {
        //Toast.makeText(this, "Back Press Not Allowed", Toast.LENGTH_SHORT).show();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("requestCode", requestCode);
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();

                String strResult = result.getContents();
                bi.g04.setText(strResult);
                if (!checkQR())
                    bi.EndButton.setVisibility(View.GONE);

/*                try {
                    String[] arrContents = strResult.split("-");
                    bi.f1asiteA.setChecked(arrContents[2].equals("S1"));
                    bi.f1asiteB.setChecked(arrContents[2].equals("S2"));
                    bi.f1asiteC.setChecked(arrContents[2].equals("S3"));
                    bi.f1asiteD.setChecked(arrContents[2].equals("S4"));
                } catch (Exception e) {
                    Toast.makeText(this, "Invalid ID", Toast.LENGTH_SHORT).show();
                    bi.fldGrpCVQR.setVisibility(View.GONE);
                }*/
//                bi.f1aspecID.setText("Ctry: " + arrContents[0] + " | " + "City: " + arrContents[1] + " | " + "Site: " + arrContents[2] + " | " + "ID: " + arrContents[3]);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean checkQR() {
        if (db.checkSampleId_G04(bi.g04.getText().toString())) {
            Toast.makeText(this, "Already Exist", Toast.LENGTH_SHORT).show();
            return false;
        } else {
//            Toast.makeText(this, "Not Exist", Toast.LENGTH_SHORT).show();
            bi.EndButton.setVisibility(View.VISIBLE);
            return true;
        }
    }

    public void scanQR(View view) {
        // Scan QR Code
        bi.EndButton.setVisibility(View.GONE);
        new IntentIntegrator(this).initiateScan();
    }


}