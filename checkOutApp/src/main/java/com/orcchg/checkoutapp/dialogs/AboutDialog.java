package com.orcchg.checkoutapp.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.orcchg.checkoutapp.R;

public class AboutDialog extends DialogFragment {
  private static final String TAG = "CheckOut_AboutDialog";
  
  public static AboutDialog newInstance() {
    AboutDialog dialog = new AboutDialog();
    return dialog;
  }
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AboutDialogStyle);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    Log.d(TAG, "Create view of AboutDialog");
    View rootView = inflater.inflate(R.layout.about_dialog_layout, container, false);
//    TextView mContentTextView = (TextView) rootView.findViewById(R.id.about_dialog_content_textview);
    Button mCloseButton = (Button) rootView.findViewById(R.id.about_dialog_CLOSE_button);
    mCloseButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
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
}
