package com.khushnish.youtubeexample;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class Utils {

    public static boolean checkInternetConnection(final Context context) {
        if (context != null) {
            final ConnectivityManager mgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mgr != null) {

                final NetworkInfo mobileInfo = mgr
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                final NetworkInfo wifiInfo = mgr
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (wifiInfo != null && wifiInfo.isAvailable()
                        && wifiInfo.isAvailable() && wifiInfo.isConnected()) {

                    final WifiManager wifiManager = (WifiManager) context
                            .getSystemService(Context.WIFI_SERVICE);
                    final WifiInfo wifiInfoStatus = wifiManager
                            .getConnectionInfo();
                    final SupplicantState supState = wifiInfoStatus
                            .getSupplicantState();

                    if (String.valueOf(supState).equalsIgnoreCase("COMPLETED")
                            || String.valueOf(supState).equalsIgnoreCase(
                            "ASSOCIATED")) {
                        // WiFi is connected
                        return true;
                    }
                }

                if (mobileInfo != null && mobileInfo.isAvailable()
                        && mobileInfo.isConnected()) {
                    // Mobile Network is connected
                    return true;
                }
            }
        }
        return false;
    }

    public static void displayDialog(String title, String msg,
                                     final Context context,
                                     String postiveBtnString) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setPositiveButton(postiveBtnString,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
