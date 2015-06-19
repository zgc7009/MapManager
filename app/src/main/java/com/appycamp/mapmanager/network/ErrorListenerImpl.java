package com.appycamp.mapmanager.network;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.appycamp.mapmanager.R;


/**
 * When I want less explicit error messages I can implement this generic implementation of the
 * Volley ErrorListener that will report an error corresponding to the network result code as
 * well as handling the visibility of the ProgressBar associated with the network call.
 */
public abstract class ErrorListenerImpl implements Response.ErrorListener {

    private final Context context;

    private interface NetworkErrorStatus {
        int BAD_REQUEST = 400;
        int UNAUTHORIZED = 401;
        int PAYMENT_REQUIRED = 402;
        int FORBIDDEN = 403;
        int NOT_FOUND = 404;
        int UNPROCESSABLE_ENTITY = 422;
        int SERVER_ERROR = 500;
    }

    public ErrorListenerImpl(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            showToast(context.getString(R.string.network_error_timeout));
        } else if (error instanceof AuthFailureError) {
            showToast(context.getString(R.string.network_error_auth));
        } else if (error instanceof ServerError) {
            showToast(context.getString(R.string.network_error_server));
        } else if (error instanceof NetworkError) {
            showToast(context.getString(R.string.network_error_network));
        } else if (error instanceof ParseError) {
            showToast(context.getString(R.string.network_error_parse));
        }

        finalizeError(error.networkResponse);
    }

    private void showToast(String text){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    protected abstract void finalizeError(NetworkResponse response);

    public static Response.ErrorListener getDefaultErrorListener(Context mContext, final ProgressBar progressBar) {

        return new ErrorListenerImpl(mContext.getApplicationContext()) {
            @Override
            protected void finalizeError(NetworkResponse response) {
                progressBar.setVisibility(View.GONE);
            }
        };
    };

}
