package us.kbase.testutils.controllers.mongo;

import static us.kbase.testutils.controllers.ControllerCommon.checkExe;
import static us.kbase.testutils.controllers.ControllerCommon.findFreePort;
import static us.kbase.testutils.controllers.ControllerCommon.makeTempDirs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.github.zafarkhaja.semver.Version;
import org.apache.commons.io.FileUtils;


/** Q&amp;D Utility to run a Mongo server for the purposes of testing from
 * Java.
 * @author gaprice@lbl.gov, sijiex@lbl.gov
 *
 */
public class MongoController {

	private final static String DATA_DIR = "data";

	private final static List<String> tempDirectories =
			new LinkedList<String>();
	static {
		tempDirectories.add(DATA_DIR);
	}

	private final static Version MONGO_DB_6_1 =
			Version.forIntegers(6,1);

	private final Path tempDir;

	private final Process mongo;

	private final int port;

	public MongoController(
			final String mongoExe,
			final Path rootTempDir)
			throws Exception {
		this(mongoExe, rootTempDir, false);
	}

	public MongoController(
			final String mongoExe,
			final Path rootTempDir,
			final boolean useWiredTiger)
			throws Exception {
		checkExe(mongoExe, "mongod server");
		tempDir = makeTempDirs(rootTempDir, "MongoController-", tempDirectories);
		port = findFreePort();
		Version dbVer = getMongoDBVer(mongoExe);
		List<String> command = getMongoServerStartCommand(mongoExe, useWiredTiger, dbVer);
		mongo = startProcess(command);
	}

	public int getServerPort() {
		return port;
	}

	public Path getTempDir() {
		return tempDir;
	}

	public void destroy(boolean deleteTempFiles) throws IOException, InterruptedException {
		if (mongo != null) {
			mongo.destroy();
		}
		if (tempDir != null && deleteTempFiles) {
			try {
				FileUtils.deleteDirectory(tempDir.toFile());
			} catch (IOException e) {
				// probably mongo deleted a file after the function listed it, race condition
				Thread.sleep(1000);
				// if it fails again just fail hard
				FileUtils.deleteDirectory(tempDir.toFile());
			}
		}
	}

	private static Version getMongoDBVer(final String mongoExe) throws IOException {

		// build MongoDB version check command
		List<String> command = new LinkedList<String>();
		command.addAll(Arrays.asList(mongoExe, "--version"));

		// start MongoDB version check process
		ProcessBuilder checkVerPb = new ProcessBuilder(command);
		Process checkVerProcess  = checkVerPb.start();

		// parse mongod --version output string
		String dbVer = new BufferedReader(
				new InputStreamReader(checkVerProcess.getInputStream()))
				.lines()
				.collect(Collectors.joining(" "))
				.split(" ")[2].substring(1);

		System.out.println("MongoDB version: " + dbVer);
		checkVerProcess.destroy();
		return Version.valueOf(dbVer);
	}

	private List<String> getMongoServerStartCommand(final String mongoExe,
													final boolean useWiredTiger,
													final Version dbVer) {
		List<String> command = new LinkedList<String>();
		command.addAll(Arrays.asList(mongoExe, "--port", "" + port,
				"--dbpath", tempDir.resolve(DATA_DIR).toString()));

		// Starting in MongoDB 6.1, journaling is always enabled.
		// As a result, MongoDB removes the storage.journal.enabled option
		// and the corresponding --journal and --nojournal command-line options.
		// https://www.mongodb.com/docs/manual/release-notes/6.1/#changes-to-journaling
		if (dbVer.lessThan(MONGO_DB_6_1)) {
			command.addAll(Arrays.asList("--nojournal"));
		}
		if (useWiredTiger) {
			command.addAll(Arrays.asList("--storageEngine", "wiredTiger"));
		}
		return command;
	}

	private Process startProcess(List<String> command) throws Exception {
		ProcessBuilder servpb = new ProcessBuilder(command)
				.redirectErrorStream(true)
				.redirectOutput(getTempDir().resolve("mongo.log").toFile());

		Process mongoProcess = servpb.start();
		Thread.sleep(1000); //wait for server to start up
		return mongoProcess;
	}

	public static void main(String[] args) throws Exception {
		MongoController ac = new MongoController(
				"/home/crushingismybusiness/mongo/3.6.12/bin/mongod",
				Paths.get("workspacetesttemp"));
		System.out.println(ac.getServerPort());
		System.out.println(ac.getTempDir());
		Scanner reader = new Scanner(System.in);
		System.out.println("any char to shut down");
		//get user input for a
		reader.next();
		ac.destroy(true);
		reader.close();
	}

}
