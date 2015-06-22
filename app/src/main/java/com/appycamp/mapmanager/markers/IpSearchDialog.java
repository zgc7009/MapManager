package com.appycamp.mapmanager.markers;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.appycamp.mapmanager.R;

/**
 * Created by Zach on 6/18/2015.
 */
public class IpSearchDialog {

    public interface OnIpSearchListener{
        void onSearchRequested(String startIp, String endIp);
    }

    private EditText[] mStartIpFields = new EditText[4];
    private EditText[] mEndIpFields = new EditText[4];
    private OnIpSearchListener mListener;
    private Resources mResources;
    private AlertDialog mAlert;
    private ProgressBar mProgressBar;

    public IpSearchDialog(RelativeLayout searchDialogView, OnIpSearchListener listener){
        mListener = listener;
        mResources = searchDialogView.getResources();

        TableRow startsearchDialogView = (TableRow) searchDialogView.findViewById(R.id.ip_search_start);
        setupIpSearchFields(startsearchDialogView, true);

        TableRow endsearchDialogView = (TableRow) searchDialogView.findViewById(R.id.ip_search_end);
        setupIpSearchFields(endsearchDialogView, false);
        mEndIpFields[3].setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchDialogView.findViewById(R.id.button_ip_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mProgressBar.getVisibility() == View.VISIBLE)
                    return;

                mProgressBar.setVisibility(View.VISIBLE);
                new ValidateIpTask(new OnIpSearchListener() {
                    @Override
                    public void onSearchRequested(String startIp, String endIp) {
                        if(mListener != null && startIp != null && endIp != null) {
                            mListener.onSearchRequested(startIp, endIp);
                            mAlert.dismiss();
                        }

                        mProgressBar.setVisibility(View.GONE);
                    }
                }, new EditText[][]{mStartIpFields, mEndIpFields});
            }
        });

        mProgressBar = (ProgressBar) searchDialogView.findViewById(R.id.progress_network);

        AlertDialog.Builder builder = new AlertDialog.Builder(searchDialogView.getContext())
                .setTitle(mResources.getString(R.string.ip_dialog_title))
                .setView(searchDialogView)
                .setNegativeButton(mResources.getString(R.string.ip_dialog_button_negative), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlert.dismiss();
                    }
                });

        mAlert = builder.create();
        mAlert.show();

    }

    public EditText[] getStartIpFields(){
        return mStartIpFields;
    }

    public EditText[] getEndIpFields(){
        return mEndIpFields;
    }

    public void setupIpSearchFields(TableRow searchDialogRowView, final boolean startIp){

        ((TextView) searchDialogRowView.findViewById(R.id.text_ip_label)).setText(startIp ? mResources.getString(R.string.ip_dialog_label_start) :
                mResources.getString(R.string.ip_dialog_label_end));

        for(int i = 1; i <= 4; i++){
            final int arrayPos = i - 1;
            int id = searchDialogRowView.getResources().getIdentifier("field_ip_" + i, "id", searchDialogRowView.getContext().getPackageName());
            final EditText ipField = (EditText) searchDialogRowView.findViewById(id);

            if(startIp)
                mStartIpFields[arrayPos] = ipField;
            else
                mEndIpFields[arrayPos] = ipField;

            ipField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 3) {
                        ipField.removeTextChangedListener(this);
                        //if(startIp) mStartIpFields[arrayPos].removeTextChangedListener(this);

                        if(Integer.parseInt(s.toString()) > 255){
                            /*Toast.makeText(mStartIpFields[0].getContext(),
                                    mStartIpFields[0].getContext().getResources().getString(R.string.toast_invalid_ip_range)
                                    , Toast.LENGTH_LONG).show(); */

                            ipField.setText(s.toString().substring(0, 2));
                            ipField.setSelection(2);
                            incrementFocus();
                        }
                        else if (arrayPos == 3 && startIp)
                            mEndIpFields[0].requestFocus();
                        else
                            incrementFocus();

                        ipField.addTextChangedListener(this);
                    }
                }

                private void incrementFocus(){
                    if (arrayPos == 3) {
                        if(startIp)
                            mEndIpFields[0].requestFocus();
                        return;
                    } else {
                        if (startIp)
                            mStartIpFields[arrayPos + 1].requestFocus();
                        else
                            mEndIpFields[arrayPos + 1].requestFocus();
                    }
                }
            });
        }
    }

    private static class ValidateIpTask extends AsyncTask<EditText[], Void, String[]> {

        private OnIpSearchListener mListener;
        private EditText[][] mFields;

        public ValidateIpTask(OnIpSearchListener listener, EditText[][] fields){
            mListener = listener;
            mFields = fields;
            execute(fields);
        }

        @Override
        protected void onPostExecute(String[] ips) {
            boolean valid = true;
            if(ips[0] == null) {
                mFields[0][3].setError(mFields[0][3].getResources().getString(R.string.ip_dialog_invalid));
                valid = false;
            }
            if(ips[1] == null){
                mFields[1][3].setError(mFields[1][3].getResources().getString(R.string.ip_dialog_invalid));
                valid = false;
            }
            if(mListener != null)
                mListener.onSearchRequested(ips[0], ips[1]);
        }

        @Override
        protected String[] doInBackground(EditText[]... fields) {
            String startIp = getIp(fields[0]);
            String endIp = getIp(fields[1]);

            return new String[]{startIp, endIp};
        }

        private String getIp(EditText[] fields){
            StringBuilder ip = new StringBuilder();
            for(int i = 0; i < 4; i++){
                ip.append(fields[i].getEditableText().toString());
                if(i != 3)
                    ip.append(".");
            }
            Log.d(IpSearchDialog.class.getSimpleName(), "Validating our ip field " + ip.toString());

            if(IpManager.validateIp(ip.toString()))
                return ip.toString();
            else
                return null;
        }
    }
}
