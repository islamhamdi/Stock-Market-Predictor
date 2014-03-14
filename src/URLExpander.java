import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import StockTwitsCreator.MyStatus;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.internal.http.HttpClient;

public class URLExpander {
	private static Queue<Status> readQueue;
	private static Queue<Status> writeQueue;
	private static LRU<String, String> urlMap;
	private static File[] statusFiles;
	private static String inDir, outDir;

	private static AtomicInteger finishedURLs;
	private static AtomicInteger repeatedURLs;
	private static AtomicInteger totalReadStatus;
	private static AtomicInteger finishedStatus;
	private static AtomicBoolean finishedReadingAllFiles;

	private static AtomicBoolean finishCurrentFile;
	private static AtomicInteger currentFileSize;
	private static AtomicInteger activeThreads;
	private static String currentFileName;

	private static final boolean enableDebug = false;

	public URLExpander(String inputDir, String outputDir) {

		// initialize buffers and map
		readQueue = new LinkedList<Status>();
		writeQueue = new LinkedList<Status>();
		urlMap = new LRUcache<String, String>(10000);

		// initialize directories
		inDir = inputDir;
		outDir = outputDir;
		File dir = new File(inDir);
		statusFiles = dir.listFiles();

		// initialize Atomic Variables
		activeThreads = new AtomicInteger(Global.THREAD_COUNT + 3); // +reader,writer,printer
		finishedURLs = new AtomicInteger(0);
		repeatedURLs = new AtomicInteger(0);
		totalReadStatus = new AtomicInteger(0);
		finishedStatus = new AtomicInteger(0);
		finishedReadingAllFiles = new AtomicBoolean(false);

		finishCurrentFile = new AtomicBoolean(false);
		currentFileSize = new AtomicInteger();
		currentFileName = "";
	}

	public void startURLExpander() throws InterruptedException {
		// start reader thread to read files
		new Thread(new ReaderThread()).start();

		// wait 2 seconds to read data
		Thread.sleep(1000 * 2);

		// start printer
		new Thread(new PrintURLsCountThread()).start();

		// URL Getter threads
		for (int i = 0; i < Global.THREAD_COUNT; i++)
			new Thread(new URLGetterThread(i)).start();

		// Writer thread
		new Thread(new WriterThread()).start();
	}

	public boolean isTerminated() {
		return activeThreads.get() == 0;
	}

	private static Status getReadyStatus() {
		boolean empty;
		synchronized (readQueue) {
			empty = readQueue.isEmpty();
		}

		while (empty) {
			if (finishedReadingAllFiles.get())
				return null;

			try {
				// sleep for 2 seconds till reading more status
				if (enableDebug)
					System.out.println("Sleep for 2 seconds\n");
				Thread.sleep(1000 * 2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			synchronized (readQueue) {
				empty = readQueue.isEmpty();
			}
		}
		synchronized (readQueue) {
			return readQueue.poll();
		}
	}

	private static void addFinishedStatus(Status s) {
		synchronized (writeQueue) {
			writeQueue.add(s);
		}
	}

	private static class ReaderThread implements Runnable {
		@Override
		public void run() {
			if (enableDebug)
				System.out.println("READER THREAD STARTS");
			for (File f : statusFiles)
				if (f.isFile()) {
					while (finishCurrentFile.get()) {
						// wait 2 seconds for writer thread to finish writing
						// last file
						try {
							Thread.sleep(1000 * 2);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					currentFileName = f.getName();

					if (enableDebug)
						System.out.println("\nRead File : " + f.getName()
								+ "\n");

					// open status file
					FileInputStream fout = null;
					ObjectInputStream oos = null;
					try {
						fout = new FileInputStream(f.getAbsolutePath());
						oos = new ObjectInputStream(fout);
					} catch (IOException e1) {
						e1.printStackTrace();
					}

					int statusCounter = 0;
					// read status file
					while (true) {
						try {
							Status s = (Status) oos.readObject();
							totalReadStatus.incrementAndGet();
							statusCounter++;

							synchronized (readQueue) {
								readQueue.add(s);
							}
						} catch (Exception e) {
							if (enableDebug)
								System.out.println("  FINISHED File : "
										+ f.getName());
							break;
						}
					}

					// close status file
					try {
						oos.close();
						fout.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					// set finish current file = true (writer thread will start)
					currentFileSize.set(statusCounter);
					finishCurrentFile.set(true);
				}

			if (enableDebug)
				System.out.println("\n======= Reader Thread End =======\n");
			finishedReadingAllFiles.set(true);
			activeThreads.decrementAndGet();
		}
	}

	private static class URLGetterThread implements Runnable {
		int threadIdx;

		public URLGetterThread(int idx) {
			threadIdx = idx;
		}

		public String expandUrl(String shortenedUrl) throws IOException {
			URL url = new URL(shortenedUrl);
			// open connection
			HttpURLConnection httpURLConnection = (HttpURLConnection) url
					.openConnection(Proxy.NO_PROXY);
			httpURLConnection.setRequestMethod("GET");

			// stop following browser redirect
			// httpURLConnection.setInstanceFollowRedirects(true);
			httpURLConnection.getHeaderFields();

			// httpURLConnection.getResponseCode();
			// extract location header containing the actual destination URL
			// httpURLConnection.disconnect();

			return httpURLConnection.getURL().toString();
		}

		@Override
		public void run() {
			if (enableDebug)
				System.out.printf("Thread %d starts\n", threadIdx);
			while (true) {
				Status urlStatus = getReadyStatus();
				if (urlStatus == null) { // finished "no more URLs"
					if (enableDebug)
						System.out.println("NULL");
					break;
				}

				URLEntity[] urls = urlStatus.getURLEntities();
				String[] newURLs = new String[urls.length];

				for (int i = 0; i < urls.length; i++) {
					String url = urls[i].getText();
					finishedURLs.incrementAndGet();

					// check if url already in urlMap
					String tmp;
					if ((tmp = urlMap.get(url)) != null) {
						repeatedURLs.incrementAndGet();
						newURLs[i] = tmp;
					} else {
						// if not found fetch it by http connection
						try {
							String source = expandUrl(url);
							urlMap.add(url, source);
							newURLs[i] = source;
						} catch (IOException e) {
							// newURLs[i] = url;
							// System.out.println("__");
							System.out.println(url);
							e.printStackTrace();
						}
					}
				}
				try {
					Status newStatus = new MyStatus(urlStatus, newURLs);
					// add newStatus to finished queue
					addFinishedStatus(newStatus);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (enableDebug)
				System.out.printf("URL Getter Thread : %d ends\n", threadIdx);
			activeThreads.decrementAndGet();
		}
	}

	private static class WriterThread implements Runnable {
		@Override
		public void run() {
			while (true) {

				int readBufferSize, writeBufferSize;
				synchronized (readQueue) {
					readBufferSize = readQueue.size();
				}
				synchronized (writeQueue) {
					writeBufferSize = writeQueue.size();
				}

				// empty list (finished)
				if (writeBufferSize == 0 && readBufferSize == 0
						&& finishedReadingAllFiles.get())
					break;

				while (!finishCurrentFile.get()) {
					// sleep 2 seconds for reader thread to complete file
					try {
						Thread.sleep(1000 * 2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				int writeSize = -1;
				synchronized (writeQueue) {
					writeSize = writeQueue.size();
				}

				if (writeSize == (int) currentFileSize.get()) {
					finishedStatus.addAndGet(writeSize);
					writeList();
					finishCurrentFile.set(false);
				} else {
					// sleep for 2 seconds till more status added to writer
					// buffer
					try {
						Thread.sleep(1000 * 2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			if (enableDebug)
				System.out.println("======= Writer Thread End =======");
			activeThreads.decrementAndGet();
		}

		private void writeList() {
			try {
				if (enableDebug)
					System.out.println("\nWriter Thread : Write new File : "
							+ currentFileName + "\n");
				FileOutputStream fout = new FileOutputStream(outDir
						+ currentFileName);
				ObjectOutputStream oos = new ObjectOutputStream(fout);

				synchronized (writeQueue) {
					while (!writeQueue.isEmpty()) {
						oos.writeObject(writeQueue.poll());
					}
				}

				oos.close();
				fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static class PrintURLsCountThread implements Runnable {

		@Override
		public void run() {
			int last = 0, lastRepeated = 0, minute = 0;
			while (true) {
				int rSize, wSize;
				synchronized (readQueue) {
					rSize = readQueue.size();
				}

				synchronized (writeQueue) {
					wSize = writeQueue.size();
				}

				int finishedurls, repeated, totalRead, finishedStat;
				finishedurls = finishedURLs.get();
				repeated = repeatedURLs.get();
				totalRead = totalReadStatus.get();
				finishedStat = finishedStatus.get();

				if (enableDebug) {
					System.out
							.printf("\nminute : %d, URLs : %d, Repeated : %d, Total : %d, Total Repeated : %d, Average : %d\n",
									minute++, finishedurls - last, repeated
											- lastRepeated, finishedurls,
									repeated, (finishedurls / minute));

					System.out
							.printf("Total readed tweets : %d, Total written tweets : %d\n",
									totalRead, finishedStat);
					System.out.printf(
							"read buffer : %d, write buffer : %d\n\n", rSize,
							wSize);
				}

				last = finishedurls;
				lastRepeated = repeated;

				if (rSize == 0 && wSize == 0 && finishedReadingAllFiles.get()) {
					if (enableDebug)
						System.out.println("\n=== FINISHED ===\n");
					break;
				}

				try {
					// sleep for 2 seconds
					Thread.sleep(1000 * 2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (enableDebug)
				System.out.println("\n=== Print URLs Thread End ===\n");
			activeThreads.decrementAndGet();
		}
	}

	private static class LRUcache<S extends Comparable<S>, T> implements
			LRU<S, T> {
		private int size;
		private TreeMap<S, Node> tr;
		private linkedList ls;
		private final Object lock = new Object();

		public LRUcache(int capacity) {
			size = capacity;
			ls = new linkedList();
			tr = new TreeMap<S, Node>();
		}

		@Override
		public void setSize(int size) {
			synchronized (lock) {
				this.size = size;
			}
		}

		@Override
		public Entry<S, T> add(S key, T value) {
			synchronized (lock) {
				if (tr.containsKey(key)) {
					Node n = ls.remove(tr.get(key));
					ls.add(n);
					n.value = value;
					return null;
				} else {
					Node n = new Node(key, value);
					ls.add(n);
					tr.put(key, n);
					if (tr.size() > size) {
						n = tr.remove(ls.removeLast().key);
						return new AbstractMap.SimpleEntry<S, T>(n.key, n.value);
					} else {
						return null;
					}
				}
			}

		}

		@Override
		public void remove(S key) {
			synchronized (lock) {
				ls.remove(tr.get(key));
				tr.remove(key);
			}
		}

		@Override
		public T get(S key) {
			synchronized (lock) {
				if (tr.containsKey(key)) {
					Node n = ls.remove(tr.get(key));
					ls.add(n);
					return n.value;
				} else {
					// not found
					return null;
				}
			}
		}

		@Override
		public LinkedList<Entry<S, T>> getEntrySet() {
			synchronized (lock) {
				LinkedList<Entry<S, T>> lst = new LinkedList<>();
				Node n = ls.head;
				while (n != null) {
					lst.add(new AbstractMap.SimpleEntry<S, T>(n.key, n.value));
					n = n.next;
				}
				return lst;
			}
		}

		class linkedList {
			Node head;
			Node tail;

			public void add(Node n) {
				if (head == null) {
					head = tail = n;
				} else {
					tail.next = n;
					n.prev = tail;
					tail = tail.next;
					n.next = null;
				}
			}

			public Node remove(Node n) {
				if (n == tail && n == head) {
					tail = head = null;
				} else if (n == tail) {
					tail.prev.next = null;
					tail = tail.prev;
				} else if (n == head) {
					head = head.next;
				} else {
					n.prev.next = n.next;
					n.next.prev = n.prev;
				}
				return n;
			}

			public Node removeLast() {
				Node tmp = head;
				head = head.next;
				return tmp;
			}
		}

		class Node {
			Node prev;
			Node next;
			S key;
			T value;

			public Node(S k, T v) {
				key = k;
				value = v;
			}
		}

	}

	interface LRU<S extends Comparable<S>, T> {
		public void setSize(int size);

		/**
		 * add key and value
		 * 
		 * @param key
		 * @param value
		 * @return null if LRU size less than the max size, else return least
		 *         recently used object and this object should be written to
		 *         file
		 */
		public Entry<S, T> add(S key, T value);

		/**
		 * remove object from LRU set
		 * 
		 * @param key
		 */
		public void remove(S key);

		/**
		 * get key's Entry
		 * 
		 * @param key
		 * @return object or null if it doesn't exist
		 */
		public T get(S key);

		/**
		 * return entry set
		 * 
		 * @return
		 */
		public LinkedList<Entry<S, T>> getEntrySet();
	}
}