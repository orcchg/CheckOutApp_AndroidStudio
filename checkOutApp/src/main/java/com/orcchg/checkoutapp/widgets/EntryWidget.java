package com.orcchg.checkoutapp.widgets;

import org.apache.commons.io.FilenameUtils;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.orcchg.checkoutapp.EntryPage;
import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.items.Entry;
import com.orcchg.checkoutapp.utils.Utility;
import com.orcchg.checkoutapp.utils.Utility.ResourceURLType;
import com.squareup.picasso.Picasso;

public class EntryWidget extends Fragment {
  private static final String TAG = "CheckOut_EntryWidget";
  private static final String bundleKey_name = "bundleKey_name";
  private static final String bundleKey_resourceURL = "bundleKey_resourceURL";
  private static final String bundleKey_isArchived = "bundleKey_isArchived";
  private static final String bundleKey_lastDate = "bundleKey_lastDate";
  public static final String out_bundleKey_isModified = "out_bundleKey_isModified";
  public static final String out_bundleKey_name = bundleKey_name;
  public static final String out_bundleKey_resourceURL = bundleKey_resourceURL;
  public static final String out_bundleKey_isArchived = bundleKey_isArchived;
  
  private TextView mTitleTextView;
  private TextView mLastDateTextView;
  private CheckBox mArchivedCheckBox;
  
  private String mName;
  private String mResourceURL;
  private String mLastDate;
  private boolean mIsArchived;
  private boolean mIsModified;
  
  public static EntryWidget newInstance(final Entry entry) {
    EntryWidget widget = new EntryWidget();
    Bundle args = new Bundle();
    args.putString(bundleKey_name, entry.getName());
    args.putString(bundleKey_resourceURL, entry.getResourceURL());
    args.putString(bundleKey_lastDate, Utility.parseMillis(entry.getLastCheckDate()));
    args.putBoolean(bundleKey_isArchived, entry.isArchived());
    widget.setArguments(args);
    return widget;
  }
  
  /* Lifecycle methods */
  // --------------------------------------------------------------------------
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    mName = args.getString(bundleKey_name);
    mResourceURL = args.getString(bundleKey_resourceURL);
    mLastDate = args.getString(bundleKey_lastDate);
    mIsArchived = args.getBoolean(bundleKey_isArchived);
    mIsModified = false;
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    Log.d(TAG, "Create view of EntryWidget");
    View rootView = inflater.inflate(R.layout.entry_widget_layout, container, false);
    
    mTitleTextView = (TextView) rootView.findViewById(R.id.entry_widget_title_textview);
    mLastDateTextView = (TextView) rootView.findViewById(R.id.entry_widget_last_date_textview);
    mArchivedCheckBox = (CheckBox) rootView.findViewById(R.id.entry_widget_archive_checkbox);
    mArchivedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {      
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mIsArchived = isChecked;
        mIsModified = true;
      }
    });
    
    FrameLayout mContainerLayout = (FrameLayout) rootView.findViewById(R.id.entry_widget_resource_container);
    FrameLayout.LayoutParams resourceViewParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    View resourceView = null;
    @ResourceURLType int resourceURLtype = Utility.resolveResourceURLType(mResourceURL);
    switch (resourceURLtype) {
      case Utility.RESOURCE_URL_IMAGE:
        Log.d(TAG, "Image resource");
        ImageView imageView = new ImageView(getActivity().getBaseContext());
        Picasso.with(getActivity().getBaseContext()).load("file://" + mResourceURL).fit().into(imageView);
        resourceView = imageView;
        break;
      case Utility.RESOURCE_URL_FILE:
        Log.d(TAG, "File resource");
      case Utility.RESOURCE_URL_UNKNOWN:
      default:
        TextView textView = new TextView(getActivity().getBaseContext());
        textView.setBackgroundColor(Color.WHITE);
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_library_books_black_48dp);
        textView.setCompoundDrawablePadding(60);  // in dp
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(26);  // in sp
        textView.setText(FilenameUtils.getName(mResourceURL));
        resourceViewParams.height = LayoutParams.WRAP_CONTENT;
        resourceView = textView;
        break;
      case Utility.RESOURCE_URL_MAP:
        Log.d(TAG, "Map resource");
        // TODO: MapView to be used
        break;
      case Utility.RESOURCE_URL_VIDEO:
        Log.d(TAG, "Video resource");
        VideoView videoView = new VideoView(getActivity().getBaseContext());
        videoView.setVideoPath(mResourceURL);
        videoView.setMediaController(new MediaController(getActivity()));
        videoView.requestFocus(0);
        videoView.seekTo(1);  // in ms, show first frame
        resourceView = videoView;
        break;
      case Utility.RESOURCE_URL_WEB:
        Log.d(TAG, "Web resource");
        WebView webView = new WebView(getActivity().getBaseContext());
        webView.getSettings().setBuiltInZoomControls(true);
        webView.loadUrl(mResourceURL);
        resourceView = webView;
        break;
    }
    resourceViewParams.gravity = Gravity.CENTER;
    mContainerLayout.addView(resourceView, resourceViewParams);
    
    Button modifyButton = (Button) rootView.findViewById(R.id.entry_widget_MODIFY_button);
    modifyButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        EntryPage activity = (EntryPage) getActivity();
        activity.showEntryUpdateDialog();
      }
    });
    Button deleteButton = (Button) rootView.findViewById(R.id.entry_widget_DELETE_button);
    deleteButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        EntryPage activity = (EntryPage) getActivity();
        activity.showAlertDialog();
      }
    });
    
    refreshViews();
    return rootView;
  }
  
  /* Access methods */
  // --------------------------------------------------------------------------
  public Bundle requestResult() {
    Bundle bundle = new Bundle();
    bundle.putString(out_bundleKey_name, getName());
    bundle.putString(out_bundleKey_resourceURL, getResourceURL());
    bundle.putBoolean(out_bundleKey_isArchived, isArchived());
    bundle.putBoolean(out_bundleKey_isModified, mIsModified);
    mIsModified = false;
    return bundle;
  }
  
  public void modifyEntry(final String name, final String resourceURL) {
    mName = name;
    mResourceURL = resourceURL;
    mIsModified = true;
    refreshViews();
  }
  
  /* Private methods */
  // --------------------------------------------------------------------------
  private void refreshViews() {
    mTitleTextView.setText(mName);
    mLastDateTextView.setText(mLastDate);
    mArchivedCheckBox.setChecked(mIsArchived);
  }
  
  private String getName() { return mName; }
  private String getResourceURL() { return mResourceURL; }
  private boolean isArchived() { return mIsArchived; }
  
  private void openResource() {
    Log.i(TAG, "Opening resource with URL: " + mResourceURL);
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mResourceURL));
    if (intent != null && intent.resolveActivity(getActivity().getPackageManager()) != null) {
      startActivity(intent);
    } else {
      Log.e(TAG, "[No Activity] Unable to open resource with URL: " + mResourceURL);
    }
  }
}
