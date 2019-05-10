package com.github.ghmxr.timeswitch.ui.bottomdialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.github.ghmxr.timeswitch.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BottomDialogForBatteryPercentageWithEnabledSelection extends BottomDialogWith2WheelViews {

    public static int SELECTION_VALUE_MORE_THAN=0;
    public static int SELECTION_VALUE_LESS_THAN=1;

    public BottomDialogForBatteryPercentageWithEnabledSelection(@NonNull Context context, boolean isEnabled, int value_first, int value_second) {
        super(context, context.getResources().getString(R.string.activity_taskgui_exceptions_battery_percentage),"%", isEnabled);
        wheelview_first.setSeletion(value_first);
        wheelview_second.setSeletion(value_second-1);
    }


    @NonNull
    @Override
    public List<String> getFirstWheelViewSelectionItems(Context context) {
        return Arrays.asList(context.getResources().getString(R.string.dialog_battery_compare_more_than),context.getResources().getString(R.string.dialog_battery_compare_less_than));
    }

    @NonNull
    @Override
    public List<String> getSecondWheelViewSelectionItems() {
        ArrayList<String> selections_percentages=new ArrayList<>();
        for(int i=1;i<=99;i++){
            selections_percentages.add(String.valueOf(i));
        }
        return selections_percentages;
    }

    @Override
    @NonNull
    public Integer getFirstSelectionValue() {
        return wheelview_first.getSeletedIndex();
    }

    @Override
    @NonNull
    public Integer getSecondSelectionValue() {
        return Integer.parseInt(wheelview_second.getSeletedItem());
    }

    public static class BottomDialogForBatteryPercentageWithoutEnabledSelection extends BottomDialogForBatteryPercentageWithEnabledSelection{
        public BottomDialogForBatteryPercentageWithoutEnabledSelection(@NonNull Context context, int value_first, int value_second) {
            super(context, true, value_first, value_second);
            checkbox_enable.setVisibility(View.GONE);
        }
    }
}
