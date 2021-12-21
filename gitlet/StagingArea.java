package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Directories.STAGING_FILE;

/**
 * The staging area for this repository where files are marked for addition and
 * deletion.
 * @author Connor Bernard
 */
public class StagingArea implements Serializable {
    /**
     * HashMap of files marked for addition.
     */
    private HashMap<String, String> markedForAddition =
            new HashMap<String, String>();
    /**
     * HashSet of files marked for removal.
     */
    private HashSet<String> markedForRemoval = new HashSet<String>();
    /**
     * Marks a file for addition.
     * @param fileName name of file to mark
     * @param hash     hash of file to mark
     */
    public void markForAddition(String fileName, String hash) {
        markedForRemoval.remove(fileName);
        markedForAddition.put(fileName, hash);
    }
    /**
     * Marks a file for removal.
     * @param fileName name of file to mark
     */
    public void markForRemoval(String fileName) {
        removeFromMarkedForAddition(fileName);
        markedForRemoval.add(fileName);
    }
    /**
     * Removes a file from the addition staging area.
     * @param fileName name of file to remove
     */
    public void removeFromMarkedForAddition(String fileName) {
        markedForAddition.remove(fileName);
    }
    /**
     * Removes a file from the removal staging area.
     * @param fileName name of file to remove
     */
    public void removeFromMarkedForRemoval(String fileName) {
        markedForRemoval.remove(fileName);
    }
    /**
     * Getter method for addedFiles.
     * @return addedFiles
     */
    public HashMap<String, String> getMarkedForAddition() {
        return markedForAddition;
    }
    /**
     * Getter method for removedFiles.
     * @return removedFiles
     */
    public HashSet<String> getMarkedForRemoval() {
        return markedForRemoval;
    }
    /**
     * Clears the staging area.
     */
    public void clear() {
        markedForAddition.clear();
        markedForRemoval.clear();
    }
    /**
     * Writes this staging area to the staging area file.
     */
    public void write() {
        Utils.writeObject(STAGING_FILE.file(), this);
    }
    /**
     * Updates this staging area to the staging area stored in the staging area
     * file.
     */
    public void read() {
        StagingArea updated = Utils.readObject(STAGING_FILE.file(),
                StagingArea.class);
        markedForAddition = updated.markedForAddition;
        markedForRemoval = updated.markedForRemoval;
    }
}
