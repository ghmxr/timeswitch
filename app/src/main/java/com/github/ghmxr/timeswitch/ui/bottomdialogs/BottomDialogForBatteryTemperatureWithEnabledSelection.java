package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.github.ghmxr.timeswitch.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BottomDialogForBatteryTemperatureWithEnabledSelection extends BottomDialogWith2WheelViews {
    public BottomDialogForBatteryTemperatureWithEnabledSelection(@NonNull Context context, boolean isEnabled, int selection_first, int selection_second) {
        super(context, context.getResources().getString(R.string.activity_taskgui_exceptions_battery_temperature),"¡æ", isEnabled);
        wheelview_first.setSeletion(selection_first);
        wheelview_second.setSeletion(selection_second);
    }

    @NonNull
    @Override
    public List<String> getFirstWheelViewSelectionItems(Context context) {
        return Arrays.asList(context.getResources().getString(R.string.dialog_battery_compare_higher_than),context.getResources().getString(R.string.dialog_battery_compare_lower_than));
    }

    @NonNull
    @Override
    public List<String> getSecondWheelViewSelectionItems() {
        ArrayList<String> list=new ArrayList<>();
        for(int i=0;i<=65;i++){
            list.add(String.valueOf(i));
        }
        return list;
    }

    @Override
    @NonNull
    public Integer getFirstSelectionValue() {
        return wheelview_first.getSeletedIndex();
    }

    @Override
    @NonNull
    public Integer getSecondSelectionValue() {
        return wheelview_second.getSeletedIndex();
    }

    public static class BottomDialogForBatteryTemperatureWithoutEnabledSelection extends BottomDialogForBatteryTemperatureWithEnabledSelection{
        public BottomDialogForBatteryTemperatureWithoutEnabledSelection(@NonNull Context context, int selection_first, int selection_second) {
            super(context, true, selection_first, selection_second);
            checkbox_enable.setVisibility(View.GONE);
        }
    }
}
