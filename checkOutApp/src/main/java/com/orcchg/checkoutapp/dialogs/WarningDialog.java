package com.orcchg.checkoutapp.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.utils.RequestCode;

public class WarningDialog extends DialogFragment {
  private static final String TAG = "CheckOut_WarningDialog";
  private static final String bundleKey_message = "bundleKey_message";
  private static final String bundleKey_summary = "bundleKey_summary";
  public static final int RESULT_CODE = RequestCode.WARNING_DIALOG;
  
  private String mMessage;
  private String mSummary;
  
  public static WarningDialog newInstance(final String message) {
    return newInstance(message, "");
  }
  
  public static WarningDialog newInstance(
      final String message,
      final String summary) {
    WarningDialog dialog = new WarningDialog();
    Bundle args = new Bundle();
    args.putString(bundleKey_message, message);
    args.putString(bundleKey_summary, summary);
    dialog.setArguments(args);
    return dialog;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NO_FRAME, R.style.WarningDialogStyle);
    Bundle args = getArguments();
    mMessage = args.getString(bundleKey_message);
    mSummary = args.getString(bundleKey_summary);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    Log.d(TAG, "Create view of WarningDialog");
    View rootView = inflater.inflate(R.layout.warning_dialog_layout, container, false);
    
    TextView mMessageTextView = (TextView) rootView.findViewById(R.id.warning_dialog_message_textview);
    TextView mSummaryTextView = (TextView) rootView.findViewById(R.id.warning_dialog_summary_textview);
    mMessageTextView.setText(mMessage);
    mSummaryTextView.setText(mSummary);
    
    Button closeButton = (Button) rootView.findViewById(R.id.warning_dialog_close_button);
    closeButton.setOnClickListener(new OnClickListener() {
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
