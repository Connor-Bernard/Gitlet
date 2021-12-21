package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static gitlet.Directories.*;
import static gitlet.TextColors.RESET_COLOR;
import static gitlet.TextColors.WARNING_COLOR;
import static gitlet.TextColors.ERROR_COLOR;
import static gitlet.TextColors.COMPLETION_COLOR;
/**
 * The object format of this repository.
 * @author Connor Bernard
 */
public class Repo {
    /**
     * Staging area being used for this repo.
     */
    private StagingArea stage;
    /**
     * Standard constructor for repo that initializes and updates the
     * staging area if possible.
     */
    public Repo() {
        if (REPO_DIR.file().exists()) {
            stage = new StagingArea();
            stage.read();
        }
    }
    /**
     * Initializes the repo.
     */
    public void init() {
        stage = new StagingArea();
        REPO_DIR.file().mkdir();
        BLOBS_DIR.file().mkdir();
        COMMITS_DIR.file().mkdir();
        BRANCHES_DIR.file().mkdir();
        REMOTE_DIR.file().mkdir();
        try {
            STAGING_FILE.file().createNewFile();
        } catch (IOException e) {
            System.out.println(ERROR_COLOR.toString() + e + RESET_COLOR);
            System.exit(0);
        }
        Commit initialCommit = new Commit();
        String head = initialCommit.getHash();
        File headFile = HEAD_FILE.file();
        File master = new File(BRANCHES_DIR + "/master");
        File initialCommitFile = new File(COMMITS_DIR + "/" + head);
        File currentCommit = CURRENT_COMMIT.file();
        Utils.writeContents(currentCommit, head);
        Utils.writeContents(headFile, "master");
        Utils.writeContents(master, head);
        Utils.writeContents(initialCommitFile, Utils.serialize(initialCommit));
        try {
            master.createNewFile();
            initialCommitFile.createNewFile();
            currentCommit.createNewFile();
        } catch (IOException e) {
            System.out.println(ERROR_COLOR.toString() + e + RESET_COLOR);
            System.exit(0);
        }
        stage.write();
    }
    /**
     * Adds the file name to the StagingArea.
     * @param fileName file to add
     */
    public void add(String fileName) {
        Commit currCommit = getCurrCommit();
        File fileToAdd = new File(fileName);
        if (!fileToAdd.exists()) {
            if (!currCommit.getBlobs().containsKey(fileName)) {
                System.out.println(ERROR_COLOR + "File does not exist."
                        + RESET_COLOR);
            } else {
                stage.removeFromMarkedForRemoval(fileName);
                File addBack = new File(USER_DIR + "/" + fileName);
                Utils.writeContents(addBack, Utils.readContentsAsString(
                        new File(BLOBS_DIR + "/"
                                + currCommit.getBlobs().get(fileName))));
                try {
                    addBack.createNewFile();
                } catch (IOException e) {
                    System.out.println(ERROR_COLOR.toString() + e
                            + RESET_COLOR);
                    System.exit(0);
                }
                stage.write();
            }
            System.exit(0);
        }
        String fileHash = Utils.sha1(Utils.readContentsAsString(fileToAdd));
        stage.removeFromMarkedForRemoval(fileName);
        if (!currCommit.containsFile(fileName)
                || !currCommit.getBlobs().get(fileName).equals(fileHash)) {
            try {
                File blob = new File(BLOBS_DIR + "/" + fileHash);
                Utils.writeContents(blob,
                        Utils.readContentsAsString(fileToAdd));
                blob.createNewFile();
            } catch (IOException e) {
                System.out.println(ERROR_COLOR.toString() + e + RESET_COLOR);
                System.exit(0);
            }
            stage.markForAddition(fileName, fileHash);
        }
        stage.write();
    }
    /**
     * Makes a new commit with a given commit message.
     * @param commitMessage commit message
     */
    public void commit(String commitMessage) {
        if (stage.getMarkedForAddition().isEmpty()
                && stage.getMarkedForRemoval().isEmpty()) {
            System.out.println(WARNING_COLOR + "No changes added to the commit."
                    + RESET_COLOR);
            System.exit(0);
        }
        if (commitMessage.isEmpty()) {
            System.out.println(WARNING_COLOR + "Please enter a commit message."
                    + RESET_COLOR);
            System.exit(0);
        }
        Commit currCommit = getCurrCommit();
        if (!currCommit.getHash()
                .equals(Utils.readContentsAsString(CURRENT_COMMIT.file()))) {
            System.out.println(ERROR_COLOR + "Currently at a detached pointer"
                    + "state." + RESET_COLOR);
        }
        HashMap<String, String> newBlobs = new HashMap<String, String>();
        for (String fileName : currCommit.getBlobs().keySet()) {
            newBlobs.put(fileName, currCommit.getBlobs().get(fileName));
        }
        HashMap<String, String> markedForAddition =
                stage.getMarkedForAddition();
        HashSet<String> markedForRemoval = stage.getMarkedForRemoval();
        for (String fileName : markedForAddition.keySet()) {
            newBlobs.put(fileName, markedForAddition.get(fileName));
        }
        for (String fileName : markedForRemoval) {
            newBlobs.remove(fileName);
        }
        Commit thisCommit = new Commit(
                Utils.readContentsAsString(HEAD_FILE.file()), commitMessage,
                currCommit.getHash(), newBlobs);
        String thisCommitHash = thisCommit.getHash();
        File thisCommitFile = new File(COMMITS_DIR + "/"
                + thisCommitHash);
        Utils.writeContents(new File(BRANCHES_DIR + "/"
                        + Utils.readContentsAsString(HEAD_FILE.file())),
                thisCommit.getHash());
        Utils.writeContents(CURRENT_COMMIT.file(), thisCommitHash);
        Utils.writeObject(thisCommitFile, thisCommit);
        try {
            thisCommitFile.createNewFile();
        } catch (IOException e) {
            System.out.println(ERROR_COLOR.toString() + e + RESET_COLOR);
            System.exit(0);
        }
        stage.clear();
        stage.write();
    }
    /**
     * Privatized version of the commit class for merge implementation.
     * @param commitMessage    message for the commit
     * @param firstParentHash  first parent commit's hash ID
     * @param secondParentHash second parent commit's hash ID
     */
    private void commit(String commitMessage, String firstParentHash,
                        String secondParentHash) {
        Commit currCommit = getCurrCommit();
        if (!currCommit.getHash().equals(Utils.
                readContentsAsString(CURRENT_COMMIT.file()))) {
            System.out.println(ERROR_COLOR + "Currently at a detached pointer"
                    + "state." + RESET_COLOR);
        }
        HashMap<String, String> newBlobs = new HashMap<String, String>();
        for (String fileName : currCommit.getBlobs().keySet()) {
            newBlobs.put(fileName, currCommit.getBlobs().get(fileName));
        }
        HashMap<String, String> markedForAddition =
                stage.getMarkedForAddition();
        HashSet<String> markedForRemoval = stage.getMarkedForRemoval();
        for (String fileName : markedForAddition.keySet()) {
            newBlobs.put(fileName, markedForAddition.get(fileName));
        }
        for (String fileName : markedForRemoval) {
            newBlobs.remove(fileName);
        }
        Commit thisCommit = new Commit(Utils.
                readContentsAsString(HEAD_FILE.file()), commitMessage,
                firstParentHash, secondParentHash, newBlobs);
        String thisCommitHash = thisCommit.getHash();
        File thisCommitFile = new File(COMMITS_DIR + "/"
                + thisCommitHash);
        Utils.writeContents(new File(BRANCHES_DIR + "/"
                        + Utils.readContentsAsString(HEAD_FILE.file())),
                thisCommit.getHash());
        Utils.writeContents(CURRENT_COMMIT.file(), thisCommitHash);
        Utils.writeObject(thisCommitFile, thisCommit);
        Utils.writeContents(HEAD_FILE.file(), currCommit.getBranch());
        try {
            thisCommitFile.createNewFile();
        } catch (IOException e) {
            System.out.println(ERROR_COLOR.toString() + e + RESET_COLOR);
            System.exit(0);
        }
        stage.clear();
        stage.write();
    }
    /**
     * Stages a file for removal.
     * @param fileName file to stage for removal
     */
    public void rm(String fileName) {
        Commit currCommit = getCurrCommit();
        File fileToRemove = new File(fileName);
        if (!fileToRemove.exists()) {
            if (currCommit.getBlobs().containsKey(fileName)) {
                stage.markForRemoval(fileName);
                stage.write();
                System.exit(0);
            }
            System.out.println(ERROR_COLOR + "Specified file does not exist"
                    + RESET_COLOR);
            System.exit(0);
        }
        if (currCommit.getBlobs().containsKey(fileName)) {
            Utils.restrictedDelete(USER_DIR + "/" + fileName);
            stage.markForRemoval(fileName);
        } else if (stage.getMarkedForAddition().containsKey(fileName)) {
            stage.removeFromMarkedForAddition(fileName);
        } else {
            System.out.println(WARNING_COLOR + "No reason to remove the file."
                    + RESET_COLOR);
            System.exit(0);
        }
        stage.write();
    }
    /**
     * Prints the log of commits.
     */
    public void log() {
        Commit currCommit = getCurrCommit();
        while (currCommit != null) {
            System.out.println(currCommit);
            currCommit = readCommitFromHash(currCommit.getParentHash());
        }
    }
    /**
     * Displays info about all commits ever made.
     */
    public void globalLog() {
        for (String hash : Utils.plainFilenamesIn(COMMITS_DIR.file())) {
            Commit thisCommit = Utils.readObject(new File(COMMITS_DIR
                    + "/" + hash), Commit.class);
            System.out.println(thisCommit);
        }
    }
    /**
     * Prints out the ids of all commits that have the given commit message.
     * @param commitMessage message to check for instances of
     */
    public void find(String commitMessage) {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS_DIR.file());
        boolean found = false;
        for (String hash : allCommits) {
            if (Utils.readObject(new File(COMMITS_DIR + "/" + hash),
                    Commit.class).getCommitMessage().equals(commitMessage)) {
                System.out.println(hash);
                found = true;
            }
        }
        if (!found) {
            System.out.println(ERROR_COLOR + "Found no commit with that "
                    + "message." + RESET_COLOR);
        }
    }
    /**
     * Displays status of current directory i.e. branches, staged files, removed
     * files, modifications not staged for commit, and untracked files.
     */
    public void status() {
        Commit currCommit = getCurrCommit();
        String currBranch = Utils.readContentsAsString(HEAD_FILE.file());
        List<String> allFiles = Utils.plainFilenamesIn(USER_DIR.file());
        Set<String> markedForRemoval = stage.getMarkedForRemoval();
        Set<String> markedForAddition = stage.getMarkedForAddition().keySet();
        HashSet<String> trackedFiles = new HashSet<String>();
        List<String> branches = Utils.plainFilenamesIn(BRANCHES_DIR.file());
        trackedFiles.addAll(markedForAddition);
        trackedFiles.addAll(markedForRemoval);
        trackedFiles.addAll(currCommit.getBlobs().keySet());
        System.out.println("=== Branches ===");
        for (String branch : branches) {
            if (branch.equals(currBranch)) {
                System.out.println(COMPLETION_COLOR + "*" + branch
                        + RESET_COLOR);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String fileName : markedForAddition) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        System.out.print(ERROR_COLOR);
        for (String fileName : markedForRemoval) {
            System.out.println(fileName);
        }
        System.out.println(RESET_COLOR);
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.print(WARNING_COLOR);
        printModificationNotStageForCommit(currCommit);
        for (String fileName : currCommit.getBlobs().keySet()) {
            if (!new File(USER_DIR + "/" + fileName).exists()
                    && !stage.getMarkedForRemoval().contains(fileName)) {
                System.out.println(fileName + " (deleted)");
            }
        }
        System.out.println(RESET_COLOR);
        System.out.println("=== Untracked Files ===");
        System.out.print(WARNING_COLOR);
        for (String fileName : Utils.plainFilenamesIn(USER_DIR.file())) {
            if (fileName.length() > 4 && fileName.endsWith(".txt")
                    && !currCommit.containsFile(fileName)
                    && !stage.getMarkedForAddition().containsKey(fileName)
                    && !stage.getMarkedForRemoval().contains(fileName)) {
                System.out.println(fileName);
            }
        }
        System.out.println(RESET_COLOR);
    }
    /**
     * Prints the modifications not yet staged for commit.
     * @param currCommit the current commit being used in the status method
     */
    private void printModificationNotStageForCommit(Commit currCommit) {
        stage.read();
        HashSet<String> currBlobs = new HashSet<String>(
                Utils.plainFilenamesIn(BLOBS_DIR.file()));
        for (String fileName : Utils.plainFilenamesIn(USER_DIR.file())) {
            if (fileName.length() > 4 && fileName.endsWith(".txt")
                    && currCommit.containsFile(fileName)
                    && !stage.getMarkedForAddition().containsKey(fileName)
                    && !stage.getMarkedForRemoval().contains(fileName)) {
                if (!currBlobs.contains(Utils.sha1(Utils.readContentsAsString(
                        new File(USER_DIR + "/" + fileName))))) {
                    System.out.println(fileName + " (modified)");
                }
            }
        }
    }
    /**
     * Checks out a given branch.
     * @param branchName the branch to checkout
     */
    public void checkoutBranch(String branchName) {
        Commit currCommit = getCurrCommit();
        checkoutBranchFailureCaseChecker(branchName, currCommit);
        Utils.writeContents(HEAD_FILE.file(), branchName);
        Commit commitToCheckout = readCommitFromHash(Utils.readContentsAsString(
                new File(BRANCHES_DIR + "/" + branchName)));
        Utils.writeContents(CURRENT_COMMIT.file(), commitToCheckout.getHash());
        HashMap<String, String> checkedOutBlobs = commitToCheckout.getBlobs();
        for (String fileName : currCommit.getBlobs().keySet()) {
            if (checkedOutBlobs.get(fileName) != null) {
                Utils.writeContents(new File(fileName), Utils.
                        readContentsAsString(new File(BLOBS_DIR + "/"
                                + checkedOutBlobs.get(fileName))));
            } else {
                Utils.restrictedDelete(fileName);
            }
        }
        for (String fileName : checkedOutBlobs.keySet()) {
            if (!currCommit.containsFile(fileName)
                    && new File(BLOBS_DIR + "/" + checkedOutBlobs.
                    get(fileName)).exists()) {
                File newFile = new File(fileName);
                Utils.writeContents(newFile, Utils.readContentsAsString(
                        new File(BLOBS_DIR + "/"
                                + checkedOutBlobs.get(fileName))));
                try {
                    newFile.createNewFile();
                } catch (IOException e) {
                    System.out.println(ERROR_COLOR.toString() + e
                            + RESET_COLOR);
                    System.exit(0);
                }
            }
        }
        Utils.writeContents(HEAD_FILE.file(), branchName);
        Utils.writeContents(CURRENT_COMMIT.file(), commitToCheckout.getHash());
        stage.write();
    }
    /**
     * Checks for failure cases needed for checkoutBranch.
     * @param branchName name of branch being checked out
     * @param currCommit the current commit
     */
    private void checkoutBranchFailureCaseChecker(String branchName,
                                                  Commit currCommit) {
        if (stage.getMarkedForAddition().size() > 0
                || stage.getMarkedForRemoval().size() > 0) {
            System.out.println(WARNING_COLOR + "Uncommitted changes."
                    + RESET_COLOR);
            System.exit(0);
        }
        if (!new File(BRANCHES_DIR + "/" + branchName).exists()) {
            System.out.println(ERROR_COLOR + "No such branch exists."
                    + RESET_COLOR);
            System.exit(0);
        }
        if (Utils.readContentsAsString(HEAD_FILE.file()).equals(branchName)) {
            System.out.println(WARNING_COLOR
                    + "No need to checkout the current branch." + RESET_COLOR);
            System.exit(0);
        }
        for (String fileName : Utils.plainFilenamesIn(USER_DIR.file())) {
            if (fileName.length() > 4 && fileName.endsWith(".txt")
                    && !currCommit.containsFile(fileName)) {
                System.out.println(WARNING_COLOR + "There is an untracked file"
                        + "in the way; delete it, or add and commit it first."
                        + RESET_COLOR);
                System.exit(0);
            }
        }
    }
    /**
     * Checks out a given file.
     * @param fileName the file to checkout
     */
    public void checkoutFile(String fileName) {
        checkoutFileFromCommit(Utils.readContentsAsString(
                CURRENT_COMMIT.file()), fileName);
    }
    /**
     * Checks out a file from a given commit.
     * @param commitID ID of commit to check the file out from
     * @param fileName the name of the file to check out
     */
    public void checkoutFileFromCommit(String commitID, String fileName) {
        Commit currCommit = getCurrCommit();
        if (stage.getMarkedForAddition().size() > 0
                || stage.getMarkedForRemoval().size() > 0) {
            System.out.println(WARNING_COLOR + "Uncommitted changes."
                    + RESET_COLOR);
            System.exit(0);
        }
        Commit commitToCheckout = readCommitFromHash(commitID);
        if (commitToCheckout == null) {
            System.out.println(ERROR_COLOR + "No commit with that id exists."
                    + RESET_COLOR);
            System.exit(0);
        }
        HashMap<String, String> checkedOutBlobs = commitToCheckout.getBlobs();
        if (checkedOutBlobs.containsKey(fileName)) {
            File file = new File(USER_DIR + "/" + fileName);
            File blobFile = new File(BLOBS_DIR + "/"
                    + checkedOutBlobs.get(fileName));
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    System.exit(0);
                }
            }
            if (!blobFile.exists()) {
                System.exit(0);
            }
            Utils.writeContents(file, Utils.readContentsAsString(blobFile));
        } else {
            System.out.println(ERROR_COLOR
                    + "File does not exist in that commit." + RESET_COLOR);
            System.exit(0);
        }
    }
    /**
     * Private version of checkoutFileFromCommit used in merge implementation.
     * @param commitID hash of commit to check file out from
     * @param fileName name of file to check out
     */
    private void checkoutFileFromCommitMerge(String commitID, String fileName) {
        Commit currCommit = getCurrCommit();
        Commit commitToCheckout = readCommitFromHash(commitID);
        if (commitToCheckout == null) {
            System.out.println(ERROR_COLOR + "No commit with that id exists."
                    + RESET_COLOR);
            System.exit(0);
        }
        HashMap<String, String> checkedOutBlobs = commitToCheckout.getBlobs();
        if (checkedOutBlobs.containsKey(fileName)) {
            File file = new File(USER_DIR + "/" + fileName);
            File blobFile = new File(BLOBS_DIR + "/"
                    + checkedOutBlobs.get(fileName));
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    System.exit(0);
                }
            }
            if (!blobFile.exists()) {
                System.exit(0);
            }
            Utils.writeContents(file, Utils.readContentsAsString(blobFile));
        } else {
            System.out.println(ERROR_COLOR
                    + "File does not exist in that commit." + RESET_COLOR);
            System.exit(0);
        }
    }
    /**
     * Creates a new branch at the current HEAD.
     * @param branchName the name of the branch to create
     */
    public void branch(String branchName) {
        Commit currCommit = getCurrCommit();
        File newBranch = new File(BRANCHES_DIR + "/" + branchName);
        if (newBranch.exists()) {
            System.out.println(ERROR_COLOR
                    + "A branch with that name already exists." + RESET_COLOR);
            System.exit(0);
        }
        if (branchName.equals(Utils.readContentsAsString(HEAD_FILE.file()))) {
            System.out.println(WARNING_COLOR
                    + "No need to checkout the current branch." + RESET_COLOR);
            System.exit(0);
        }
        Utils.writeContents(newBranch, currCommit.getHash());
        try {
            new File(BRANCHES_DIR + "/" + branchName).createNewFile();
        } catch (IOException e) {
            System.out.println(ERROR_COLOR.toString() + e + RESET_COLOR);
            System.exit(0);
        }
    }
    /**
     * Deletes the branch with the given name.
     * @param branchName name of branch to remove
     */
    public void rmBranch(String branchName) {
        if (!new File(BRANCHES_DIR + "/" + branchName).exists()) {
            System.out.println(ERROR_COLOR
                    + "A branch with that name does not exist." + RESET_COLOR);
            System.exit(0);
        }
        if (Utils.readContentsAsString(HEAD_FILE.file()).equals(branchName)) {
            System.out.println(ERROR_COLOR + "Cannot remove the current branch."
                    + RESET_COLOR);
            System.exit(0);
        }
        new File(BRANCHES_DIR + "/" + branchName).delete();
    }
    /**
     * Sets the current commit to the commit at the given commit ID.
     * @param commitID commit ID to reset to
     */
    public void reset(String commitID) {
        if (readCommitFromHash(commitID) == null) {
            System.out.println(ERROR_COLOR + "No commit with that id exists."
                    + RESET_COLOR);
            System.exit(0);
        }
        Commit currCommit = getCurrCommit();
        Commit commitToResetTo = readCommitFromHash(commitID);
        for (String fileName : Utils.plainFilenamesIn(USER_DIR.file())) {
            if (fileName.length() > 4 && fileName.endsWith(".txt")
                    && !(currCommit.containsFile(fileName)
                    || stage.getMarkedForAddition().containsKey(fileName)
                    || stage.getMarkedForRemoval().contains(fileName))) {
                System.out.println(WARNING_COLOR + "There is an untracked"
                        + " file in the way; delete it, or add and commit"
                        + " it first." + RESET_COLOR);
                System.exit(0);
            }
        }
        for (String fileName : stage.getMarkedForRemoval()) {
            Utils.restrictedDelete(USER_DIR + "/" + fileName);
        }
        for (String fileName : stage.getMarkedForAddition().keySet()) {
            Utils.restrictedDelete(USER_DIR + "/" + fileName);
        }
        for (String fileName : currCommit.getBlobs().keySet()) {
            Utils.restrictedDelete(USER_DIR + "/" + fileName);
        }
        stage.clear();
        stage.write();
        Utils.writeContents(HEAD_FILE.file(), commitToResetTo.getBranch());
        Utils.writeContents(CURRENT_COMMIT.file(), commitToResetTo.getHash());
        Utils.writeContents(new File(BRANCHES_DIR + "/"
                + commitToResetTo.getBranch()), commitToResetTo.getHash());
        HashMap<String, String> checkOutBlobs = commitToResetTo.getBlobs();
        for (String fileName : checkOutBlobs.keySet()) {
            checkoutFileFromCommit(commitID, fileName);
        }
    }
    /**
     * Merges the current branch with the given branch.
     * @param branchName branch to merge with current branch.
     */
    public void merge(String branchName) {
        if (!Utils.plainFilenamesIn(BRANCHES_DIR.file()).contains(branchName)) {
            System.out.println(WARNING_COLOR
                    + "A branch with that name does not exist." + RESET_COLOR);
            System.exit(0);
        }
        Commit givenCommit = readCommitFromHash(Utils.readContentsAsString(
                new File(BRANCHES_DIR + "/" + branchName)));
        Commit currCommit = getCurrCommit();
        Utils.writeContents(new File(BRANCHES_DIR + "/"
                + currCommit.getBranch()), currCommit.getHash());
        Commit splitPoint = getSplit(givenCommit);
        HashMap<String, String> currBlobs = currCommit.getBlobs();
        HashMap<String, String> givenBlobs = givenCommit.getBlobs();
        HashMap<String, String> splitBlobs = splitPoint.getBlobs();
        HashSet<String> allFileNames = new HashSet<String>(currBlobs.keySet());
        allFileNames.addAll(givenBlobs.keySet());
        allFileNames.addAll(splitBlobs.keySet());
        mergeFailureCaseChecker(branchName, currCommit, givenCommit,
                splitPoint);
        secondaryFailureCaseChecker(allFileNames, givenBlobs, currBlobs,
                splitBlobs, currCommit, givenCommit);
        String givenBranch = givenCommit.getBranch();
        String currBranch = currCommit.getBranch();
        Utils.writeContents(CURRENT_COMMIT.file(), currCommit.getHash());
        Utils.writeContents(HEAD_FILE.file(), currCommit.getBranch());
        commit("Merged " + givenBranch + " into " + currBranch
                + ".", getCurrCommit().getHash(), givenCommit.getHash());
    }
    /**
     * Checks for some failure cases in merge.
     * @param branchName name of branch being merged
     * @param currCommit commit of current branch
     * @param givenCommit commit of branch being merged
     * @param splitPoint most recent common ancestor
     */
    private void mergeFailureCaseChecker(String branchName, Commit currCommit,
                                         Commit givenCommit,
                                         Commit splitPoint) {
        if (Utils.readContentsAsString(HEAD_FILE.file()).equals(branchName)) {
            System.out.println(WARNING_COLOR
                    + "Cannot merge a branch with itself." + RESET_COLOR);
            System.exit(0);
        }
        if (currCommit.getBranch().equals(givenCommit.getBranch())
                && currCommit.getBranch().equals(Utils.readContentsAsString(
                HEAD_FILE.file()))) {
            System.out.println(WARNING_COLOR
                    + "Given branch is an ancestor of the current branch."
                    + RESET_COLOR);
            System.exit(0);
        }
        if (stage.getMarkedForAddition().size() > 0
                || stage.getMarkedForRemoval().size() > 0) {
            System.out.println(WARNING_COLOR + "You have uncommitted changes."
                    + RESET_COLOR);
            System.exit(0);
        }
        for (String fileName : Utils.plainFilenamesIn(USER_DIR.file())) {
            if (fileName.length() > 4 && fileName.endsWith(".txt")
                    && !currCommit.containsFile(fileName)) {
                System.out.println(WARNING_COLOR + "There is an untracked "
                        + "file in the way; delete it, or add and commit it"
                        + " first." + RESET_COLOR);
                System.exit(0);
            }
        }
        if (givenCommit == null) {
            System.out.println("Specified branch does not exist.");
            System.exit(0);
        }
        if (getCurrCommit() == givenCommit) {
            System.out.println("Given branch is an ancestor of the current"
                    + " branch.");
            System.exit(0);
        }
        if (splitPoint.getHash().equals(currCommit.getHash())) {
            reset(givenCommit.getHash());
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
    }
    /**
     * Checks some other failure cases for each file being iterated through in
     * the commit in Merge.
     * @param allFileNames list of all file names being used
     * @param givenBlobs the blobs of the given commit
     * @param currBlobs the blobs of the current commit
     * @param splitBlobs the blobs of the split commit
     * @param currCommit the commit of the current branch
     * @param givenCommit the commit of the given branch
     */
    private void secondaryFailureCaseChecker(HashSet<String> allFileNames,
                                             HashMap<String, String> givenBlobs,
                                             HashMap<String, String> currBlobs,
                                             HashMap<String, String> splitBlobs,
                                             Commit currCommit,
                                             Commit givenCommit) {
        for (String fileName : allFileNames) {
            if (givenBlobs.containsKey(fileName)
                    && currBlobs.containsKey(fileName)
                    && splitBlobs.containsKey(fileName)) {
                if (currBlobs.get(fileName).equals(splitBlobs.get(fileName))
                        && !givenBlobs.get(fileName).equals(splitBlobs.
                        get(fileName))) {
                    checkoutFileFromCommitMerge(givenCommit.getHash(),
                            fileName);
                    add(fileName);
                    continue;
                } else if (splitBlobs.get(fileName).equals(givenBlobs.
                        get(fileName))) {
                    continue;
                } else if (currBlobs.get(fileName).equals(givenBlobs.
                        get(fileName))) {
                    continue;
                }
            } else if (!currBlobs.containsKey(fileName)
                    && !givenBlobs.containsKey(fileName)) {
                continue;
            } else if (!splitBlobs.containsKey(fileName)
                    && !givenBlobs.containsKey(fileName)) {
                continue;
            } else if (!splitBlobs.containsKey(fileName)
                    && givenBlobs.containsKey(fileName)
                    && !currBlobs.containsKey(fileName)) {
                checkoutFileFromCommitMerge(givenCommit.getHash(), fileName);
                add(fileName);
                continue;
            } else if (splitBlobs.containsKey(fileName)
                    && currBlobs.containsKey(fileName)
                    && splitBlobs.get(fileName).equals(currBlobs.get(fileName))
                    && !givenBlobs.containsKey(fileName)) {
                rm(fileName);
                continue;
            } else if (splitBlobs.containsKey(fileName)
                    && givenBlobs.containsKey(fileName)
                    && !currBlobs.containsKey(fileName)
                    && givenBlobs.get(fileName).equals(splitBlobs.
                    get(fileName))) {
                continue;
            } else if (splitBlobs.containsKey(fileName)
                    && givenBlobs.containsKey(fileName)) {
                continue;
            }
            writeConflictFile(fileName, currBlobs, givenBlobs);
            System.out.println(WARNING_COLOR + "Encountered a merge conflict."
                    + RESET_COLOR);
        }
    }
    /**
     * Gets the split for the current branch.
     * @param givenBranchCommit the given commit for the merge call
     * @return the commit of the split point
     */
    private Commit getSplit(Commit givenBranchCommit) {
        TreeSet<String> currAncestors =
                new TreeSet<String>(getCurrCommit().getAncestors());
        TreeSet<String> givenAncestors =
                new TreeSet<String>(givenBranchCommit.getAncestors());
        ArrayList<String> commonAncestors = new ArrayList<String>();
        for (String hash : currAncestors) {
            if (givenAncestors.contains(hash)) {
                commonAncestors.add(hash);
            }
        }
        TreeMap<String, Boolean> isMarked = new TreeMap<String, Boolean>();
        for (String fileName : Utils.plainFilenamesIn(COMMITS_DIR.file())) {
            isMarked.put(fileName, false);
        }
        ArrayDeque<Commit> queue = new ArrayDeque<Commit>();
        queue.add(getCurrCommit());
        isMarked.put(getCurrCommit().getHash(), true);
        while (!queue.isEmpty()) {
            Commit removed = queue.remove();
            if (commonAncestors.contains(removed.getHash())) {
                return removed;
            }
            if (removed.getParentHash() != null
                    && !isMarked.get(removed.getParentHash())) {
                queue.add(readCommitFromHash(removed.getParentHash()));
                isMarked.put(readCommitFromHash(removed.getParentHash()).
                        getHash(), true);
            }
            if (removed.getSecondParentHash() != null
                    && !isMarked.get(removed.getSecondParentHash())) {
                queue.add(readCommitFromHash(removed.getSecondParentHash()));
                isMarked.put(readCommitFromHash(removed.getSecondParentHash()).
                        getHash(), true);
            }
        }
        return new Commit();
    }
    /**
     * Writes a conflict to a conflicting file.
     * @param fileName name of file to write to
     * @param currBlobs the blobs in the current commit
     * @param givenBlobs the blobs in the given commit
     */
    private void writeConflictFile(String fileName,
                                   HashMap<String, String> currBlobs,
                                   HashMap<String, String> givenBlobs) {
        String newContents = "";
        newContents += "<<<<<<< HEAD\n";
        if (currBlobs.containsKey(fileName)) {
            newContents += (Utils.readContentsAsString(
                    new File(USER_DIR + "/" + fileName)));
        }
        newContents += "=======\n";
        if (givenBlobs.containsKey(fileName)
                && givenBlobs.get(fileName) != null) {
            newContents += Utils.readContentsAsString(
                    new File(BLOBS_DIR + "/"
                            + givenBlobs.get(fileName)));
        }
        newContents += ">>>>>>>\n";
        Utils.writeContents(new File(USER_DIR
                + "/" + fileName), newContents);
        add(fileName);
        stage.write();
    }
    /**
     * Adds a remote with the given name and directory.
     * @param remoteName name of the remote
     * @param remoteDirectory directory of the remote
     */
    public void addRemote(String remoteName, String remoteDirectory) {
        if (Utils.plainFilenamesIn(REMOTE_DIR.file()).contains(remoteName)) {
            System.out.println(ERROR_COLOR
                    + "A remote with that name already exists." + RESET_COLOR);
            System.exit(0);
        }
        remoteDirectory = remoteDirectory.replace('/',
                File.separatorChar);
        File remote = new File(REMOTE_DIR + "/" + remoteName);
        Utils.writeContents(remote, remoteDirectory);
        try {
            remote.createNewFile();
        } catch (IOException e) {
            System.out.println(ERROR_COLOR.toString() + e + RESET_COLOR);
            System.exit(0);
        }
    }
    /**
     * Removes the specified remote.
     * @param remoteName name of remote to remove
     */
    public void rmRemote(String remoteName) {
        if (!Utils.plainFilenamesIn(REMOTE_DIR.file()).contains(remoteName)) {
            System.out.println(ERROR_COLOR
                    + "A remote with that name does not exist." + RESET_COLOR);
            System.exit(0);
        }
        new File(REMOTE_DIR + "/" + remoteName).delete();
    }
    /**
     * Pushes the current branch to the remote at the given branch.
     * @param remoteName name of remote to push to
     * @param remoteBranchName branch in commit to push to
     */
    public void push(String remoteName, String remoteBranchName) {
        if (!new File(getRemoteDirectory(remoteName)).exists()) {
            System.out.println(ERROR_COLOR + "Remote directory not found."
                    + RESET_COLOR);
            System.exit(0);
        }
        for (String blobHash : getCurrCommit().getBlobs().values()) {
            if (!new File(getRemoteDirectory(remoteName)
                    + "/Blobs/" + blobHash).exists()) {
                System.out.println(ERROR_COLOR
                        + "Please pull down remote changes before pushing."
                        + RESET_COLOR);
                System.exit(0);
            }
        }
        //TODO: implement
    }
    /**
     * Fetches the remote branch from the specified remote.
     * @param remoteName remote to fetch from
     * @param remoteBranchName branch to fetch
     */
    public void fetch(String remoteName, String remoteBranchName) {
        if (!new File(getRemoteDirectory(remoteName)).exists()) {
            System.out.println(ERROR_COLOR + "Remote directory not found."
                    + RESET_COLOR);
            System.exit(0);
        }
        if (!new File(getRemoteDirectory(remoteName)
                + "/Branches/" + remoteBranchName).exists()) {
            System.out.println(ERROR_COLOR
                    + "That remote does not have that branch."
                    + RESET_COLOR);
            System.exit(0);
        }
        //TODO: implement
    }
    /**
     * Pulls the remote branch from the given remote.
     * @param remoteName remote to pull from
     * @param remoteBranchName branch to pull
     */
    public void pull(String remoteName, String remoteBranchName) {
        fetch(remoteName, remoteBranchName);
        push(remoteBranchName, remoteBranchName);
    }
    /**
     * Gets the directory associated with the given remote name.
     * @param remoteName name of remote to get the directory of
     * @return directory of given remote as a string
     */
    private String getRemoteDirectory(String remoteName) {
        File remote = new File(REMOTE_DIR + "/" + remoteName);
        if (remote.exists()) {
            return Utils.readContentsAsString(remote);
        }
        return null;
    }
    /**
     * Gets the commit associated with a given hash.
     * @param hash hash of commit to get
     * @return commit corresponding to the input hash
     */
    private Commit readCommitFromHash(String hash) {
        if (hash == null) {
            return null;
        }
        for (String fileName : Utils.plainFilenamesIn(COMMITS_DIR.file())) {
            if (fileName.startsWith(hash)) {
                return Utils.readObject(new File(COMMITS_DIR + "/"
                        + fileName), Commit.class);
            }
        }
        return null;
    }
    /**
     * Getter method for the current commit (from current commit file).
     * @return current commit
     */
    private Commit getCurrCommit() {
        return readCommitFromHash(Utils.readContentsAsString(CURRENT_COMMIT.
                file()));
    }
    /**
     * Sets this repo's staging area to the input staging area.
     * @param newStage the staging area to update this repo's staging area to
     */
    public void setStage(StagingArea newStage) {
        stage = newStage;
    }
}
