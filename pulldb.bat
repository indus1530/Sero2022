del mccp2-gadap.db
adb -d shell "run-as edu.aku.hassannaqvi.tpvics_r2 cat /data/data/edu.aku.hassannaqvi.tpvics_r2/databases/TPVICS_R2.db > /sdcard/TPVICS_R2.db"
adb pull /sdcard/TPVICS_R2.db.db
"C:\Program Files\Google\Chrome\Application\chrome.exe"  --profile-directory=Default --app-id=bponbdjkefbmgkfiiphhabghkkfocook
@echo Databased Pulled!
