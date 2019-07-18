package com.triple.trackme.CurrentTrack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.triple.trackme.R;

public class StopTrackDialog {

    public Dialog create(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.stopTrackDialogMessage)
                .setPositiveButton(R.string.stopTrackDialogPositiveButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { }
                })
                .setNegativeButton(R.string.stopTrackDialogNegativeButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CurrentTrackView.endTrackAndSave();
                    }
                });

        return builder.create();
    }
}