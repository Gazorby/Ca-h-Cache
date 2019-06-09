package com.mimi.cachecache;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HiddenUserActivity extends AppCompatActivity {
    private int mInterval = 10000; // 10 seconds by default, can be changed later
    private Handler mHandler;
    private SessionManager session;
    // Codes used to ask permissions
    private static final int PERM_GSM_CODE = 1;
    private static final int PERM_NETWORK_CODE = 2;
    private static final int PERM_INTERNET_CODE = 3;

    private static final String OpenCellIdToken = "ff4908e586f1b4";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide);
        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    PERM_INTERNET_CODE);
        }
        // If net state permission isn't granted, ask user
        if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    PERM_NETWORK_CODE);
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                    PERM_NETWORK_CODE);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERM_GSM_CODE);
        }

        session = new SessionManager(getApplicationContext());
        session.createLoginSession(UserCategory.HIDDEN.toString());
    }

    /**
     * Update location periodically
     */
    private Runnable updateLocation = new Runnable() {
        @Override
        public void run() {
            try {
                db.collection("User")
                        .document(session.getUserDetails().get("userId"))
                        .set(getLocation())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("LOCATION UPDATE", session.getUserDetails().get("userId"));
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("WARNING", "Error updating location for id " +
                                        session.getUserDetails().get("userId"), e);
                            }
                        });
            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(updateLocation, mInterval);
            }
        }
    };

    /**
     * Start to run task periodically
     */
    private void startUpdatingLocation() {
        updateLocation.run();
    }

    /**
     * Stop to run task periodically
     */
    private void stopUpdatingLocation() {
        mHandler.removeCallbacks(updateLocation);
    }

    public void onDestroy() {
        super.onDestroy();
        stopUpdatingLocation();
    }

    public void start(View view) {
        mHandler = new Handler();
        startUpdatingLocation();
    }

    /**
     * Convert wifi signal frequency to wifi channel
     * @param freq, the signal frequency in decibels
     * @return the wifi channel corresponding to the frequency
     */
    private int freqToChannel(int freq) {
        if (freq == 2484)
            return 14;

        if (freq < 2484)
            return (freq - 2407) / 5;

        return freq / 5 - 1000;
    }

    /**
     * Get information on the connected wifi network
     * @param context, Application context
     * @return, WifiInfo object
     */
    private WifiInfo getWifiInfo(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo == null) {
            return null;
        }

        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            return wifiManager.getConnectionInfo();
        } else {
            return null;
        }
    }

    /**
     * Get Json array of information about all connected cells of the given radio type
     * @param radio, type of radio : gsm, cdma, wcdma, lte
     * @return A JSONArray object containing JSONObjects that describe all connected cells of the given radio
     * @throws JSONException
     */
    private JSONArray getCellIdArray(Radio radio) throws JSONException {
        // Get list of all cell info the device is registered on
        final TelephonyManager telephony = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        JSONArray cellArr = new JSONArray();


        List<CellInfo> cellsList = telephony.getAllCellInfo();
        if (!cellsList.isEmpty()) {
            for (CellInfo cell : cellsList) {
                JSONObject cellID = new JSONObject();
                if (cell instanceof CellInfoGsm && radio.equals(Radio.GSM)) {
                    CellIdentityGsm cellIdentity = ((CellInfoGsm) cell).getCellIdentity();
                    cellID.put("lac", cellIdentity.getLac());
                    cellID.put("cid", cellIdentity.getCid());
                    cellID.put("tA", ((CellInfoGsm) cell).getCellSignalStrength().getTimingAdvance());

                } else if (cell instanceof CellInfoWcdma && radio.equals(Radio.WCDMA)) {
                    CellIdentityWcdma cellIdentity = ((CellInfoWcdma) cell).getCellIdentity();
                    cellID.put("lac", cellIdentity.getLac());
                    cellID.put("cid", cellIdentity.getCid());
                } else if (cell instanceof CellInfoLte && radio.equals(Radio.LTE)) {
                    CellIdentityLte cellIdentity = ((CellInfoLte) cell).getCellIdentity();
                    cellID.put("lac", cellIdentity.getTac());
                    cellID.put("cid", cellIdentity.getCi());
                    cellID.put("psc", cellIdentity.getPci());
                    cellID.put("tA", ((CellInfoLte) cell).getCellSignalStrength().getTimingAdvance());
                }
                cellArr.put(cellID);
            }
        }
        return cellArr;
    }

    /**
     * Get network identifiers
     * @param radio, network type : gsm, wcdma, lte
     * @param code, mcc or mnc
     * @return the corresponding code
     */
    private Integer getNetworkId(Radio radio, NetworkCode code) {
        // Get list of all cell info the device is registered on
        final TelephonyManager telephony = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        // If location permission isn't granted, ask user
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERM_GSM_CODE);
        }

        CellInfo cell = telephony.getAllCellInfo().get(0);
        if (cell instanceof CellInfoGsm && radio.equals(Radio.GSM)) {
            CellIdentityGsm cellIdentity = ((CellInfoGsm) cell).getCellIdentity();

            if (code.equals(NetworkCode.MCC)) {
                return cellIdentity.getMcc();
            }
            else {
                return cellIdentity.getMnc();
            }

        } else if (cell instanceof CellInfoWcdma && radio.equals(Radio.WCDMA)) {
            CellIdentityWcdma cellIdentity = ((CellInfoWcdma) cell).getCellIdentity();
            if (code.equals(NetworkCode.MNC)) {
                return cellIdentity.getMcc();
            }
            else {
                return cellIdentity.getMnc();
            }
        } else if (cell instanceof CellInfoLte && radio.equals(Radio.LTE)) {
            CellIdentityLte cellIdentity = ((CellInfoLte) cell).getCellIdentity();
            if (code.equals(NetworkCode.MCC)) {
                return cellIdentity.getMcc();
            }
            else {
                return cellIdentity.getMnc();
            }
        } else {
            return null;
        }
    }

    /**
     * Get smartphone location based on cells and wifi networks
     * @throws JSONException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private Map getLocation() throws JSONException, ExecutionException, InterruptedException {
        Radio radio = Radio.GSM;
        Context context = getApplicationContext();
        WifiInfo wifiInfos = getWifiInfo(context);
        JSONArray wifiArr = new JSONArray();
        JSONArray cellArr = getCellIdArray(radio);
        JSONObject wifi = new JSONObject();
        JSONObject request = new JSONObject();

        // Get cells infos
        request.put("token", OpenCellIdToken);
        request.put("radio", radio.toString());
        request.put("mcc", getNetworkId(radio, NetworkCode.MCC));
        request.put("mnc", getNetworkId(radio, NetworkCode.MNC));
        request.put("wifi", wifiArr);
        request.put("cells", cellArr);

        // Get wifi networks infos
        wifi.put("bssid", wifiInfos.getBSSID());
        wifi.put("channel", freqToChannel(wifiInfos.getFrequency()));
        wifi.put("frequency", wifiInfos.getFrequency());
        wifi.put("signal", WifiManager.calculateSignalLevel(wifiInfos.getRssi(), 100));

        wifiArr.put(wifi);
        System.out.println(request);

        // Post request
        String url="https://us1.unwiredlabs.com/v2/process.php";
        PostRequest postRequest = new PostRequest(url, request);
        ExecutorService service =  Executors.newSingleThreadExecutor();
        Future<JSONObject> future = service.submit(postRequest);
        return PostRequest.jsonToMap(future.get());
    }
}
