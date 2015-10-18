package com.ckw.mymobilesafe;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ckw.mymobilesafe.db.dao.BlackNumberDao;
import com.ckw.mymobilesafe.domain.BlackNumberInfo;

public class CallSmsSafeActivity extends Activity {
	
	
	private ListView lv_callsms_safe;
	private List<BlackNumberInfo> infos;
	private BlackNumberDao dao;
	private CallSmsSafeAdapter adapter;
	
	private LinearLayout ll_loading;
	private int offset = 0;
	private int maxnumber = 10;    //һ��������10������
	private boolean flag = false;  //�Ƿ��Ѿ��������
	private Toast toast;
	private int total = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_sms_safe);
		lv_callsms_safe = (ListView) findViewById(R.id.lv_callsms_safe);
		ll_loading = (LinearLayout)findViewById(R.id.ll_loading);
		ll_loading.setVisibility(View.VISIBLE);
		
		dao = new BlackNumberDao(this);
		//��������
		fillData();
		
		//��listviewע��һ�������¼��ļ���������������ĩβʱ�ͼ�������
		lv_callsms_safe.setOnScrollListener(new OnScrollListener() {
			//������״̬�ı�ʱ����
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch(scrollState){
				case OnScrollListener.SCROLL_STATE_IDLE://����״̬
					//��ȡ���һ���ɼ���Ŀ�ڼ����е�λ��
					int lastposition = lv_callsms_safe.getLastVisiblePosition();
					int firstposition = lv_callsms_safe.getFirstVisiblePosition();
					if(flag && lastposition == infos.size()-1){
						if(toast == null){
							toast = Toast.makeText(getApplicationContext(), "�����Ѿ����������\nû�����ݿ��Լ�����", 0);
						}else{
							toast.show();
						}	
					}else if(lastposition == infos.size()-1){
						offset = offset + maxnumber;
						ll_loading.setVisibility(View.VISIBLE);
						fillData();
					}
					
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://����״̬
					break;
				case OnScrollListener.SCROLL_STATE_FLING://���Ի���״̬
					break;
				}
			}
			
			//������ʱ����õķ���
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		
	}
	
	private void fillData() {
		new Thread(){
			public void run() {
				if(infos == null){
					infos = dao.findPart(offset, maxnumber);
				}else{
					infos.addAll(dao.findPart(offset, maxnumber));//����׷�ӵ����ϵ�ĩβ
				}
				if(infos!=null && infos.size()==dao.totalNumber()){ //������ݿ������е������Ѿ�������ϣ�����true
					flag = true;
				}else{
					flag = false;
				}
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ll_loading.setVisibility(View.INVISIBLE);
						if(adapter == null){
							adapter = new CallSmsSafeAdapter();
							lv_callsms_safe.setAdapter(adapter);
						}else{
							adapter.notifyDataSetChanged();
						}
					}
				});
			};
		}.start();
	}

	private class CallSmsSafeAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return infos.size();
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder;
			if(convertView == null){
				view = View.inflate(getApplicationContext(), R.layout.list_item_callsms, null);
				
				//��һ�μ��ص�ʱ��Ͱ����Ǵ�����
				holder = new ViewHolder();
				holder.tv_number = (TextView) view.findViewById(R.id.tv_black_number);
				holder.tv_mode = (TextView) view.findViewById(R.id.tv_block_mode);
				holder.iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
				
				//��ŵ����ؼ��У�Ҳ����view��
				view.setTag(holder);
			}else{
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}
			
			holder.tv_number.setText(infos.get(position).getNumber());
			String mode = infos.get(position).getMode();
			if("1".equals(mode)){
				holder.tv_mode.setText("�绰����");
			}else if("2".equals(mode)){
				holder.tv_mode.setText("��������");
			}else{
				holder.tv_mode.setText("ȫ������");
			}
			
			holder.iv_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new Builder(CallSmsSafeActivity.this);
					builder.setTitle("����");
					builder.setMessage("ȷ��Ҫɾ��������¼ô��");
					builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//ɾ�����ݿ������
							dao.delete(infos.get(position).getNumber());
							//���½��档
							infos.remove(position);
							//֪ͨlistview��������������
							adapter.notifyDataSetChanged();
						}
					});
					builder.setNegativeButton("ȡ��", null);
					builder.show();
				}
			});
			
			return view;
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}
	
	/**
	 * view���������
	 *��¼���ӵ��ڴ��ַ
	 */
	static class ViewHolder{
		TextView tv_number;
		TextView tv_mode;
		ImageView iv_delete;
	}
	
	
	private EditText et_blacknumber;
	private CheckBox cb_phone;
	private CheckBox cb_sms;
	private Button bt_ok;
	private Button bt_cancel;
	
	
	public void addBlackNumber(View view){
		AlertDialog.Builder builder = new Builder(this);
		final AlertDialog dialog = builder.create();
		
		View contentview = View.inflate(this, R.layout.dialog_add_blacknumber, null);
		et_blacknumber = (EditText) contentview.findViewById(R.id.et_blacknumber);
		cb_phone = (CheckBox) contentview.findViewById(R.id.cb_phone);
		cb_sms = (CheckBox) contentview.findViewById(R.id.cb_sms);
		bt_ok = (Button) contentview.findViewById(R.id.ok);
		bt_cancel = (Button) contentview.findViewById(R.id.cancel);
		
		dialog.setView(contentview, 0, 0, 0, 0);
		dialog.show();
		bt_ok.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

				String blacknumber = et_blacknumber.getText().toString().trim();
				if(TextUtils.isEmpty(blacknumber)){
					Toast.makeText(getApplicationContext(), "���������벻��Ϊ��", 0).show();
					return;
				}
				String mode ;
				if(cb_phone.isChecked()&&cb_sms.isChecked()){
					//ȫ������
					mode = "3";
				}else if(cb_phone.isChecked()){
					//�绰����
					mode = "1";
				}else if(cb_sms.isChecked()){
					//��������
					mode = "2";
				}else{
					Toast.makeText(getApplicationContext(), "��ѡ������ģʽ", 0).show();
					return;
				}
				//���ݱ��ӵ����ݿ�
				dao.add(blacknumber, mode);
				//����listview������������ݡ�
				BlackNumberInfo info = new BlackNumberInfo();
				info.setMode(mode);
				info.setNumber(blacknumber);
				infos.add(0, info);
				//֪ͨlistview�������������ݸ����ˡ�
				adapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		});
		bt_cancel.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

				dialog.dismiss();
			}
		});
	}
	
}

















