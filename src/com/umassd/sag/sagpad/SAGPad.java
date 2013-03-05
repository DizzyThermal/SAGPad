package com.umassd.sag.sagpad;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SAGPad extends Activity implements OnClickListener
{
	LinearLayout ll;
	private LinearLayout messageView = null;
	Dialog dialog;
	Resources r;
	final String TAG = "SAGPad";
	private final String messagesFile = "messages.txt";
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button addButton = (Button)findViewById(R.id.addNote);
        addButton.setOnClickListener(this);
        
        ll = (LinearLayout)findViewById(R.id.scrollViewLayout);
    }
    
    @Override
    protected void onStart()
    {
        super.onResume();
        r = getResources();
        
        populateMessages(messagesFile);
    }
    
	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.addNote:
				EditText note = (EditText)findViewById(R.id.noteField);
				TextView newNote = new TextView(this);
				newNote.setText(printDate() + "\n" + note.getText().toString().trim());
				newNote.setTextSize((float)16);
				newNote.setPadding(5, 10, 5, 10);
				
				LinearLayout message = new LinearLayout(this);
				message.setOrientation(LinearLayout.VERTICAL);
				registerForContextMenu(message);
				View separator = new View(this);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1, 1f);
				separator.setLayoutParams(lp);
				separator.setBackgroundColor(Color.DKGRAY);
				
				message.addView(newNote);
				message.addView(separator);
				message.setId(ll.getChildCount()+1);
				ll.addView(message);

				note.setText("");
				saveMessages(messagesFile);
				break;
		}
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_clear:
                LinearLayout ll = (LinearLayout)findViewById(R.id.scrollViewLayout);
                ll.removeAllViews();
                saveMessages(messagesFile);
                
                View separator = new View(this);
        		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1, 1f);
        		separator.setLayoutParams(lp);
        		separator.setBackgroundColor(Color.DKGRAY);
        		
        		ll.addView(separator);
        		saveMessages(messagesFile);
                return true;
        }
        
        return false;
    }
    
    @Override  
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {  
    	super.onCreateContextMenu(menu, v, menuInfo);  
 		menu.setHeaderTitle("Message Options");
 		menu.add(0, v.getId(), 0, R.string.context_edit);
     	menu.add(0, v.getId(), 0, R.string.context_remove);
     	menu.add(0, v.getId(), 0, R.string.context_copy);
     	messageView = (LinearLayout)v;
    }
    
    @Override  
    public boolean onContextItemSelected(MenuItem item)
    {
    	if(item.getTitle().equals(r.getString(R.string.context_edit)))
    	{
    		dialog = new Dialog(this);
			
			dialog.setContentView(R.layout.menu_edit);
			dialog.setTitle(r.getString(R.string.prompt_edit_title));
			
			Button pButton = (Button)dialog.findViewById(R.id.prompt_edit_positive);
			pButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					EditText eMessage = (EditText)dialog.findViewById(R.id.eNoteField);
					TextView message = (TextView)messageView.getChildAt(0);
					message.setText(printDate() + "\n" + eMessage.getText().toString().trim());
					saveMessages(messagesFile);
					dialog.cancel();
				}
			});
			
			Button nButton = (Button)dialog.findViewById(R.id.prompt_edit_negative);
			nButton.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					dialog.cancel();
				}
			});
			
			dialog.show();
			EditText eMessage = (EditText)dialog.findViewById(R.id.eNoteField);
			TextView message = (TextView)messageView.getChildAt(0);
			eMessage.setText(message.getText().toString().split("]")[1].trim());
    	}
    	else if(item.getTitle().equals(r.getString(R.string.context_remove)))
    	{
    		ll.removeView(findViewById(messageView.getId()));
    		saveMessages(messagesFile);
    	}
    	else if(item.getTitle().equals(r.getString(R.string.context_copy)))
    	{
    		ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE); 
    		TextView message = (TextView)messageView.getChildAt(0);
			
    		clipboard.setText(message.getText().toString().split("]")[1].trim());
    	}
    	else
    		return false;
    	return true;  
    }  
    
    public void saveMessages(String file)
	{
		try
		{
			OutputStreamWriter oWriter = new OutputStreamWriter(openFileOutput(file, 0));
			
			for(int i = 0; i < ll.getChildCount()-1; i++)
			{
				LinearLayout messageLayout = (LinearLayout)ll.getChildAt(i+1);
				TextView message = (TextView)messageLayout.getChildAt(0);
				oWriter.append(message.getText().toString() + "*EOM*");
			}
		
		oWriter.flush();
		oWriter.close();
		}
		catch (java.io.IOException exception) { Log.e(TAG, "IOException caused by trying to access " + file, exception); };
	}
    
    public void populateMessages(String file)
	{
		ll.removeAllViews();
		View separator = new View(this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1, 1f);
		separator.setLayoutParams(lp);
		separator.setBackgroundColor(Color.DKGRAY);
		
		ll.addView(separator);
		
		try
		{
			File inFile = getBaseContext().getFileStreamPath(file);
			
			if (inFile.exists())
			{
				InputStream iStream = openFileInput(file);
				InputStreamReader iReader = new InputStreamReader(iStream);
				BufferedReader bReader = new BufferedReader(iReader);
				
				String fullLine = "";
				String line;
				while((line = bReader.readLine()) != null)
				{
					if(line.equals(""))
						continue;
					fullLine = fullLine + line + "\n";
				}

				String[] lines = fullLine.split("\\*EOM\\*");
				for(int i = 0; i < lines.length-1; i++)
				{
					TextView newNote = new TextView(this);
					newNote.setText(lines[i]);
					newNote.setTextSize((float)16);
					newNote.setPadding(5, 10, 5, 10);
					
					LinearLayout message = new LinearLayout(this);
					message.setOrientation(LinearLayout.VERTICAL);
					registerForContextMenu(message);
					View sep = new View(this);
					LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1, 1f);
					sep.setLayoutParams(lpp);
					sep.setBackgroundColor(Color.DKGRAY);
					
					message.addView(newNote);
					message.addView(sep);
					message.setId(ll.getChildCount()+1);
					ll.addView(message);
				}
				
				iStream.close();
			}
			else
				Log.w(TAG, "\"" + messagesFile + "\" was not found!");
		}
		catch (java.io.FileNotFoundException exception) { Log.e(TAG, "FileNotFoundException caused by openFileInput(fileName)", exception); }
		catch (IOException exception) 					{ Log.e(TAG, "IOException caused by buffreader.readLine()", exception); 			}
	}
    
    public static String printDate()
    {
    	Calendar c = Calendar.getInstance();
    	
    	String hour = (c.get(Calendar.HOUR) == 0)?"12":Integer.toString(c.get(Calendar.HOUR));
    	String minute = Integer.toString(c.get(Calendar.MINUTE));
    	String second = Integer.toString(c.get(Calendar.SECOND));
    	String amOrPm = (c.get(Calendar.AM_PM) == 0)?"AM":"PM"; 
    	
    	return "" + pad(hour) + ":" + pad(minute) + ":" + pad(second) + " " + amOrPm + "";
    }
    
    public static String pad(String str)
    {
    	if(str.length() == 1)
    		return "0" + str;
    	
    	return str;
    }
}