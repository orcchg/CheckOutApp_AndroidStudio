package com.orcchg.checkoutapp.dialogs;

import android.content.Intent;
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

public class AlertDialog extends DialogFragment {
  private static final String TAG = "MamboGj_AlertDialog";
  private static final String bundleKey_message = "bundleKey_message";
  private static final String bundleKey_summary = "bundleKey_summary";
  public static final String out_bundleKey_result = "out_bundleKey_result";
  public static final String out_bundleKey_extra = "out_bundleKey_extra";
  public static final int RESULT_CODE = RequestCode.ALERT_DIALOG;
  
  private String mMessage;
  private String mSummary;
  private Bundle mExtraBundle;
  
  public static AlertDialog newInstance(final String message) {
    return newInstance(message, "");
  }
  
  public static AlertDialog newInstance(
      final String message,
      final String summary) {
    AlertDialog dialog = new AlertDialog();
    Bundle args = new Bundle();
    args.putString(bundleKey_message, message);
    args.putString(bundleKey_summary, summary);
    dialog.setArguments(args);
    return dialog;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AlertDialogStyle);
    Bundle args = getArguments();
    mMessage = args.getString(bundleKey_message);
    mSummary = args.getString(bundleKey_summary);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    Log.d(TAG, "Create view of AlertDialog");
    View rootView = inflater.inflate(R.layout.alert_dialog_layout, container, false);
    
    TextView mMessageTextView = (TextView) rootView.findViewById(R.id.alert_dialog_message_textview);
    TextView mSummaryTextView = (TextView) rootView.findViewById(R.id.alert_dialog_summary_textview);
    mMessageTextView.setText(mMessage);
    mSummaryTextView.setText(mSummary);
    
    Button cancelButton = (Button) rootView.findViewById(R.id.alert_dialog_cancel_button);
    cancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        sendResult(false);
        disposeDialog();
      }
    });
    Button okButton = (Button) rootView.findViewById(R.id.alert_dialog_ok_button);
    okButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        sendResult(true);
        disposeDialog();
      }
    });
    
    return rootView;
  }
  
  public void setExtraBundle(final Bundle bundle) {
    mExtraBundle = bundle;
  }
  
  /* Private methods */
  // --------------------------------------------------------------------------
  private void sendResult(boolean result) {
    Log.d(TAG, "Sending result...");
    Intent intent = new Intent();
    intent.putExtra(out_bundleKey_result, result);
    if (mExtraBundle != null) { intent.putExtra(out_bundleKey_extra, mExtraBundle); }
    getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_CODE, intent);
  }
  
  private void disposeDialog() {
    dismiss();
  }
}
