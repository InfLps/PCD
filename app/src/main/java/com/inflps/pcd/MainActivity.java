package com.inflps.pcd;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.SharedPreferences;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.TypedValue;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.inflps.pcd.CORE.BRUSH_CORE.*;
import com.inflps.pcd.CORE.BRUSH_CORE.ADAPTER.*;
import com.inflps.pcd.CORE.DRAWING_CORE.*;
import com.inflps.pcd.CORE.DRAWING_CORE.DRAWABLE.ShapeIconDrawable;
import com.inflps.pcd.CORE.FONTS.ADAPTER.FontAdapter;
import com.inflps.pcd.CORE.FONTS.ADAPTER.FontItem;
import com.inflps.pcd.CORE.LAYERS.LayerTouchHelperCallback;
import com.inflps.pcd.CORE.LISTENERS.DrawingViewListener;
import com.inflps.pcd.CORE.PROJECT_CORE.*;
import com.inflps.pcd.CORE.TOOLS.*;
import com.inflps.pcd.MISCELLANEOUS.*;
import com.inflps.pcd.WIDGET.COLOR_PICKER.*;
import com.inflps.pcd.WIDGET.CONTROL_VIEW.*;
import com.inflps.pcd.WIDGET.MISCELLANEOUS.*;
import com.inflps.pcd.WIDGET.PANEL.BottomPanelHandler;
import java.io.OutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.text.SimpleDateFormat;


public class MainActivity extends AppCompatActivity {
	
	private int curcolor = Color.BLACK; private int curalpha = 255; private String highlight = "#353535"; 
	private int wd = 1280; private int hg = 1280;
	private int realwd = 1280; private int realhg = 1280;
	private int fillState = 1; private int tState = 0;
	
	private BottomPanelHandler bottomPanelHandler;
	
	private boolean isSaving = false; private boolean isEditingPrimary = true; private boolean isUncreatedProject = false;
	
	private LayerAdapter layerAdapter; private BrushAdapter adapter; private FontAdapter fontAdapter;
	
	private List<LayerItem> layerItems;
	private List<BrushItem> items = new ArrayList<>();
	private List<FontItem> fontList = new ArrayList<>();
	
	private String currentProjectPath = null;
	
	private ActivityResultLauncher<String> imagePickerLauncher;
	private ActivityResultLauncher<String> brushPickerLauncher;
	private ActivityResultLauncher<String> fontPickerLauncher;
	
	private static final String BRUSH_FOLDER = "Brushes";
	
	private final DrawingState.ShapeType[] shapeOrder = {
		    DrawingState.ShapeType.LINE, DrawingState.ShapeType.RECTANGLE, DrawingState.ShapeType.CIRCLE, 
		    DrawingState.ShapeType.TRIANGLE, DrawingState.ShapeType.RIGHT_TRIANGLE, DrawingState.ShapeType.CAPSULE, 
		    DrawingState.ShapeType.KITE, DrawingState.ShapeType.PENTAGON, DrawingState.ShapeType.HEXAGON,
		    DrawingState.ShapeType.HEPTAGON, DrawingState.ShapeType.OCTAGON, DrawingState.ShapeType.DECAGON,
		    DrawingState.ShapeType.PLUS, DrawingState.ShapeType.DIAMOND, DrawingState.ShapeType.STAR_3,
		    DrawingState.ShapeType.STAR_4, DrawingState.ShapeType.STAR, DrawingState.ShapeType.STAR_6,
		    DrawingState.ShapeType.GEAR, DrawingState.ShapeType.LIGHTNING, DrawingState.ShapeType.SPEECH_BUBBLE,
		    DrawingState.ShapeType.THINKING_BUBBLE, DrawingState.ShapeType.ARROW, DrawingState.ShapeType.SHIELD,
		    DrawingState.ShapeType.L_SHAPE, DrawingState.ShapeType.COIL, DrawingState.ShapeType.CHEVRON,
		    DrawingState.ShapeType.CYLINDER, DrawingState.ShapeType.CONE, DrawingState.ShapeType.CUBE,
		    DrawingState.ShapeType.PYRAMID, DrawingState.ShapeType.PRISM_TRIANGULAR, DrawingState.ShapeType.HEART,
		    DrawingState.ShapeType.CLOUD, DrawingState.ShapeType.SEMICIRCLE, DrawingState.ShapeType.CRESCENT,
		    DrawingState.ShapeType.PIE_SLICE, DrawingState.ShapeType.TRAPEZOID, DrawingState.ShapeType.PARALLELOGRAM,
		    DrawingState.ShapeType.TETRAHEDRON, DrawingState.ShapeType.OCTAHEDRON, DrawingState.ShapeType.ICOSAHEDRON,
		    DrawingState.ShapeType.TREFOIL, DrawingState.ShapeType.TAG
	};
	
	private enum ToolType {
		    PEN,
		    SMUDGE,
		    BUCKET,
		    TEXT,
		    SHAPE,
		    NONE
	}
	
	private ToolType currentTool = ToolType.PEN;
	private ColorDrawable highlightDrawable;
	private DrawingState.PaintStyle currentStyle = DrawingState.PaintStyle.STROKE;
	private ImageView[] shapeButtons = new ImageView[44];
	private OnClickListener shapeClickListener;
	private String FILE_TAG = "";
	private String DRAWING_NAME = "";
	private String CURRENT_TIME = "";
	private String FULL_FILE_NAME = "";
	private String FULL_PROJECT_NAME = "";
	
	private FrameLayout rootBackground;
	private DrawingView pdv;
	private LinearLayout main_container;
	private FrameLayout foreground;
	private LinearLayout toolbar;
	private LinearLayout center_container;
	private LinearLayout bttm_container;
	private LinearLayout tb1;
	private ImageView back;
	private EditText projtitle;
	private ImageView commit;
	private ImageView undo;
	private ImageView redo;
	private ImageView more;
	private LinearLayout center_toolbar;
	private LinearLayout spacebar2;
	private LinearLayout lib;
	private LinearLayout lb;
	private LinearLayout inv;
	private LinearLayout smudgePowerGroup;
	private LinearLayout filltoleranceGroup;
	private LinearLayout swichPenGroup;
	private LinearLayout strokeColorGroup;
	private LinearLayout strokeThicknessGroup;
	private LinearLayout objectAlphaGroup;
	private DragControlSmudgeView smudgePower;
	private DragControlToleranceView filltolerance;
	private ToolSwapView switchPen;
	private ColorScrubView col;
	private DragControlView strokeThickness;
	private DragControlOpacityView objectAlpha;
	private ImageView layers;
	private LinearLayout layer_container;
	private LinearLayout new_layer_bttn;
	private RecyclerView layersRecyclerView;
	private ImageView nlbttn_icon;
	private LinearLayout LinearLayout1;
	private LinearLayout bb1;
	private LinearLayout clear_all_bttn;
	private TextView clear_text_bttn;
	private LinearLayout h1;
	private LinearLayout chps;
	private ImageView pen;
	private ImageView smudge;
	private ImageView text;
	private ImageView image;
	private ImageView bucket;
	private ImageView shapes;
	private LinearLayout line;
	private ImageView arrow;
	private LinearLayout textMode;
	private LinearLayout brushMode;
	private TextView textit;
	private EditText textInput;
	private TextView textt;
	private RecyclerView fontRecyclerView;
	private TextView warnTag;
	private RecyclerView recycler_brushes;
	
	private AlertDialog.Builder d_alert;
	private AlertDialog.Builder d_about;
	private AlertDialog.Builder d_exit_confirmation;
	private SharedPreferences wtag;
	private SharedPreferences CANV;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.main);
		initialize(_savedInstanceState);
		initializeLogic();

		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				boolean alertDL = CANV.getBoolean("ALERTDIAL", true);
				if (bottomPanelHandler.isExpanded()) { bottomPanelHandler.collapse(); return; }
				if (alertDL) {
					AlertDialog dialog = new AlertDialog.Builder(MainActivity.this,
							android.R.style.Theme_DeviceDefault_Dialog_Alert)
							.setTitle("Exiting the project?")
							.setMessage("Your work will be saved once you exit.")
							.setPositiveButton("Exit", (d, which) -> {
								if (!isUncreatedProject) { saveProject(); } finish();
								overridePendingTransition(R.anim.fade_in, R.anim.slide_rotate_out);
							})
							.setNegativeButton("Cancel", null)
							.create();
					if (dialog.getWindow() != null) { dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg); }
					dialog.show();
				} else {
					if (!isUncreatedProject) { saveProject(); }
					finish();
					overridePendingTransition(R.anim.fade_in, R.anim.slide_rotate_out);
				}
			}
		});
	}
	
	private void initialize(Bundle _savedInstanceState) {
		rootBackground = findViewById(R.id.rootBackground);
		pdv = findViewById(R.id.pdv);
		main_container = findViewById(R.id.main_container);
		foreground = findViewById(R.id.foreground);
		toolbar = findViewById(R.id.toolbar);
		center_container = findViewById(R.id.center_container);
		bttm_container = findViewById(R.id.bttm_container);
		tb1 = findViewById(R.id.tb1);
		back = findViewById(R.id.back);
		projtitle = findViewById(R.id.projtitle);
		commit = findViewById(R.id.commit);
		undo = findViewById(R.id.undo);
		redo = findViewById(R.id.redo);
		more = findViewById(R.id.more);
		center_toolbar = findViewById(R.id.center_toolbar);
		spacebar2 = findViewById(R.id.spacebar2);
		lib = findViewById(R.id.lib);
		lb = findViewById(R.id.lb);
		inv = findViewById(R.id.inv);
		smudgePowerGroup = findViewById(R.id.smudgePowerGroup);
		filltoleranceGroup = findViewById(R.id.filltoleranceGroup);
		swichPenGroup = findViewById(R.id.swichPenGroup);
		strokeColorGroup = findViewById(R.id.strokeColorGroup);
		strokeThicknessGroup = findViewById(R.id.strokeThicknessGroup);
		objectAlphaGroup = findViewById(R.id.objectAlphaGroup);
		smudgePower = findViewById(R.id.smudgePower);
		filltolerance = findViewById(R.id.filltolerance);
		switchPen = findViewById(R.id.switchPen);
		col = findViewById(R.id.col);
		strokeThickness = findViewById(R.id.strokeThickness);
		objectAlpha = findViewById(R.id.objectAlpha);
		layers = findViewById(R.id.layers);
		layer_container = findViewById(R.id.layer_container);
		new_layer_bttn = findViewById(R.id.new_layer_bttn);
		layersRecyclerView = findViewById(R.id.layersRecyclerView);
		nlbttn_icon = findViewById(R.id.nlbttn_icon);
		LinearLayout1 = findViewById(R.id.LinearLayout1);
		bb1 = findViewById(R.id.bb1);
		clear_all_bttn = findViewById(R.id.clear_all_bttn);
		clear_text_bttn = findViewById(R.id.clear_text_bttn);
		h1 = findViewById(R.id.h1);
		chps = findViewById(R.id.chps);
		pen = findViewById(R.id.pen);
		smudge = findViewById(R.id.smudge);
		text = findViewById(R.id.text);
		image = findViewById(R.id.image);
		bucket = findViewById(R.id.bucket);
		shapes = findViewById(R.id.shapes);
		line = findViewById(R.id.line);
		arrow = findViewById(R.id.arrow);
		textMode = findViewById(R.id.textMode);
		brushMode = findViewById(R.id.brushMode);
		textit = findViewById(R.id.textit);
		textInput = findViewById(R.id.textInput);
		textt = findViewById(R.id.textt);
		fontRecyclerView = findViewById(R.id.fontRecyclerView);
		warnTag = findViewById(R.id.warnTag);
		recycler_brushes = findViewById(R.id.recycler_brushes);
		d_alert = new AlertDialog.Builder(this);
		d_about = new AlertDialog.Builder(this);
		d_exit_confirmation = new AlertDialog.Builder(this);
		wtag = getSharedPreferences("prefdata", Activity.MODE_PRIVATE);
		CANV = getSharedPreferences("CANV.SETTINGS", Activity.MODE_PRIVATE);

		back.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View _view) {
				AlertDialog dialog = new android.app.AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert).setTitle("Abandon unsaved edits to the project?")
						.setMessage(" If you confirm this, you will not be able to restore the changes edited from this session.")
						.setPositiveButton("Exit", (d, which) -> {
							isUncreatedProject = true; finish();
						})
						.setNegativeButton("Cancel", null)
						.create();
				if (dialog.getWindow() != null) { dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);}
				dialog.show();
				return true;
			}
		});
		
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				getOnBackPressedDispatcher().onBackPressed();
			}
		});
		
		projtitle.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				final String _charSeq = _param1.toString();
				FULL_PROJECT_NAME = _charSeq;
			}
			
			@Override
			public void beforeTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				
			}
			
			@Override
			public void afterTextChanged(Editable _param1) {
				
			}
		});
		
		commit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				pdv.commitCurrentShape();
			}
		});
		
		undo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				pdv.undo();
				updateCurrentLayerPreview();
			}
		});
		
		redo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				pdv.redo();
				updateCurrentLayerPreview();
			}
		});
		
		more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				View moptionspopupView = getLayoutInflater().inflate(R.layout.more_options, null);
				final PopupWindow moptionspopup = new PopupWindow(moptionspopupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
				LinearLayout background = moptionspopupView.findViewById(R.id.background);
				LinearLayout o1 = moptionspopupView.findViewById(R.id.o1);
				LinearLayout o2 = moptionspopupView.findViewById(R.id.o2);
				LinearLayout o3 = moptionspopupView.findViewById(R.id.o3);
				background.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)15, (int)1, 0xFF424242, 0xE9111111));
				setIcState(1);
				
				applyHoverEffect(o1, highlight);
				applyHoverEffect(o2, highlight);
				applyHoverEffect(o3, highlight);
				o1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						Bitmap exportedBitmap = pdv.exportHighRes();
						if (exportedBitmap != null) {
								saveBitmapToPictures(exportedBitmap, FULL_FILE_NAME);
						} else {
								Toast.makeText(MainActivity.this, "Nothing to save!", Toast.LENGTH_SHORT).show();
						}
						
						
						moptionspopup.dismiss();
					}
				});
				o2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						_startupDialog(2);
						moptionspopup.dismiss();
					}
				});
				o3.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						pdv.fitCanvasToScreen();
						moptionspopup.dismiss();
					}
				});
				moptionspopup.setAnimationStyle(android.R.style.Animation_Dialog);
				moptionspopup.showAsDropDown(more, 0, -10);
				moptionspopup.setBackgroundDrawable(new BitmapDrawable());
			}
		});
		
		layers.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (lb.getVisibility() == View.GONE) {
					    if (lb.getWidth() == 0) {
						        lb.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
							            @Override
							            public void onGlobalLayout() {
								                lb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
								                startSlideAnimation(true);
								            }
							        });
						        lb.setVisibility(View.VISIBLE); lb.setAlpha(0f);
						    } else {
						        startSlideAnimation(true);
						    }
				} else {
					    startSlideAnimation(false);
				}
			}
		});
		
		new_layer_bttn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (layerItems.size() < 30) {
					    pdv.addLayer();
					    int newLayerId = pdv.getCurrentLayerId();
					    layerItems.add(new LayerItem(newLayerId, true, false, PorterDuff.Mode.SRC_OVER, false));
					    layerAdapter.notifyItemInserted(layerItems.size() - 1);
					    layerAdapter.setSelectedLayer(newLayerId);
					    layersRecyclerView.scrollToPosition(layerItems.size() - 1);
				} else {
					    Toast.makeText(MainActivity.this, "Maximum 30 layers allowed.", Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		clear_all_bttn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				pdv.clearCurrentLayer();
				updateCurrentLayerPreview();
			}
		});
		
		pen.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				currentTool = ToolType.PEN;
				activatePen();
			}
		});
		
		smudge.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				currentTool = ToolType.SMUDGE;
				activateSmudge();
			}
		});
		
		text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				pdv.addText(textInput.getText().toString()); String hexCurColor = String.format("#%06X", (0xFFFFFF & curcolor));
				pdv.setDrawingColor(hexCurColor); pdv.setAlpha(curalpha);
				hideClearCanvasBttn(); setIcState(3);
				bottomPanelHandler.expand();
				setTState(1);
				smudgePowerGroup.setVisibility(View.GONE);
				filltoleranceGroup.setVisibility(View.GONE);
				swichPenGroup.setVisibility(View.GONE);
				inv.setVisibility(View.VISIBLE);
				strokeThickness.setAlpha((float)(0.3d));
				col.setAlpha((float)(1));
				objectAlpha.setAlpha((float)(1));
			}
		});
		
		image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				launchImagePicker();
			}
		});
		
		bucket.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				currentTool = ToolType.BUCKET;
				activateBucket();
			}
		});
		
		shapes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				String hexCurColor = String.format("#%06X", (0xFFFFFF & curcolor));
				pdv.setDrawingColor(hexCurColor); pdv.setAlpha(curalpha);
				View shpspopupView = getLayoutInflater().inflate(R.layout.shapes, null);
				final PopupWindow shpspopup = new PopupWindow(shpspopupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
				LinearLayout background = shpspopupView.findViewById(R.id.background);
				
				ImageView f1 = shpspopupView.findViewById(R.id.f1);
				ImageView f2 = shpspopupView.findViewById(R.id.f2);
				
				applyHoverEffect(f1, highlight);
				applyHoverEffect(f2, highlight);
				background.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)15, (int)1, 0xFF424242, 0xE9111111));
				if (fillState == 0) {
					f1.setColorFilter(Color.parseColor("#FFC107"));
					f2.setColorFilter(Color.parseColor("#FFFFFF"));
					pdv.setPaintStyle(DrawingState.PaintStyle.FILL);
					refreshShapeIcons(DrawingState.PaintStyle.FILL);
				}
				else {
					f1.setColorFilter(Color.parseColor("#FFFFFF"));
					f2.setColorFilter(Color.parseColor("#FFC107"));
					pdv.setPaintStyle(DrawingState.PaintStyle.STROKE);
					refreshShapeIcons(DrawingState.PaintStyle.STROKE);
				}
				OnClickListener shapeClickListener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						int index = (int) v.getTag();
						DrawingState.ShapeType selectedType = shapeOrder[index];
						pdv.setDrawingMode(DrawingState.DrawingMode.SHAPE);
						pdv.setAlpha(objectAlpha.getCurrentValue());
						pdv.setShapeType(selectedType);
								
						boolean needsStroke = false;
						boolean needsFill = false;
								
						switch (selectedType) {
							case LINE: case COIL: case CYLINDER: case CONE: 
							case CUBE: case PYRAMID: case PRISM_TRIANGULAR: 
							case TETRAHEDRON: case OCTAHEDRON: case ICOSAHEDRON: 
							case TREFOIL:
							needsStroke = true;
							break;
							case CLOUD:
							needsFill = true;
							break;
						}
								
						if (fillState == 1 && needsStroke) {
							pdv.setPaintStyle(DrawingState.PaintStyle.STROKE);
						} 
						else if (fillState == 2 && needsFill) {
							pdv.setPaintStyle(DrawingState.PaintStyle.FILL);
						}
						setIcState(6);
						hideClearCanvasBttn();
						shpspopup.dismiss();
						smudgePowerGroup.setVisibility(View.GONE);
						filltoleranceGroup.setVisibility(View.GONE);
						swichPenGroup.setVisibility(View.GONE);
						inv.setVisibility(View.VISIBLE);
						strokeThickness.setAlpha((float)(1));
						col.setAlpha((float)(1));
						objectAlpha.setAlpha((float)(1));
						setTState(2);
						bottomPanelHandler.collapse();
					}
				};
				
				shapeButtons = new ImageView[44];
				for (int i = 0; i < 44; i++) {
						int resId = getResources().getIdentifier("s" + (i + 1), "id", getPackageName());
						shapeButtons[i] = shpspopupView.findViewById(resId);
						shapeButtons[i].setTag(i);
					    applyHoverEffect(shapeButtons[i], highlight);
						shapeButtons[i].setOnClickListener(shapeClickListener);
				}
				
				refreshShapeIcons(currentStyle);
				f1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						f1.setColorFilter(Color.parseColor("#FFC107"));
						f2.setColorFilter(Color.parseColor("#FFFFFF"));
						pdv.setPaintStyle(DrawingState.PaintStyle.FILL);
						pdv.setAlpha(objectAlpha.getCurrentValue());
						refreshShapeIcons(DrawingState.PaintStyle.FILL);
					}
				});
				f2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						f1.setColorFilter(Color.parseColor("#FFFFFF"));
						f2.setColorFilter(Color.parseColor("#FFC107"));
						pdv.setPaintStyle(DrawingState.PaintStyle.STROKE);
						pdv.setAlpha(objectAlpha.getCurrentValue());
						refreshShapeIcons(DrawingState.PaintStyle.STROKE);
					}
				});
				shpspopup.setAnimationStyle(android.R.style.Animation_Dialog);
				shpspopup.showAtLocation(shapes, Gravity.BOTTOM | Gravity.RIGHT, 60, 60);
				shpspopup.setBackgroundDrawable(new BitmapDrawable());
			}
			
			void refreshShapeIcons(DrawingState.PaintStyle style) {
					int color = Color.parseColor("#FFFFFF");
					for (int i = 0; i < shapeButtons.length; i++) {
							if (shapeButtons[i] != null) {
									shapeButtons[i].setImageDrawable(new ShapeIconDrawable(shapeOrder[i], color, style));
							}
					}
			}
		});
		
		arrow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (textMode.getVisibility() == View.VISIBLE || brushMode.getVisibility() == View.VISIBLE) {
					bottomPanelHandler.toggle();
				}
			}
		});
		
		textInput.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				final String _charSeq = _param1.toString();
				pdv.updateActiveText(_charSeq);
			}
			
			@Override
			public void beforeTextChanged(CharSequence _param1, int _param2, int _param3, int _param4) {
				
			}
			
			@Override
			public void afterTextChanged(Editable _param1) {
				
			}
		});
		
		warnTag.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View _view) {
				View woptionspopupView = getLayoutInflater().inflate(R.layout.warn_hide_option, null);
				final PopupWindow woptionspopup = new PopupWindow(woptionspopupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
				LinearLayout background = woptionspopupView.findViewById(R.id.background);
				LinearLayout o1 = woptionspopupView.findViewById(R.id.o1);
				LinearLayout o2 = woptionspopupView.findViewById(R.id.o2);
				background.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)15, (int)1, 0xFF424242, 0xE9111111));
				applyHoverEffect(o1, highlight);
				applyHoverEffect(o2, highlight);
				o1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						warnTag.setVisibility(View.GONE);
						woptionspopup.dismiss();
					}
				});
				o2.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View _view) {
						wtag.edit().putString("hide", "true").commit();
						warnTag.setVisibility(View.GONE);
						woptionspopup.dismiss();
					}
				});
				woptionspopup.setAnimationStyle(android.R.style.Animation_Dialog);
				woptionspopup.showAsDropDown(warnTag, 0, 0);
				woptionspopup.setBackgroundDrawable(new BitmapDrawable());
			}
		});
	}
	
	private void initializeLogic() {
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			    getWindow().setBackgroundBlurRadius(20); 
		}
		
		DRAWING_NAME = "PCD";
		FILE_TAG = generateRandomTag();
		CURRENT_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
		            .format(new Date());
		FULL_FILE_NAME = DRAWING_NAME.concat("_".concat(CURRENT_TIME.concat("_".concat(FILE_TAG.concat(".png")))));
		projtitle.setText(FULL_PROJECT_NAME);
		if (!CANV.contains("MAXPROXY")) {
				CANV.edit()
				.putBoolean("ANTIALIAS", true)
				.putString("LAYERTYPERENDER", "HARDWARE")
				.putString("MAXPROXY", "2048")
				.putInt("DEFAULTCOLOR", 0xFF000000)
				.putBoolean("ALERTDIAL", true)
				.apply();
		}
		
		initSettings();
		
		strokeThickness.setCircleColor(Color.WHITE);
		strokeThickness.setTextColor(Color.BLACK);
		strokeThickness.setTextSize(16);
		strokeThickness.setSensitivity(0.3f);
		strokeThickness.setMinValue(1);
		strokeThickness.setMaxValue(500); 
		strokeThickness.setCurrentValue(10);
		
		strokeThickness.setOnValueChangeListener(new DragControlView.OnValueChangeListener() {
				@Override
				public void onValueChange(float newValue) {
						pdv.setStrokeWidth((int)newValue);
				        pdv.setSmudgeBrushSize((int)newValue);
				}
		});
		
		objectAlpha.setCheckerSquareSizeDp(10);
		objectAlpha.setCheckerColor1(Color.parseColor("#C9C9C9"));
		objectAlpha.setCheckerColor2(Color.parseColor("#838383"));
		objectAlpha.setIndicatorColor(Color.WHITE);
		objectAlpha.setTextColor(Color.BLACK);
		objectAlpha.setTextSizeDp(16);
		objectAlpha.setMinValue(0);
		objectAlpha.setMaxValue(255);
		objectAlpha.setCurrentValue(255);
		objectAlpha.setSensitivity(0.5f);
		objectAlpha.setMinOpacityAlpha(0);
		objectAlpha.setMaxOpacityAlpha(255);
		objectAlpha.setOnValueChangeListener(new DragControlOpacityView.OnValueChangeListener() {
				@Override
				public void onValueChange(float newValue) {
						pdv.setAlpha((int)newValue);
				        curalpha = ((int)newValue);
				}
		});
		
		filltolerance.setColor(Color.WHITE);
		filltolerance.setOnValueChangeListener(new DragControlToleranceView.OnValueChangeListener() {
				@Override
				public void onValueChange(float newValue) {
						pdv.setFillTolerance((int)newValue);
				}
		});
		
		
		smudgePower.setSensitivity(0.5f);
		smudgePower.setTrackColor(Color.TRANSPARENT);
		smudgePower.setProgressColor(Color.WHITE);
		smudgePower.setOnValueChangeListener(new DragControlSmudgeView.OnValueChangeListener() {
				@Override
				public void onValueChange(float newValue) {
				        pdv.setSmudgeStrength(newValue / 100.0f);
				}
		});
		
		col.setOnColorScrubListener(new ColorScrubView.OnColorScrubListener() {
			    @Override
			    public void onColorChanged(int color) {
				        pdv.setDrawingColor(String.format("#%06X", (0xFFFFFF & color)));
				        curcolor = color;
				    }
			
			    @Override
			    public void onColorClicked(int color) {
				        showColorPickerPopup(color);
				    }
		});
		
		Drawable penIcon = getResources().getDrawable(R.drawable.pen, getTheme());
		Drawable eraserIcon = getResources().getDrawable(R.drawable.eraser, getTheme());
		
		switchPen.setPrimaryDrawable(penIcon);
		switchPen.setSecondaryDrawable(eraserIcon);
		
		switchPen.setOnToolSwapListener(isPrimaryActive -> {
				if (isPrimaryActive) {
						pdv.setNeutralMode();
						pdv.setDrawingMode(DrawingState.DrawingMode.FREEHAND);
						int ccolor = col.getCurrentColor();
						pdv.setDrawingColor(String.format("#%06X", (0xFFFFFF & ccolor)));
						hideClearCanvasBttn();
				        pen.setImageResource(R.drawable.pen);
				} else {
						clear_all_bttn.setVisibility(View.VISIBLE);
						pdv.setEraserMode(true);
				        pen.setImageResource(R.drawable.eraser);
				}
		});
		
		float[] tb1Corners = {0, 0, 0, 0, 0, 0, 0, 0};
		setCustomBackground(tb1, tb1Corners);
		
		float[] bb1Corners = {20, 20, 20, 20, 0, 0, 0, 0};
		setCustomBackground(bb1, bb1Corners);
		
		float[] strokeColorGroupDrawable = {50, 50, 50, 50, 50, 50, 50, 50};
		setCustomBackground(strokeColorGroup, strokeColorGroupDrawable);
		
		float[] lbCorners = {0, 0, 0, 0, 0, 0, 10, 10};
		setCustomBackground(lb, lbCorners);
		
		float[] libCorners = {16, 16, 0, 0, 0, 0, 16, 16};
		setCustomBackground(lib, libCorners);
		
		float[] clrcvsbtnCorners = {20, 20, 20, 20, 20, 20, 20, 20};
		setCustomBackground(clear_all_bttn, clrcvsbtnCorners);
		
		lb.setClipToOutline(true);
		
		float[] objectAlphaGroupDrawable = {50, 50, 50, 50, 50, 50, 50, 50};
		setCustomBackground(objectAlphaGroup, objectAlphaGroupDrawable);
		
		float[] strokeThicknessGroupDrawable = {50, 50, 50, 50, 50, 50, 50, 50};
		setCustomBackground(strokeThicknessGroup, strokeThicknessGroupDrawable);
		
		float[] swichPenGroupDrawable = {50, 50, 50, 50, 50, 50, 50, 50};
		setCustomBackground(swichPenGroup, swichPenGroupDrawable);
		
		float[] filltoleranceGroupDrawable = {50, 50, 50, 50, 50, 50, 50, 50};
		setCustomBackground(filltoleranceGroup, filltoleranceGroupDrawable);
		
		float[] smudgePowerGroupDrawable = {50, 50, 50, 50, 50, 50, 50, 50};
		setCustomBackground(smudgePowerGroup, smudgePowerGroupDrawable);
		
		pdv.setDrawingMode(DrawingState.DrawingMode.FREEHAND);
		pdv.setStrokeWidth(10f);
		pdv.setCanvasBackgroundColor(0xFF292929);
		setIcState(1);
		View[] myTools = {back, undo, redo, commit, more, layers, pen, smudge, text, image, bucket, shapes, arrow, new_layer_bttn, clear_all_bttn};
		for (View tool : myTools) {
			    if (tool != null) {
				        applyHoverEffect(tool, highlight);
				    }
		}
		
		final PorterDuff.Mode[] blendModes = {
				PorterDuff.Mode.SRC_OVER, 
			    PorterDuff.Mode.MULTIPLY, 
			    PorterDuff.Mode.SCREEN,
				PorterDuff.Mode.OVERLAY, 
			    PorterDuff.Mode.DARKEN, 
			    PorterDuff.Mode.LIGHTEN,
				PorterDuff.Mode.XOR, 
			    PorterDuff.Mode.CLEAR
		};
		
		pdv.setLogicalCanvasSize(1280, 1280);
		pdv.setCanvasBackgroundColor(Color.WHITE);
		layerItems = new ArrayList<>();
		layerItems.add(new LayerItem(0, pdv.isLayerVisible(0), pdv.isLayerLocked(0), pdv.getLayerBlendMode(0), false));
		layersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		layerAdapter = new LayerAdapter(layerItems);
		layerAdapter.setSelectedLayer(0); 
		layersRecyclerView.setAdapter(layerAdapter);
		ItemTouchHelper.Callback callback = new LayerTouchHelperCallback(layerAdapter, pdv);
		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
		itemTouchHelper.attachToRecyclerView(layersRecyclerView);
		
		pdv.setDrawingViewListener(new DrawingViewListener() {
				@Override
				public void onDrawingStarted() { }
				
				@Override
				public void onDrawingFinished() {
						pdv.refreshCanvas();
						for (LayerItem item : layerItems) {
								item.setPreviewBitmap(pdv.getLayerBitmap(item.getLayerId()));
						}
						layerAdapter.notifyDataSetChanged();
				}
				
				@Override
				public void onZoomStarted() { }
				
				@Override
				public void onZoomFinished() { }
				
				@Override
				public void onLayersChanged() { }
				
				@Override
				public void onShapeAdded(DrawingState.ShapeType shapeType) { 
						commit.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onImageAdded(int width, int height) {
						commit.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onTextAdded(String text) {
						commit.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onTextEditStarted(String currentText) { }
				
				@Override
				public void onActiveObjectTransforming(RectF bounds, float rotation) {
						commit.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onActiveObjectSelected(boolean isSelected) {
						commit.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onObjectCommitted() {
						commit.setVisibility(View.GONE);
						
						switch (currentTool) {
								case PEN:
								pdv.postDelayed(() -> activatePen(), 70);
								break;
								case SMUDGE:
								pdv.postDelayed(() -> activateSmudge(), 70);
								break;
								case BUCKET:
								pdv.postDelayed(() -> activateBucket(), 70);
								break;
								default:
								break;
						}
				}
				
				@Override
				public void onCanvasResized(int width, int height) { }
				
				@Override
				public void onColorChanged(int newColor) { 
				        col.setCurrentColor(newColor);
				    }
				
				@Override
				public void onOpacityChanged(int newOpacity) { 
						curalpha = newOpacity;
				}
				
				@Override
				public void onBrushSizeChanged(float newSize) { }
			        
			    @Override
			    public void onLayerThumbnailUpdated(int layerId, Bitmap thumbnail) {
				        runOnUiThread(() -> {
					            for (int i = 0; i < layerItems.size(); i++) {
						                if (layerItems.get(i).getLayerId() == layerId) {
							                    layerItems.get(i).setPreviewBitmap(thumbnail);
							                    layerAdapter.notifyItemChanged(i);
							                    break;
							                }
						            }
					        });
				    }
		});
		
		bottomPanelHandler = new BottomPanelHandler(bb1, h1, chps);
		bottomPanelHandler.setSwipeEnabled(false);
		currentProjectPath = getIntent().getStringExtra("projectPath");
		
		if (currentProjectPath != null) {
				loadProject(currentProjectPath);
				File file = new File(currentProjectPath);
				String fileName = file.getName();
				int dotIndex = fileName.lastIndexOf('.');
				String projectName =
				(dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
			    projtitle.setText(projectName);
			    FULL_FILE_NAME = projectName.concat("_".concat(CURRENT_TIME.concat("_".concat(FILE_TAG.concat(".png")))));
		} else {
				pdv.setLogicalCanvasSize(1280, 1280);
				pdv.setCanvasBackgroundColor(Color.WHITE);
				pdv.setCurrentLayer(1);
				_startupDialog(1);
			    
			    int intCol = CANV.getInt("DEFAULTCOLOR", Color.BLACK); 
				curcolor = intCol;
				String hexSColor = String.format("#%06X", (0xFFFFFF & intCol));
				pdv.setDrawingColor(hexSColor);
			    col.setCurrentColor(intCol);
		}
		
		setupBrushGrid();
		setupFontRecyclerView();
		loadFonts();
		setTState(0);
		imagePickerLauncher = registerForActivityResult(
		new ActivityResultContracts.GetContent(),
		uri -> {
				if (uri != null) {
						importImageFromUri(uri);
				}
		});
		
		brushPickerLauncher = registerForActivityResult(
		new ActivityResultContracts.GetContent(),
		uri -> {
				if (uri != null) {
						importBrushFromUri(uri);
				}
		});
		
		fontPickerLauncher = registerForActivityResult(
		    new ActivityResultContracts.GetContent(),
		    uri -> {
			        if (uri != null) {
				            handleFontUri(uri);
				        }
			    }
		);
		
		new Thread(() -> {
				BrushRepository.installDefaultBrushes(this);
				runOnUiThread(() -> setupBrushGrid());
		}).start();
		
		textInput.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)50, 0xFFFFFFFF));
		GradientDrawable tagI = new GradientDrawable();
		int d = (int) getResources().getDisplayMetrics().density;
		tagI.setColor(0x00FFFFFF);
		tagI.setCornerRadius(d * 12);
		tagI.setStroke(d * 2, 0xFFFFC107);
		warnTag.setBackground(tagI);
		warnTag.setElevation(d * 5);
		warnTag.setText("This feature is experimental, meaning that some features may be unstable and the application may run slowly, especially if the canvas size is very large and depending on the brush configuration!");
		warnTag.setTextColor(0xFFFFC107);
		warnTag.setPadding(d * 8, d * 4, d * 8, d * 4);
		warnTag.setGravity(Gravity.CENTER);
		warnTag.setTextSize(8f);
	}
	private void setTState (int state) {
		    if (state == 0) {
			        textMode.setVisibility(View.GONE);
			        brushMode.setVisibility(View.VISIBLE);
			    } else if (state == 1) {
			        textMode.setVisibility(View.VISIBLE);
			        brushMode.setVisibility(View.GONE);
			    } else {
			        textMode.setVisibility(View.GONE);
			        brushMode.setVisibility(View.GONE);
			    }
	}
	
	private void hideClearCanvasBttn() {
			if (clear_all_bttn.getVisibility() == View.VISIBLE) {
					clear_all_bttn.setVisibility(View.GONE);
			}
	}
	
	private void updateCombinedColorPreview(int combinedColor, TextView _hex) {
			String hexCombinedColor = String.format("#%06X", (0xFFFFFF & combinedColor));
			pdv.setDrawingColor(hexCombinedColor);
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
	
	private void startSlideAnimation(boolean open) {
			float width = lb.getWidth(); lb.setAlpha(1f);
			if (open) {
					lb.setVisibility(View.VISIBLE);
					lb.setTranslationX(width); lib.setTranslationX(width);
					lb.animate().translationX(0).setDuration(300).start();
					lib.animate().translationX(0).setDuration(300).start();
			} else {
					lb.animate().translationX(width).setDuration(300).start();
					lib.animate().translationX(width).setDuration(300)
					.withEndAction(() -> {
							lb.setVisibility(View.GONE); lib.setTranslationX(0);
					}).start();
			}
	}
	
	private void activatePen() {
		pdv.setDrawingMode(DrawingState.DrawingMode.FREEHAND); pdv.setAlpha(curalpha);
		setIcState(1); setTState(0);
		smudgePowerGroup.setVisibility(View.GONE);
		filltoleranceGroup.setVisibility(View.GONE);
		swichPenGroup.setVisibility(View.VISIBLE);
		commit.setVisibility(View.GONE);
		inv.setVisibility(View.GONE);
		strokeThickness.setAlpha((float)(1));
		col.setAlpha((float)(1));
		objectAlpha.setAlpha((float)(1));
	}
	
	private void activateSmudge() {
		pdv.setDrawingMode(DrawingState.DrawingMode.SMUDGE); pdv.setNeutralMode();
		setIcState(2); setTState(2);
		hideClearCanvasBttn(); bottomPanelHandler.collapse();
		smudgePowerGroup.setVisibility(View.VISIBLE);
		filltoleranceGroup.setVisibility(View.GONE);
		swichPenGroup.setVisibility(View.GONE);
		commit.setVisibility(View.GONE);
		inv.setVisibility(View.GONE);
		strokeThickness.setAlpha((float)(1));
		col.setAlpha((float)(0.3d));
		objectAlpha.setAlpha((float)(0.3d));
	}
	
	private void activateBucket() {
		pdv.setDrawingMode(DrawingState.DrawingMode.FILL); pdv.setNeutralMode();
		setIcState(5); setTState(2);
		pdv.setAlpha(curalpha);
		hideClearCanvasBttn(); bottomPanelHandler.collapse();
		smudgePowerGroup.setVisibility(View.GONE);
		filltoleranceGroup.setVisibility(View.VISIBLE);
		swichPenGroup.setVisibility(View.GONE);
		commit.setVisibility(View.GONE);
		inv.setVisibility(View.GONE);
		strokeThickness.setAlpha((float)(0.3d));
		col.setAlpha((float)(1));
		objectAlpha.setAlpha((float)(1));
	}
	private void showColorPickerPopup(int initialColor) {
			View colpopupView = getLayoutInflater().inflate(R.layout.color_picker, null);
			final PopupWindow colpopup = new PopupWindow(colpopupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
			LinearLayout background = colpopupView.findViewById(R.id.background);
			TextView hex = colpopupView.findViewById(R.id.hexout);
			TextView dismiss = colpopupView.findViewById(R.id.dismiss);
			HueSVSquareColorPicker colorPicker = colpopupView.findViewById(R.id.colorPicker);
			RGBSlidersView rgbSliders = colpopupView.findViewById(R.id.rgbsliders);
			background.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)15, (int)1, 0xFF424242, 0xE9111111));
			colorPicker.setColor(curcolor);
			rgbSliders.setColor(curcolor);
			updateCombinedColorPreview((curcolor), hex);
		    applyHoverEffect(dismiss, highlight);
		    
			colorPicker.setOnColorChangedListener(new HueSVSquareColorPicker.OnColorChangedListener() {
					@Override
					public void onColorChanged(int color) {
							
							pdv.setDrawingColor(String.format("#%06X", (0xFFFFFF & color)));
							col.setCurrentColor(color); 
							rgbSliders.setColor(color);
							updateCombinedColorPreview(color, hex);
							curcolor = color; pdv.setAlpha(curalpha);
					}
			});
			rgbSliders.setOnRGBChangedListener(new RGBSlidersView.OnRGBChangedListener() {
					@Override
					public void onRGBChanged(int r, int g, int b) {
							int newColor = Color.rgb(r, g, b);
							
							col.setCurrentColor(newColor); 
							pdv.setDrawingColor(String.format("#%06X", (0xFFFFFF & newColor)));
							
							colorPicker.setColor(newColor);
							updateCombinedColorPreview(newColor, hex);
							curcolor = newColor; pdv.setAlpha(curalpha);
					}
			});
			dismiss.setOnClickListener(v -> colpopup.dismiss());
			
			colpopup.setAnimationStyle(android.R.style.Animation_Dialog);
			colpopup.setBackgroundDrawable(new BitmapDrawable());
			colpopup.showAtLocation(col, Gravity.BOTTOM | Gravity.LEFT, 60, 60);
	}
	
	private void importImageFromUri(Uri imageUri) {
			try {
					InputStream inputStream = getContentResolver().openInputStream(imageUri);
					Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
					if (originalBitmap != null) {
							pdv.importImage(originalBitmap); pdv.setAlpha(curalpha);
							setIcState(4);
							setTState(2);
							strokeThickness.setAlpha((float)(0.3d));
							col.setAlpha((float)(0.3d));
							objectAlpha.setAlpha((float)(1));
				            bottomPanelHandler.collapse();
					}
			} catch (Exception e) {
					e.printStackTrace();
					showToast("Failed to load image: " + e.getMessage());
			}
	}
	
	private void openFilePicker() {
			brushPickerLauncher.launch("*/*"); 
	}
	
	
	private void launchImagePicker() {
			imagePickerLauncher.launch("image/*");
	}
	
	private void openFontPicker() {
			fontPickerLauncher.launch("*/*"); 
	}
	
	
	private void showToast(String message) {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}
	
	private void setupFontRecyclerView() {
			RecyclerView rv = findViewById(R.id.fontRecyclerView);
			rv.setLayoutManager(new GridLayoutManager(this, 4));
			
			FontAdapter.OnFontClickListener fontListener = new FontAdapter.OnFontClickListener() {
					@Override
					public void onFontClick(FontItem item) {
							if (pdv != null) pdv.setTypeface(item.typeface);
					}
					
					@Override
					public void onImportClick() {
							MainActivity.this.openFontPicker(); 
					}
					
					@Override
					public void onFontLongClick(FontItem item, int position) {
							MainActivity.this.showDeleteDialog(item, position); 
					}
			};
			
			fontAdapter = new FontAdapter(fontList, fontListener);
			rv.setAdapter(fontAdapter);
	}
	
	private void loadFonts() {
			fontList.clear();
			fontList.add(new FontItem(true));
			fontList.add(new FontItem("Default", null, Typeface.DEFAULT));
			
			String json = getSharedPreferences("fonts", MODE_PRIVATE).getString("list", null);
			if (json != null) {
					try {
							Type type = new TypeToken<List<FontItem>>(){}.getType();
							List<FontItem> saved = new Gson().fromJson(json, type);
							for (FontItem item : saved) {
									File file = new File(item.filePath);
									if (file.exists()) {
											item.typeface = Typeface.createFromFile(file);
											fontList.add(item);
									}
							}
					} catch (Exception e) { e.printStackTrace(); }
			}
			if (fontAdapter != null) fontAdapter.notifyDataSetChanged();
	}
	
	private void handleFontUri(Uri uri) {
			try {
					String fileName = getFileName(uri);
					File internalDir = new File(getFilesDir(), "fonts");
					if (!internalDir.exists()) internalDir.mkdirs();
					
					File destFile = new File(internalDir, fileName);
					try (InputStream in = getContentResolver().openInputStream(uri);
					OutputStream out = new FileOutputStream(destFile)) {
							byte[] buffer = new byte[1024];
							int read;
							while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
					}
					
					Typeface tf = Typeface.createFromFile(destFile);
					fontList.add(new FontItem(fileName, destFile.getAbsolutePath(), tf));
					saveMetadata();
					fontAdapter.notifyItemInserted(fontList.size() - 1);
			} catch (Exception e) {
					showToast("Error loading font: " + e.getMessage());
			}
	}
	
	private void showDeleteDialog(FontItem item, int position) {
			AlertDialog dialog = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
			.setTitle("Delete Font")
			.setMessage("Remove " + item.fontName + " permanently?")
			.setPositiveButton("Delete", (d, w) -> {
					File file = new File(item.filePath);
					if (file.exists()) file.delete();
					
					fontList.remove(position);
					saveMetadata();
					fontAdapter.notifyItemRemoved(position);
					if (pdv != null) pdv.setTypeface(Typeface.DEFAULT);
			})
			.setNegativeButton("Cancel", null)
			.create();
			if (dialog.getWindow() != null) {
					dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
			}
			dialog.show();
	}
	
	private void saveMetadata() {
			List<FontItem> toSave = new ArrayList<>();
			for (FontItem f : fontList) if (f.filePath != null) toSave.add(f);
			String json = new Gson().toJson(toSave);
			getSharedPreferences("fonts", MODE_PRIVATE).edit().putString("list", json).apply();
	}
	
	private void setIcState(int state){
			if (state == 1){
					pen.setColorFilter(Color.parseColor("#FFC107"));
					smudge.setColorFilter(Color.parseColor("#FFFFFF"));
					text.setColorFilter(Color.parseColor("#FFFFFF"));
					image.setColorFilter(Color.parseColor("#FFFFFF"));
					bucket.setColorFilter(Color.parseColor("#FFFFFF"));
					shapes.setColorFilter(Color.parseColor("#FFFFFF"));
			} else if (state == 2) {
					pen.setColorFilter(Color.parseColor("#FFFFFF"));
					smudge.setColorFilter(Color.parseColor("#FFC107"));
					text.setColorFilter(Color.parseColor("#FFFFFF"));
					image.setColorFilter(Color.parseColor("#FFFFFF"));
					bucket.setColorFilter(Color.parseColor("#FFFFFF"));
					shapes.setColorFilter(Color.parseColor("#FFFFFF"));
			} else if (state == 3) {
					pen.setColorFilter(Color.parseColor("#FFFFFF"));
					smudge.setColorFilter(Color.parseColor("#FFFFFF"));
					text.setColorFilter(Color.parseColor("#FFC107"));
					image.setColorFilter(Color.parseColor("#FFFFFF"));
					bucket.setColorFilter(Color.parseColor("#FFFFFF"));
					shapes.setColorFilter(Color.parseColor("#FFFFFF"));
			} else if (state == 4) {
					pen.setColorFilter(Color.parseColor("#FFFFFF"));
					smudge.setColorFilter(Color.parseColor("#FFFFFF"));
					text.setColorFilter(Color.parseColor("#FFFFFF"));
					image.setColorFilter(Color.parseColor("#FFC107"));
					bucket.setColorFilter(Color.parseColor("#FFFFFF"));
					shapes.setColorFilter(Color.parseColor("#FFFFFF"));
			} else if (state == 5) {
					pen.setColorFilter(Color.parseColor("#FFFFFF"));
					smudge.setColorFilter(Color.parseColor("#FFFFFF"));
					text.setColorFilter(Color.parseColor("#FFFFFF"));
					image.setColorFilter(Color.parseColor("#FFFFFF"));
					bucket.setColorFilter(Color.parseColor("#FFC107"));
					shapes.setColorFilter(Color.parseColor("#FFFFFF"));
			} else if (state == 6) {
					pen.setColorFilter(Color.parseColor("#FFFFFF"));
					smudge.setColorFilter(Color.parseColor("#FFFFFF"));
					text.setColorFilter(Color.parseColor("#FFFFFF"));
					image.setColorFilter(Color.parseColor("#FFFFFF"));
					bucket.setColorFilter(Color.parseColor("#FFFFFF"));
					shapes.setColorFilter(Color.parseColor("#FFC107"));
			}
	}
	
	
	private void setCustomBackground(View view, float[] cornerRadiiDp) {
			GradientDrawable backgroundDrawable = new GradientDrawable();
			float density = this.getResources().getDisplayMetrics().density;
			backgroundDrawable.setColor(0xDE111111);
			float[] cornerRadiiPx = new float[8];
			for (int i = 0; i < 8; i++) {
					cornerRadiiPx[i] = cornerRadiiDp[i] * density;
			}
			backgroundDrawable.setCornerRadii(cornerRadiiPx);
			view.setBackground(backgroundDrawable);
	}
	
	private void applyBackgroundStyle(View view, @ColorInt int backgroundColor, @Nullable float[] cornerRadiiDp) {
			if (view == null) return;
			
			float density = view.getResources().getDisplayMetrics().density;
			
			GradientDrawable drawable;
			Drawable currentBackground = view.getBackground();
			if (currentBackground instanceof GradientDrawable) {
					drawable = (GradientDrawable) currentBackground.mutate();
			} else {
					drawable = new GradientDrawable();
			}
			drawable.setColor(backgroundColor);
			if (cornerRadiiDp != null && cornerRadiiDp.length == 8) {
					float[] cornerRadiiPx = new float[8];
					for (int i = 0; i < 8; i++) {
							cornerRadiiPx[i] = cornerRadiiDp[i] * density;
					}
					drawable.setCornerRadii(cornerRadiiPx);
			}
			drawable.setStroke(1, Color.WHITE);
			view.setElevation(2 * density);
			view.setBackground(drawable);
	}
	
	
	public void updateCurrentLayerPreview() {
			for (LayerItem item : layerItems) {
					item.setPreviewBitmap(pdv.getLayerBitmap(item.getLayerId()));
			}
			layerAdapter.notifyDataSetChanged();
	}
	
	public void updateLayerPreview(int layerId) {
			runOnUiThread(() -> {
					int position = -1;
					for (int i = 0; i < layerItems.size(); i++) {
							if (layerItems.get(i).getLayerId() == layerId) {
									position = i;
									break;
							}
					}
					if (position != -1) {
							LayerItem item = layerItems.get(position);
							item.setPreviewBitmap(pdv.getLayerBitmap(layerId));
							layerAdapter.notifyItemChanged(position);
					}
			});
	}
	
	public void updateAllLayerPreviews() {
			runOnUiThread(() -> {
					for (LayerItem item : layerItems) {
							item.setPreviewBitmap(pdv.getLayerBitmap(item.getLayerId()));
					}
					layerAdapter.notifyDataSetChanged();
			});
	}
	
	private void updateLayerItem(int layerId, Boolean isVisible, Boolean isLocked, PorterDuff.Mode blendMode) {
			for (int i = 0; i < layerItems.size(); i++) {
					LayerItem item = layerItems.get(i);
					if (item.getLayerId() == layerId) {
							if (isVisible != null) item.setVisible(isVisible);
							if (isLocked != null) item.setLocked(isLocked);
							if (blendMode != null) item.setBlendMode(blendMode);
							layerAdapter.notifyItemChanged(i);
							break;
					}
			}
			updateLayerPreview(layerId);
	}
	
	public static class LayerItem {
			private int layerId;
			private Bitmap previewBitmap;
			private boolean isVisible;
			private boolean isLocked;
			private PorterDuff.Mode blendMode;
			private boolean isSelected;
			
			public LayerItem(int layerId, boolean isVisible, boolean isLocked, PorterDuff.Mode blendMode, boolean isSelected) {
					this.layerId = layerId;
					this.isVisible = isVisible;
					this.isLocked = isLocked;
					this.blendMode = blendMode;
					this.isSelected = isSelected;
			}
			
			public int getLayerId() { return layerId; }
			public Bitmap getPreviewBitmap() { return previewBitmap; }
			public void setPreviewBitmap(Bitmap previewBitmap) { this.previewBitmap = previewBitmap; }
			public boolean isVisible() { return isVisible; }
			public void setVisible(boolean visible) { isVisible = visible; }
			public boolean isLocked() { return isLocked; }
			public void setLocked(boolean locked) { isLocked = locked; }
			public PorterDuff.Mode getBlendMode() { return blendMode; }
			public void setBlendMode(PorterDuff.Mode blendMode) { this.blendMode = blendMode; }
			public boolean isSelected() { return isSelected; }
			public void setSelected(boolean selected) { isSelected = selected; }
	}
	
	private void initSettings() {
		    boolean useAA = CANV.getBoolean("ANTIALIAS", true);
		    pdv.setAntiAliasEnabled(useAA);
		
		    String renderMode = CANV.getString("LAYERTYPERENDER", "HARDWARE");
		    pdv.setRenderingMode(renderMode.equals("HARDWARE"));
		
		    try {
			        String maxProxy = CANV.getString("MAXPROXY", "2048");
			        pdv.setMaxProxyResolution(Integer.parseInt(maxProxy));
			    } catch (NumberFormatException e) {
			        pdv.setMaxProxyResolution(2048);
			    }
	}
	
	private static String generateRandomTag() {
			String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
			StringBuilder tag = new StringBuilder();
			Random random = new Random();
			for (int i = 0; i < 6; i++) {
					tag.append(chars.charAt(random.nextInt(chars.length())));
			}
			return tag.toString();
	}
	
	public void saveBitmapToPictures(Bitmap bitmap, String filename) {
		    if (bitmap == null || bitmap.isRecycled()) return;
		
		    ContentResolver resolver = getContentResolver();
		    ContentValues contentValues = new ContentValues();
		    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
		    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
		    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PCD");
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			        contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
			    }
		
		    Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
		
		    try {
			        if (imageUri == null) throw new IOException("Failed to create MediaStore record.");
			
			        try (OutputStream fos = resolver.openOutputStream(imageUri)) {
				            if (fos == null) throw new IOException("Failed to open output stream.");
				            
				            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
				            fos.flush();
				        }
			
			        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				            contentValues.clear();
				            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
				            resolver.update(imageUri, contentValues, null, null);
				        }
			
			        runOnUiThread(() -> Toast.makeText(this, "Image saved to Pictures!", Toast.LENGTH_SHORT).show());
			
			    } catch (IOException e) {
			        if (imageUri != null) resolver.delete(imageUri, null, null);
			        runOnUiThread(() -> Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
			    } finally {
			        if (bitmap != null && !bitmap.isRecycled()) {
				            bitmap.recycle();
				        }
			    }
	}
	
	private void importBrushFromUri(Uri uri) {
		    try {
			        String fileName = getFileName(uri);
			        if (fileName == null) fileName = "brush_" + System.currentTimeMillis() + ".brush";
			
			        File brushDir = new File(getExternalFilesDir(null), BRUSH_FOLDER);
			        if (!brushDir.exists() && !brushDir.mkdirs()) {
				            throw new Exception("Could not create directory: " + brushDir.getAbsolutePath());
				        }
			
			        File destFile = new File(brushDir, fileName);
			
			        if (destFile.exists()) {
				            Toast.makeText(this, "Brush already exists. Loading...", Toast.LENGTH_SHORT).show();
				            loadBrushToViewAndList(destFile);
				            return;
				        }
			
			        try (InputStream is = getContentResolver().openInputStream(uri);
			             FileOutputStream fos = new FileOutputStream(destFile)) {
				            byte[] buffer = new byte[8192];
				            int read;
				            while ((read = is.read(buffer)) != -1) {
					                fos.write(buffer, 0, read);
					            }
				            fos.flush();
				        }
			
			        loadBrushToViewAndList(destFile);
			
			    } catch (Exception e) {
			        Log.e("Import", "Failed", e);
			        Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
			    }
	}
	
	private void loadBrushToViewAndList(File brushFile) {
		    BrushArchiveLoader.loadFromArchive(brushFile, new BrushArchiveLoader.BrushLoadCallback() {
			        @Override
			        public void onSuccess(BrushSettings settings) {
				            runOnUiThread(() -> {
					                pdv.useBrush(settings);
					                
					                Bitmap thumb = BrushRepository.generateSineThumbnail(settings); 
					                BrushItem newItem = new BrushItem(settings.name, brushFile.getAbsolutePath(), thumb, false, false);
					                
					                if (adapter != null) {
						                    items.add(newItem);
						                    adapter.notifyItemInserted(items.size() - 1);
						                    adapter.notifyDataSetChanged();
						                }
					                
					                setupBrushGrid();
					                //Toast.makeText(MainActivity.this, "Loaded: " + settings.name, Toast.LENGTH_SHORT).show();
					            });
				        }
			
			        @Override
			        public void onError(Exception e) {
				            runOnUiThread(() -> 
				                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
				            );
				        }
			    });
	}
	
	
	
	private String getFileName(Uri uri) {
		    String result = null;
		    if (uri.getScheme().equals("content")) {
			        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
				            if (cursor != null && cursor.moveToFirst()) {
					                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
					                if (nameIndex != -1) result = cursor.getString(nameIndex);
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
	
	
	public void saveProject() {
			if (currentProjectPath == null) {
					File folder = new File(getFilesDir(), "projects");
					if (!folder.exists()) folder.mkdirs();
					currentProjectPath = new File(folder, FULL_PROJECT_NAME + ".pcdproj").getAbsolutePath();
			}
			
			final String finalPath = currentProjectPath;
			
			new Thread(() -> {
					try {
							ProjectSerializer.saveToPcdProj(
							MainActivity.this, 
							finalPath, 
							pdv.getLayerManager(), 
							pdv.getLogicalWidth(), 
							pdv.getLogicalHeight(),
				            pdv.getColor()
							);
					} catch (Exception e) {
							e.printStackTrace();
					}
			}).start();
	}
	
	private void loadProject(String path) {
			this.currentProjectPath = path; 
			new Thread(() -> {
					try {
							ProjectManifest manifest = ProjectSerializer.loadFromPcdProj(path, pdv.getLayerManager());
							
							runOnUiThread(() -> {
									pdv.setLogicalCanvasSize(manifest.width, manifest.height);
					                pdv.setDrawingColor(String.format("#%06X", (0xFFFFFF & manifest.color)));
					                curcolor = manifest.color;
									pdv.notifyProjectLoaded();
									
									layerItems.clear();
									ArrayList<Integer> order = pdv.getLayerManager().getLayerDrawingOrder();
									
									int currentId = pdv.getCurrentLayerId();
									
									for (int id : order) {
											LayerItem newItem = new LayerItem(
											id, 
											pdv.isLayerVisible(id), 
											pdv.isLayerLocked(id), 
											pdv.getLayerBlendMode(id),
											false 
											);
											
											newItem.setPreviewBitmap(pdv.getLayerBitmap(id));
											layerItems.add(newItem);
									}
									
									layerAdapter.setSelectedLayer(currentId); 
									
									layerAdapter.notifyDataSetChanged();
									commit.setVisibility(View.GONE);
									pdv.fitCanvasToScreen();
							});
					} catch (Exception e) {
							e.printStackTrace();
							runOnUiThread(() -> Toast.makeText(this, "The project could not be opened.", Toast.LENGTH_SHORT).show());
					}
			}).start();
	}
	
	public void setupBrushGrid() {
			RecyclerView grid = findViewById(R.id.recycler_brushes);
			if (grid.getLayoutManager() == null) {
					grid.setLayoutManager(new GridLayoutManager(this, 4));
			}
			
			new Thread(() -> {
					List<BrushItem> items = BrushRepository.loadBrushesFromStorage(this);
					runOnUiThread(() -> {
							if (adapter == null) {
									adapter = new BrushAdapter(items, new BrushAdapter.OnBrushClickListener() {
											@Override
											public void onBrushSelected(String filePath) {
													if (filePath == null) return;
													File file = new File(filePath);
													BrushArchiveLoader.loadFromArchive(
													file,
													new BrushArchiveLoader.BrushLoadCallback() {
															@Override
															public void onSuccess(BrushSettings settings) {
																	runOnUiThread(() -> {
																			pdv.useBrush(settings);
																	});
															}
															
															@Override
															public void onError(Exception e) {
																	Bitmap tip = BitmapFactory.decodeFile(filePath);
																	if (tip == null) return;
																	
																	BrushSettings fallback = new BrushSettings();
																	fallback.texture = tip;
																	fallback.name = file.getName();
																	
																	runOnUiThread(() -> {
																			pdv.useBrush(fallback);
																	});
															}
													}
													);
											}
											
											@Override
											public void onImportClicked() {
													View cboptionspopupView = getLayoutInflater().inflate(R.layout.cust_brush_more_options, null);
													final PopupWindow cboptionspopup = new PopupWindow(cboptionspopupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
													LinearLayout background = cboptionspopupView.findViewById(R.id.background);
													LinearLayout o1 = cboptionspopupView.findViewById(R.id.o1);
													LinearLayout o2 = cboptionspopupView.findViewById(R.id.o2);
													background.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)15, (int)1, 0xFF424242, 0xE9111111));
													applyHoverEffect(o1, highlight);
													applyHoverEffect(o2, highlight);
													o1.setOnClickListener(new OnClickListener() {
															@Override
															public void onClick(View _view) {
																	openFilePicker();
																	cboptionspopup.dismiss();
															}
													});
													o2.setOnClickListener(new OnClickListener() {
															@Override
															public void onClick(View _view) {
																	Intent intent = new Intent(MainActivity.this, BrushActivity.class);
																	ActivityOptions options = ActivityOptions.makeCustomAnimation(
																	MainActivity.this, R.anim.slide_rotate_in, R.anim.fade_out);
																	startActivity(intent, options.toBundle());
																	overridePendingTransition(R.anim.slide_rotate_in, R.anim.fade_out);
																	cboptionspopup.dismiss();
															}
													});
													
													cboptionspopup.setAnimationStyle(android.R.style.Animation_Dialog);
													cboptionspopup.showAsDropDown(pen, 0, 0);
													cboptionspopup.setBackgroundDrawable(new BitmapDrawable());
											}
											
											@Override
											public void onDefaultPenBrush() { pdv.useDefaultPen(); }
											
											@Override
											public void onBrushLongClick(BrushItem item, View anchorView) {
													View popupView = getLayoutInflater().inflate(R.layout.custom_brush_more_options, null);
													final PopupWindow popup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
													LinearLayout background = popupView.findViewById(R.id.background);
													LinearLayout btnEdit = popupView.findViewById(R.id.o1);
													LinearLayout btnDelete = popupView.findViewById(R.id.o2);
													background.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b, int c, int d) { this.setCornerRadius(a); this.setStroke(b, c); this.setColor(d); return this; } }.getIns((int)15, (int)1, 0xFF424242, 0xE9111111));
													applyHoverEffect(btnEdit, highlight);
													applyHoverEffect(btnDelete, highlight);
													
													btnEdit.setOnClickListener(v -> {
															popup.dismiss();
															
															Intent intent = new Intent(MainActivity.this, BrushActivity.class);
															intent.putExtra("brush_path", item.filePath); 
															
															ActivityOptions options = ActivityOptions.makeCustomAnimation(MainActivity.this, R.anim.slide_rotate_in, R.anim.fade_out);
															startActivity(intent, options.toBundle());
													});
													
													btnDelete.setOnClickListener(v -> {
															popup.dismiss();
															
															AlertDialog dialog = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
															.setTitle("Delete Brush")
															.setMessage("Remove " + item.name + " brush permanently?")
															.setPositiveButton("Delete", (d, which) -> {
																	File file = new File(item.filePath);
																	if (file.exists()) {
																			boolean deleted = file.delete();
																			if (deleted) {
																					setupBrushGrid();
																					Toast.makeText(MainActivity.this, "Brush deleted", Toast.LENGTH_SHORT).show();
																			}
																	}
															})
															.setNegativeButton("Cancel", null)
															.create();
															if (dialog.getWindow() != null) {
																	dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
															}
															dialog.show();
													});
													popup.setAnimationStyle(android.R.style.Animation_Dialog);
													popup.setBackgroundDrawable(new BitmapDrawable());
													popup.showAsDropDown(anchorView, 0, -anchorView.getHeight());
											}
									});
									grid.setAdapter(this.adapter);
							} else {
									this.adapter.updateData(items);
							}
							
				            if (adapter != null) {
					                int pos = adapter.getSelectedPosition();
					                if (pos != -1) { grid.scrollToPosition(pos); }
					            }
					});
			}).start();
	}
	public class LayerAdapter extends Adapter<LayerAdapter.LayerViewHolder> {
			private List<LayerItem> layers;
			private int selectedLayerId = -1;
			
			public LayerAdapter(List<LayerItem> layers) {
					this.layers = layers;
			}
			
			public List<LayerItem> getLayers() {
					return layers;
			}
			
			public void setSelectedLayer(int layerId) {
					this.selectedLayerId = layerId;
					
					for (int i = 0; i < layers.size(); i++) {
							LayerItem item = layers.get(i);
							boolean shouldBeSelected = (item.getLayerId() == layerId);
							
							if (item.isSelected() != shouldBeSelected) {
									item.setSelected(shouldBeSelected);
									notifyItemChanged(i);
							}
					}
			}
			
			public void onItemMove(int fromPosition, int toPosition) {
					if (fromPosition < toPosition) {
							for (int i = fromPosition; i < toPosition; i++) {
									Collections.swap(layers, i, i + 1);
							}
					} else {
							for (int i = fromPosition; i > toPosition; i--) {
									Collections.swap(layers, i, i - 1);
							}
					}
					notifyItemMoved(fromPosition, toPosition);
			}
			
			@Override
			public LayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
					View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layer_view, parent, false);
					return new LayerViewHolder(view);
			}
			
			@Override
			public void onBindViewHolder(LayerViewHolder holder, int position) {
					LayerItem layer = layers.get(position);
					
					holder.layerPreviewImageView.setImageBitmap(layer.getPreviewBitmap());
					holder.blendModeTextView.setText(layer.getBlendMode().name());
					
					holder.visibilityToggleButton.setImageResource(
					layer.isVisible() ? R.drawable.visibility : R.drawable.visibility_off
					);
					holder.lockToggleButton.setImageResource(
					layer.isLocked() ? R.drawable.lock : R.drawable.unlock
					);
					
					applySelectionStyling(holder, layer.isSelected());
					holder.itemView.setOnClickListener(v -> {
							if (layer.getLayerId() == selectedLayerId) {
									showMoreOptionsPopup(holder.itemView, layer, holder.getAdapterPosition(), holder);
							} else {
									pdv.setCurrentLayer(layer.getLayerId());
									setSelectedLayer(layer.getLayerId());
							}
					});
					
					holder.visibilityToggleButton.setOnClickListener(v -> {
							boolean newState = !layer.isVisible();
							layer.setVisible(newState);
							pdv.setLayerVisibility(layer.getLayerId(), newState);
							holder.visibilityToggleButton.setImageResource(
							newState ? R.drawable.visibility : R.drawable.visibility_off
							);
					});
					
					holder.lockToggleButton.setOnClickListener(v -> {
							boolean newState = !layer.isLocked();
							layer.setLocked(newState);
							pdv.setLayerLocked(layer.getLayerId(), newState);
							
							holder.lockToggleButton.setImageResource(
							newState ? R.drawable.lock : R.drawable.unlock
							);
					});
			}
			
			@Override
			public int getItemCount() {
					return layers.size();
			}
			
			private void applySelectionStyling(LayerViewHolder holder, boolean isSelected) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							GradientDrawable background = new GradientDrawable();
							background.setColor(0xaceeeeee);
							background.setCornerRadius(15f);
							
							if (isSelected) {
									background.setStroke(6, Color.RED);
							} else {
									background.setStroke(0, Color.TRANSPARENT);
							}
							
							holder.imageContainer.setBackground(background);
							holder.imageContainer.setClipToOutline(true);
							holder.focus.setVisibility(View.GONE);
					} else {
							holder.imageContainer.setBackgroundColor(Color.WHITE);
							holder.focus.setVisibility(isSelected ? View.VISIBLE : View.GONE);
					}
			}
			
			private void showMoreOptionsPopup(View anchorView, LayerItem layer, int position, LayerViewHolder holder) {
					Context context = anchorView.getContext();
					View layoutpopupView = LayoutInflater.from(context).inflate(R.layout.layer_more_options, null);
					int strokeWidthPx = Math.round(1 * context.getResources().getDisplayMetrics().density);
					
					PopupWindow layoutpopup = new PopupWindow(
					layoutpopupView,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					true
					);
					
					LinearLayout background = layoutpopupView.findViewById(R.id.background);
					LinearLayout o1 = layoutpopupView.findViewById(R.id.o1);
					LinearLayout o2 = layoutpopupView.findViewById(R.id.o2);
					ImageView arrow = layoutpopupView.findViewById(R.id.arrow);
					
					GradientDrawable popupBg = new GradientDrawable();
					popupBg.setCornerRadius(15f);
					popupBg.setStroke(strokeWidthPx, 0xFF424242);
					popupBg.setColor(0xFF111111);
					background.setBackground(popupBg);
					arrow.setImageResource(R.drawable.arrow_right);
					
					LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) arrow.getLayoutParams();
					float density = context.getResources().getDisplayMetrics().density;
					int negativeMarginPx = Math.round(-1 * density); 
					int marginTopPx = Math.round(10 * density);
					params.setMargins(negativeMarginPx, marginTopPx, 0, 0);
					arrow.setLayoutParams(params);
					
					applyHoverEffect(o1, highlight);
					applyHoverEffect(o2, highlight);
					
					o1.setOnClickListener(w -> {
							layoutpopup.dismiss();
							showBlendModesPopup(anchorView, layer, position, holder);
					});
					
					o2.setOnClickListener(w -> {
							layoutpopup.dismiss();
							deleteLayer(context, layer, position);
					});
					
					layoutpopup.setAnimationStyle(android.R.style.Animation_Dialog);
					layoutpopup.setBackgroundDrawable(new BitmapDrawable());
					layoutpopupView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
					int popupWidth = layoutpopupView.getMeasuredWidth();
					int popupHeight = layoutpopupView.getMeasuredHeight();
					int[] listLocation = new int[2];
					int[] itemLocation = new int[2];
					View recyclerView = (View) anchorView.getParent();
					
					if (recyclerView != null) {
							recyclerView.getLocationInWindow(listLocation);
							anchorView.getLocationInWindow(itemLocation);
							int xPos = listLocation[0] - popupWidth - 10; 
							int yPos = itemLocation[1] + (anchorView.getHeight() / 2) - (popupHeight / 2);
							layoutpopup.showAtLocation(recyclerView, Gravity.NO_GRAVITY, xPos, yPos);
					} else {
							layoutpopup.showAsDropDown(anchorView, -popupWidth, -anchorView.getHeight());
					}
					
			}
			
			private void showBlendModesPopup(View anchorView, LayerItem layer, int position, LayerViewHolder holder) {
					Context context = anchorView.getContext();
					final PorterDuff.Mode[] blendModes = {
							PorterDuff.Mode.SRC_OVER,
							PorterDuff.Mode.MULTIPLY,
							PorterDuff.Mode.SCREEN,
							PorterDuff.Mode.OVERLAY,
							PorterDuff.Mode.DARKEN,
							PorterDuff.Mode.LIGHTEN,
							PorterDuff.Mode.XOR,
							PorterDuff.Mode.CLEAR
					};
					
					View blendModePopupView = LayoutInflater.from(context).inflate(R.layout.layer_blend_modes, null);
					
					final PopupWindow blendModePopup = new PopupWindow(
					blendModePopupView,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					true
					);
					
					LinearLayout blendModeBackground = blendModePopupView.findViewById(R.id.background);
					if (blendModeBackground != null) {
							blendModeBackground.setBackground(new GradientDrawable() {
									public GradientDrawable getIns(int a, int b, int c, int d) {
											this.setCornerRadius(a);
											this.setStroke(b, c);
											this.setColor(d);
											return this;
									}
							}.getIns(15, 1, 0xFF424242, 0xE9111111));
					}
					
					LinearLayout optionsContainer = blendModePopupView.findViewById(R.id.options_container);
					for (PorterDuff.Mode mode : blendModes) {
							TextView modeTextView = new TextView(context);
							LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT
							);
							modeTextView.setLayoutParams(params);
							modeTextView.setText(mode.name());
							modeTextView.setTextSize(16);
							modeTextView.setPadding(30, 20, 30, 20);
							modeTextView.setTextColor(0xFFFFFFFF);
							applyHoverEffect(modeTextView, highlight);
							
							if (mode == layer.getBlendMode()) {
									modeTextView.setBackground(new GradientDrawable() {
											public GradientDrawable getIns(int a, int b) {
													this.setCornerRadius(a);
													this.setColor(b);
													return this;
											}
									}.getIns(15, 0x30FFFFFF));
							}
							
							modeTextView.setOnClickListener(modeView -> {
									pdv.setLayerBlendMode(layer.getLayerId(), mode);
									layer.setBlendMode(mode);
									LayerAdapter.this.notifyItemChanged(position); 
									blendModePopup.dismiss();
									
									if (context instanceof MainActivity) {
											((MainActivity) context).updateAllLayerPreviews();
									}
							});
							optionsContainer.addView(modeTextView);
					}
					
					blendModePopup.setAnimationStyle(android.R.style.Animation_Dialog);
					blendModePopup.showAsDropDown(anchorView, 0, 0); 
					blendModePopup.setBackgroundDrawable(new BitmapDrawable());
			}
			
			private void deleteLayer(Context context, LayerItem layer, int positionToDelete) {
					if (positionToDelete == 0 && layers.size() == 1) {
							Toast.makeText(context, "Cannot delete the base layer.", Toast.LENGTH_SHORT).show();
							return;
					}
					
					AlertDialog dialog = new AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
					.setTitle("Confirmation")
					.setMessage("Do you want to permanently delete layer " + positionToDelete + "?\nWarning, this action cannot be undone!")
					.setPositiveButton("Delete", (d, which) -> {
							pdv.removeCurrentLayer();
							if (positionToDelete != RecyclerView.NO_POSITION) {
									layers.remove(positionToDelete);
									notifyItemRemoved(positionToDelete);
									
									if (layers.isEmpty()) {
											setSelectedLayer(-1);
									} else {
											int newSelectedPosition = Math.min(positionToDelete, layers.size() - 1);
											if (newSelectedPosition >= 0) {
													int newLayerId = layers.get(newSelectedPosition).getLayerId();
													pdv.setCurrentLayer(newLayerId);
													setSelectedLayer(newLayerId);
											} else {
													setSelectedLayer(-1);
											}
									}
									if (context instanceof MainActivity) {
											((MainActivity) context).updateAllLayerPreviews();
									}
							}
					})
					.setNegativeButton("Cancel", null)
					.create();
					
					if (dialog.getWindow() != null) {
							dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
					}
					dialog.show();
			}
			
			public class LayerViewHolder extends ViewHolder {
					View focus;
					FrameLayout imageContainer;
					ImageView layerPreviewImageView;
					ImageView lockToggleButton;
					ImageView visibilityToggleButton;
					TextView blendModeTextView;
					
					public LayerViewHolder(View itemView) {
							super(itemView);
							focus = itemView.findViewById(R.id.focus);
							imageContainer = itemView.findViewById(R.id.image_container);
							layerPreviewImageView = itemView.findViewById(R.id.layer_preview_image_view);
							lockToggleButton = itemView.findViewById(R.id.lock_img);
							visibilityToggleButton = itemView.findViewById(R.id.visibility_img);
							blendModeTextView = itemView.findViewById(R.id.layer_blend_mode_text_view);
					}
			}
			
			
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (!isUncreatedProject) {
			saveProject();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (!isUncreatedProject) {
			saveProject();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (adapter != null) {
			    new Thread(() -> {
				        List<BrushItem> items = BrushRepository.loadBrushesFromStorage(this);
				        runOnUiThread(() -> {
					            adapter.updateData(items);
					            int pos = adapter.getSelectedPosition();
					            if (pos != -1) {
						                recycler_brushes.scrollToPosition(pos); 
						            }
					        });
				    }).start();
		} else {
			    setupBrushGrid();
		}
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (wtag.getString("hide", "").equals("true")) {
			warnTag.setVisibility(View.GONE);
		}
	}
	public void _startupDialog(final double _type) {
		final AlertDialog startupDialog = new AlertDialog.Builder(MainActivity.this).create();
		View startupDialogView = getLayoutInflater().inflate(R.layout.new_proj, null);
		startupDialog.setView(startupDialogView);
		LinearLayout background = startupDialogView.findViewById(R.id.background);
		EditText etWidth = startupDialogView.findViewById(R.id.etWidth);
		EditText etHeight = startupDialogView.findViewById(R.id.etHeight);
		EditText fn = startupDialogView.findViewById(R.id.filenm);
		RatioSimulatingView ratioView = startupDialogView.findViewById(R.id.ratioView);
		TextView create = startupDialogView.findViewById(R.id.create_bttn);
		TextView cancel = startupDialogView.findViewById(R.id.cancel_bttn);
		CheckBox cb = startupDialogView.findViewById(R.id.cb);
		RecyclerView rvTemplates = startupDialogView.findViewById(R.id.rvTemplates);
		int screenWidth = getResources().getDisplayMetrics().widthPixels;
		int screenHeight = getResources().getDisplayMetrics().heightPixels;
		if (_type == 1) {
			startupDialog.setCanceledOnTouchOutside(false);
			startupDialog.setCancelable(false);
			isUncreatedProject = true;
		}
		else {
			startupDialog.setCanceledOnTouchOutside(true);
			startupDialog.setCancelable(true);
			fn.setEnabled(false);
			fn.setAlpha((float)(0.5d));
			create.setText("Proceed");
		}
		applyHoverEffect(create, highlight);
		applyHoverEffect(cancel, highlight);
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
		final boolean[] keepAspectRatio = {false};
		etWidth.setInputType(InputType.TYPE_CLASS_NUMBER);
		etHeight.setInputType(InputType.TYPE_CLASS_NUMBER);
		if (_type == 1) {
			fn.setText(DRAWING_NAME.concat("_".concat(CURRENT_TIME.concat("_".concat(FILE_TAG)))));
			etWidth.setText(String.valueOf((long)(realwd)));
			etHeight.setText(String.valueOf((long)(realhg)));
			ratioView.setDimensions(realwd, realhg);
		}
		else {
			fn.setText(projtitle.getText().toString());
			etWidth.setText(String.valueOf((long)(pdv.getLogicalWidth())));
			etHeight.setText(String.valueOf((long)(pdv.getLogicalHeight())));
			ratioView.setDimensions(pdv.getLogicalWidth(), pdv.getLogicalHeight());
		}
		TextWatcher dimensionWatcher = new TextWatcher() {
			private EditText currentEditText;
			private boolean isUpdating = false;
			 @Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				currentEditText = etWidth.hasFocus() ? etWidth : etHeight;
			}
			
			 @Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (isUpdating || s.toString().isEmpty()) return;
					try {
							int value = Integer.parseInt(s.toString());
							if (value < 1) value = 1;
							if (value > 4096) value = 4096;
							isUpdating = true;
							if (value != Integer.parseInt(s.toString())) {
									currentEditText.setText(String.valueOf(value));
									currentEditText.setSelection(String.valueOf(value).length());
							}
							if (keepAspectRatio[0]) {
									EditText otherEditText = (currentEditText == etWidth) ? etHeight : etWidth;
									otherEditText.setText(String.valueOf(value));
									otherEditText.setSelection(String.valueOf(value).length());
							}
					} catch (NumberFormatException e) {
					        
					} finally {
							isUpdating = false;
					}
			}
			
			 @Override
			public void afterTextChanged(Editable s) {
					try {
							int width = 0;
							if (!etWidth.getText().toString().isEmpty()) {
									width = Integer.parseInt(etWidth.getText().toString());
							}
							int height = 0;
							if (!etHeight.getText().toString().isEmpty()) {
									height = Integer.parseInt(etHeight.getText().toString());
							}
							ratioView.setDimensions(width, height);
					} catch (NumberFormatException e) {
							ratioView.setDimensions(8, 8);
					}
			}
		};
		
		etWidth.addTextChangedListener(dimensionWatcher);
		etHeight.addTextChangedListener(dimensionWatcher);
		TextWatcher nameWatcher = new TextWatcher() {
			private EditText currentEditText;
			 @Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			 @Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
					if (s.toString().trim().isEmpty()) {
							DRAWING_NAME = "PIX_DRWING";
					} else {
							DRAWING_NAME = s.toString();
					}
			}
			
			 @Override
			public void afterTextChanged(Editable s) {
			}
		};
		
		fn.addTextChangedListener(nameWatcher);
		cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				keepAspectRatio[0] = isChecked;
			}
		});
		List<TemplateModel> templates = new ArrayList<>();
		templates.add(new TemplateModel("Screen", R.drawable.temp_5, screenWidth, screenHeight));
		templates.add(new TemplateModel("1:1", R.drawable.temp_3, 1280, 1280));
		templates.add(new TemplateModel("4:5", R.drawable.temp_9, 1080, 1350));
		templates.add(new TemplateModel("16:9", R.drawable.temp_4, 1920, 1080));
		templates.add(new TemplateModel("3:1", R.drawable.temp_8, 1500, 500));
		templates.add(new TemplateModel("A4 (96)", R.drawable.temp_1, 794, 1123));
		templates.add(new TemplateModel("A4 (96)", R.drawable.temp_2, 1123, 794));
		templates.add(new TemplateModel("A4 (72)", R.drawable.temp_1, 595, 842));
		templates.add(new TemplateModel("A4 (72)", R.drawable.temp_2, 842, 595));
		templates.add(new TemplateModel("A3 (96)", R.drawable.temp_7, 1123, 1587));
		templates.add(new TemplateModel("A3 (96)", R.drawable.temp_6, 1587, 1123));
		
		rvTemplates.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
		rvTemplates.setAdapter(new TemplateAdapter(templates, new TemplateAdapter.OnTemplateClickListener() {
			    @Override
			    public void onTemplateClick(TemplateModel t) {
				        etWidth.setText(String.valueOf(t.getWidth()));
				        etHeight.setText(String.valueOf(t.getHeight()));
				    }
		}));
		
		cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
				if (_type == 1) {
					finish(); 
					overridePendingTransition(R.anim.fade_in, R.anim.slide_rotate_out);
				}
				else {
					startupDialog.dismiss();
				}
			}
		});
		create.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View _view) {
				wd = Integer.parseInt(etWidth.getText().toString());
				hg = Integer.parseInt(etHeight.getText().toString());
				realwd = Integer.parseInt(etWidth.getText().toString());
				realhg = Integer.parseInt(etHeight.getText().toString());
				if (_type == 1) {
					DRAWING_NAME = fn.getText().toString();
					FULL_FILE_NAME = DRAWING_NAME.concat("_".concat(CURRENT_TIME.concat("_".concat(FILE_TAG.concat(".png")))));
					FULL_PROJECT_NAME = DRAWING_NAME;
					CURRENT_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
					            .format(new Date());
					projtitle.setText(FULL_PROJECT_NAME);
					isUncreatedProject = false;
				}
				pdv.setLogicalCanvasSize(wd, hg);
				pdv.fitCanvasToScreen();
				startupDialog.dismiss();
			}
		});
		startupDialog.show();
		
		if (startupDialog.getWindow() != null) {
			    startupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}
	}
	
}
