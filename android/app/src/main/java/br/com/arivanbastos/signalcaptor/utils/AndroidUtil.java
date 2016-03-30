package br.com.arivanbastos.signalcaptor.utils;

import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class AndroidUtil
{
    public static TextView getTextViewByTag(Activity activity, String tag)
    {
        ArrayList<View> views = getViewsByTag(activity, tag);
        return (TextView)views.get(0);
    }

    public static ArrayList<View> getViewsByTag(Activity activity, String tag)
    {
        // http://stackoverflow.com/questions/24612364/how-to-get-root-viewgroup-from-a-layout-that-was-set-using-setcontentview
        return getViewsByTag((ViewGroup)activity.findViewById(android.R.id.content), tag);
    }

    public static ArrayList<View> getViewsByTag(ViewGroup root, String tag)
    {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    public static void setDpHeight(Context context, TextView v, int height)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (height * scale + 0.5f);
        v.setHeight(pixels);
    }

    public static void setDpHeight(Context context, ViewGroup v, int height)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (height * scale + 0.5f);
        v.setMinimumHeight(pixels);
    }
}
