adb push TPVICS_R2.db.db /sdcard/TPVICS_R2.db.db
adb shell "run-as edu.aku.hassannaqvi.tpvics_r2 chmod 666 /data/data/edu.aku.hassannaqvi.tpvics_r2/databases/TPVICS_R2.db
adb shell rm /data/data/edu.aku.hassannaqvi.tpvics_r2/databases/TPVICS_R2.db
adb -d shell "run-as edu.aku.hassannaqvi.tpvics_r2 cat /sdcard/TPVICS_R2.db > /data/data/edu.aku.hassannaqvi.tpvics_r2/databases/TPVICS_R2.db
adb shell "run-as edu.aku.hassannaqvi.tpvics_r2 chmod 600 /data/data/edu.aku.hassannaqvi.tpvics_r2/databases/TPVICS_R2.db
@echo Databased Pulled!