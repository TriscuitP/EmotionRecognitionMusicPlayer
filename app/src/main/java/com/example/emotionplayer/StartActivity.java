package com.example.emotionplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity
{
    TextView newText;

    private Button noControl;
    private Button calm;
    private Button happy;

    private static boolean control = true;
    private static int choice = 0;

    // User is brought here to choose which mood they would like to be in. Then they are brought to
    // MainActivity, which is the music player
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        newText = findViewById(R.id.newText);
        newText.setText("What mood you feeling today?");

        noControl = findViewById(R.id.noControl);
        noControl.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                control = false;
                startActivity(new Intent(StartActivity.this, MainActivity.class));
            }
        });

        calm = findViewById(R.id.calm);
        calm.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                choice = 1;
                control = true;
                startActivity(new Intent(StartActivity.this, MainActivity.class));
            }
        });

        happy = findViewById(R.id.happy);
        happy.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                choice = 2;
                control = true;
                startActivity(new Intent(StartActivity.this, MainActivity.class));
            }
        });


    }

    public static boolean getControl()
    {
        return control;
    }

    public static int getChoice()
    {
        return choice;
    }


}
