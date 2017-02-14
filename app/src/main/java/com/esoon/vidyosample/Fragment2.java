package com.esoon.vidyosample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class Fragment2 extends Fragment implements OnClickListener
{
	private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		if (rootView == null)
		{
			rootView = inflater.inflate(R.layout.fragment2_layout, null);
			Button bnt = (Button) rootView.findViewById(R.id.bntopenwin);
			bnt.setOnClickListener(this);
		}

		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null)
		{
			parent.removeView(rootView);
		}
		return rootView;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.bntopenwin:
				Intent it = new Intent(this.getActivity(),VidyoSampleActivity.class);
				this.startActivity(it);
				break;
		
		}

	}

}
