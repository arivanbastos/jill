package br.com.arivanbastos.signalcaptor.utils;

import android.graphics.Color;

public class ColorUtil {
    private static float HUE_INCREMENT = 70f;
    private static float SATURATION_INCREMENT = 0.15f;
    private static float BRIGHT_INCREMENT = 0.1f;

    private static int MAX_COLORS = 60;

    private static float hue = 0;
    private static float saturation = 0.4f;
    private static float bright = 0.5f;
    private static int aux = 0;

    public static int nextUniqueColor()
    {
        int color =Color.HSVToColor(new float[]{hue, saturation, bright});

        hue += HUE_INCREMENT;
        if (hue >= 360)
        {
            hue = hue%360;

            if (aux%2==0) {
                saturation += SATURATION_INCREMENT;
                if (saturation >= 1) saturation= 0.4f;
            }
            else
            {
                bright+= BRIGHT_INCREMENT;
                if (bright >= 1) bright= 0.5f;
            }

            aux++;
        }

        return color;
    }
}

