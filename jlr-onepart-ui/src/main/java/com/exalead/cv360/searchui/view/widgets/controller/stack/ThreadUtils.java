package com.exalead.cv360.searchui.view.widgets.controller.stack;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ThreadUtils {

	private final static int MAX_FRAMES = 100;

	public static int getNumberOfThreads() {
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();

		return threads.getThreadCount();
	}

	public static String getThreadDumpString() {
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();

		StringBuilder sb = new StringBuilder();

		sb.append(String.format("Thread dump at: %1$tF %1$tT", new java.util.Date()));
		sb.append("\n\n");

		long[] deadLockedArray = threads.findDeadlockedThreads();
		Set<Long> deadlocks = new HashSet<Long>();
		if (deadLockedArray != null) {
			for (long i : deadLockedArray) {
				deadlocks.add(i);
			}
		}

		Map<Long, Thread> threadMap = new HashMap<Long, Thread>();
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			threadMap.put(t.getId(), t);
		}

		ThreadInfo[] infos = threads.dumpAllThreads(true, true);
		for (ThreadInfo info : infos) {
			StringBuilder threadMetaData = new StringBuilder();
			Thread thread = threadMap.get(info.getThreadId());
			if (thread != null) {
				threadMetaData.append("(");
				threadMetaData.append(thread.isDaemon() ? "daemon " : "");
				threadMetaData.append(thread.isInterrupted() ? "interrupted " : "");
				threadMetaData.append("prio=" + thread.getPriority());
				threadMetaData.append(")");
			}
			String s = toString(info).trim();
			s = s.replaceFirst("Id=\\d+", "$0 " + threadMetaData);
			sb.append(s);
			sb.append("\n");

			if (deadlocks.contains(info.getThreadId())) {
				sb.append(" ** Deadlocked **");
				sb.append("\n");
			}

			sb.append("\n");
		}
		return sb.toString();

	}

	/**
	 * Copier coller du toString de ThreadInfo en modifiant le MAX_FRAMES
	 * 
	 * @param info
	 * @return
	 */
	public static String toString(final ThreadInfo info) {
		StringBuffer sb = new StringBuffer("\"" + info.getThreadName() + "\"" + " Id=" + info.getThreadId() + " "
				+ info.getThreadState());
		if (info.getLockName() != null) {
			sb.append(" on " + info.getLockName());
		}
		if (info.getLockOwnerName() != null) {
			sb.append(" owned by \"" + info.getLockOwnerName() + "\" Id=" + info.getLockOwnerId());
		}
		if (info.isSuspended()) {
			sb.append(" (suspended)");
		}
		if (info.isInNative()) {
			sb.append(" (in native)");
		}
		sb.append('\n');
		int i = 0;
		for (; i < info.getStackTrace().length && i < MAX_FRAMES; i++) {
			StackTraceElement ste = info.getStackTrace()[i];
			sb.append("\tat " + ste.toString());
			sb.append('\n');
			if (i == 0 && info.getLockInfo() != null) {
				Thread.State ts = info.getThreadState();
				switch (ts) {
				case BLOCKED:
					sb.append("\t-  blocked on " + info.getLockInfo());
					sb.append('\n');
					break;
				case WAITING:
					sb.append("\t-  waiting on " + info.getLockInfo());
					sb.append('\n');
					break;
				case TIMED_WAITING:
					sb.append("\t-  waiting on " + info.getLockInfo());
					sb.append('\n');
					break;
				default:
				}
			}

			for (MonitorInfo mi : info.getLockedMonitors()) {
				if (mi.getLockedStackDepth() == i) {
					sb.append("\t-  locked " + mi);
					sb.append('\n');
				}
			}
		}
		if (i < info.getStackTrace().length) {
			sb.append("\t...");
			sb.append('\n');
		}

		LockInfo[] locks = info.getLockedSynchronizers();
		if (locks.length > 0) {
			sb.append("\n\tNumber of locked synchronizers = " + locks.length);
			sb.append('\n');
			for (LockInfo li : locks) {
				sb.append("\t- " + li);
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

}
