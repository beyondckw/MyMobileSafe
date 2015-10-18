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
	private int maxnumber = 10;    //一次最多加载10条数据
	private boolean flag = false;  //是否已经加载完毕
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
		//加载数据
		fillData();
		
		//给listview注册一个滚动事件的监听器，当滚动到末尾时就加载数据
		lv_callsms_safe.setOnScrollListener(new OnScrollListener() {
			//当滚动状态改变时调用
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch(scrollState){
				case OnScrollListener.SCROLL_STATE_IDLE://空闲状态
					//获取最后一个可见条目在集合中的位置
					int lastposition = lv_callsms_safe.getLastVisiblePosition();
					int firstposition = lv_callsms_safe.getFirstVisiblePosition();
					if(flag && lastposition == infos.size()-1){
						if(toast == null){
							toast = Toast.makeText(getApplicationContext(), "数据已经加载完毕了\n没有数据可以加载了", 0);
						}else{
							toast.show();
						}	
					}else if(lastposition == infos.size()-1){
						offset = offset + maxnumber;
						ll_loading.setVisibility(View.VISIBLE);
						fillData();
					}
					
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://触摸状态
					break;
				case OnScrollListener.SCROLL_STATE_FLING://惯性滑动状态
					break;
				}
			}
			
			//滚动的时候调用的方法
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
					infos.addAll(dao.findPart(offset, maxnumber));//数据追加到集合的末尾
				}
				if(infos!=null && infos.size()==dao.totalNumber()){ //如果数据库中所有的数据已经加载完毕，返回true
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
				
				//第一次加载的时候就把它们存起来
				holder = new ViewHolder();
				holder.tv_number = (TextView) view.findViewById(R.id.tv_black_number);
				holder.tv_mode = (TextView) view.findViewById(R.id.tv_block_mode);
				holder.iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
				
				//存放到父控件中，也就是view里
				view.setTag(holder);
			}else{
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}
			
			holder.tv_number.setText(infos.get(position).getNumber());
			String mode = infos.get(position).getMode();
			if("1".equals(mode)){
				holder.tv_mode.setText("电话拦截");
			}else if("2".equals(mode)){
				holder.tv_mode.setText("短信拦截");
			}else{
				holder.tv_mode.setText("全部拦截");
			}
			
			holder.iv_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new Builder(CallSmsSafeActivity.this);
					builder.setTitle("警告");
					builder.setMessage("确定要删除这条记录么？");
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//删除数据库的内容
							dao.delete(infos.get(position).getNumber());
							//更新界面。
							infos.remove(position);
							//通知listview数据适配器更新
							adapter.notifyDataSetChanged();
						}
					});
					builder.setNegativeButton("取消", null);
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
	 * view对象的容器
	 *记录孩子的内存地址
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
					Toast.makeText(getApplicationContext(), "黑名单号码不能为空", 0).show();
					return;
				}
				String mode ;
				if(cb_phone.isChecked()&&cb_sms.isChecked()){
					//全部拦截
					mode = "3";
				}else if(cb_phone.isChecked()){
					//电话拦截
					mode = "1";
				}else if(cb_sms.isChecked()){
					//短信拦截
					mode = "2";
				}else{
					Toast.makeText(getApplicationContext(), "请选择拦截模式", 0).show();
					return;
				}
				//数据被加到数据库
				dao.add(blacknumber, mode);
				//更新listview集合里面的内容。
				BlackNumberInfo info = new BlackNumberInfo();
				info.setMode(mode);
				info.setNumber(blacknumber);
				infos.add(0, info);
				//通知listview数据适配器数据更新了。
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

















