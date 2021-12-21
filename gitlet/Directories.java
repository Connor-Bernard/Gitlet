package gitlet;

import java.io.File;

/**
 * Enumeration associated with all directories used in this program.
 * @author Connor bernard
 */
public enum Directories {
    /**
     * All directories.
     */
    USER_DIR(System.getProperty("user.dir")),
    REPO_DIR(USER_DIR + "/.gitlet"),
    BLOBS_DIR(REPO_DIR + "/Blobs"),
    COMMITS_DIR(REPO_DIR + "/Commits"),
    BRANCHES_DIR(REPO_DIR + "/Branches"),
    REMOTE_DIR(USER_DIR + "/Remotes"),
    STAGING_FILE(REPO_DIR + "/Stage"),
    HEAD_FILE(REPO_DIR + "/HEAD"),
    CURRENT_COMMIT(REPO_DIR + "/CurrentCommit"),;
    /**
     * String value associated with each directory.
     */
    private final String dir;
    /**
     * File object associated with each directory.
     */
    private final File file;
    /**
     * Basic constructor for a directory.
     * @param directory String of each directory
     */
    Directories(String directory) {
        dir = directory;
        file = new File(directory);
    }
    /**
     * Getter method for the file representation of this enum.
     * @return file representation of this directory
     */
    public File file() {
        return file;
    }
    /**
     * String representation of each enum.
     * @return string representation of the enum.
     */
    @Override
    public String toString() {
        return dir;
    }
}
