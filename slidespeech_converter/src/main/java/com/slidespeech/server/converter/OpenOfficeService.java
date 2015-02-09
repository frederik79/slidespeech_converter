package com.slidespeech.server.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;

import com.slidespeech.server.converter.openoffice.OpenOfficeConnection;
import com.slidespeech.server.converter.openoffice.OpenOfficeException;
import com.slidespeech.server.converter.openoffice.SocketOpenOfficeConnection;

public class OpenOfficeService {
	private static final String OOFFICE_BIN = "/opt/libreoffice3.5/program/soffice";
	private static final int OOFFICE_PORT = 18100;
	private OpenOfficeConnection _connection = null;

	public synchronized int getProcessID() {
		int pID = -1;

		String cmd = "/bin/pidof soffice.bin";
		// ProcessBuilder pb = new ProcessBuilder(cmd);
		try {
			Process process = Runtime.getRuntime().exec(cmd);

			process.waitFor();

			InputStream inputStream = process.getInputStream();

			String pidString = convertStreamToString(inputStream);

			if (pidString.length() > 0) {
				pidString = pidString.replace("\n", "");

				pidString = pidString.split(" ")[0];

				if (!pidString.equals("")) {
					pID = Integer.parseInt(pidString);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return pID;
	}

	synchronized String convertStreamToString(java.io.InputStream is) {
		try {
			return new java.util.Scanner(is).useDelimiter("\\A").next();
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
	}

	synchronized public boolean stopService() {

		boolean success = false;

		if (isRunning()) {
			int processID = getProcessID();

			String cmd = "/bin/kill " + processID;
			Process process;

			try {
				process = Runtime.getRuntime().exec(cmd);

				while (isRunning()) {
					Thread.sleep(100);
				}

				success = true;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			success = true;
		}
		return success;
	}

	synchronized public boolean startService() {
		boolean success = false;
		if (isRunning()) 
		{
			success = true;
		} else 
		{

			// TODO: obviously this is not thread safe!!!
			// -> has to be synchronized as soon as we want to have multiple
			// conversions at the same time
			// change ((HornetQConnectionFactory) cf).setConsumerWindowSize(0)
			// in Application Context to take more than one job at a time from
			// the queue
			System.out.println("soffice service not running - restarting");
			String[] cmd = new String[] {
					"bash",
					"-c",
					OOFFICE_BIN
							+ " -env:UserInstallation=file:///tmp/oo_jboss --headless --accept=\"socket,host=127.0.0.1,port="
							+ OOFFICE_PORT
							+ ";urp\" --nodefault --nofirststartwizard --nolockcheck --nologo --norestore &" };
			try {
				Process process = Runtime.getRuntime().exec(cmd);

				while (!isRunning()) {
					Thread.sleep(1000);
				}
				success = true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		return success;
	}

	private int getPort() {
		return OOFFICE_PORT;
	}

	public synchronized OpenOfficeConnection openConnection() throws OpenOfficeException {
		try {
			if (_connection == null || !_connection.isConnected())
				_connection = new SocketOpenOfficeConnection(getPort());
			_connection.connect();
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return _connection;

	}

	synchronized public void closeConnection() {
		// close the connection
		try {
			if (_connection != null && _connection.isConnected())
				_connection.disconnect();
		} catch (Exception e) {
			// TODO: handle exceptions
		}
	}

	synchronized public boolean isRunning() {
		boolean isRunning = false;

		int processID = getProcessID();
		if (processID > 0) {
			isRunning = true;
		}

		return isRunning;
	}

}