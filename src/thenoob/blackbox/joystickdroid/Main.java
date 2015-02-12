package thenoob.blackbox.joystickdroid;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Main extends ActionBarActivity {
	public static final String COMP_NAME = "COMP_NAME";
	SharedPreferences pref;
	EditText host_name;
	Button start;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		String hostname = pref.getString(COMP_NAME, null);
		this.host_name = (EditText) findViewById(R.id.comname);
		start = (Button) findViewById(R.id.start);
		if(host_name != null) {
			this.host_name.setText(hostname);
		}
		start.setOnClickListener(new View.OnClickListener(
				) {
			
			@Override
			public void onClick(View v) {
				final String hostname = host_name.getText().toString();
				if(hostname == null || hostname.length() == 0 ) {
					Toast.makeText(Main.this, R.string.empty_hostname, Toast.LENGTH_SHORT).show();
				}
				else if(!isWifiAvailable()) {
					Toast.makeText(Main.this, R.string.wifi_unavailabe, Toast.LENGTH_SHORT).show();
				}
				
				else if(hostname != null) {
					Thread checkhost = new Thread(new Runnable() {
						
						private boolean isReachable(String host) {
							
							try {
								Socket socket = new Socket();
								socket.connect(new InetSocketAddress(InetAddress.getByName(host.trim()), 6066)
								, 500);
								socket.close();
								return true;
							}
							catch (UnknownHostException e) {
								return false;
							}
							catch(SocketTimeoutException e) {
								return false;
							}
							catch(IOException e){
								
								return false;
							}
							catch(Exception e) {
								return false;
							}
							
						}
						
						@Override
						public void run() {
							if(isReachable(hostname)) {
								runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										Toast.makeText(Main.this,R.string.saving_hostname, Toast.LENGTH_LONG).show();
										startActivity(new Intent(getApplicationContext(), FluxActivity.class));
									}
								});
								SharedPreferences.Editor editor = pref.edit();
								editor.putString(COMP_NAME, hostname);
								editor.commit();
							}
							else {
								runOnUiThread(new Runnable() {
					
									@Override
									public void run() {
										Toast.makeText(Main.this, R.string.unreachable_host, Toast.LENGTH_LONG).show();
									}
								});
							}
						}
					});
					checkhost.start();
					
				}
			}
			
			
		});
	}
	
	private boolean isWifiAvailable(){
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(mWifi.isConnected())
			return true;
		else
			return false;
	}
}
