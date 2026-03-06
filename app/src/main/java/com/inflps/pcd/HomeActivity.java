package com.inflps.pcd;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inflps.pcd.CORE.PROJECT_CORE.ADAPTER.ProjectAdapter;
import com.inflps.pcd.CORE.PROJECT_CORE.ADAPTER.ProjectItem;
import com.inflps.pcd.WIDGET.COLOR_PICKER.HueSVSquareColorPicker;
import com.inflps.pcd.WIDGET.COLOR_PICKER.RGBSlidersView;
import com.inflps.pcd.WIDGET.REFRESH_LAYOUT.ScaleRefreshLayout;
import java.io.InputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class HomeActivity extends AppCompatActivity {
	
	private ProjectAdapter adapter;
	private List<ProjectItem> projectList = new ArrayList<>();
	
	private int currentSortMode = 0;
	private String highlight = "#353535"; 
	
	private static final int PICK_PROJECT_FILE = 1001;
	
	private LinearLayout background;
	private FrameLayout foreground;
	private LinearLayout RecyclerViewContainer;
	private LinearLayout ToolBarContainer;
	private ScaleRefreshLayout refreshLayout;
	private ImageView ImageView3;
	private RecyclerView recyclerView;
	private LinearLayout toolbar;
	private TextView subt;
	private LinearLayout LinearLayout1;
	private ImageView ImageView1;
	private ImageView more;
	
	private AlertDialog.Builder info;
	private SharedPreferences CANV;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
    android.transition.Transition moveTrans = android.transition.TransitionInflater.from(this)
    .inflateTransition(R.transition.shared_element_move);
    getWindow().setSharedElementEnterTransition(moveTrans);
    getWindow().setSharedElementExitTransition(moveTrans);
}
		setContentView(R.layout.home);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		background = findViewById(R.id.background);
		foreground = findViewById(R.id.foreground);
		RecyclerViewContainer = findViewById(R.id.RecyclerViewContainer);
		ToolBarContainer = findViewById(R.id.ToolBarContainer);
		refreshLayout = findViewById(R.id.refreshLayout);
		ImageView3 = findViewById(R.id.ImageView3);
		recyclerView = findViewById(R.id.recyclerView);
		toolbar = findViewById(R.id.toolbar);
		subt = findViewById(R.id.subt);
		LinearLayout1 = findViewById(R.id.LinearLayout1);
		ImageView1 = findViewById(R.id.ImageView1);
		more = findViewById(R.id.more);
		info = new AlertDialog.Builder(this);
		CANV = getSharedPreferences("CANV.SETTINGS", Activity.MODE_PRIVATE);
		
		subt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				View menuSortView = getLayoutInflater().inflate(R.layout.menu_projects_sort, null);
				final PopupWindow menuSort = new PopupWindow(menuSortView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
				LinearLayout background = menuSortView.findViewById(R.id.background);
				LinearLayout o1 = menuSortView.findViewById(R.id.o1);
				LinearLayout o2 = menuSortView.findViewById(R.id.o2);
				LinearLayout o3 = menuSortView.findViewById(R.id.o3);
				background.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)15, (int)1, 0xFF424242, 0xE9111111));
				applyHoverEffect(o1, highlight);
				applyHoverEffect(o2, highlight);
				applyHoverEffect(o3, highlight);
				o1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						currentSortMode = 0;
						refreshData();
						subt.setText("Sort by: Newest");
						menuSort.dismiss();
					}
				});
				o2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						currentSortMode =1;
						refreshData();
						subt.setText("Sort by: A-Z");
						menuSort.dismiss();
					}
				});
				o3.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						currentSortMode =2;
						refreshData();
						subt.setText("Sort by: Z-A");
						menuSort.dismiss();
					}
				});
				menuSort.setAnimationStyle(android.R.style.Animation_Dialog);
				menuSort.showAsDropDown(subt, 0, 0);
				menuSort.setBackgroundDrawable(new BitmapDrawable());
			}
		});
		
		more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				View mHomePopupView = getLayoutInflater().inflate(R.layout.menu_home, null);
				final PopupWindow mHomePopup = new PopupWindow(mHomePopupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
				LinearLayout background = mHomePopupView.findViewById(R.id.background);
				LinearLayout o1 = mHomePopupView.findViewById(R.id.o1);
				LinearLayout o2 = mHomePopupView.findViewById(R.id.o2);
				LinearLayout o3 = mHomePopupView.findViewById(R.id.o3);
				LinearLayout o4 = mHomePopupView.findViewById(R.id.o4);
				background.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)15, (int)1, 0xFF424242, 0xE9111111));
				applyHoverEffect(o1, highlight);
				applyHoverEffect(o2, highlight);
				applyHoverEffect(o3, highlight);
				applyHoverEffect(o4, highlight);
				o1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						openFilePicker();
						mHomePopup.dismiss();
					}
				});
				o2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						String url = "https://github.com/InfLps/PCD";
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(url));
							_view.getContext().startActivity(i);
						mHomePopup.dismiss();
					}
				});
				o3.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						_settingsDialog();
						mHomePopup.dismiss();
					}
				});
				o4.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						StringBuilder message = new StringBuilder();
						message.append("Pocket Canvas Draw (PCD)\n");
						message.append("v.6-stable | 2020-2026\n\n");
						
						message.append("Developed by InfLps\n");
						message.append("Licensed under Apache 2.0\n\n");
						
						message.append("━━━━━━━━━━━━━━━\n\n");
						
						message.append("Developer's Note:\n");
						message.append("PCD is an evolving open-source project. While we strive for precision, you may encounter experimental features or bugs.\n\n");
						
						message.append("Get Involved:\n");
						message.append("Your feedback drives our updates! Report issues or contribute code on our repository to help build the future of mobile digital art.");
						
						AlertDialog dialog = new AlertDialog.Builder(HomeActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
						.setTitle("About PCD")
						.setMessage(message.toString())
						.setPositiveButton("Dismiss", (d, which) -> {
								
						})
						.setNegativeButton("View GitHub", (d, which) -> {
								String url = "https://github.com/InfLps/PCD";
								Intent i = new Intent(Intent.ACTION_VIEW);
								i.setData(Uri.parse(url));
								_view.getContext().startActivity(i);
						})
						.setNeutralButton("Acknowledgements", (d, which) -> {
								StringBuilder libs = new StringBuilder();
								libs.append("PCD wouldn't be possible without these amazing open-source libraries:\n\n");
								
								libs.append("• Google Gson (2.10.1)\n");
								libs.append("  JSON serialization/deserialization\n\n");
								
								libs.append("• AndroidX Libraries\n");
								libs.append("  Modern UI components and animations\n\n");
								
								libs.append("• Apache Commons IO\n");
								libs.append("  Efficient file handling and utilities\n");
								AlertDialog ackDialog = new AlertDialog.Builder(HomeActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
								.setTitle("Third-Party Licenses")
								.setMessage(libs.toString())
								.setPositiveButton("Back", (innerD, innerWhich) -> {
								})
								.create();
								if (ackDialog.getWindow() != null) {
										ackDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
								}
								
								ackDialog.show();
						})
						
						.create();
						if (dialog.getWindow() != null) {
								dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
						}
						dialog.show();
						
						mHomePopup.dismiss();
					}
				});
				mHomePopup.setAnimationStyle(android.R.style.Animation_Dialog);
				mHomePopup.showAsDropDown(more, 0, 0);
				mHomePopup.setBackgroundDrawable(new BitmapDrawable());
			}
		});
	}
	
	private void initializeLogic() {
		
		initRecyclerView();
		applyHoverEffect(more, highlight);
		applyHoverEffect(subt, highlight);
		subt.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)50, 0xE6111111));
		toolbar.setBackground(new GradientDrawable(GradientDrawable.Orientation.BR_TL, new int[] {0xFF131313,0xE6111111}));
	}
	
	private void showColorPickerPopup(int initialColor, final View view) {
			View colpopupView = getLayoutInflater().inflate(R.layout.color_picker, null);
			final PopupWindow colpopup = new PopupWindow(colpopupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
			LinearLayout background = colpopupView.findViewById(R.id.background);
			TextView hex = colpopupView.findViewById(R.id.hexout);
			TextView dismiss = colpopupView.findViewById(R.id.dismiss);
			HueSVSquareColorPicker colorPicker = colpopupView.findViewById(R.id.colorPicker);
			RGBSlidersView rgbSliders = colpopupView.findViewById(R.id.rgbsliders);
			background.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)15, (int)1, 0xFF424242, 0xE9111111));
			colorPicker.setColor(initialColor);
			rgbSliders.setColor(initialColor);
		    applyHoverEffect(dismiss, highlight);
		    
		colorPicker.setOnColorChangedListener(new HueSVSquareColorPicker.OnColorChangedListener() {
			    @Override
			    public void onColorChanged(int color) {
				        rgbSliders.setColor(color);
				        view.setBackgroundColor(color);
				        CANV.edit().putInt("DEFAULTCOLOR", color).apply();
				        updateCombinedColorPreview(color, hex);
				    }
		});
		
			rgbSliders.setOnRGBChangedListener(new RGBSlidersView.OnRGBChangedListener() {
					@Override
					public void onRGBChanged(int r, int g, int b) {
							int newColor = Color.rgb(r, g, b);
							colorPicker.setColor(newColor);
				            view.setBackgroundColor(newColor);
				            CANV.edit().putInt("DEFAULTCOLOR", newColor).apply();
				            updateCombinedColorPreview(newColor, hex);
					}
			});
			dismiss.setOnClickListener(v -> colpopup.dismiss());
			
			colpopup.setAnimationStyle(android.R.style.Animation_Dialog);
			colpopup.setBackgroundDrawable(new BitmapDrawable());
			colpopup.showAtLocation(view, Gravity.BOTTOM | Gravity.LEFT, 60, 60);
	}
	
	private void updateCombinedColorPreview(int combinedColor, TextView _hex) {
			String hexCombinedColor = String.format("#%06X", (0xFFFFFF & combinedColor));
			int red = Color.red(combinedColor);
			int green = Color.green(combinedColor);
			int blue = Color.blue(combinedColor);
			_hex.setText(String.format("#%06X", combinedColor));
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
	
	private void openFilePicker() {
		    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		    intent.setType("*/*");
		    intent.addCategory(Intent.CATEGORY_OPENABLE);
		    startActivityForResult(Intent.createChooser(intent, "Select Project File"), PICK_PROJECT_FILE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		    super.onActivityResult(requestCode, resultCode, data);
		    if (requestCode == PICK_PROJECT_FILE && resultCode == RESULT_OK && data != null) {
			        Uri uri = data.getData();
			        if (uri != null) {
				            importProjectFromUri(uri);
				        }
			    }
	}
	
	private void importProjectFromUri(Uri uri) {
		    try {
			        String fileName = "imported_project_" + System.currentTimeMillis() + ".pcdproj";
			        android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
			        if (cursor != null && cursor.moveToFirst()) {
				            int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
				            if (nameIndex != -1) fileName = cursor.getString(nameIndex);
				            cursor.close();
				        }
			        File projectFolder = new File(getFilesDir(), "projects");
			        if (!projectFolder.exists()) projectFolder.mkdirs();
			        File destFile = new File(projectFolder, fileName);
			        try (InputStream is = getContentResolver().openInputStream(uri);
                         OutputStream os = new FileOutputStream(destFile)) {
				            byte[] buffer = new byte[1024];
				            int length;
				            while ((length = is.read(buffer)) > 0) {
					                os.write(buffer, 0, length);
					            }
				        }
			
			        Toast.makeText(this, "Imported: " + fileName, Toast.LENGTH_SHORT).show();
			        refreshData();
			
			    } catch (Exception e) {
			        e.printStackTrace();
			        Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			    }
	}
	
	private void initRecyclerView() {
			adapter = new ProjectAdapter(this, projectList, new ProjectAdapter.OnProjectActionListener() {
					@Override
					public void onProjectOpen(ProjectItem item) {
							Intent intent = new Intent(HomeActivity.this, MainActivity.class);
							if (!item.isNewButton) {
									intent.putExtra("projectPath", item.path);
							}
							
							ActivityOptions options = ActivityOptions.makeCustomAnimation(
							HomeActivity.this, 
							R.anim.slide_rotate_in,
							R.anim.fade_out
							);
							
							startActivity(intent, options.toBundle());
							overridePendingTransition(R.anim.slide_rotate_in, R.anim.fade_out);
							
					}
					
					@Override
					public void onRename(ProjectItem item) {
							final EditText input = new EditText(HomeActivity.this);
							input.setText(item.title.replace(".pcdproj", ""));
							input.setSelection(input.getText().length());
				            input.setTextColor(Color.WHITE);
							
							AlertDialog dialog = new AlertDialog.Builder(HomeActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
							.setTitle("Rename Project")
							.setView(input)
							.setPositiveButton("Save", (d, which) -> {
									String newName = input.getText().toString().trim();
									if (newName.isEmpty()) return;
									
									newName += ".pcdproj";
									File oldFile = new File(item.path);
									File newFile = new File(oldFile.getParent(), newName);
									
									if (oldFile.renameTo(newFile)) {
											refreshData();
									} else {
											Toast.makeText(HomeActivity.this, "Rename failed", Toast.LENGTH_SHORT).show();
									}
							})
							.setNegativeButton("Cancel", null)
							.create();
							if (dialog.getWindow() != null) {
									dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
							}
							dialog.show();
					}
					
					@RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
					public void onExport(ProjectItem item) {
							exportProjectToPublic(item);
					}
					
					@Override
					public void onDelete(ProjectItem item) {
							AlertDialog dialog = new AlertDialog.Builder(HomeActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
							.setTitle("Delete Project")
							.setMessage("Are you sure you want to delete '" + item.title + "'?")
							.setPositiveButton("Delete", (d, which) -> {
									File file = new File(item.path);
									if (file.exists() && file.delete()) {
											refreshData();
									}
							})
							.setNegativeButton("Cancel", null)
							.create();
							if (dialog.getWindow() != null) {
									dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
							}
							dialog.show();
					}
					
			});
			recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
			recyclerView.setAdapter(adapter);
			refreshData();
			
			refreshLayout.setOnRefreshListener(new ScaleRefreshLayout.OnRefreshListener() {
					@Override
					public void onRefresh() {
							refreshData();
					}
			});
	}
	
	private void refreshData() {
			projectList.clear();
			ProjectItem newBtn = new ProjectItem();
			newBtn.isNewButton = true;
			projectList.add(newBtn);
			List<ProjectItem> itemsFromFile = new ArrayList<>();
			loadProjectsFromStorage(itemsFromFile);
			
			Collections.sort(itemsFromFile, (p1, p2) -> {
					switch (currentSortMode) {
							case 1: // A-Z
							return p1.title.compareToIgnoreCase(p2.title);
							case 2: // Z-A
							return p2.title.compareToIgnoreCase(p1.title);
							case 0: // Newest First (Default)
							default:
							long d1 = new File(p1.path).lastModified();
							long d2 = new File(p2.path).lastModified();
							return Long.compare(d2, d1);
					}
			});
			
			projectList.addAll(itemsFromFile);
			
			if (adapter != null) {
					adapter.notifyDataSetChanged();
			}
			
			if (refreshLayout != null) {
					refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 500);
			}
	}
	
	
	@RequiresApi(api = Build.VERSION_CODES.Q)
    private void exportProjectToPublic(ProjectItem item) {
			File sourceFile = new File(item.path);
			if (!sourceFile.exists()) return;
			
			ContentValues values = new ContentValues();
			values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, item.title);
			values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");
			values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
			
			ContentResolver resolver = getContentResolver();
			Uri uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
			
			if (uri != null) {
					try (InputStream is = new FileInputStream(sourceFile);
                         OutputStream os = resolver.openOutputStream(uri)) {
							
							byte[] buffer = new byte[1024];
							int length;
							while ((length = is.read(buffer)) > 0) {
									os.write(buffer, 0, length);
							}
							Toast.makeText(this, "Exported to Downloads folder", Toast.LENGTH_LONG).show();
					} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
					}
			}
	}
	
	
	private void loadProjectsFromStorage(List<ProjectItem> list) {
			File folder = new File(getFilesDir(), "projects");
			if (!folder.exists()) folder.mkdirs();
			
			File[] files = folder.listFiles();
			if (files != null) {
					for (File f : files) {
							if (f.getName().endsWith(".pcdproj")) {
									Bitmap thumb = getThumbnailFromZip(f);
									list.add(new ProjectItem(f.getName(), f.getAbsolutePath(), thumb));
							}
					}
			}
	}
	
	private Bitmap getThumbnailFromZip(File file) {
			try (ZipFile zipFile = new ZipFile(file)) {
					ZipEntry entry = zipFile.getEntry("thumbnail.png");
					if (entry != null) {
							InputStream is = zipFile.getInputStream(entry);
							return BitmapFactory.decodeStream(is);
					}
			} catch (Exception e) {
					e.printStackTrace();
			}
			return null;
			
			
	}
	
	public void _settingsDialog() {
		final AlertDialog sDialog = new AlertDialog.Builder(HomeActivity.this).create();
		View sDialogView = getLayoutInflater().inflate(R.layout.settings, null);
		sDialog.setView(sDialogView);
		LinearLayout background = sDialogView.findViewById(R.id.background);
		TextView dismiss = sDialogView.findViewById(R.id.dismiss_bttn);
		
		Switch antialiasSw = sDialogView.findViewById(R.id.antialiassw);
		Switch askexitSw = sDialogView.findViewById(R.id.askexitsw);
		RadioGroup renderingGroup = sDialogView.findViewById(R.id.RadioGroup1);
		RadioGroup proxyGroup = sDialogView.findViewById(R.id.radiogroup2);
		
		LinearLayout o3 = sDialogView.findViewById(R.id.o3);
		LinearLayout col = sDialogView.findViewById(R.id.col);
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		int screenHeight = getResources().getDisplayMetrics().heightPixels;
		int intCol = CANV.getInt("DEFAULTCOLOR", Color.BLACK); 
		col.setBackgroundColor(intCol);
		float cornerRadiusDp = 20f;
		float cornerRadiusPx = TypedValue.applyDimension(
		    TypedValue.COMPLEX_UNIT_DIP,
		    cornerRadiusDp,
		    getResources().getDisplayMetrics()
		);
		
		background.setBackground(new GradientDrawable() {
			    public GradientDrawable getIns(float a, int b, int c, int d) {
				        this.setCornerRadius(a);
				        this.setStroke(b, c);
				        this.setColor(d);
				        return this;
				    }
		}.getIns(cornerRadiusPx, 1, 0xFFE0E0E0, 0xFF111111));
		
		applyHoverEffect(dismiss, highlight);
		applyHoverEffect(col, highlight);
		
		dismiss.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
				sDialog.dismiss();
			}
		});
		o3.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
					col.performClick();
				}
		});
		col.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
				showColorPickerPopup(intCol, col);
			}
		});
		antialiasSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
			    CANV.edit().putBoolean("ANTIALIAS", isChecked).apply();
		});
		
		askexitSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
			    CANV.edit().putBoolean("ALERTDIAL", isChecked).apply();
		});
		
		renderingGroup.setOnCheckedChangeListener((group, checkedId) -> {
			    boolean isHardware = (checkedId == R.id.gpu_rb);
			    CANV.edit().putString("LAYERTYPERENDER", isHardware ? "HARDWARE" : "SOFTWARE").apply();
		});
		
		proxyGroup.setOnCheckedChangeListener((group, checkedId) -> {
			    String res = "2048";
			    if (checkedId == R.id.prox4096) res = "4096";
			    else if (checkedId == R.id.prox2048) res = "2048";
			    else if (checkedId == R.id.prox1280) res = "1280";
			    else if (checkedId == R.id.prox720) res = "720";
			    else if (checkedId == R.id.prox512) res = "512";
			    
			    CANV.edit().putString("MAXPROXY", res).apply();
		});
		
		antialiasSw.setChecked(CANV.getBoolean("ANTIALIAS", true));
		askexitSw.setChecked(CANV.getBoolean("ALERTDIAL", true));
		
		String renderMode = CANV.getString("LAYERTYPERENDER", "HARDWARE");
		renderingGroup.check(renderMode.equals("HARDWARE") ? R.id.gpu_rb : R.id.cpu_rb);
		
		String maxProxy = CANV.getString("MAXPROXY", "2048");
		switch (maxProxy) {
			    case "4096": proxyGroup.check(R.id.prox4096); break;
			    case "2048": proxyGroup.check(R.id.prox2048); break;
			    case "1280": proxyGroup.check(R.id.prox1280); break;
			    case "720":  proxyGroup.check(R.id.prox720); break;
			    case "512":  proxyGroup.check(R.id.prox512); break;
		}
		
		sDialog.show();
		
		if (sDialog.getWindow() != null) {
			    sDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}
	}
	
}
