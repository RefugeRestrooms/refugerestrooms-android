package org.refugerestrooms.alt;

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
		return context.getString(R.string.score) + " " + (score < 0 ? context.getString(R.string.unknown) : "" + score);
	}
	
	private static int getScoreColour(int score) {
		if (score < 0) {
			return Color.GRAY;
		}
		float[] hsv = new float[3];
		hsv[0] = ((float) score) / 100 * 120;
		hsv[1] = 1;
		hsv[2] = 1;
		return Color.HSVToColor(hsv);
	}
}
