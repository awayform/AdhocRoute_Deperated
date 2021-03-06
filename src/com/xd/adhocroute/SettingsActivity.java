package com.xd.adhocroute;

import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiChannel;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.xd.adhocroute.route.IPEditPreference;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	
	private EditTextPreference ssid;
	private ListPreference channel;
	private IPEditPreference ip;
	private IPEditPreference mask;
	private EditTextPreference wan;
	private String [] channelEntry;
	
	private CheckBoxPreference openDynCheckGateway;
	private CheckBoxPreference openStaticGateway;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	private void init() {
		openDynCheckGateway = (CheckBoxPreference)findPreference("is_dyncheck_gateway_enabled");
		openStaticGateway = (CheckBoxPreference)findPreference("is_static_gateway_enabled");
//		wan = (EditTextPreference) findPreference("wan");
//		ssid = (EditTextPreference) findPreference("ssid");
//		channel = (ListPreference) findPreference("lan_channel");
//		ip = (IPPreference) findPreference("adhoc_ip");
//		mask = (IPPreference) findPreference("adhoc_mask");
//		channel.setEntryValues(getAllChannels());
//		channel.setValue(sp.getString("lan_channel", channelEntry[0]));
//		channel.setEntries(getAllChannelNames());
	}

	private String [] getAllChannels() {
		List<WifiChannel> cs = ((WifiManager) getSystemService(WIFI_SERVICE)).getSupportedChannels();//new api
		if (cs.size() > 0) {
			channelEntry = new String[cs.size()];
			for (int i = 0; i < cs.size(); i++) {
			channelEntry[i] = Integer.toString(cs.get(i).frequency);
			}
		} else {
			channelEntry = new String[1];
			channelEntry[0] = Integer.toString(2412);
		}
		return channelEntry;
	}
	
	private String [] getAllChannelNames() {
		String [] channelName = new String[channelEntry.length];
		for (int i = 0; i < channelEntry.length; i++) {
			channelName[i] = i+1 + " - " + channelEntry[i] + " MHz";
		}
		return channelName;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// 静态设置为网关和动态设置为网关只能选一种方式
		if (key.equals("is_dyncheck_gateway_enabled")) {
			if (sharedPreferences.getBoolean("is_dyncheck_gateway_enabled", false)) {
				openStaticGateway.setChecked(false);
			}
		} else if(key.equals("is_static_gateway_enabled")) {
			if (sharedPreferences.getBoolean("is_static_gateway_enabled", false)) {
				openDynCheckGateway.setChecked(false);
			}
		}
	}

	
}
