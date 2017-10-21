package com.dvdlister;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

/**
 * Created by Jean-Paul on 10/20/2017.
 */

public class InputTextDialog extends AlertDialog{
    private EditText input ;
    private String location = "";

    protected InputTextDialog(final Context context) {
        super(context);
        input = new EditText(context);
        this.setView( input );

        this.setButton(Dialog.BUTTON_POSITIVE,"Set",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    location = input.getText().toString();
                    if (location.compareTo("") == 0) {
                        location = "Default";
                    }
                    InputTextDialog.super.notifyAll();
                }
        });
    }

    String getInputText(){
        return input.toString();
    }


}
