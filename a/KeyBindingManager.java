/*
 * Copyright (C) 2010 Rob Elsner
 *
 * Licensed under the GNU General Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package ca.wollersheim.dennis.keypad;


import static com.example.android.skeletonapp.R.id.Button0;
import static com.example.android.skeletonapp.R.id.Button1;
import static com.example.android.skeletonapp.R.id.Button2;
import static com.example.android.skeletonapp.R.id.Button3;
import static com.example.android.skeletonapp.R.id.Button4;
import static com.example.android.skeletonapp.R.id.Button5;
import static com.example.android.skeletonapp.R.id.Button6;
import static com.example.android.skeletonapp.R.id.Button7;
import static com.example.android.skeletonapp.R.id.Button8;
import static com.example.android.skeletonapp.R.id.Button9;

import static com.example.android.skeletonapp.R.id.ButtonHash;
import static com.example.android.skeletonapp.R.id.ButtonStar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class KeyBindingManager implements KeyMapBinder, OnClickListener {

	/**
	 * Add a value here which is the button name (preferably similar to the
	 * layout id) and the default action
	 * 
	 * @author robelsner
	 * 
	 */
	private String cmd2Send="";
public enum MythKey {
		BUTTON_0("key 0", Button0), BUTTON_1("key 1", Button1), BUTTON_2(
				"key 2", Button2), BUTTON_3("key 3", Button3), BUTTON_4(
				"key 4", Button4), BUTTON_5("key 5", Button5), BUTTON_6(
				"key 6", Button6), BUTTON_7("key 7", Button7), BUTTON_8(
				"key 8", Button8), BUTTON_9("key 9", Button9),
		BUTTON_HASH("key #", ButtonHash), BUTTON_STAR(
				"key *", ButtonStar);

		private final String defaultCommand;
		private final int layoutId;

		private MythKey(final String command, final int layoutId) {
			this.defaultCommand = command;
			this.layoutId = layoutId;
		}

		public final String getDefaultCommand() {
			return defaultCommand;
		}

		public final int getButtonId() {
			return layoutId;
		}

		public static MythKey getByName(final String name) {
			for (MythKey key : MythKey.values()) {
				if (key.name().equals(name))
					return key;
			}
			return MythKey.BUTTON_0;
		}

		public static List<KeyBindingEntry> createDefaultList() {
			ArrayList<KeyBindingEntry> entries = new ArrayList<KeyBindingEntry>();
			entries.add(new KeyBindingEntry("0", BUTTON_0,
					BUTTON_0.defaultCommand, false));
			entries.add(new KeyBindingEntry("1", BUTTON_1,
					BUTTON_1.defaultCommand, false));
			entries.add(new KeyBindingEntry("2", BUTTON_2,
					BUTTON_2.defaultCommand, false));
			entries.add(new KeyBindingEntry("3", BUTTON_3,
					BUTTON_3.defaultCommand, false));
			entries.add(new KeyBindingEntry("4", BUTTON_4,
					BUTTON_4.defaultCommand, false));
			entries.add(new KeyBindingEntry("5", BUTTON_5,
					BUTTON_5.defaultCommand, false));
			entries.add(new KeyBindingEntry("6", BUTTON_6,
					BUTTON_6.defaultCommand, false));
			entries.add(new KeyBindingEntry("7", BUTTON_7,
					BUTTON_7.defaultCommand, false));
			entries.add(new KeyBindingEntry("8", BUTTON_8,
					BUTTON_8.defaultCommand, false));
			entries.add(new KeyBindingEntry("9", BUTTON_9,
					BUTTON_9.defaultCommand, false));
			entries.add(new KeyBindingEntry("#", BUTTON_HASH,
					BUTTON_HASH.defaultCommand, false));
			entries.add(new KeyBindingEntry("*", BUTTON_STAR,
					BUTTON_STAR.defaultCommand, false));
			return entries;
		}
	}

	private static final String MythMote = "Logging:";

	private void createDefaultEntries() {
		for (KeyBindingEntry entry : KeyBindingManager.MythKey
				.createDefaultList()) {
			binder.bind(entry);
		}
	}
			
	private KeyMapBinder binder = null;
	private MQTT_Sender scomm=null;

	private Map<View, KeyBindingEntry> viewToEntryMap = new HashMap<View, KeyBindingEntry>();

	public KeyBindingManager( final KeyMapBinder binder, MQTT_Sender scomm) {
		Log.d(MythMote, "Created KeyBindingManager with binder " + binder );
		this.binder = binder;
		this.scomm = scomm;
		this.createDefaultEntries();
		
	}

	public View bind(KeyBindingEntry entry) {
		Log.d(MythMote, "Bind " + entry.getFriendlyName() + " to "
				+ entry.getCommand());
		View v = binder.bind(entry);
		viewToEntryMap.put(v, entry);
		return v;
	}

	public KeyBindingEntry getCommand(final View initiatingView) {
		return viewToEntryMap.get(initiatingView);
	}
	public void storeCommand(String command) {
		if (command.equals("#")) {
			sendCommand( cmd2Send);
			cmd2Send="";
		} else {
			cmd2Send += command;
		}
	}
	
	public void sendCommand( String cmd) {
		scomm.SendCommand(cmd);
		
	}
	
	public void onClick(View v) {

		KeyBindingEntry entry = viewToEntryMap.get(v);

		if (null != entry ) {
			Log.d(MythMote, "onClick " + entry.getFriendlyName()
					+ " command " + entry.getCommand());
			
			//send command
			storeCommand(entry.getCommand());
			
		}

	}
}
