package com.inflps.pcd;

import android.animation.*;
import android.app.*;
import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import android.os.Bundle;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.io.*;
import java.io.InputStream;
import java.text.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;
import android.Manifest;
import android.os.Environment;
import android.provider.Settings;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.DialogInterface;
import android.net.Uri;
import android.provider.Settings;

public class PermissionsActivity extends AppCompatActivity {
	
	private Timer _timer = new Timer();
	
	private static final int REQUEST_CODE_STORAGE = 100;
	
	private LinearLayout LinearLayout1;
	private LinearLayout linear6;
	private ImageView imageview3;
	private TextView textview3;
	
	private Intent permission = new Intent();
	private AlertDialog.Builder builder;
	private Intent i = new Intent();
	private TimerTask t;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.permissions);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		LinearLayout1 = findViewById(R.id.LinearLayout1);
		linear6 = findViewById(R.id.linear6);
		imageview3 = findViewById(R.id.imageview3);
		textview3 = findViewById(R.id.textview3);
		builder = new AlertDialog.Builder(this);
	}
	
	private void initializeLogic() {
		
		if (hasStoragePermission()) {
			goToHome();
		}
		else {
			requestStoragePermission();
		}
	}
	
	private boolean hasStoragePermission() {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
					== PackageManager.PERMISSION_GRANTED;
			} else {
					return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_GRANTED;
			}
	}
	
	private void requestStoragePermission() {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.READ_MEDIA_IMAGES},
					REQUEST_CODE_STORAGE);
			} else {
					ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
					REQUEST_CODE_STORAGE);
			}
	}
	
	
	@Override
	public void onRequestPermissionsResult(int requestCode,
	@NonNull String[] permissions,
	@NonNull int[] grantResults) {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
			
			if (requestCode == REQUEST_CODE_STORAGE) {
					if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
							goToMain();
					} else {
							boolean showRationale;
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
									showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES);
							} else {
									showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
							}
							
							if (!showRationale) {
									Toast.makeText(this, "Permission denied. Please allow it in settings.", Toast.LENGTH_LONG).show();
									openAppSettings();
							} else {
									Toast.makeText(this, "Permission is required to continue.", Toast.LENGTH_SHORT).show();
									requestStoragePermission();
							}
					}
			}
	}
	
	private void goToMain() {
			Intent i = new Intent(this, MainActivity.class);
			startActivity(i);
			finish();
	}
	
	private void goToHome() {
			Intent i = new Intent(this, HomeActivity.class);
			startActivity(i);
			finish();
	}
	
	private void openAppSettings() {
			Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts("package", getPackageName(), null);
			intent.setData(uri);
			startActivity(intent);
			finish();
	}
	
}