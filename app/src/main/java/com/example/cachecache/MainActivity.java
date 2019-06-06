package com.example.cachecache;

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
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static final int GSM_PERM_CODE = 1;
    static final int NET_PERM_CODE = 2;
    static final String OpenCellIdToken = "ff4908e586f1b4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("auth", "i'm here");

    }

    /**
     * Convert wifi signal frequency to wifi channel
     * @param freq, the signal frequency in decibels
     * @return the wifi channel corresponding to the frequency
     */
    public int freqToChannel(int freq) {
        if (freq == 2484)
            return 14;

        if (freq < 2484)
            return (freq - 2407) / 5;

        return freq/5 - 1000;
    }

    public WifiInfo getWifiInfo(Context context) {
        // If net state permission isn't granted, ask user
        if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    NET_PERM_CODE);
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_WIFI_STATE},
                    NET_PERM_CODE);
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo == null) {
            return null;
        }

        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            return wifiManager.getConnectionInfo();
        }
        else {
            return null;
        }
    }

    public void getLocation(View view) throws JSONException {
        // If location permission isn't granted, ask user
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    GSM_PERM_CODE);
        }

        Log.i("auth", "i'm here");
        JSONArray wifiNetworks = new JSONArray();
        JSONArray cellNetworks = new JSONArray();
        JSONObject request = new JSONObject();
        request.put("wifi", wifiNetworks);
        request.put("cells", cellNetworks);
        Context context = getApplicationContext();
        WifiInfo wifiInfos = getWifiInfo(context);
        final TelephonyManager telephony = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

//        API >= 22
//        final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
//        final SubscriptionInfo subscriptionInfo = subscriptionManager.getActiveSubscriptionInfoList().get(0);
//        request.put("mcc", subscriptionInfo.getMcc());
//        request.put("mnc", subscriptionInfo.getMnc());

        request.put("token", OpenCellIdToken);
        // Get list of all cell info the device is registered on
        List<CellInfo> cells = telephony.getAllCellInfo();
        if (! cells.isEmpty()) {
            for (CellInfo cell : cells) {
                System.out.println(cell.toString());
                if (cell instanceof CellInfoGsm) {
                    CellIdentityGsm cellIdentity = ((CellInfoGsm) cell).getCellIdentity();
                    request.put("mcc", cellIdentity.getMcc());
                    request.put("mnc", cellIdentity.getMnc());
                } else if (cell instanceof CellInfoWcdma) {
                    CellIdentityWcdma cellIdentity = ((CellInfoWcdma) cell).getCellIdentity();
                    request.put("mcc", cellIdentity.getMcc());
                    request.put("mnc", cellIdentity.getMnc());
                } else if (cell instanceof CellInfoLte) {
                    CellIdentityLte cellIdentity = ((CellInfoLte) cell).getCellIdentity();
                    request.put("mcc", cellIdentity.getMcc());
                    request.put("mnc", cellIdentity.getMnc());
                }
            }
        }
        JSONObject cell = new JSONObject();

        // Get wifi networks infos
        JSONObject wifi = new JSONObject();
        wifi.put("bssid", wifiInfos.getBSSID());
        wifi.put("channel", freqToChannel(wifiInfos.getFrequency()));
        wifi.put("frequency", wifiInfos.getFrequency());
        wifi.put("signal", WifiManager.calculateSignalLevel(wifiInfos.getRssi(), 100));

        System.out.println(request);
        System.out.println(wifi.toString());
    }
}
