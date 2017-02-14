package com.esoon.vidyosample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class Fragment1 extends Fragment implements OnClickListener {
	private View rootView;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(rootView == null)
		{
			rootView = inflater.inflate(R.layout.fragment1_layout, null);
			View v = rootView.findViewById(R.id.button_advise);
			v.setOnClickListener(this);
			
			 v = rootView.findViewById(R.id.Button_buss);
			v.setOnClickListener(this);
			 v = rootView.findViewById(R.id.Button_consult);
			v.setOnClickListener(this);
			
		}
		
		
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if(parent != null){
			parent.removeView(rootView);
			System.out.println ("removeView");
		}
		
		
		return rootView;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.Button_consult:
		case R.id.Button_buss:
		case R.id.button_advise:
				Intent it = new Intent();
				//it.setClass(packageContext, cls)
				it.setClass(Fragment1.this.getActivity(), SubActivityConsult.class);
				this.startActivity(it);
			break;
		
		}
		
	}

	
}
