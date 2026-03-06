package com.inflps.pcd;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import com.inflps.pcd.CORE.BRUSH_CORE.*;
import androidx.core.app.ActivityOptionsCompat;
import com.inflps.pcd.WIDGET.SLIDERS.RotaryJogSlider;


public class BrushActivity extends AppCompatActivity {
	
	private BrushSettings currentSettings = new BrushSettings();
	private BrushEngine engine = new BrushEngine();
	private Bitmap rawImportedBitmap;
	private File currentBrushFile = null; 
	private static final int PICK_IMAGE_REQUEST = 1001;
	private String highlight = "#353535"; 
	
	private LinearLayout background;
	private LinearLayout toolbar;
	private LinearLayout brushPreviewContainer;
	private ScrollView scrollView;
	private ImageView back;
	private TextView title;
	private ImageView export;
	private ImageView save;
	private BrushPreviewView previewView;
	private LinearLayout scrollContainer;
	private TextView txtBN;
	private LinearLayout BNc_ner;
	private TextView txtB;
	private LinearLayout TBc_ner;
	private LinearLayout TBFc_ner;
	private TextView txtS;
	private LinearLayout Sc_ner;
	private TextView textview6;
	private LinearLayout Ac_ner;
	private TextView txtFP;
	private LinearLayout FPc_ner;
	private TextView txtSJ;
	private LinearLayout SJc_ner;
	private TextView txtJA;
	private LinearLayout JAc_ner;
	private TextView txtSzJ;
	private LinearLayout SzJc_ner;
	private EditText editName;
	private ImageView bitmapPreview;
	private LinearLayout c_ner1;
	private TextView imgFileName;
	private TextView txtPick;
	private CheckBox invertTextureCB;
	private RotaryJogSlider spacingSeek;
	private EditText spacingET;
	private RotaryJogSlider angleSeek;
	private EditText angleET;
	private CheckBox followingPathCB;
	private RotaryJogSlider scatterSeek;
	private EditText scatterJitterET;
	private RotaryJogSlider jitterAngleSeek;
	private EditText jitterAngleET;
	private RotaryJogSlider sizeSeek;
	private EditText sizeET;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.brush);
		initialize(_savedInstanceState);
		initializeLogic();

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				AlertDialog dialog = new AlertDialog.Builder(BrushActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert).setTitle("Exit the editor?")
						.setMessage("If you have not saved your changes, it is no longer possible to recover the changes you have made.")
						.setPositiveButton("Exit", (d, which) -> {
							finish();
							overridePendingTransition(R.anim.fade_in, R.anim.slide_rotate_out);
						})
						.setNegativeButton("Cancel", null)
						.create();
				if (dialog.getWindow() != null) {
					dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
				}
				dialog.show();
			}
		});
	}
	
	private void initialize(Bundle _savedInstanceState) {
		background = findViewById(R.id.background);
		toolbar = findViewById(R.id.toolbar);
		brushPreviewContainer = findViewById(R.id.brushPreviewContainer);
		scrollView = findViewById(R.id.scrollView);
		back = findViewById(R.id.back);
		title = findViewById(R.id.title);
		export = findViewById(R.id.export);
		save = findViewById(R.id.save);
		previewView = findViewById(R.id.previewView);
		scrollContainer = findViewById(R.id.scrollContainer);
		txtBN = findViewById(R.id.txtBN);
		BNc_ner = findViewById(R.id.BNc_ner);
		txtB = findViewById(R.id.txtB);
		TBc_ner = findViewById(R.id.TBc_ner);
		TBFc_ner = findViewById(R.id.TBFc_ner);
		txtS = findViewById(R.id.txtS);
		Sc_ner = findViewById(R.id.Sc_ner);
		textview6 = findViewById(R.id.textview6);
		Ac_ner = findViewById(R.id.Ac_ner);
		txtFP = findViewById(R.id.txtFP);
		FPc_ner = findViewById(R.id.FPc_ner);
		txtSJ = findViewById(R.id.txtSJ);
		SJc_ner = findViewById(R.id.SJc_ner);
		txtJA = findViewById(R.id.txtJA);
		JAc_ner = findViewById(R.id.JAc_ner);
		txtSzJ = findViewById(R.id.txtSzJ);
		SzJc_ner = findViewById(R.id.SzJc_ner);
		editName = findViewById(R.id.editName);
		bitmapPreview = findViewById(R.id.bitmapPreview);
		c_ner1 = findViewById(R.id.c_ner1);
		imgFileName = findViewById(R.id.imgFileName);
		txtPick = findViewById(R.id.txtPick);
		invertTextureCB = findViewById(R.id.invertTextureCB);
		spacingSeek = findViewById(R.id.spacingSeek);
		spacingET = findViewById(R.id.spacingET);
		angleSeek = findViewById(R.id.angleSeek);
		angleET = findViewById(R.id.angleET);
		followingPathCB = findViewById(R.id.followingPathCB);
		scatterSeek = findViewById(R.id.scatterSeek);
		scatterJitterET = findViewById(R.id.scatterJitterET);
		jitterAngleSeek = findViewById(R.id.jitterAngleSeek);
		jitterAngleET = findViewById(R.id.jitterAngleET);
		sizeSeek = findViewById(R.id.sizeSeek);
		sizeET = findViewById(R.id.sizeET);
		
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				getOnBackPressedDispatcher().onBackPressed();
			}
		});
		
		export.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				exportToDownloads();
			}
		});
		
		save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				saveBrushToLibrary();
			}
		});
	}
	
	private void initializeLogic() {
		
		editName.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)50, 0xFF424242));
		spacingET.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)50, 0xFF424242));
		angleET.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)50, 0xFF424242));
		scatterJitterET.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)50, 0xFF424242));
		jitterAngleET.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)50, 0xFF424242));
		sizeET.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)50, 0xFF424242));
		View[] myTools = {TBc_ner, save, export, back};
		for (View tool : myTools) {
			    if (tool != null) {
				        applyHoverEffect(tool, highlight);
				    }
		}
		
		String editPath = getIntent().getStringExtra("brush_path");
		if (editPath != null) {
				currentBrushFile = new File(editPath);
				BrushArchiveLoader.loadFromArchive(currentBrushFile, new BrushArchiveLoader.BrushLoadCallback() {
						@Override
						public void onSuccess(final BrushSettings loadedSettings) {
								runOnUiThread(() -> {
										currentSettings.name = loadedSettings.name;
										currentSettings.spacing = loadedSettings.spacing;
										currentSettings.stampAngle = loadedSettings.stampAngle;
										currentSettings.followPath = loadedSettings.followPath;
										currentSettings.scatterJitter = loadedSettings.scatterJitter;
										currentSettings.sizeJitter = loadedSettings.sizeJitter;
										currentSettings.angleJitter = loadedSettings.angleJitter;
										currentSettings.texture = loadedSettings.texture;
										previewView.setSettings(currentSettings);
										updateUIFromSettings();
										setupSyncLogic();
										previewView.renderPath();
								});
						}
						
						@Override
						public void onError(Exception e) {
						}
				});
		} else {
				Bitmap defaultTexture = createDefaultTexture();
				currentSettings.texture = defaultTexture;
				rawImportedBitmap = defaultTexture;
				bitmapPreview.setImageBitmap(defaultTexture);
				imgFileName.setText("default_circle.png");
				setupSyncLogic();
				previewView.renderPath();
		}
		initUI();
		TBc_ner.setOnClickListener(v -> openGallery());
		invertTextureCB.setOnCheckedChangeListener((btn, isChecked) -> {
				updateBrushTexture();
		});
		
		editName.addTextChangedListener(new TextWatcher() {
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
						if (s != null) {
								currentSettings.name = s.toString();
						}
				}
				
				@Override 
				public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
				
				@Override 
				public void afterTextChanged(Editable s) { }
		});
	}
	
	private void initUI() {
			previewView.setSettings(currentSettings);
			spacingSeek.setConfig(0.0f, 15.0f, currentSettings.spacing);
			angleSeek.setConfig(0.0f, 360.0f, currentSettings.stampAngle);
			scatterSeek.setConfig(0.0f, 50.0f, currentSettings.scatterJitter);
			jitterAngleSeek.setConfig(0.0f, 360.0f, currentSettings.angleJitter);
			sizeSeek.setConfig(0.0f, 3.0f, currentSettings.sizeJitter);
	}
	
	private void setupSyncLogic() {
		    syncETToSlider(spacingET, spacingSeek, val -> {
			        currentSettings.spacing = val;
			        previewView.renderPath(); 
			    });
		    spacingSeek.setOnValueChangedListener(val -> {
			        currentSettings.spacing = val;
			        spacingET.setText(String.format(Locale.US, "%.2f", val));
			        previewView.renderPath(); 
			    });
		    syncETToSlider(angleET, angleSeek, val -> {
			        currentSettings.stampAngle = val;
			        previewView.renderPath(); 
			    });
		    angleSeek.setOnValueChangedListener(val -> {
			        currentSettings.stampAngle = val;
			        angleET.setText(String.format(Locale.US, "%.1f", val));
			        previewView.renderPath(); 
			    });
		    syncETToSlider(scatterJitterET, scatterSeek, val -> {
			        currentSettings.scatterJitter = val;
			        previewView.renderPath(); 
			    });
		    scatterSeek.setOnValueChangedListener(val -> {
			        currentSettings.scatterJitter = val;
			        scatterJitterET.setText(String.format(Locale.US, "%.1f", val));
			        previewView.renderPath(); 
			    });
		    syncETToSlider(jitterAngleET, jitterAngleSeek, val -> {
			        currentSettings.angleJitter = val;
			        previewView.renderPath(); 
			    });
		    jitterAngleSeek.setOnValueChangedListener(val -> {
			        currentSettings.angleJitter = val;
			        jitterAngleET.setText(String.format(Locale.US, "%.1f", val));
			        previewView.renderPath(); 
			    });
		    syncETToSlider(sizeET, sizeSeek, val -> {
			        currentSettings.sizeJitter = val;
			        previewView.renderPath(); 
			    });
		    sizeSeek.setOnValueChangedListener(val -> {
			        currentSettings.sizeJitter = val;
			        sizeET.setText(String.format(Locale.US, "%.2f", val));
			        previewView.renderPath(); 
			    });
		
		    followingPathCB.setOnCheckedChangeListener((btn, isChecked) -> {
			        currentSettings.followPath = isChecked;
			        previewView.renderPath(); 
			    });
	}
	
	private void syncETToSlider(final EditText et, final RotaryJogSlider slider, final ValueUpdater updater) {
			et.addTextChangedListener(new TextWatcher() {
					@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
					@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
					
					@Override
					public void afterTextChanged(Editable s) {
							if (et.hasFocus()) {
									try {
											String input = s.toString();
											if (input.isEmpty()) return;
											float val = Float.parseFloat(input);
						            		slider.setValue(val);
											updater.onUpdate(val);
									} catch (NumberFormatException e) {
									}
							}
					}
			});
	}
	
	interface ValueUpdater {
			void onUpdate(float value);
	}
	
	private void updateBrushTexture() {
			if (rawImportedBitmap != null) {
					boolean shouldInvert = invertTextureCB.isChecked();
					Bitmap processed = createBrushStamp(rawImportedBitmap, shouldInvert);
					currentSettings.texture = processed;
					
					bitmapPreview.setImageBitmap(processed);
					previewView.renderPath();
			}
	}
	
	private void saveBrushToLibrary() {
			String name = editName.getText().toString().trim();
			if (name.isEmpty()) name = "NewBrush";
			currentSettings.name = name;
			String fileName = name.replaceAll("[^a-zA-Z0-9.-]", "_");
			
			if (currentBrushFile == null) {
					File internalDir = new File(getExternalFilesDir(null), "Brushes");
					if (!internalDir.exists()) internalDir.mkdirs();
					currentBrushFile = new File(internalDir, fileName + "_" + System.currentTimeMillis() + ".brush");
			} else {
			}
			
			if (currentSettings.texture != null) {
					BrushExporter.saveBrush(this, currentSettings, currentSettings.texture, currentBrushFile);
					Toast.makeText(this, "Brush saved to library!", Toast.LENGTH_SHORT).show();
			}
	}
	
	private void exportToDownloads() {
			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("application/octet-stream");
			intent.putExtra(Intent.EXTRA_TITLE, currentSettings.name + ".brush");
			startActivityForResult(intent, 3003);
	}
	
	private Bitmap createDefaultTexture() {
		    int size = 256;
		    Bitmap b = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		    Canvas canvas = new Canvas(b);
		    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		    RadialGradient gradient = new RadialGradient(
		        size / 2f, size / 2f, size / 2f,
		        Color.WHITE, Color.TRANSPARENT,
		        Shader.TileMode.CLAMP);
		    
		    paint.setShader(gradient);
		    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
		    
		    return b;
	}
	
	private void updateUIFromSettings() {
			editName.setText(currentSettings.name);
			
			spacingSeek.setValue(currentSettings.spacing);
			sizeSeek.setValue(currentSettings.sizeJitter);
			angleSeek.setValue(currentSettings.stampAngle);
			jitterAngleSeek.setValue(currentSettings.angleJitter);
			scatterSeek.setValue(currentSettings.scatterJitter);
			
			
			spacingET.setText(String.format(Locale.US, "%.2f", currentSettings.spacing));
			sizeET.setText(String.format(Locale.US, "%.2f", currentSettings.sizeJitter));
			angleET.setText(String.format(Locale.US, "%.2f", currentSettings.stampAngle));
			jitterAngleET.setText(String.format(Locale.US, "%.2f", currentSettings.angleJitter));
			scatterJitterET.setText(String.format(Locale.US, "%.2f", currentSettings.scatterJitter));
			
			followingPathCB.setChecked(currentSettings.followPath);
			
			if (currentSettings.texture != null) {
					bitmapPreview.setImageBitmap(currentSettings.texture);
					rawImportedBitmap = currentSettings.texture;
					imgFileName.setText("embedded_pattern.png"); 
			} else {
					imgFileName.setText("No file selected");
			}
	}
	
	public void applyHoverEffect(final View view, final String hexColor) {
		    final int color = Color.parseColor(hexColor);
		    view.setOnHoverListener(new OnHoverListener() {
			        @Override
			        public boolean onHover(View v, MotionEvent event) {
				            switch (event.getAction()) {
					                case MotionEvent.ACTION_HOVER_ENTER:
					                    triggerEffect(v, true, color);
					                    return true;
					                case MotionEvent.ACTION_HOVER_EXIT:
					                    triggerEffect(v, false, color);
					                    return true;
					            }
				            return false;
				        }
			    });
		
		    view.setOnTouchListener(new OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
				            switch (event.getAction()) {
					                case MotionEvent.ACTION_DOWN:
					                    triggerEffect(v, true, color);
					                    break;
					                case MotionEvent.ACTION_UP:
					                case MotionEvent.ACTION_CANCEL:
					                    triggerEffect(v, false, color);
					                    break;
					            }
				            return false;
				        }
			    });
	}
	
	private void triggerEffect(View v, boolean active, int color) {
		    float targetScale = active ? 0.95f : 1.0f;
		    float targetAlpha = active ? 0.8f : 1.0f;
		
		    v.animate()
		            .scaleX(targetScale)
		            .scaleY(targetScale)
		            .alpha(targetAlpha)
		            .setDuration(120)
		            .start();
		
		    if (active) {
			        GradientDrawable shape = new GradientDrawable();
			        shape.setShape(GradientDrawable.RECTANGLE);
			        shape.setCornerRadius(1000f); 
			        shape.setColor(adjustAlpha(color, 0.35f));
			        v.setForeground(shape);
			    } else {
			        v.setForeground(null);
			    }
	}
	
	private int adjustAlpha(int color, float factor) {
		    int alpha = Math.round(Color.alpha(color) * factor);
		    int red = Color.red(color);
		    int green = Color.green(color);
		    int blue = Color.blue(color);
		    return Color.argb(alpha, red, green, blue);
	}
	
	private void openGallery() {
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, PICK_IMAGE_REQUEST);
	}
	
	private Bitmap createBrushStamp(Bitmap source, boolean invert) {
			int size = 256; 
			Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
			Bitmap scaled = Bitmap.createScaledBitmap(source, size, size, true);
			
			for (int x = 0; x < size; x++) {
					for (int y = 0; y < size; y++) {
							int pixel = scaled.getPixel(x, y);
							
							int oldAlpha = Color.alpha(pixel);
							int r = Color.red(pixel);
							int g = Color.green(pixel);
							int b = Color.blue(pixel);
							
							int brightness = (r + g + b) / 3;
							
							int finalAlpha;
							if (oldAlpha < 255) {
									finalAlpha = oldAlpha;
							} else {
									finalAlpha = invert ? brightness : (255 - brightness);
							}
							int finalRGB = invert ? 0 : 255; 
							
							output.setPixel(x, y, Color.argb(finalAlpha, finalRGB, finalRGB, finalRGB));
					}
			}
			return output;
	}
	
	
	private String getFileName(Uri uri) {
		    String result = null;
		    if (uri.getScheme().equals("content")) {
			        try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
				            if (cursor != null && cursor.moveToFirst()) {
					                int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
					                if (index != -1) result = cursor.getString(index);
					            }
				        }
			    }
		    if (result == null) {
			        result = uri.getPath();
			        int cut = result.lastIndexOf('/');
			        if (cut != -1) result = result.substring(cut + 1);
			    }
		    return result;
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			
			if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
					Bitmap rawBitmap = null; 
					
					try {
							Uri imageUri = data.getData();
							InputStream is = getContentResolver().openInputStream(imageUri);
							rawBitmap = BitmapFactory.decodeStream(is);
							
							if (rawBitmap != null) {
									boolean shouldInvert = invertTextureCB.isChecked(); 
					                String fileName = getFileName(imageUri);
					    imgFileName.setText(fileName);
									
									Bitmap processed = createBrushStamp(rawBitmap, shouldInvert);
									
									currentSettings.texture = processed;
									bitmapPreview.setImageBitmap(processed);
									previewView.renderPath(); 
									
									this.rawImportedBitmap = rawBitmap; 
							}
					} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
					}
			} else if (requestCode == 3003 && resultCode == RESULT_OK && data != null) {
					Uri uri = data.getData();
					try (OutputStream os = getContentResolver().openOutputStream(uri)) {
							BrushExporter.saveBrushToStream(currentSettings, currentSettings.texture, os);
							Toast.makeText(this, "Exported to Downloads!", Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
							e.printStackTrace();
					}
			}
	}
}
