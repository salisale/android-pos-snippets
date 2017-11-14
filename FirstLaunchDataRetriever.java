package com.example.havensbee.myposapp.retriever;

import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This pulls device verification data from our cloud server
 * PHP returns json specifying the verification result
 * {"status":"-1"} if no record/incorrect username
 * {"status":"-2","claim_date":"xx-xx-xx xx:xx:xx"} if key has already been claimed
 * {"status":"0"} if password is incorrect
 * {"status":"1","mac_id":"Dxxx") if success
 */
public class FirstLaunchDataRetriever {
    String getVerifLoginFile;
    public boolean USER_DOES_NOT_EXIST;
    public boolean DB_CONNECTION_SUCCESSFUL = true;
    public boolean PASSWORD_CORRECT;
    public boolean ALREADY_CLAIMED;
    public boolean NO_DEVICE_ID_EXIST;
    public String CLAIM_DATE; // For claimed user and password
    public String MAC_ID; // Machine ID; if successfully claimed
    public int PLAN; // 1: permanent, 2: monthly, 3: trial version 
    public String PLAN2_EXP_DATE, PLAN3_EXP_DATE; // Expiration date
    Context context;


    public FirstLaunchDataRetriever(Context context, String verif_id, String verif_pass, String cloudIP) {
        getVerifLoginFile = "http://" + cloudIP + "/mypos_claim.php";
        this.context = context;

        String imei = getIMEI()==null? "unknown":getIMEI();
        String android_id = getAndroidID()==null? "unknown":getAndroidID();
        String serial = getSerial()==null? "unknown":getSerial();
        NO_DEVICE_ID_EXIST = imei.equals("unknown")
                && android_id.equals("unknown") &&
                serial.equals("unknown");

        if (NO_DEVICE_ID_EXIST) {
            // We don't authorize the use of this app
        } else {

            String json = cloudVerif(verif_id, verif_pass, imei, android_id, serial);

            if (DB_CONNECTION_SUCCESSFUL) {
                // This also gets field for CLAIM_DATE if already claimed
                String status = getSTATUS(json.trim());
                USER_DOES_NOT_EXIST = status.equals("-1");
                PASSWORD_CORRECT = status.equals("1");
                ALREADY_CLAIMED = status.equals("-2");
            }
        }
    }

    /**
     * @return json string
     */
    private String cloudVerif(String verif_id, String verif_pass, String imei, String android_id,
                              String serial) {
            String urlParameters = "verif_id=" + verif_id + "&verif_pass=" + verif_pass +
                    "&imei=" + imei + "&android_id=" + android_id + "&serial=" + serial;
            Log.i(getClass().getSimpleName(), "urlParam:" + urlParameters);

            String out = "";
            try {
                URL url = new URL(getVerifLoginFile);
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(6000);
                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

                writer.write(urlParameters);
                writer.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                out = br.readLine();

                if (out==null) {DB_CONNECTION_SUCCESSFUL = false;}

                writer.close();
                br.close();

            } catch (IOException e) {
                DB_CONNECTION_SUCCESSFUL = false;
                Crashlytics.logException(e);
            }
            return out;

    }

    private String getSTATUS(String rawData) {
        String out = "";
        try {
            JSONObject json_data = new JSONObject(rawData);
            out = json_data.getString("status");
            if (out.equals("-2")) {
                CLAIM_DATE = json_data.getString("claim_date");
            } else if (out.equals("1")) { // return mac_id and plan
                MAC_ID = json_data.getString("mac_id");
                PLAN = json_data.getInt("plan");

                PLAN2_EXP_DATE = json_data.getString("plan2_exp_date");
                PLAN3_EXP_DATE = json_data.getString("plan3_exp_date");

            }
        } catch (JSONException e) { // IOException above already handles failed connection
            Crashlytics.logException(e);
        }
        return out;
    }
    private String getIMEI() {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getDeviceId();
    }
    private String getAndroidID() {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
    private String getSerial() {
        return android.os.Build.SERIAL;
    }
}
