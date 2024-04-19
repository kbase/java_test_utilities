package us.kbase.testutils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;

public class TestCommon {
	
	public static final String LONG101;
	public static final String LONG1001;
	static {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 100; i++) {
			sb.append("a");
		}
		final String s100 = sb.toString();
		final StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			sb2.append(s100);
		}
		LONG101 = s100 + "a";
		LONG1001 = sb2.toString() + "a";
	}
	
	public static void stfuLoggers() {
		((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME))
			.setLevel(ch.qos.logback.classic.Level.OFF);
		java.util.logging.Logger.getLogger("com.mongodb")
			.setLevel(java.util.logging.Level.OFF);
	}
	
	public static void printJava() {
		System.out.println("Java: " +
				System.getProperty("java.runtime.version"));
	}

	public static void assertExceptionCorrect(
			final Throwable got,
			final Throwable expected) {
		assertThat("incorrect exception. trace:\n" +
				ExceptionUtils.getStackTrace(got),
				got.getLocalizedMessage(),
				is(expected.getLocalizedMessage()));
		assertThat("incorrect exception type", got, instanceOf(expected.getClass()));
	}
	
	//http://quirkygba.blogspot.com/2009/11/setting-environment-variables-in-java.html
	@SuppressWarnings("unchecked")
	public static Map<String, String> getenv()
			throws NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		Map<String, String> unmodifiable = System.getenv();
		Class<?> cu = unmodifiable.getClass();
		Field m = cu.getDeclaredField("m");
		m.setAccessible(true);
		return (Map<String, String>) m.get(unmodifiable);
	}
	
	@SafeVarargs
	public static <T> Set<T> set(T... objects) {
		return new HashSet<T>(Arrays.asList(objects));
	}
	
	public static Instant inst(final long epoch) {
		return Instant.ofEpochMilli(epoch);
	}
	
	public static class LogEvent {
		
		public final Level level;
		public final String message;
		public final String className;
		public final Throwable ex;
		
		public LogEvent(final Level level, final String message, final Class<?> clazz) {
			this.level = level;
			this.message = message;
			this.className = clazz.getName();
			ex = null;
		}

		public LogEvent(final Level level, final String message, final String className) {
			this.level = level;
			this.message = message;
			this.className = className;
			ex = null;
		}
		
		public LogEvent(
				final Level level,
				final String message,
				final Class<?> clazz,
				final Throwable ex) {
			this.level = level;
			this.message = message;
			this.className = clazz.getName();
			this.ex = ex;
		}
		
		public LogEvent(
				final Level level,
				final String message,
				final String className,
				final Throwable ex) {
			this.level = level;
			this.message = message;
			this.className = className;
			this.ex = ex;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LogEvent [level=");
			builder.append(level);
			builder.append(", message=");
			builder.append(message);
			builder.append(", className=");
			builder.append(className);
			builder.append(", ex=");
			builder.append(ex);
			builder.append("]");
			return builder.toString();
		}
	}
	
	public static List<ILoggingEvent> setUpSLF4JTestLoggerAppender(final String package_) {
		final Logger authRootLogger = (Logger) LoggerFactory.getLogger(package_);
		authRootLogger.setAdditive(false);
		authRootLogger.setLevel(Level.ALL);
		final List<ILoggingEvent> logEvents = new LinkedList<>();
		final AppenderBase<ILoggingEvent> appender =
				new AppenderBase<ILoggingEvent>() {
			@Override
			protected void append(final ILoggingEvent event) {
				logEvents.add(event);
			}
		};
		appender.start();
		authRootLogger.addAppender(appender);
		return logEvents;
	}
	
	public static void assertLogEventsCorrect(
			final List<ILoggingEvent> logEvents,
			final LogEvent... expectedlogEvents) {
		
		assertThat("incorrect log event count for list: " + logEvents, logEvents.size(),
				is(expectedlogEvents.length));
		final Iterator<ILoggingEvent> iter = logEvents.iterator();
		for (final LogEvent le: expectedlogEvents) {
			final ILoggingEvent e = iter.next();
			assertThat("incorrect log level", e.getLevel(), is(le.level));
			assertThat("incorrect originating class", e.getLoggerName(), is(le.className));
			assertThat("incorrect message", e.getFormattedMessage(), is(le.message));
			final IThrowableProxy err = e.getThrowableProxy();
			if (err != null) {
				if (le.ex == null) {
					fail(String.format("Logged exception where none was expected: %s %s %s",
							err.getClassName(), err.getMessage(), le));
				} else {
					assertThat("incorrect error class for event " + le, err.getClassName(),
							is(le.ex.getClass().getName()));
					assertThat("incorrect error message for event " + le, err.getMessage(),
							is(le.ex.getMessage()));
				}
			} else if (le.ex != null) { 
				fail("Expected exception but none was logged: " + le);
			}
		}
	}
	

}
