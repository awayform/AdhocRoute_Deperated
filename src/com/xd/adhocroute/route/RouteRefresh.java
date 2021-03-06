package com.xd.adhocroute.route;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;

import com.xd.adhocroute.data.OlsrDataDump;

/**
 * 封装了获取路由相关信息的方法，通过回调接口更新数据
 * @author qhyuan1992
 *
 */
public class RouteRefresh {
	protected static final int REFRESH_SUCCESS = 0X01;
	public static final int REFRESH_UNSTARTED = 0x03;
	private Callback refreshListener;
	private ExecutorService exec = Executors.newSingleThreadExecutor();
	
	private Handler handler = new MyHandler(this);
	
	public static class MyHandler extends Handler{
		WeakReference<RouteRefresh> outerReference;
		
		public MyHandler(RouteRefresh outer) {
			outerReference = new WeakReference<RouteRefresh>(outer);;
		}
		public void handleMessage(android.os.Message msg) {
			RouteRefresh routeRefresh = outerReference.get();
			if (routeRefresh == null) return;
			switch (msg.what) {
			case REFRESH_SUCCESS:
				routeRefresh.refreshListener.onSuccess((OlsrDataDump) msg.obj);
				break;
			case REFRESH_UNSTARTED:
				routeRefresh.refreshListener.onException(REFRESH_UNSTARTED);
				break;
			default:
				break;
			}
		}
	}
	
	public void refreshRoute(Callback refreshListener) {
		this.refreshListener = refreshListener;
		exec.execute(new RefreshRunnable());
	}

	private class RefreshRunnable implements Runnable {

		public void run() {
			JsonInfo jsonInfo = new JsonInfo();
			OlsrDataDump olsrDataDump = jsonInfo.all();
			if (olsrDataDump.getRaw().equals("")) {
				// 路由未开启
				handler.sendEmptyMessage(REFRESH_UNSTARTED);
			}
			else {
				// 开启了路由可以看到信息
				handler.obtainMessage(REFRESH_SUCCESS, olsrDataDump).sendToTarget();
			}
		}
	}

	public interface Callback {
		public void onSuccess(OlsrDataDump olsrDataDump);
		public void onException(int exception);
	}

}
