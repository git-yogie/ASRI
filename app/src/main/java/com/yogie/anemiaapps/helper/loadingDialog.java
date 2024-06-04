package com.yogie.anemiaapps.helper;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.yogie.anemiaapps.R;

public class loadingDialog {

    private  Context context;
    private  Dialog dialog;
    private  SpinKitView spinKitView;
    private  TextView textViewMessage;

    public loadingDialog(Context context) {
        this.context = context;
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.loading_popup);
        dialog.setCancelable(false);

        spinKitView = dialog.findViewById(R.id.spin_kit);
        textViewMessage = dialog.findViewById(R.id.textViewMessage);
    }

    public void setMessage(String message) {
        textViewMessage.setText(message);
    }

    public void show() {
        dialog.show();
        Sprite doubleBounce = new DoubleBounce();
        spinKitView.setIndeterminateDrawable(doubleBounce);
    }

    public void dismiss() {
        dialog.dismiss();
    }


}
