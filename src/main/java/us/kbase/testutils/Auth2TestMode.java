package us.kbase.testutils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

public class Auth2TestMode {
	
	public static void createAuthUser(
			final URL authURL,
			final String userName,
			final String displayName)
			throws Exception {
		final URL target = new URL(authURL.toString() + "/api/V2/testmodeonly/user");
		final HttpURLConnection conn = getPOSTConnection(target);

		final DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
		writer.writeBytes(new ObjectMapper().writeValueAsString(ImmutableMap.of(
				"user", userName,
				"display", displayName)));
		writer.flush();
		writer.close();

		checkForError(conn);
	}

	private static HttpURLConnection getPOSTConnection(final URL target) throws Exception {
		return getConnection("POST", target);
	}
	
	private static HttpURLConnection getPUTConnection(final URL target) throws Exception {
		return getConnection("PUT", target);
	}
	
	private static HttpURLConnection getConnection(final String verb, final URL target)
			throws Exception {
		final HttpURLConnection conn = (HttpURLConnection) target.openConnection();
		conn.setRequestMethod(verb);
		conn.setRequestProperty("content-type", "application/json");
		conn.setRequestProperty("accept", "application/json");
		conn.setDoOutput(true);
		return conn;
	}

	private static void checkForError(final HttpURLConnection conn) throws IOException {
		final int rescode = conn.getResponseCode();
		if (rescode < 200 || rescode >= 300) {
			System.out.println("Response code: " + rescode);
			String err = IOUtils.toString(conn.getErrorStream()); 
			System.out.println(err);
			if (err.length() > 200) {
				err = err.substring(0, 200);
			}
			throw new TestException(err);
		}
	}

	public static String createLoginToken(final URL authURL, String user) throws Exception {
		final URL target = new URL(authURL.toString() + "/api/V2/testmodeonly/token");
		final HttpURLConnection conn = getPOSTConnection(target);

		final DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
		writer.writeBytes(new ObjectMapper().writeValueAsString(ImmutableMap.of(
				"user", user,
				"type", "Login")));
		writer.flush();
		writer.close();

		checkForError(conn);
		final String out = IOUtils.toString(conn.getInputStream());
		@SuppressWarnings("unchecked")
		final Map<String, Object> resp = new ObjectMapper().readValue(out, Map.class);
		return (String) resp.get("token");
	}
	
	public static void createCustomRole(
			final URL authURL,
			final String role,
			final String description)
			throws Exception {
		final URL target = new URL(authURL.toString() + "/api/V2/testmodeonly/customroles");
		final HttpURLConnection conn = getPOSTConnection(target);

		final DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
		writer.writeBytes(new ObjectMapper().writeValueAsString(ImmutableMap.of(
				"id", role,
				"desc", description)));
		writer.flush();
		writer.close();

		checkForError(conn);
	}
	
	// will zero out standard roles, which don't do much in test mode
	public static void setUserRoles(
			final URL authURL,
			final String user,
			final List<String> customRoles)
			throws Exception {
		final URL target = new URL(authURL.toString() + "/api/V2/testmodeonly/userroles");
		final HttpURLConnection conn = getPUTConnection(target);
		
		final DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
		writer.writeBytes(new ObjectMapper().writeValueAsString(ImmutableMap.of(
				"user", user,
				"customroles", customRoles)));
		writer.flush();
		writer.close();

		checkForError(conn);
	}

}
