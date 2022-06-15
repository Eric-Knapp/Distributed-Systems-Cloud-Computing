package edu.stevens.cs549.ftpserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Stack;
import java.util.logging.Logger;

import edu.stevens.cs549.ftpinterface.IServer;

/**
 *
 * @author dduggan
 */
public class Server extends UnicastRemoteObject implements IServer {

	static final long serialVersionUID = 0L;

	public static Logger log = Logger.getLogger("edu.stevens.cs.cs549.ftpserver");

	/*
	 * For multi-homed hosts, must specify IP address on which to bind a server
	 * socket for file transfers. See the constructor for ServerSocket that allows
	 * an explicit IP address as one of its arguments.
	 */
	private InetAddress host;

	final static int BACKLOG_LENGTH = 5;

	/*
	 *********************************************************************************************
	 * Current working directory.
	 */
	static final int MAX_PATH_LEN = 1024;
	private Stack<String> cwd = new Stack<String>();

	/*
	 *********************************************************************************************
	 * Data connection.
	 */

	enum Mode {
		NONE, PASSIVE, ACTIVE
	};

	private Mode mode = Mode.NONE;

	/*
	 * If passive mode, remember the server socket.
	 */

	private ServerSocket dataChan = null;

	private int makePassive() throws IOException {
		dataChan = new ServerSocket(0, BACKLOG_LENGTH, host);
		mode = Mode.PASSIVE;
//    	return (InetSocketAddress)(dataChan.getLocalSocketAddress());
		return dataChan.getLocalPort();
	}

	/*
	 * If active mode, remember the client socket address.
	 */
	private InetSocketAddress clientSocket = null;

	private void makeActive(int clientPort) {
//    	clientSocket = s;
		try {
			clientSocket = InetSocketAddress.createUnresolved(getClientHost(), clientPort);
		} catch (ServerNotActiveException e) {
			throw new IllegalStateException("Make active", e);
		}
		mode = Mode.ACTIVE;
	}

	/*
	 **********************************************************************************************
	 */

	/*
	 * The server can be initialized to only provide subdirectories of a directory
	 * specified at start-up.
	 */
	private final String pathPrefix;

	public Server(InetAddress host, int port, String prefix) throws RemoteException {
		super(port);
		this.host = host;
		this.pathPrefix = prefix + "/";
		log.info("A client has bound to a server instance.");
	}

	public Server(InetAddress host, int port) throws RemoteException {
		this(host, port, "/");
	}

	private boolean valid(String s) {
		// File names should not contain "/".
		return (s.indexOf('/') < 0);
	}

	private static class GetThread implements Runnable {
		private ServerSocket dataChan = null;
		private InputStream file = null;

		public GetThread(ServerSocket s, InputStream f) {
			dataChan = s;
			file = f;
		}

		public void run() {
			
			Socket xfer;
			try {
				xfer = dataChan.accept();
				OutputStream os = xfer.getOutputStream();
				byte[] buffer = new byte[512];
				int nbytes = file.read(buffer, 0, 512);
				while (nbytes > 0) {
					os.write(buffer, 0, nbytes);
					nbytes = file.read(buffer, 0, 512);
				}
				file.close();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	 
	private static class PutThread implements Runnable {
		private ServerSocket dataChan = null;
		private OutputStream file = null;

		public PutThread(ServerSocket s, OutputStream f) {
			dataChan = s;
			file = f;
		}

		public void run() {
			
			Socket xfer;
			try {
				xfer = dataChan.accept();
				InputStream is = xfer.getInputStream();
				byte[] buffer = new byte[512];
				int nbytes = is.read(buffer, 0, 512);
				while (nbytes > 0) {
					file.write(buffer, 0, nbytes);
					nbytes = is.read(buffer, 0, 512);
				}
				file.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void get(String file) throws IOException, FileNotFoundException, RemoteException {
		if (!valid(file)) {
			throw new IOException("Bad file name: " + file);
		} else if (mode == Mode.ACTIVE) {
			log.info("Server connecting to client at address " + clientSocket.getHostName());
			Socket xfer = new Socket(clientSocket.getHostName(), clientSocket.getPort());
			/*
			 * TODO: connect to client socket to transfer file.
			 */
			InputStream input_Stream = new BufferedInputStream(new FileInputStream(path() + file));

			
			OutputStream output_Stream = xfer.getOutputStream();
			InputStream inputStream = input_Stream; 
			byte[] buffer = new byte[512];
			int nbytes = inputStream.read(buffer, 0, 512);
			while (nbytes > 0) {
				output_Stream.write(buffer, 0, nbytes);
				nbytes = inputStream.read(buffer, 0, 512);
			}
			inputStream.close();
			output_Stream.close();
			
		} else if (mode == Mode.PASSIVE) {
			InputStream f = new BufferedInputStream(new FileInputStream(path() + file));
			new Thread(new GetThread(dataChan, f)).start();
		}
	}

	public void put(String file) throws IOException, FileNotFoundException, RemoteException {
		/*
		 * TODO: Finish put (both ACTIVE and PASSIVE).
		 */
	
		
		if (!valid(file)) {
			throw new IOException("Bad file name: " + file);
		} else if (mode == Mode.ACTIVE) {
			log.info("Server connecting to client at address " + clientSocket.getHostName());
			Socket xfer = new Socket(clientSocket.getHostName(), clientSocket.getPort());
			/*
			 * TODO: connect to client socket to transfer file.
			 */
			OutputStream output_Stream = new BufferedOutputStream(new FileOutputStream(path() + file));

			
			InputStream input_Stream = xfer.getInputStream();
			OutputStream f = output_Stream; 
			byte[] buffer = new byte[512];
			int nbytes = input_Stream.read(buffer, 0, 512);
			while (nbytes > 0) {
				output_Stream.write(buffer, 0, nbytes);
				nbytes = input_Stream.read(buffer, 0, 512);
			}
			input_Stream.close();
			output_Stream.close();
			
		} else if (mode == Mode.PASSIVE) {
			OutputStream f = new BufferedOutputStream(new FileOutputStream(path() + file));
			new Thread(new PutThread(dataChan, f)).start();
		}
		
	}

	public String[] dir() throws RemoteException {
		// List the contents of the current directory.
		return new File(path()).list();
	}

	public void cd(String dir) throws IOException, RemoteException {
		// Change current working directory (".." is parent directory)
		if (!valid(dir)) {
			throw new IOException("Bad file name: " + dir);
		} else {
			if ("..".equals(dir)) {
				if (cwd.size() > 0)
					cwd.pop();
				else
					throw new IOException("Already in root directory!");
			} else if (".".equals(dir)) {
				;
			} else {
				File f = new File(path());
				if (!f.exists())
					throw new IOException("Directory does not exist: " + dir);
				else if (!f.isDirectory())
					throw new IOException("Not a directory: " + dir);
				else
					cwd.push(dir);
			}
		}
	}

	public String pwd() throws RemoteException {
		// List the current working directory.
		String p = "/";
		for (Enumeration<String> e = cwd.elements(); e.hasMoreElements();) {
			p = p + e.nextElement() + "/";
		}
		return p;
	}

	private String path() throws RemoteException {
		return pathPrefix + pwd();
	}

	public void port(int clientPort) {
		makeActive(clientPort);
	}

	public int pasv() throws IOException {
		return makePassive();
	}

}
