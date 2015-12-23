/**
 * @file XFooterView.java
 * @create Mar 31, 2012 9:33:43 PM
 * @author Maxwin
 * @description XListView's footer
 */
package com.me.maxwin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.xlistview.R;

public class XListViewFooter extends LinearLayout {
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_LOADING = 2;

	private Context mContext;

	private View mContentView;
	private View mProgressBar;
	private TextView mHintView;
	
	public XListViewFooter(Context context) {
		super(context);
		initView(context);
	}
	
	public XListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private String mClick="";
	
	public void setState(int state) {
		mHintView.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
		if (state == STATE_READY) {
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText(R.string.xlistview_footer_hint_ready);
		} else if (state == STATE_LOADING) {
			mProgressBar.setVisibility(View.VISIBLE);
		} else {
//			mHintView.setVisibility(View.VISIBLE);
//			mHintView.setText(R.string.xlistview_footer_hint_normal);
			
			Log.i("mm","重复");
			mHintView.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.GONE);
			
			if("click".equals(mClick)){
			     //加载更多
				mHintView.setVisibility(View.VISIBLE);
				mHintView.setText("点击加载更多");
			}
			else if("nomoredata".equals(mClick)){
				//没有更多数据
				mHintView.setVisibility(View.VISIBLE);
				mHintView.setText("没有更多数据");
			}
			else if("nodata".equals(mClick)){
				//没有数据
				mHintView.setVisibility(View.GONE);
			}
			else{
				//隐藏
				mHintView.setVisibility(View.VISIBLE);
				mHintView.setText(R.string.xlistview_footer_hint_normal);
			}
			
		}
	}
	

	
	public void setState2(int state,String flag) {

		
		mHintView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.GONE);
		mClick=flag;
		if("click".equals(flag)){
		     //加载更多
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText("点击加载更多");
		}
		else if("nomoredata".equals(flag)){
			//没有更多数据
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText("没有更多数据");
		}
		else if("nodata".equals(flag)){
			//没有数据
			mHintView.setVisibility(View.GONE);
		}
		else{
			//隐藏
			mHintView.setVisibility(View.GONE);
		}
		
	}

	
	public void setBottomMargin(int height) {
		if (height < 0) return ;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		lp.bottomMargin = height;
		mContentView.setLayoutParams(lp);
	}
	
	public int getBottomMargin() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		return lp.bottomMargin;
	}
	
	/**
	 * normal status
	 */
	public void normal() {
		mHintView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
	}
	
	
	/**
	 * loading status 
	 */
	public void loading() {
		mHintView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * hide footer when disable pull load more
	 */
	public void hide() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		lp.height = 0;
		mContentView.setLayoutParams(lp);
	}
	
	/**
	 * show footer
	 */
	public void show() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		mContentView.setLayoutParams(lp);
	}
	
	private void initView(Context context) {
		mContext = context;
		LinearLayout moreView = (LinearLayout)LayoutInflater.from(mContext).inflate(R.layout.xlistview_footer, null);
		addView(moreView);
		moreView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		mContentView = moreView.findViewById(R.id.xlistview_footer_content);
		mProgressBar = moreView.findViewById(R.id.xlistview_footer_progressbar);
		mHintView = (TextView)moreView.findViewById(R.id.xlistview_footer_hint_textview);
	}
	
	
}
