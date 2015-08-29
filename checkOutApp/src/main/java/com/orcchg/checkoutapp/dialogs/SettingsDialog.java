package com.orcchg.checkoutapp.dialogs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.orcchg.checkoutapp.CheckOutPage;
import com.orcchg.checkoutapp.R;

public class SettingsDialog extends DialogFragment {
  private static final String TAG = "CheckOut_SettingsDialog";
  private static final String DIALOG_TAG = "SettingsDialog_dialog";
  
  public static SettingsDialog newInstance() {
    SettingsDialog dialog = new SettingsDialog();
    return dialog;
  }
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NO_FRAME, R.style.SettingsDialogStyle);
  }
  
  @SuppressLint("RtlHardcoded")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    Log.d(TAG, "Create view of SettingsDialog");
    View rootView = inflater.inflate(R.layout.menu_dialog_layout, container, false);
    
    Window window = getDialog().getWindow();
    window.setGravity(Gravity.TOP | Gravity.LEFT | Gravity.START);
    WindowManager.LayoutParams params = window.getAttributes();
    Bundle args = getArguments();
    params.x = args.getInt(CheckOutPage.out_bundleKey_xPositionWindow);
    params.y = args.getInt(CheckOutPage.out_bundleKey_yPositionWindow);
    params.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
    window.setAttributes(params);
    
    TextView mAboutTextView = (TextView) rootView.findViewById(R.id.menu_dialog_about_textview);
    mAboutTextView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showAboutDialog();
        disposeDialog();
      }
    });
    TextView mThemesTextView = (TextView) rootView.findViewById(R.id.menu_dialog_themes_textview);
    mThemesTextView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // TODO: show theme picker dialog
        disposeDialog();
      }
    });
    
    return rootView;
  }
  
  /* Private methods */
  // --------------------------------------------------------------------------
  private void disposeDialog() {
    dismiss();
  }
  
  private void showAboutDialog() {
    FragmentManager fm = getActivity().getSupportFragmentManager();
    DialogFragment dialog = AboutDialog.newInstance();
    dialog.show(fm, DIALOG_TAG);
  }
}
