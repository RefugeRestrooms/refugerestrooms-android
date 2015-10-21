package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 9/26/15.
 */
import org.refugerestrooms.models.Bathroom;

import org.refugerestrooms.R;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BathroomSpecsViewUpdater {

    public static void update(View view, Bathroom bathroom, Context context) {
        TextView scoreTv = (TextView) view.findViewById(R.id.score);
        int score = bathroom.getScore();
        scoreTv.setText(getScoreDescription(context, score));
        scoreTv.setTextColor(Color.WHITE);
        scoreTv.setBackgroundColor(getScoreColour(score));

        if (bathroom.isAccessible()) {
            ImageView iv = (ImageView) view.findViewById(R.id.accessible);
            iv.setVisibility(View.VISIBLE);
        }
        if (bathroom.isUnisex()) {
            ImageView iv = (ImageView) view.findViewById(R.id.unisex);
            iv.setVisibility(View.VISIBLE);
        }
    }

    private static String getScoreDescription(Context context, int score) {
        return (score < 0 ? context.getString(R.string.unknown) : "" + score * 100 + "% POSITIVE");
    }

    private static int getScoreColour(int score) {
        if (score < 0) {
            return Color.GRAY;
        }
        if (score > .90) {
            return Color.parseColor("#A1D193");
        }
        if (score > .50) {
            return Color.parseColor("#FFC125");
        }
        return Color.RED;
    }
}