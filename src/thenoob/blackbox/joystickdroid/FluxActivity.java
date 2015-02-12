package thenoob.blackbox.joystickdroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class FluxActivity extends Activity {
	FluxView flux;
	private static final int RELOAD = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flux);	
		flux = (FluxView) findViewById(R.id.flux);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id) {
		case R.id.pref:
			startActivityForResult(new Intent(getApplicationContext(),  PreferenceDriod.class), RELOAD );
			break;
		}
		return true;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
		case RELOAD:
			flux.loadPreference();
			flux.invalidate();
			break;
		}
	}
	@Override
	protected void onStop() {
		super.onStop();
		flux.disconnect();
	}
	@Override
	protected void onRestart() {
		super.onRestart();
		flux.connect();
	}
	@Override
	protected void onDestroy() {
		flux.disconnect();
		super.onDestroy();
	}
}
