package com.esoon.vidyosample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.esoon.utils.Contants;
import com.esoon.utils.Tools;

public class SettingActivity extends Activity implements OnClickListener {

	private Button bnt_save = null;
	private EditText server360= null;
	private EditText servervideo= null;
	private EditText edit_managerid= null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		bnt_save = (Button)this.findViewById(R.id.button_save_setting);
		server360 = (EditText)this.findViewById(R.id.edit_server360);
		servervideo = (EditText)this.findViewById(R.id.edit_servervidyo);
		
		
		edit_managerid=(EditText)this.findViewById(R.id.edit_managerid);
		
		
		servervideo.setText(Contants.portal);
		server360.setText(Contants.serverurl);
		edit_managerid.setText(Contants.managerId);
		
		
		bnt_save.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		if(v.getId() == R.id.button_save_setting)
		{
			Tools.SaveSetting(this, servervideo.getText().toString(), server360.getText().toString(),
					edit_managerid.getText().toString());
			
			this.finish();
			
		}
		
	}
}
