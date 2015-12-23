/**
 * @file XListView.java
 * @package me.maxwin.view
 * @create Mar 18, 2012 6:28:41 PM
 * @author Maxwin
 * @description An ListView support (a) Pull down to refresh, (b) Pull up to load more.
 * 		Implement IXListViewListener, and see stopRefresh() / stopLoadMore().
 */
package com.me.maxwin.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xlistview.R;

public class XListView extends ListView implements OnScrollListener {

	private float mLastY = -1; // save event y
	private Scroller mScroller; // used for scroll back
	private OnScrollListener mScrollListener; // user's scroll listener

	// the interface to trigger refresh and load more.
	private IXListViewListener mListViewListener;

	// -- header view
	private XListViewHeader mHeaderView;
	// header view content, use it to calculate the Header's height. And hide it
	// when disable pull refresh.
	private RelativeLayout mHeaderViewContent;
	private TextView mHeaderTimeView;
	private int mHeaderViewHeight; // header view's height
	private boolean mEnablePullRefresh = true;
	private boolean mPullRefreshing = false; // is refreashing.

	// -- footer view
	private XListViewFooter mFooterView;
	private boolean mEnablePullLoad;
	private boolean mPullLoading;
	private boolean mIsFooterReady = false;

	// total list items, used to detect is at the bottom of listview.
	private int mTotalItemCount;

	// for mScroller, scroll back from header or footer.
	private int mScrollBack;
	private final static int SCROLLBACK_HEADER = 0;
	private final static int SCROLLBACK_FOOTER = 1;

	private final static int SCROLL_DURATION = 400; // scroll back duration
	private final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >= 50px
														// at bottom, trigger
														// load more.
	private final static float OFFSET_RADIO = 2.0f; // support iOS like pull
													// feature.

	private boolean isShowFoot = false; // 底部栏是否显示

	/**
	 * @param context
	 */
	public XListView(Context context) {
		super(context);
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initWithContext(context);
	}

	public XListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWithContext(context);
	}

	private void initWithContext(Context context) {
		mScroller = new Scroller(context, new DecelerateInterpolator());
		// XListView2 need the scroll event, and it will dispatch the event to
		// user's listener (as a proxy).
		super.setOnScrollListener(this);

		// init header view
		mHeaderView = new XListViewHeader(context);
		mHeaderViewContent = (RelativeLayout) mHeaderView.findViewById(R.id.xlistview_header_content);
		mHeaderTimeView = (TextView) mHeaderView.findViewById(R.id.xlistview_header_time);
		addHeaderView(mHeaderView);

		// init footer view
		mFooterView = new XListViewFooter(context);

		// init header height
		mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mHeaderViewHeight = mHeaderViewContent.getHeight();
				getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});

		setRefreshTime();
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		// make sure XListViewFooter is the last footer view, and only add once.
		if (mIsFooterReady == false) {
			mIsFooterReady = true;
			addFooterView(mFooterView);
		}
		super.setAdapter(adapter);
	}

	/**
	 * enable or disable pull down refresh feature.
	 * 
	 * @param enable
	 */
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			mHeaderViewContent.setVisibility(View.INVISIBLE);
		} else {
			mHeaderViewContent.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * enable or disable pull up load more feature.
	 * 
	 * @param enable
	 */

	private String mClick = "";

	/**
	 * 
	 * @param enable
	 * @param click
	 */
	public void setPullLoadEnable(boolean enable, final String click) {
		mEnablePullLoad = enable;
		mClick = click;

		if (mEnablePullLoad) {
			// 可以上拉
			mPullLoading = false;
			if ("".equals(click)) {
				isShowFoot = true;
			} else if ("click".equals(click)) {
				// 加载更多
				isShowFoot = true;
			} else if ("nomoredata".equals(click)) {
				// 没有更多数据
				isShowFoot = true;
			} else if ("toast".equals(click)) {
				// 可以显示 ，但是是隐藏的状态(方便继续上滑动)
				isShowFoot = true;
			} else {
				// toast
				isShowFoot = false;
			}
		} else {
			// 不可以上拉
			if ("click".equals(click)) {
				// 加载更多
				isShowFoot = true;
			} else if ("nomoredata".equals(click)) {
				// 没有更多数据
				isShowFoot = true;
			} else if ("nodata".equals(click)) {
				// 没有数据
				isShowFoot = false;
			} else if ("toast".equals(click)) {
				// 可以显示
				isShowFoot = false;
			} else {
				// 隐藏
				isShowFoot = false;
			}
		}

		if ("toast".equals(click)) {

			Toast.makeText(getContext(), getContext().getString(R.string.xlistview_nomoredata), Toast.LENGTH_SHORT).show();
		}

		if (isShowFoot) {
			mFooterView.setVisibility(View.VISIBLE); // 没有更多数据
		} else {
			mFooterView.setVisibility(View.GONE);
		}

		mFooterView.setState2(XListViewFooter.STATE_NORMAL, mClick);

		mFooterView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ("click".equals(click)) {
					startLoadMore(); // 加载更多
				} else if ("nomoredata".equals(click)) {
					// 没有更多数据
					Toast.makeText(getContext(), getContext().getString(R.string.xlistview_nomoredata), Toast.LENGTH_SHORT).show();
				} else {
					// 隐藏
				}
			}
		});

	}

	public void setPullLoadEnable(boolean enable) {

		if (!mEnablePullLoad) {
			// mFooterView.hide();

			mFooterView.setVisibility(View.GONE);// hide();
			mFooterView.setOnClickListener(null);
			// make sure "pull up" don't show a line in bottom when listview
			// with one page
			// setFooterDividersEnabled(false);
		} else {
			mPullLoading = false;
			// mFooterView.show();
			mFooterView.setVisibility(View.VISIBLE);// show();
			mFooterView.setState2(XListViewFooter.STATE_NORMAL, "");
			// make sure "pull up" don't show a line in bottom when listview
			// with one page
			// setFooterDividersEnabled(true);
			// both "pull up" and "click" will invoke load more.
			// mFooterView.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// startLoadMore();
			// }
			// });
			mFooterView.setOnClickListener(null);
		}
	}

	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			resetHeaderHeight();

			setRefreshTime();
		}
	}

	/**
	 * stop load more, reset footer view.
	 */
	public void stopLoadMore() {
		if (mPullLoading == true) {
			mPullLoading = false;
			mFooterView.setState(XListViewFooter.STATE_NORMAL);
		}
	}

	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

	/**
	 * set last refresh time
	 * 
	 * @param time
	 */
	public void setRefreshTime() {
		mHeaderTimeView.setText(sdf.format(new Date()));
	}

	private void invokeOnScrolling() {
		if (mScrollListener instanceof OnXScrollListener) {
			OnXScrollListener l = (OnXScrollListener) mScrollListener;
			l.onXScrolling(this);
		}
	}

	private void updateHeaderHeight(float delta) {
		mHeaderView.setVisiableHeight((int) delta + mHeaderView.getVisiableHeight());
		if (mEnablePullRefresh && !mPullRefreshing) { // 未处于刷新状态，更新箭头
			if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
				mHeaderView.setState(XListViewHeader.STATE_READY);
			} else {
				mHeaderView.setState(XListViewHeader.STATE_NORMAL);
			}
		}
		setSelection(0); // scroll to top each time
	}

	/**
	 * reset header view's height.
	 */
	private void resetHeaderHeight() {
		int height = mHeaderView.getVisiableHeight();
		if (height == 0) // not visible.
			return;
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= mHeaderViewHeight) {
			return;
		}
		int finalHeight = 0; // default: scroll back to dismiss header.
		// is refreshing, just scroll back to show all the header.
		if (mPullRefreshing && height > mHeaderViewHeight) {
			finalHeight = mHeaderViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
		// trigger computeScroll
		invalidate();
	}

	private void updateFooterHeight(float delta) {
		int height = mFooterView.getBottomMargin() + (int) delta;
		if (mEnablePullLoad && !mPullLoading) {
			if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load
													// more.
				mFooterView.setState(XListViewFooter.STATE_READY);
			} else {
				mFooterView.setState(XListViewFooter.STATE_NORMAL);
			}
		}
		mFooterView.setBottomMargin(height);

		// setSelection(mTotalItemCount - 1); // scroll to bottom
	}

	private void resetFooterHeight() {
		int bottomMargin = mFooterView.getBottomMargin();
		if (bottomMargin > 0) {
			mScrollBack = SCROLLBACK_FOOTER;
			mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
			invalidate();
		}
	}

	private void startLoadMore() {
		mPullLoading = true;
		mFooterView.setState(XListViewFooter.STATE_LOADING);
		if (mListViewListener != null) {
			mListViewListener.onLoadMore();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if (mPullLoading || mPullRefreshing) {
			return super.onTouchEvent(ev);
		}
		if (mLastY == -1) {
			mLastY = ev.getRawY();
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaY = ev.getRawY() - mLastY;
			mLastY = ev.getRawY();
			if (getFirstVisiblePosition() == 0 && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
				// the first item is showing, header has shown or pull down.
				updateHeaderHeight(deltaY / OFFSET_RADIO);
				invokeOnScrolling();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1 && (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
				// last item, already pulled up or want to pull up.
				updateFooterHeight(-deltaY / OFFSET_RADIO);
			}
			break;
		default:
			mLastY = -1; // reset
			if (getFirstVisiblePosition() == 0) {
				// invoke refresh && !mPullRefreshing
				if (mEnablePullRefresh && mHeaderView.getVisiableHeight() > mHeaderViewHeight && !mPullRefreshing) {
					mPullRefreshing = true;
					mHeaderView.setState(XListViewHeader.STATE_REFRESHING);
					if (mListViewListener != null) {
						mListViewListener.onRefresh();
					} else {
						Log.i("mm", "hide3");
					}
				} else {
					// ---------------------
					if (getLastVisiblePosition() == mTotalItemCount - 1) {
						// invoke load more.
						if (mEnablePullLoad && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA && !mPullLoading) {
							startLoadMore();
						} else {
							// bukej
							Log.i("mm", "hide9");
							// mFooterView.setState(XListViewFooter.STATE_NORMAL,
							// "hide");
							mFooterView.setState2(XListViewFooter.STATE_NORMAL, mClick);
						}
						resetFooterHeight();
					} else {
						Log.i("mm", "hide10");
					}
					// -------------

				}
				resetHeaderHeight();
			} else if (getLastVisiblePosition() == mTotalItemCount - 1) {
				// invoke load more.
				if (mEnablePullLoad && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA && !mPullLoading) {
					startLoadMore();
				} else {
					// 判断是否可见
					Log.i("mm", "hide1");

					// mFooterView.setState(XListViewFooter.STATE_NORMAL,
					// "hide");
					mFooterView.setState2(XListViewFooter.STATE_NORMAL, mClick);

				}
				resetFooterHeight();
			} else {
				Log.i("mm", "hide2");
			}
			break;
		}
		return super.onTouchEvent(ev);

	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mHeaderView.setVisiableHeight(mScroller.getCurrY());
			} else {
				mFooterView.setBottomMargin(mScroller.getCurrY());
			}
			postInvalidate();
			invokeOnScrolling();
		}
		super.computeScroll();
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mScrollListener = l;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mScrollListener != null) {
			mScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// send to user's listener
		mTotalItemCount = totalItemCount;
		if (mScrollListener != null) {
			mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	public void setXListViewListener(IXListViewListener l) {
		mListViewListener = l;
	}

	/**
	 * you can listen ListView.OnScrollListener or this one. it will invoke
	 * onXScrolling when header/footer scroll back.
	 */
	public interface OnXScrollListener extends OnScrollListener {
		public void onXScrolling(View view);
	}

	/**
	 * implements this interface to get refresh/load more event.
	 */
	public interface IXListViewListener {
		public void onRefresh();

		public void onLoadMore();
	}

}