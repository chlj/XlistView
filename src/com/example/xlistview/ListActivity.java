package com.example.xlistview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.trinea.android.common.util.ProgressDialogUtils;
import cn.trinea.android.common.webservice.WebServiceKsoapUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.me.maxwin.view.XListView;
import com.me.maxwin.view.XListView.IXListViewListener;

public class ListActivity extends Activity {

	private int mPageSize = 10;// ÿҳ10������
	private int mCurrent = 1;// ��ǰҳ��
	private static final int GETLIST = 1;
	private List<GetInfoByModuleModel> list = new ArrayList<GetInfoByModuleModel>();

	private XListView listview;
	private ItemAdapter adapter;

	private boolean mIsFirst = true;
	private boolean mIsHeader = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listview);
		findView();
		initData(true, true);
	}

	private void findView() {
		listview = (XListView) findViewById(R.id.listview);

		adapter = new ItemAdapter(getApplicationContext());
		listview.setAdapter(adapter);
		listview.setPullRefreshEnable(false);
		
		listview.setPullLoadEnable(true);
		
		listview.setXListViewListener(new IXListViewListener() {
			@Override
			public void onRefresh() {
				initData(true, true);

			}

			@Override
			public void onLoadMore() {
				initData(false, false);

			}
		});
	}

	/**
	 * 
	 * @param isFirst
	 *            true ��һ�� ����
	 * @param isHeader
	 *            trueˢ�� �� false���ظ���
	 */
	private void initData(boolean isFirst, boolean isHeader) {
		mIsHeader = isHeader;
		mIsFirst = isFirst;
		if (isFirst) {
			mCurrent = 1;
		}
		if (!isHeader) {
			mCurrent = mCurrent + 1;
		}
		Log.i("xx", "mCurrent=" + mCurrent);
		new FGetListData().execute();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case GETLIST:
				String str = (String) msg.obj;
				try {
					// ����һ
					JSONObject jsonObject = new JSONObject(str);
					String jsonString = jsonObject.getString("GetInfoByModule");
					Gson gson = new Gson();
					List<GetInfoByModuleModel> lst = gson.fromJson(jsonString, new TypeToken<List<GetInfoByModuleModel>>() {
					}.getType());

					if (lst != null && lst.size() > 0) {
						Log.i("xx", "lst.size=" + lst.size());
						if (mIsFirst) {
							list.clear(); // ˢ�� ����������� ��ֹ�������� ����bug
						}
						list.addAll(lst);
						adapter.notifyDataSetChanged();

						// size=4,mPageSize=10
						if (lst.size() < mPageSize) {
							listview.setPullLoadEnable(false, "nomoredata");// û�и�������
																			// ������ʾ�ڵײ��ģ�
						} else {
							listview.setPullLoadEnable(true, ""); // �����ϻ� ���ظ���
						}

					} else {
						if (list == null || list.size() <= 0) {
							listview.setPullLoadEnable(false, ""); // û������
						} else {
							listview.setPullLoadEnable(false, "nomoredata");// û�и�������
						}
					}
				} catch (JSONException e) {
					Log.i("xx", "error=" + e.toString());
					e.printStackTrace();

				} finally {
//					if (mIsHeader) {
//						listview.stopRefresh();
//					} else {
//						listview.stopLoadMore();
//					}
					
					listview.stopRefresh();
					listview.stopLoadMore();
				}
				break;

			default:
				break;
			}
		}

	};

	// 98��
	private class FGetListData extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			ProgressDialogUtils.showProgressDialog(ListActivity.this, "���ڷ�������,���Ժ�...");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			Map tempMap = new HashMap();
			tempMap.put("moduleID", getResources().getString(R.string.modelid));
			tempMap.put("pageSize", mPageSize);
			tempMap.put("currPage", mCurrent);
			String sResult = WebServiceKsoapUtils.GetWebService("GetInfoByModule", tempMap, getResources().getString(R.string.jar_webserviceurl), getResources().getString(R.string.jar_webservicenamespace), getResources().getString(R.string.jar_webserviceuser), getResources().getString(R.string.jar_webservicepwd));
			Message msg = Message.obtain();
			msg.what = GETLIST;
			msg.obj = sResult;
			mHandler.sendMessage(msg);
			return sResult;
		}

		@Override
		protected void onPostExecute(String result) {
			ProgressDialogUtils.dismissProgressDialog();

		}

	}

	private class ItemAdapter extends BaseAdapter {

		private Context context;

		public ItemAdapter(Context context) {
			this.context = context.getApplicationContext();
		}

		@Override
		public int getCount() {
			if (list != null && list.size() > 0) {
				return list.size();
			} else {
				return 0;
			}

		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final GViewHolder holder;
			if (convertView == null) {
				holder = new GViewHolder();
				convertView = LayoutInflater.from(context).inflate(R.layout.listview_item, (ViewGroup) null, false);

				holder.txt = (TextView) convertView.findViewById(R.id.txt);

				convertView.setTag(holder);
			} else {
				holder = (GViewHolder) convertView.getTag();
			}

			if (list != null && list.size() > 0) {

			} else {
				Log.i("xx", "is null");
			}
			holder.txt.setText(list.get(position).getTitle());

			return convertView;
		}

		private class GViewHolder {

			TextView txt;
			TextView titleView;

		}
	}

}
