package nodebox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Submits a DMG to Apple's notary service and staples the resulting ticket.
 *
 * <p>Uses {@code notarytool} (altool's notarization service was retired by Apple in
 * November 2023). Credentials are read from the environment so the same command runs
 * locally and in CI:
 * <ul>
 *     <li>{@code APPLE_ID} – Apple ID email</li>
 *     <li>{@code APPLE_APP_SPECIFIC_PASSWORD} – app-specific password for that Apple ID</li>
 *     <li>{@code APPLE_TEAM_ID} – Developer Team ID</li>
 * </ul>
 */
public class Notarizer {

    public static void main(String[] args) throws IOException, InterruptedException {
        File dmgFile = new File(args[0]);
        if (!dmgFile.exists()) {
            throw new RuntimeException("File " + args[0] + " does not exist.");
        }

        String appleId = requireEnv("APPLE_ID");
        String password = requireEnv("APPLE_APP_SPECIFIC_PASSWORD");
        String teamId = requireEnv("APPLE_TEAM_ID");

        // submit --wait blocks until the notary service finishes and exits non-zero on rejection.
        run("xcrun", "notarytool", "submit", dmgFile.getAbsolutePath(),
                "--apple-id", appleId,
                "--password", password,
                "--team-id", teamId,
                "--wait");

        // Staple the ticket so the DMG validates offline.
        run("xcrun", "stapler", "staple", dmgFile.getAbsolutePath());
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Missing required environment variable: " + name);
        }
        return value;
    }

    private static void run(String... command) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>(List.of(command));
        System.out.println("command = " + cmd);
        int exitCode = new ProcessBuilder(cmd).inheritIO().start().waitFor();
        if (exitCode != 0) {
            throw new RuntimeException(cmd.get(1) + " " + cmd.get(2) + " failed (exit " + exitCode + ").");
        }
    }
}
