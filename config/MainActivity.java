package com.testnative;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.text).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String path = "/data/data/1111111" + getPackageName();
				String cmd1 = path + "/lib/libdaemon.so";
				String cmd2 = path + "/daemon";
				String cmd3 = "chmod 777 " + cmd2;
				String cmd4 = "dd if=" + cmd1 + " of=" + cmd2;
				RootCommand(cmd4); // 拷贝lib/libtest.so到上一层目录,同时命名为test.
				RootCommand(cmd3); // 改变test的属性,让其变为可执行
				RootCommand(cmd2); // 执行test程序.
			}
		});
	}

	public boolean RootCommand(String command) {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("sh"); // 获得shell.
			DataInputStream inputStream = new DataInputStream(
					process.getInputStream());
			DataOutputStream outputStream = new DataOutputStream(
					process.getOutputStream());

			outputStream.writeBytes("cd /data/data/" + getPackageName() + "\n"); // 保证在command在自己的数据目录里执行,才有权限写文件到当前目录

			outputStream.writeBytes(command + " &\n"); // 让程序在后台运行，前台马上返回
			outputStream.writeBytes("exit\n");
			outputStream.flush();
			process.waitFor();

			byte[] buffer = new byte[inputStream.available()];
			inputStream.read(buffer);
			String s = new String(buffer);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
