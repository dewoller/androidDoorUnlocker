package ca.wollersheim.dennis.keypad;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;

import android.util.Log;
import android.widget.Toast;
import android.content.Context;

public class Rebooter extends Thread {
	long start = System.currentTimeMillis();
	
    public Rebooter() {
	super();
    }
    public void update() {
    	start = System.currentTimeMillis();
    }
    public void run() {
    	while (System.currentTimeMillis() - start <3600000) { 
            try {
				sleep(10000);
		    } catch (InterruptedException e) {}
    	}
    	//reboot();
    }
    public static void reboot(Context c) {
    try {	
			Toast.makeText(c, "preboot1", Toast.LENGTH_LONG).show();
			Runtime.getRuntime().exec(new String[]{"/system/bin/su","-c","reboot now"});
			Toast.makeText(c, "preboot2", Toast.LENGTH_LONG).show();
	        Log.i("KP", "did not reboot");
	        rebootSU();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	}

    }

    public static void rebootSU() {
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        OutputStreamWriter osw = null;


        String command="/system/bin/reboot";

        try { // Run Script

            proc = runtime.exec("su");
            osw = new OutputStreamWriter(proc.getOutputStream());
                                osw.write(command);
                    osw.flush();
            osw.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();                    
                }
            }
        }
        try {
            if (proc != null)
                proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

            }

}
