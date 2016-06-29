package com.tencent.devicedemo;

import java.util.ArrayList;
import java.util.List;

import com.tencent.av.VideoController;
import com.tencent.device.TXBinderInfo;
import com.tencent.device.TXDeviceService;

import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.GridView;


public class MainActivity extends Activity{
  
	private GridView mGridView; 
	private BinderListAdapter mAdapter;
	private NotifyReceiver  mNotifyReceiver;
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated constructor stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent startIntent = new Intent(this, TXDeviceService.class); 
		startService(startIntent);

		mGridView = (GridView) findViewById(R.id.gridView_binderlist);
		mAdapter = new BinderListAdapter(this);
		mGridView.setAdapter(mAdapter);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(TXDeviceService.BinderListChange);
		filter.addAction(TXDeviceService.OnEraseAllBinders);
		mNotifyReceiver = new NotifyReceiver();
		registerReceiver(mNotifyReceiver, filter);
		
		
        boolean bNetworkSetted = this.getSharedPreferences("TXDeviceSDK", 0).getBoolean("NetworkSetted", false);
        if (TXDeviceService.NetworkSettingMode == true && bNetworkSetted == false) {
			Intent intent = new Intent(MainActivity.this, WifiDecodeActivity.class);
			startActivity(intent);
        }
	}
	
	public void eraseAllBinders(View v) {
		AlertDialog dialog = null;
		Builder builder = new AlertDialog.Builder(this).setTitle("解除绑定").setMessage("您确定要解绑所有用户吗？").setPositiveButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();

			}
		}).setNegativeButton("解除绑定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				TXDeviceService.eraseAllBinders();
			}
		});
		dialog = builder.create();
		dialog.show();
	}
	
	public void uploadDeviceLog(View v) 
	{
		TXDeviceService.getInstance().uploadSDKLog();
	}

	protected void onResume(){
		super.onResume();
		TXBinderInfo [] arrayBinder = TXDeviceService.getBinderList();
		if (arrayBinder != null){
			List<TXBinderInfo> binderList = new ArrayList<TXBinderInfo>();
			for (int i = 0; i < arrayBinder.length; ++i){
				binderList.add(arrayBinder[i]);
			}
			if (mAdapter != null) {
				mAdapter.freshBinderList(binderList);
			}
		}
	}
	
	protected void onPause(){
		super.onPause();
	}
	
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mNotifyReceiver);
	}
	
	public class NotifyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() == TXDeviceService.BinderListChange){
				Parcelable[] listTemp = intent.getExtras().getParcelableArray("binderlist");
				List<TXBinderInfo> binderList = new ArrayList<TXBinderInfo>();
				for (int i = 0; i < listTemp.length; ++i){
					TXBinderInfo  binder = (TXBinderInfo)(listTemp[i]);
					binderList.add(binder); 
				}
				if (mAdapter != null) {
					mAdapter.freshBinderList(binderList);
				}
			} else if (intent.getAction() == TXDeviceService.OnEraseAllBinders){
				int resultCode = intent.getExtras().getInt(TXDeviceService.OperationResult);
				if (0 != resultCode) {
					showAlert("解除绑定失败", "解除绑定失败，错误码:" + resultCode);
				} else {
					showAlert("解除绑定成功", "解除绑定成功!!!");
				}
			} 
		}
	}
	
	private void showAlert(String strTitle, String strMsg) {
		// TODO Auto-generated method stub
		AlertDialog dialogError;
		Builder builder = new AlertDialog.Builder(this).setTitle(strTitle).setMessage(strMsg).setPositiveButton("取消", null).setNegativeButton("确定",null);
		dialogError = builder.create();
		dialogError.show();
	}
}
