package com.example.autostart;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {
    
    private static final String LOG_TAG = "AutoRunManager";
    
	private ListView lv;          
	private Button allowBt;       //�����б���ʾ��ť
	private Button forbidBt;      //��ֹ�б���ʾ��ť
	private Button goBt;			//�Ż�ִ�а�ť
	private Context context;
	private PackageManager mPackageManager;
	private MyAdapter mAdapter;
	private ProgressDialog progressDialog;
	Intent intent;
	private ArrayList<HashMap<String, Object>> allowList;			//����������Ӧ����Ϣ����
	private ArrayList<HashMap<String, Object>> forbidList;			//��ֹ������Ӧ����Ϣ����
	private List<ResolveInfo> allowInfoList =new ArrayList<ResolveInfo>(); 	 //��ȡ������receiver����Ϣ
	private List<ResolveInfo> forbidInfoList=new ArrayList<ResolveInfo>();  //��ȡ��������ֹ������receiver����Ϣ
	private int flag;												//allowBt��forbidBt�ĵ����¼�����allowBtʱΪ0�����forbidBtʱΪ1
	protected static final int START = 0;							//��ʾprogressdiaglog
	protected static final int STOP = 1;							//�ر�progressdiaglog
	protected static final int PROCESS = 2;							//progressdiaglog

	private static HashMap<Integer,Boolean> isSelected; 			//����checkbox�ĵ����Ϣ
	
    private List<Intent> mForbiddenIntent;
    private String[] mForbiddenReceiver = new String[] {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_SCREEN_ON,
            Intent.ACTION_SCREEN_OFF,
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_BATTERY_LOW,
            Intent.ACTION_BATTERY_OKAY
    };
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//���ֳ�ʼ��
		flag = 0;
		context = this;
		this.startService(new Intent(context, BootTestService.class));
		
	    mForbiddenIntent = new ArrayList<Intent>();
        for (String forbiddenAction : mForbiddenReceiver)
        {
            Intent forbiddenIntent = new Intent(forbiddenAction);
            mForbiddenIntent.add(forbiddenIntent);
        }
		
		lv = (ListView) findViewById(R.id.lv);
		allowBt = (Button) findViewById(R.id.allow_bt);
		forbidBt = (Button) findViewById(R.id.forbid_bt);
		goBt = (Button) findViewById(R.id.go_bt);
		progressDialog = new ProgressDialog(MainActivity.this);  
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);	//����progressdiaglog���ΪԲ�ν�����  
		progressDialog.setTitle("��ʾ");									//����progressdiaglog����
		progressDialog.setMessage("�Ż���...");  
		progressDialog.setCancelable(false);							//����progressdiaglog�������Ƿ���԰��˻ؼ�ȡ��  
		mPackageManager = getPackageManager();
		intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
		allowList = new ArrayList<HashMap<String, Object>>();
		forbidList = new ArrayList<HashMap<String, Object>>();
		updateAllowList();												//��������������Ӧ���б�
		mAdapter = new MyAdapter(allowList,context);
		lv.setAdapter(mAdapter);										//Ϊlv����������
		lv.setItemsCanFocus(false);
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);				//����lv����Ϊ��ѡ
		//����lv��ÿһ��item�ĵ�����������ÿһ��item��������Ӧ��checkbox
		lv.setOnItemClickListener(new OnItemClickListener() {			
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position,long id) {  
	        	// ȡ��ViewHolder����������ʡȥ��ͨ������findViewByIdȥʵ����������Ҫ��cbʵ���Ĳ���  
	        	ViewHolder holder = (ViewHolder) view.getTag();  
	            // �ı�CheckBox��״̬  
	            holder.cb.toggle();  
	            // ��CheckBox��ѡ��״����¼����  
	            MyAdapter.getIsSelected().put(position, holder.cb.isChecked());   
	        }  
	    });  
		//����allowBt�ĵ������������allowList�����Ҹ���lv����ʾ
		allowBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				flag = 0;
				updateAllowList();
				mAdapter.refresh(allowList);
			}
		});
		//����forbidBt�ĵ������������forbidList�����Ҹ���lv����ʾ
		forbidBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				flag = 1;
				updateForbidList();
				mAdapter.refresh(forbidList);
			}
		});
		//����goBt�ĵ����������ѡ�е�Ӧ�ý����Ż����������Ҹ���lv����ʾ
		goBt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				//�����½��̣�ִ���Ż�����
				new Thread(){
				public void run(){	
					Message msg = new Message();
					msg.what = START;
					//��mHandler����msg����ʾ��ʼ��ʾ������
					mHandler.sendMessage(msg);			
					String cmd;
					//��ȡcheckbox��ѡ����Ϣ
					//allowBt�����������ǰlv��ʾ������������Ӧ���б�ʱ�����н�ֹѡ��Ӧ���������Ĳ���
					if(flag == 0){				
						isSelected = mAdapter.getIsSelected();
						for(int i = 0; i < allowList.size(); i++){
							//Ѱ��checkbox��ѡ�е�Ӧ��
							if(isSelected.get(i)){		
								//��ȡ��Ӧ�ð�����packagereceiver����ʽΪ��package/receiver��
								String packageReceiverList[] = allowList.get(i).get("packageReceiver").toString().split(";");	
								//���͵�ǰ�Ż���Ӧ������
								Message msg1 = new Message();
								msg1.obj = allowList.get(i).get("appName");
								msg1.what = PROCESS;
								mHandler.sendMessage(msg1);
								//disable��Щreceiver
								for(int j = 0; j < packageReceiverList.length; j++){
									cmd = "pm disable "+packageReceiverList[j];
									//����receiver����$���ţ���Ҫ����һ��������"$"�滻��$
									cmd = cmd.replace("$", "\""+"$"+"\"");
									//ִ������
									execCmd(cmd);			
								}
							}
						}
						updateAllowList();		//����allowList
					//forbidBt�����������ǰlv��ʾ����ֹ������Ӧ���б�ʱ�����лָ�ѡ��Ӧ���������Ĳ���
					}else{
						isSelected = mAdapter.getIsSelected();
						for(int i = 0; i < forbidList.size(); i++){
							if(isSelected.get(i)){
								String packageReceiverList[] = forbidList.get(i).get("packageReceiver").toString().split(";");
								//���͵�ǰ�Ż���Ӧ������
								Message msg1 = new Message();
								msg1.obj = forbidList.get(i).get("appName");
								msg1.what = PROCESS;
								mHandler.sendMessage(msg1);
								for(int j = 0; j < packageReceiverList.length; j++){
									cmd = "pm enable "+packageReceiverList[j];
									cmd = cmd.replace("$", "\""+"$"+"\"");
									execCmd(cmd);			
								}
							}
						}
						updateForbidList();
					}
					Message msg2 = new Message();
					msg2.what = STOP;
					//��mHandler����msg����ʾ�رս�����
					mHandler.sendMessage(msg2);
				}
				}.start();
			}
		});
	}
	
	//�������thread��UI���£�������ʾ���������رս�������ˢ��listview��ʾ
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case START: progressDialog.show(); 
						break;
			case STOP: progressDialog.cancel(); 
					   if(flag == 0)
						   mAdapter.refresh(allowList);
					   else
						   mAdapter.refresh(forbidList); 
					   break;
			case PROCESS: progressDialog.setMessage(msg.obj.toString()+"�Ż���...");
							break;

			}
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	//allowList�ĸ��²���
	public void updateAllowList(){
        // ��ȡ������receiver����Ϣ
        for (Intent forbiddenIntent : mForbiddenIntent)
        {
            List<ResolveInfo> allowReceivers = mPackageManager.queryBroadcastReceivers(
                    forbiddenIntent,
                    PackageManager.GET_RECEIVERS);
            allowInfoList.addAll(allowReceivers);
        }
		int k = 0;
		//ȥ��ϵͳӦ��receiver
		while(k < allowInfoList.size()){
			if((allowInfoList.get(k).activityInfo.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==1||  
					(allowInfoList.get(k).activityInfo.applicationInfo.flags&ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)==1){ 
				allowInfoList.remove(k);
			}else
				k++;
		}
		//���allowList
		allowList.clear();
		String appName = null;
		String packageReceiver = null;
		Object icon = null;
		//��ȡallowInfoList�е�һ��receiver��Ӧ��Ӧ�õ�����
		if(allowInfoList.size() > 0){
			appName = mPackageManager.getApplicationLabel(allowInfoList.get(0).activityInfo.applicationInfo).toString();
			//��ȡallowInfoList�е�һ��receiver��Ӧ��Ӧ�õİ�����receiver���ƣ���ʽΪ"package/receiver"
			packageReceiver = allowInfoList.get(0).activityInfo.packageName + "/" + allowInfoList.get(0).activityInfo.name;
			//��ȡallowInfoList�е�һ��receiver��Ӧ��Ӧ�õ�ͼ����Ϣ
			icon =  mPackageManager.getApplicationIcon(allowInfoList.get(0).activityInfo.applicationInfo);
			for(int i = 1; i < allowInfoList.size(); i++){ 
				//����Ӧ����Ϣ
				HashMap<String, Object> map = new HashMap<String, Object>();
				//����һ��Ӧ�ÿ��ܰ������receiver����Ҫ����Щreceiver�Ͷ�Ӧ��Ӧ�����Ʒ���ͬһ��map�У�������Щ��ͬ��receiver��";"�������Ա�֮����split����ȡ��
				if(appName.equals(mPackageManager.getApplicationLabel(allowInfoList.get(i).activityInfo.applicationInfo).toString())){
					packageReceiver = packageReceiver + ";" + allowInfoList.get(i).activityInfo.packageName + "/" + allowInfoList.get(i).activityInfo.name;
					//�����ǰ��receiver��֮ǰ��receiver��Ӧ���ǲ�ͬ��Ӧ�ã���ô��֮ǰ��Ӧ����Ϣ���浽map�У�Ȼ��洢��allowList�С�
				}else{
					map.put("icon", icon);
					map.put("appName", appName);
					map.put("packageReceiver", packageReceiver);
					allowList.add(map);
					packageReceiver = allowInfoList.get(i).activityInfo.packageName + "/" + allowInfoList.get(i).activityInfo.name;
					appName = mPackageManager.getApplicationLabel(allowInfoList.get(i).activityInfo.applicationInfo).toString();
					icon =  mPackageManager.getApplicationIcon(allowInfoList.get(i).activityInfo.applicationInfo);
				}
			}
			//��allowInfoList�е����һ��Ӧ����Ϣ���浽allowList��
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("icon", icon);
			map.put("appName", appName);
			map.put("packageReceiver", packageReceiver);
			allowList.add(map);
		}	
	}
	
	//forbidList�ĸ��²���
	public void updateForbidList(){
		//��ȡ��������ֹ������receiver����Ϣ
	    for (Intent forbiddenIntent : mForbiddenIntent)
        {
            List<ResolveInfo> forbiddenReceivers = mPackageManager.queryBroadcastReceivers(
                forbiddenIntent,
                PackageManager.GET_DISABLED_COMPONENTS);
            forbidInfoList.addAll(forbiddenReceivers);
        }
		int k = 0;
		//ȥ��ϵͳӦ��receiver�Լ�������������receiver
		while(k < forbidInfoList.size()){
			if((forbidInfoList.get(k).activityInfo.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==1||  
					(forbidInfoList.get(k).activityInfo.applicationInfo.flags&ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)==1){ 
				forbidInfoList.remove(k);
			}else
				k++;
		}
		k = 0;
		while(k < forbidInfoList.size()){
			 ComponentName mComponentName = new ComponentName(forbidInfoList.get(k).activityInfo.packageName, forbidInfoList.get(k).activityInfo.name);
			 if(mPackageManager.getComponentEnabledSetting(mComponentName)!=2)
				 forbidInfoList.remove(k);
			 else
				 k++;
		}
		forbidList.clear();
		String appName = null;
		String packageReceiver = null;
		Object icon = null;
		if(forbidInfoList.size() > 0){
			appName = mPackageManager.getApplicationLabel(forbidInfoList.get(0).activityInfo.applicationInfo).toString();
			//��ȡforbidInfoList�е�һ��receiver��Ӧ��Ӧ�õİ�����receiver���ƣ���ʽΪ"package/receiver"
			packageReceiver = forbidInfoList.get(0).activityInfo.packageName + "/" + forbidInfoList.get(0).activityInfo.name;
			//��ȡforbidInfoList�е�һ��receiver��Ӧ��Ӧ�õ�ͼ����Ϣ
			icon =  mPackageManager.getApplicationIcon(forbidInfoList.get(0).activityInfo.applicationInfo);
			for(int i = 1; i < forbidInfoList.size(); i++){
				HashMap<String, Object> map = new HashMap<String, Object>(); 
				//����һ��Ӧ�ÿ��ܰ������receiver����Ҫ����Щreceiver�Ͷ�Ӧ��Ӧ�����Ʒ���ͬһ��map�У�������Щ��ͬ��receiver��";"�������Ա�֮����split����ȡ��
				if(appName.equals(mPackageManager.getApplicationLabel(forbidInfoList.get(i).activityInfo.applicationInfo).toString())){
					packageReceiver = packageReceiver + ";" + forbidInfoList.get(i).activityInfo.packageName + "/" + forbidInfoList.get(i).activityInfo.name;
					//�����ǰ��receiver��֮ǰ��receiver��Ӧ���ǲ�ͬ��Ӧ�ã���ô��֮ǰ��Ӧ����Ϣ���浽map�У�Ȼ��洢��forbidList�С�				
					}else{
						map.put("icon", icon);
						map.put("appName", appName);
						map.put("packageReceiver", packageReceiver);
						forbidList.add(map);
						packageReceiver = forbidInfoList.get(i).activityInfo.packageName + "/" + forbidInfoList.get(i).activityInfo.name;
						appName = mPackageManager.getApplicationLabel(forbidInfoList.get(i).activityInfo.applicationInfo).toString();
						icon =  mPackageManager.getApplicationIcon(forbidInfoList.get(i).activityInfo.applicationInfo);
					}
				}
			//��forbidInfoList�е����һ��Ӧ����Ϣ���浽forbidList��,position+1��forbidInfoList��С���ʱ����ʾ
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("icon", icon);
			map.put("appName", appName);
			map.put("packageReceiver", packageReceiver);
			forbidList.add(map);
		}
	}
	
	//��rootȨ��ִ���ⲿ����"pm disable"��"pm enable"
	public static boolean execCmd(String cmd) {
	    Log.d(LOG_TAG,"pm cmd = "+cmd);
	    //07-22 18:34:42.902: D/AutoRunManager(26753): pm cmd = pm enable com.chaozh.iReaderFree/com.igexin.sdk.SdkReceiver
	    Process process = null;
	    DataOutputStream os = null;
	    try {
	        process = Runtime.getRuntime().exec("su"); //�л���root�ʺ�
	        os = new DataOutputStream(process.getOutputStream());
	        os.writeBytes(cmd + "\n");
	        os.writeBytes("exit\n");
	        os.flush();
	        process.waitFor();
	    } catch (Exception e) {
	        return false;
	    } finally {
	        try {
	            if (os != null) {
	                os.close();
	            }
	            process.destroy();
	        } catch (Exception e) {
	        }
	    }
	    return true;
	}

}
