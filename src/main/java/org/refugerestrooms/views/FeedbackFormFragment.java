package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 7/15/2015.
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.refugerestrooms.R;

public class FeedbackFormFragment extends android.support.v4.app.Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.feedback_form, container, false);

        Button buttonSend = (Button) rootView.findViewById(R.id.buttonSend);
       // final EditText textTo = (EditText) rootView.findViewById(R.id.editTextTo);
        final EditText textSubject = (EditText) rootView.findViewById(R.id.editTextSubject);
        final EditText textMessage = (EditText) rootView.findViewById(R.id.editTextMessage);

        buttonSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                String to = getString(R.string.email);
                String subject = textSubject.getText().toString();
                String message = textMessage.getText().toString();

                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ to});
                email.putExtra(Intent.EXTRA_SUBJECT, subject);
                email.putExtra(Intent.EXTRA_TEXT, message);

                //need this to prompts email client only
                email.setType("message/rfc822");

                startActivity(Intent.createChooser(email, "Choose an Email client :"));

            }
        });
        return rootView;
    }
}
