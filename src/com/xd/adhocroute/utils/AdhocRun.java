package com.xd.adhocroute.utils;

import java.net.InetAddress;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.xd.adhocroute.AdhocRouteApp;

public class AdhocRun {
	private Thread mScanTread;
	private Context context;
	private WifiManager wifiManager;
	private WifiConfigurationNew wifiConfig;
//	private boolean showScan = true;

	public AdhocRun(Context context) {
		this.context = context;
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiConfig = new WifiConfigurationNew();
	}

	public void startScanThread() {
		mScanTread = new Thread(new Runnable() {
			@Override
			public void run() {
				startScan();
				synchronized (this) {
					while (true) {
						Log.i(AdhocRouteApp.TAG, "start rescan.");
						try {
							wait(2000);
//							if(showScan == false)
//                                continue;
							wifiManager.startScan();
							wait(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});
		mScanTread.start();
	}

	private void startScan() {
		if (wifiManager != null) {
			if (wifiManager.isWifiEnabled() == false) {
				wifiManager.setWifiEnabled(true);
			}
			wifiManager.startScan();
		}
	}

	public boolean constructAdhoc() {
		startScan();
//		showScan = false;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String ssid = sp.getString("ssid", "");
		String channel = sp.getString("lan_channel", "");
		// String gw = "192.168.2.1";
		String ip = sp.getString("adhoc_ip", "");
		String mask = sp.getString("adhoc_mask", "");

		// 进行检查参数是否设置以及合理性
		if (checkParams(ssid, channel, ip, mask)) {
			try {
				wifiConfig.SSID = "\"" + ssid + "\"";
				wifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
				wifiConfig.setIpAssignment("STATIC");
				wifiConfig.setIpAddress(InetAddress.getByName(ip),
						24);
				wifiConfig.setDNS(InetAddress.getByName("8.8.8.8"));
				wifiConfig.isIBSS = true;// new api
				wifiConfig.frequency = Integer.parseInt(channel);// new
				int id = wifiManager.addNetwork(wifiConfig);
				if (id < 0) {
					Log.i(AdhocRouteApp.TAG, "Failed to add Ad-hoc network");
					Toast.makeText(context, "Failed to add Ad-hoc network", 0)
							.show();
				} else {
					wifiManager.enableNetwork(id, true);
					wifiManager.saveConfiguration();
				}
			} catch (Exception e) {
				Log.i(AdhocRouteApp.TAG, "Wifi configuration failed!");
				Toast.makeText(context, "Failed to add Ad-hoc network", 0).show();
				e.printStackTrace();
				return false;
			}
			Toast.makeText(context,"Ad-hoc start successfully id " + wifiManager.getConnectionInfo().getNetworkId(), 0).show();
//			showScan = true;
			return true;
		}
		return false;
	}

	private boolean checkParams(String ssid, String channel, String ip,
			String mask) {
		String show = "";
		if (ssid.equals(""))
			show = "ssid";
		else if (channel.equals(""))
			show = "channel";
		else if (ip.equals(""))
			show = "ip";
		else if (mask.equals(""))
			show = "mask";
		if (!show.equals("")) {
			Toast.makeText(context, show + " is not set", 0).show();
			return false;
		}
		return true;
		// 继续检查参数是否规范

	}

	public int toNumMask(String maskStr) {
		String[] ipSegment = maskStr.split("\\.");
		int num = 0;
		for (int n = 0; n < ipSegment.length; n++) {
			char[] array = Integer.toBinaryString(
					Integer.parseInt(ipSegment[n])).toCharArray();
			for (int i = 0; i < array.length; i++) {
				if (array[i] == '1') {
					num++;
				}
			}
		}
		return num;
	}
}