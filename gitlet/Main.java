package gitlet;

import static gitlet.Directories.REPO_DIR;
import static gitlet.Directories.STAGING_FILE;
import static gitlet.TextColors.*;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Connor Bernard
 */
public class Main {
    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        Repo repo;
        if (args.length < 1) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        repo = new Repo();
        if (!hasBeenInitialized() && !args[0].equals("init")) {
            System.out.println(ERROR_COLOR
                    + "Not in an initialized Gitlet directory." + RESET_COLOR);
            System.exit(0);
        } else if (!args[0].equals("init")) {
            repo.setStage(Utils.readObject(STAGING_FILE.file(),
                    StagingArea.class));
        }
        switch (args[0]) {
        case "init":
            if (hasBeenInitialized()) {
                System.out.println(WARNING_COLOR + "A Gitlet"
                        + " version-control system already exists in the"
                        + " current directory." + RESET_COLOR);
                System.exit(0);
            }
            repo.init();
            break;
        case "add":
            checkOperands(args.length, 2);
            repo.add(args[1]);
            break;
        case "commit":
            if (args.length == 1) {
                System.out.println(WARNING_COLOR + "Please enter a commit"
                        + " message." + RESET_COLOR);
                System.exit(0);
            }
            checkOperands(args.length, 2);
            repo.commit(args[1]);
            break;
        case "rm":
            checkOperands(args.length, 2);
            repo.rm(args[1]);
            break;
        case "log":
            repo.log();
            break;
        case "global-log":
            repo.globalLog();
            break;
        case "find":
            checkOperands(args.length, 2);
            repo.find(args[1]);
            break;
        case "status":
            repo.status();
            break;
        default:
            mainPart2(repo, args);
        }
    }
    /**
     * Second part of main because stylistically it is an absolute abomination
     * for a single method to be over 90 lines...
     *
     * @param repo repo passed in from first main method
     * @param args args passed in from first main method
     */
    public static void mainPart2(Repo repo, String... args) {
        switch (args[0]) {
        case "checkout":
            checkOperands(args.length, 2);
            if (args.length == 2) {
                repo.checkoutBranch(args[1]);
            } else if (args.length == 3) {
                repo.checkoutFile(args[2]);
            } else if (args[2].equals("--")) {
                repo.checkoutFileFromCommit(args[1], args[3]);
            } else {
                System.out.println(ERROR_COLOR + "Incorrect operands"
                        + RESET_COLOR);
                System.exit(0);
            }
            break;
        case "branch":
            checkOperands(args.length, 2);
            repo.branch(args[1]);
            break;
        case "rm-branch":
            checkOperands(args.length, 2);
            repo.rmBranch(args[1]);
            break;
        case "reset":
            checkOperands(args.length, 2);
            repo.reset(args[1]);
            break;
        case "merge":
            checkOperands(args.length, 2);
            repo.merge(args[1]);
            break;
        case "add-remote":
            checkOperands(args.length, 3);
            repo.addRemote(args[1], args[2]);
            break;
        case "rm-remote":
            checkOperands(args.length, 2);
            repo.rmRemote(args[1]);
            break;
        case "push":
            checkOperands(args.length, 3);
            repo.push(args[1], args[2]);
            break;
        case "fetch":
            checkOperands(args.length, 3);
            repo.fetch(args[1], args[2]);
            break;
        case "pull":
            checkOperands(args.length, 3);
            repo.pull(args[1], args[2]);
            break;
        default:
            System.out.println(ERROR_COLOR + "No command with that name"
                    + " exists." + RESET_COLOR);
            System.exit(0);
        }
    }
    /**
     * Checks to see if the repo has been initialized.
     *
     * @return whether the repo has been initialized
     */
    private static boolean hasBeenInitialized() {
        return REPO_DIR.file().isDirectory();
    }
    /**
     * Checks to see if sufficient operands were given on function call.
     *
     * @param numGivenOperands    number of operands provided
     * @param numRequiredOperands number of operands required
     */
    public static void checkOperands(int numGivenOperands,
                                     int numRequiredOperands) {
        if (numGivenOperands < numRequiredOperands) {
            System.out.println(ERROR_COLOR + "Incorrect operands"
                    + RESET_COLOR);
            System.exit(0);
        }
    }
}
