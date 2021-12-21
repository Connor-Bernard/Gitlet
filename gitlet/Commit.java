package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;

import static gitlet.Directories.COMMITS_DIR;

/**
 * A commit to a repository.
 *
 * @author Connor Bernard
 */
public class Commit implements Serializable {
    /**
     * The first parent's hash.
     */
    private final String parentHash;
    /**
     * This commit's second parent.
     */
    private String secondParentHash;
    /**
     * The message of this commit.
     */
    private final String commitMessage;
    /**
     * This commit's hash.
     */
    private String hash;
    /**
     * This commit's time stamp.
     */
    private String time;
    /**
     * This commit's blobs.
     */
    private final HashMap<String, String> blobs;
    /**
     * This commit's branch.
     */
    private final String branch;
    /**
     * This commit's ancestors.
     */
    private final TreeSet<String> ancestors = new TreeSet<String>();
    /**
     * Constructor for the initial commit.
     */
    public Commit() {
        this("master", "initial commit", null,
                new HashMap<String, String>());
        time = "Wed Dec 31 16:00:00 1969 -0800";
    }
    /**
     * General constructor for a commit.
     * @param inputBranch the branch of this commit
     * @param message message for the commit
     * @param firstParentHash    this commit's parent's hash
     * @param inputFiles         the hashmap of this commit's blobs
     */
    public Commit(String inputBranch, String message, String firstParentHash,
                  HashMap<String, String> inputFiles) {
        commitMessage = message;
        parentHash = firstParentHash;
        blobs = inputFiles;
        branch = inputBranch;
        time = new SimpleDateFormat("EEE MMM d HH:mm:ss YYYY Z")
                .format(new Date());
    }
    /**
     * Constructor for commit with two parents.
     * @param inputBranch branch of this commit
     * @param message    message for the commit
     * @param firstParentHash       this commit's parent's hash
     * @param parent2Hash this commit's second parent
     * @param inputFiles            the hashmap of this commit's blobs
     */
    public Commit(String inputBranch, String message, String firstParentHash,
                  String parent2Hash, HashMap<String, String> inputFiles) {
        this(inputBranch, message, firstParentHash, inputFiles);
        secondParentHash = parent2Hash;
    }
    /**
     * Gets this commit's hash.
     * @return this commit's hash
     */
    public String getHash() {
        if (hash == null) {
            hash = Utils.sha1(Utils.serialize(this));
        }
        return hash;
    }
    /**
     * Gets this commit's parent's hash.
     * @return the parent's hash
     */
    public String getParentHash() {
        return parentHash;
    }
    /**
     * Sets this Commit's second parent.
     * @param parent2Hash new secondParent to set to
     */
    public void setSecondParentHash(String parent2Hash) {
        secondParentHash = parent2Hash;
    }
    /**
     * Gets this commit's second parent's hash.
     * @return this commit's second parent's hash
     */
    public String getSecondParentHash() {
        return secondParentHash;
    }
    /**
     * Gets this commit's commit message.
     * @return this commit's commit message
     */
    public String getCommitMessage() {
        return commitMessage;
    }
    /**
     * Gets this commit's commit time.
     * @return this commit's commit time
     */
    public String getTime() {
        return time;
    }
    /**
     * Gets this commit's branch.
     * @return this commit's branch
     */
    public String getBranch() {
        return branch;
    }
    /**
     * Gets this commit's blobs.
     * @return this commit's blobs
     */
    public HashMap<String, String> getBlobs() {
        return blobs;
    }
    /**
     * Checks to see if this commit contains a blob.
     * @param fileName name of file to check
     * @return whether this commit contains the file
     */
    public boolean containsFile(String fileName) {
        return blobs.containsKey(fileName);
    }
    /**
     * Checks to see if this commit contains the input blob.
     * @param blobHash Blob to check
     * @return whether input blob is in this commit
     */
    public boolean containsBlob(String blobHash) {
        return blobs.containsValue(blobHash);
    }
    /**
     * Adds a blob to this commit.
     * @param fileName name of file corresponding to the blob being added
     * @param blobHash hash of blob to add to this commit
     */
    public void addBlob(String fileName, String blobHash) {
        blobs.put(fileName, blobHash);
    }
    /**
     * Getter method for this commit's ancestors.
     * @return a set of this commit's ancestors
     */
    public TreeSet<String> getAncestors() {
        getAncestorsRecursive(hash);
        return ancestors;
    }
    /**
     * Recursive helper method for the getAncestors method.
     * @param curr current branch
     */
    private void getAncestorsRecursive(String curr) {
        if (readCommitFromHash(curr).getParentHash() != null) {
            getAncestorsRecursive(readCommitFromHash(curr).getParentHash());
        }
        ancestors.add(curr);
        if (readCommitFromHash(curr).getSecondParentHash() != null) {
            getAncestorsRecursive(readCommitFromHash(curr)
                    .getSecondParentHash());
        }
    }
    /**
     * Gets the commit associated with a given hash.
     * @param commitHash hash to get the commit of
     * @return the commit associated with the given hash
     */
    private Commit readCommitFromHash(String commitHash) {
        return Utils.readObject(new File(COMMITS_DIR + "/"
                + commitHash), Commit.class);
    }
    /**
     * This objects representation as a string.
     * @return string representation of this Commit
     */
    @Override
    public String toString() {
        return "===\ncommit " + getHash() + "\nDate: " + time + "\n"
                + commitMessage + "\n";
    }
}
