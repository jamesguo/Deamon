package com.testnative1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.testnative1.R;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Build;
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
				new Thread(new Runnable() {

					@Override
					public void run() {
						AssetManager assetManager = getAssets();
						try {
							String path = getFilesDir().getParentFile()
									.getAbsolutePath();
							File file = new File(path + File.separator
									+ "daemon");
							if (file.exists()) {
								file.delete();
							}
							InputStream sourceFile = assetManager
									.open("libdaemon.so");
							copyFile(sourceFile, file);
							startDaemon();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			}
		});
	}

	public static void copyFile(InputStream inputStream, File targetFile)
			throws IOException {
		// 新建文件输入流并对它进行缓冲
		BufferedInputStream inBuff = new BufferedInputStream(inputStream);

		// 新建文件输出流并对它进行缓冲
		FileOutputStream output = new FileOutputStream(targetFile);
		BufferedOutputStream outBuff = new BufferedOutputStream(output);

		// 缓冲数组
		byte[] b = new byte[1024 * 5];
		int len;
		while ((len = inBuff.read(b)) != -1) {
			outBuff.write(b, 0, len);
		}
		// 刷新此缓冲的输出流
		outBuff.flush();

		// 关闭流
		inBuff.close();
		outBuff.close();
		output.close();
		inputStream.close();
	}
	private void startDaemon() {
		String path = "/data/data/" + getPackageName();
		File file = new File(path + "/.lock");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		String cmd2 = "";
		if (Build.VERSION.SDK_INT < 17) {
			cmd2 = path + "/daemon " + getPackageName();
		} else {
			cmd2 = path + "/daemon " + getPackageName() + " "
					+ (getUserSerial() == null ? "0" : getUserSerial());
		}
		String cmd3 = "chmod 777 " + path + "/daemon ";
		RootCommand(cmd3);
		RootCommand(cmd2);
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

	private String getUserSerial() {
		Object userManager = getSystemService("user");
		if (userManager == null) {
			return null;
		}
		try {
			Method myUserHandleMethod = android.os.Process.class.getMethod(
					"myUserHandle", (Class<?>[]) null);
			Object myUserHandle = myUserHandleMethod.invoke(
					android.os.Process.class, (Object[]) null);
			Method getSerialNumberForUser = userManager.getClass().getMethod(
					"getSerialNumberForUser", myUserHandle.getClass());
			long userSerial = (Long) getSerialNumberForUser.invoke(userManager,
					myUserHandle);
			return String.valueOf(userSerial);
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		return null;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
