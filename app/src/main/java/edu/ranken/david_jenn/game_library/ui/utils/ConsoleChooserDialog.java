package edu.ranken.david_jenn.game_library.ui.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.ranken.david_jenn.game_library.R;

public class ConsoleChooserDialog {
    private final Map<String, Boolean> selectedConsoles;
    private final Map<String, Boolean> supportedConsoles;

    private final AlertDialog dialog;

    public ConsoleChooserDialog(
       @NonNull Context context,
       @NonNull CharSequence title,
       @Nullable Map<String, Boolean> supportedConsoles,
       @Nullable Map<String, Boolean> selectedConsoles,
       @NonNull OnChooseListener onChoose
    ) {

        ColorStateList selectedColor = context.getColorStateList(R.color.orange_300);
        ColorStateList unSelectedColor = context.getColorStateList(R.color.unselected_color_list);


        // get inflater
        LayoutInflater inflater = LayoutInflater.from(context);

        // inflate layout
        View contentView = inflater.inflate(R.layout.console_chooser_dialog, null, false);

        //find views
        ImageButton xboxButton = contentView.findViewById(R.id.xboxButton);
        ImageButton playstationButton = contentView.findViewById(R.id.playstationButton);
        ImageButton computerButton = contentView.findViewById(R.id.computerButton);
        ImageButton switchButton = contentView.findViewById(R.id.switchButton);



        // supported
        if (supportedConsoles == null) {
            this.supportedConsoles = null;
        } else {
            this.supportedConsoles = supportedConsoles;
            boolean xbox = Objects.equals(supportedConsoles.get("xbox-one"), Boolean.TRUE);
            xboxButton.setEnabled(xbox);

            // .. other consoles

            boolean playstation = Objects.equals(supportedConsoles.get("playstation-4"), Boolean.TRUE);
            playstationButton.setEnabled(playstation);

            boolean computer = Objects.equals(supportedConsoles.get("computer"), Boolean.TRUE);
            computerButton.setEnabled(computer);

            boolean nintendoSwitch = Objects.equals(supportedConsoles.get("nintendo-switch"), Boolean.TRUE);
            switchButton.setEnabled(nintendoSwitch);

        }

        // selected
        if (selectedConsoles == null) {
            this.selectedConsoles = new HashMap<>();
        } else {
            this.selectedConsoles = selectedConsoles;

            boolean xbox = Objects.equals(selectedConsoles.get("xbox-one"), Boolean.TRUE);
            if(xbox) {
                xboxButton.setImageResource(R.drawable.ic_xbox_one_lg);
                xboxButton.setBackgroundTintList(selectedColor);
            }

            boolean playstation = Objects.equals(selectedConsoles.get("playstation-4"), Boolean.TRUE);
            if(playstation) {
                playstationButton.setImageResource(R.drawable.ic_playstation_filled);
                playstationButton.setBackgroundTintList(selectedColor);
            }

            boolean computer = Objects.equals(selectedConsoles.get("computer"), Boolean.TRUE);
            if(computer) {
                computerButton.setImageResource(R.drawable.ic_computer_lg);
                computerButton.setBackgroundTintList(selectedColor);
            }

            boolean nintendoSwitch = Objects.equals(selectedConsoles.get("nintendo-switch"), Boolean.TRUE);
            if(nintendoSwitch) {
                switchButton.setImageResource(R.drawable.ic_switch_lg);
                switchButton.setBackgroundTintList(selectedColor);

            }



            // show that a console is selected
            // xboxButton.setBackgroundTintList();
            // xboxButton.setImageResourse();
            // ... other consoles

        }

        // register listeners
        xboxButton.setOnClickListener((view) -> {
            boolean checked = Objects.equals(this.selectedConsoles.get("xbox-one"), Boolean.TRUE);
            this.selectedConsoles.put("xbox-one", !checked);
            if(!checked) {
                xboxButton.setImageResource(R.drawable.ic_xbox_one_lg);
                xboxButton.setBackgroundTintList(selectedColor);

            } else {
                xboxButton.setImageResource(R.drawable.ic_xbox_outline);
                xboxButton.setBackgroundTintList(unSelectedColor);
            }

            // ... other consoles
        });

        playstationButton.setOnClickListener((view) -> {
            boolean checked = Objects.equals(this.selectedConsoles.get("playstation-4"), Boolean.TRUE);
            this.selectedConsoles.put("playstation-4", !checked);

            if(!checked) {
                playstationButton.setImageResource(R.drawable.ic_playstation_filled);
                playstationButton.setBackgroundTintList(selectedColor);
            } else {
                playstationButton.setImageResource(R.drawable.ic_playstation_outline);
                playstationButton.setBackgroundTintList(unSelectedColor);
            }
        });

        computerButton.setOnClickListener((view) -> {
            boolean checked = Objects.equals(this.selectedConsoles.get("computer"), Boolean.TRUE);
            this.selectedConsoles.put("computer", !checked);

            if(!checked) {
                computerButton.setImageResource(R.drawable.ic_computer_lg);
                computerButton.setBackgroundTintList(selectedColor);
            } else {
                computerButton.setImageResource(R.drawable.ic_computer_outline);
                computerButton.setBackgroundTintList(unSelectedColor);
            }
        });

        switchButton.setOnClickListener((view) -> {
            boolean checked = Objects.equals(this.selectedConsoles.get("nintendo-switch"), Boolean.TRUE);
            this.selectedConsoles.put("nintendo-switch", !checked);

            if(!checked) {
                switchButton.setImageResource(R.drawable.ic_switch_lg);
                switchButton.setBackgroundTintList(selectedColor);
            } else {
                switchButton.setImageResource(R.drawable.ic_nintendo_switch_outline);
                switchButton.setBackgroundTintList(unSelectedColor);
            }
        });

        // ... other consoles

        // build dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setView(contentView);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            onChoose.onChoose(this.selectedConsoles);
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            // Do nothing
        });

        dialog = builder.create();
    }


    public void show() { dialog.show(); }
    public void cancel() { dialog.cancel(); }

    public interface OnChooseListener {
        void onChoose(Map<String, Boolean> selectedConsoles);
    }
}
