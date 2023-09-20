package gitlet;

import java.io.File;
import static gitlet.Utils.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.io.Serializable;

/** Represents a gitlet repository.
 * Contains the methods that actually execute commands entered by the user.
 *
 * @author alysa liu, testings
 */
public class Repository implements Serializable {
    /** List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**The current working directory.*/
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory.*/
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /**The directory of commits*/
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    /**The directory of blobs*/
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");

    /**Head pointer with hash of working commit.*/
    private String head;
    /**HashMap of all branches.*/
    private HashMap branches;
    /**Current working branch.*/
    private String workingBranch;
    /** Files staged for addition*/
    private HashMap stagedAddition;
    /** Files staged for removal*/
    private HashMap stagedRemoval;

    public void init() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
            COMMITS_DIR.mkdir();
            BLOBS_DIR.mkdir();
            branches = new HashMap<String, String>();
            stagedAddition = new HashMap<String, String>();
            saveStagedAddition();
            stagedRemoval = new HashMap<String, String>();
            saveStagedRemoval();
            Commit initialCommit = new Commit(new Date(0), "initial commit", null, null);
            saveCommit(initialCommit);
            head = hashCommit(initialCommit);
            workingBranch = "master";
            branches.put(workingBranch, head);
            saveBranches();
        } else {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
        }
    }

    public void add(String fileName) {
        loadStagedAddition();
        loadStagedRemoval();
        File addedFile = join(CWD, fileName);
        String addedFileBlob = "File does not exist.";
        if (!addedFile.exists()) {
            System.out.println(addedFileBlob);
            return;
        } else {
            addedFileBlob = readContentsAsString(addedFile);
        }
        Commit workingCommit = loadCommit(head);
        Object currentBlob = workingCommit.trackedFiles().get(fileName);
        if (currentBlob != null && ((String) currentBlob).equals(sha1(addedFileBlob))) {
            stagedAddition.remove(fileName);
            stagedRemoval.remove(fileName);
        } else {
            stagedAddition.put(fileName, sha1(addedFileBlob));
            stagedRemoval.remove(fileName);
            saveBlob(addedFileBlob);
        }
        saveStagedAddition();
        saveStagedRemoval();
    }

    public void remove(String fileName) {
        loadStagedRemoval();
        loadStagedAddition();
        Commit workingCommit = loadCommit(head);
        File removedFile = join(CWD, fileName);
        String removedFileBlob = "File doesn't exist";
        if (removedFile.exists()) {
            removedFileBlob = readContentsAsString(removedFile);
        }
        if (stagedAddition.containsKey(fileName)) {
            stagedAddition.remove(fileName);
        } else if (workingCommit.trackedFiles().containsKey(fileName)) {
            stagedRemoval.put(fileName, sha1(removedFileBlob));
            restrictedDelete(fileName);
        } else {
            System.out.println("No reason to remove the file.");
        }
        saveStagedRemoval();
        saveStagedAddition();
    }

    public void commit(String message) {
        if (message.length() == 0) {
            System.out.println("Please enter a commit message.");
        }
        Commit newCommit = new Commit(new Date(), message, head, null);
        loadStagedAddition();
        loadStagedRemoval();

        if (stagedRemoval.isEmpty() && stagedAddition.isEmpty()) {
            if (!message.substring(0, 6).equals("Merged")) {
                System.out.println("No changes added to the commit.");
            }
        }

        newCommit.addToStagingArea(stagedAddition);
        newCommit.rmFromStagingArea(stagedRemoval);
        saveCommit(newCommit);
        head = hashCommit(newCommit);
        branches.put(workingBranch, head);
        saveBranches();
        stagedAddition.clear();
        stagedRemoval.clear();
        saveStagedAddition();
        saveStagedRemoval();
    }

    public void log() {
        Commit currentCommit = loadCommit(head);
        while (currentCommit.parent() != null) {
            printCommit(currentCommit);
            currentCommit = loadCommit(currentCommit.parent());
        }
        printCommit(currentCommit);
    }

    public void globalLog() {
        if (plainFilenamesIn(COMMITS_DIR) == null) {
            return;
        }
        for (String fileName : plainFilenamesIn(COMMITS_DIR)) {
            if (!(fileName.substring(fileName.length() - 2).equals("tf"))) {
                Commit currentCommit = loadCommit(fileName);
                printCommit(currentCommit);
            }
        }
    }

    public void checkout(String commitToBeLoaded, String fileName) {
        if (commitToBeLoaded.length() < 40) {
            commitToBeLoaded = abbreviated(commitToBeLoaded);
        }
        if (!plainFilenamesIn(COMMITS_DIR).contains(commitToBeLoaded)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit workingCommit = loadCommit(commitToBeLoaded);
        if (!workingCommit.trackedFiles().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        workingCommit.loadTrackedFiles(hashCommit(workingCommit));
        String loadedBlob = loadBlob((String) workingCommit.trackedFiles().get(fileName));
        File f = join(CWD, fileName);
        writeContents(f, loadedBlob);
    }

    private String abbreviated(String commitToBeLoaded) {
        for (Object file : plainFilenamesIn(COMMITS_DIR)) {
            String name = (String) file;
            if (commitToBeLoaded.equals(name.substring(0, commitToBeLoaded.length()))) {
                if (!name.substring(name.length() - 2).equals("tf")) {
                    commitToBeLoaded = name;
                }
            }
        }
        return commitToBeLoaded;
    }

    public void checkoutFile(String fileName) {
        checkout(head, fileName);
    }

    public void checkoutCommit(String commitID, String fileName) {
        checkout(commitID, fileName);
    }

    public void checkoutBranch(String branch) {
        loadBranches();
        String headOfBranch = (String) branches.get(branch);

        if (!branches.containsKey(branch)) {
            System.out.println("No such branch exists.");
            return;
        } else if (branch.equals(workingBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }

        Commit branchCommit = loadCommit(headOfBranch);
        Commit workingCommit = loadCommit(head);

        for (Object file : plainFilenamesIn(CWD)) {
            String fileName = (String) file;
            if (!workingCommit.trackedFiles().containsKey(fileName)) {
                if (branchCommit.trackedFiles().containsKey(fileName)) {
                    String workingBlob = sha1(readContentsAsString(join(CWD, fileName)));
                    String branchBlob = (String) branchCommit.trackedFiles().get(fileName);
                    if (!workingBlob.equals(branchBlob)) {
                        System.out.println("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        return;
                    }
                }
            }
        }

        for (Object file : branchCommit.trackedFiles().keySet()) {
            if (!branchCommit.trackedFiles().containsKey((String) file)) {
                restrictedDelete((String) file);
            }
            checkout(headOfBranch, (String) file);
        }

        for (Object file : stagedAddition.keySet()) {
            if (!branchCommit.trackedFiles().containsKey(file)) {
                restrictedDelete((String) file);
            }
        }

        for (Object file : workingCommit.trackedFiles().keySet()) {
            if (!branchCommit.trackedFiles().containsKey(file)) {
                restrictedDelete((String) file);
            }
        }

        File trackingHeadFiles = join(CWD, headOfBranch + "tf");
        writeObject(trackingHeadFiles, workingCommit.trackedFiles());
        stagedAddition.clear();
        stagedRemoval.clear();
        saveStagedAddition();
        saveStagedRemoval();
        workingBranch = branch;
        head = headOfBranch;
    }

    public void branch(String branchName) {
        loadBranches();
        if (branches.containsKey(branchName)) {
            System.out.println("Branch with that name already exists.");
        } else {
            branches.put(branchName, head);
            saveBranches();
        }
    }

    public void find(String message) {
        boolean contains = false;
        if (plainFilenamesIn(COMMITS_DIR) == null) {
            return;
        }
        for (String fileName : plainFilenamesIn(COMMITS_DIR)) {
            if (!(fileName.substring(fileName.length() - 2).equals("tf"))) {
                Commit currentCommit = loadCommit(fileName);
                if (currentCommit.message().equals(message)) {
                    contains = true;
                    System.out.println(fileName);
                }
            }
        }
        if (!contains) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        loadBranches();
        loadStagedRemoval();
        loadStagedAddition();

        System.out.println("=== Branches ===");
        ArrayList<String> forBranch = new ArrayList<>();
        for (Object stringy : branches.keySet()) {
            forBranch.add((String) stringy);
        }
        java.util.Collections.sort(forBranch);
        for (Object branch : forBranch) {
            String branchName = (String) branch;
            String branchCode = (String) branches.get(branchName);
            if (branchCode.equals(head)) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();


        //staged files
        System.out.println("=== Staged Files ===");
        ArrayList<String> forAdd = new ArrayList<>();
        for (Object stringy : stagedAddition.keySet()) {
            forAdd.add((String) stringy);
        }
        java.util.Collections.sort(forAdd);
        for (Object item : forAdd) {
            System.out.println(item);
        }
        System.out.println();


         //removed files
        System.out.println("=== Removed Files ===");
        ArrayList<String> forRemoval = new ArrayList<>();
        for (Object stringy : stagedRemoval.keySet()) {
            forRemoval.add((String) stringy);
        }
        java.util.Collections.sort(forRemoval);
        for (Object rm : forRemoval) {
            System.out.println(rm);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public void rmBranch(String branchName) {
        loadBranches();
        if (branches.get(branchName) == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branches.get(branchName).equals(head)) {
            System.out.println("Cannot remove the current branch.");
        } else if (branches.containsKey(branchName)) {
            branches.remove(branchName);
            saveBranches();
        }
    }

    public void reset(String commitID) {
        loadBranches();
        if (!plainFilenamesIn(COMMITS_DIR).contains(commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        if (commitID.length() < 40) {
            commitID = abbreviated(commitID);
        }
        Commit workingCommit = loadCommit(commitID);
        Commit headCommit = loadCommit(head);
        workingCommit.loadTrackedFiles(commitID);
        headCommit.loadTrackedFiles(head);
        String work = workingBranch;
        branches.put("temp", commitID);
        saveBranches();
        checkoutBranch("temp");
        branches.remove("temp");
        workingBranch = work;
        branches.put(workingBranch, commitID);
        head = commitID;
        saveBranches();
    }

    public void merge(String branchName) {
        boolean conflicted = false;
        loadBranches();
        loadStagedAddition();
        loadStagedRemoval();
        if (branches.get(branchName) == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (!(stagedAddition.isEmpty() && stagedRemoval.isEmpty())) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        boolean untrack = untracked(branchName);
        if (untrack) {
            return;
        }
        if (branchName.equals(workingBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        String splitPoint = split(branchName);
        if (splitPoint.equals(branches.get(branchName))) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        ArrayList<String> parentsB = parentsB(branchName);
        if (parentsB.contains(head)) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            commit("Merged " + branchName + " into " + workingBranch + ".");
        }
        Commit branchCommit = loadCommit((String) branches.get(branchName));
        Commit workingCommit = loadCommit(head);
        Commit splitPointCommit = loadCommit(splitPoint);
        for (Object file : branchCommit.trackedFiles().keySet()) {
            String splitFileBlob = (String) splitPointCommit.trackedFiles().get(file);
            String workFileBlob = (String) workingCommit.trackedFiles().get(file);
            String branchFileBlob = (String) branchCommit.trackedFiles().get(file);
            if (splitFileBlob == null) {
                if (workFileBlob == null) {
                    checkout((String) branches.get(branchName), (String) file);
                    stagedAddition.put(file, branchFileBlob);
                } else if (!workFileBlob.equals(branchFileBlob)) {
                    conflicted = conflict((String) file, workingCommit, branchCommit);
                }
            }
        }
        forDelete(workingCommit, branchCommit, splitPoint);
        for (Object file : splitPointCommit.trackedFiles().keySet()) {
            String sb = (String) splitPointCommit.trackedFiles().get(file);
            String wb = (String) workingCommit.trackedFiles().get(file);
            String bb = (String) branchCommit.trackedFiles().get(file);
            if ((wb == null && !(bb == null))) {
                if (!bb.equals(sb)) {
                    conflicted = conflict((String) file, workingCommit, branchCommit);
                }
            }
            if (wb != null && bb != null && !wb.equals(bb) && !wb.equals(sb)) {
                conflicted = conflict((String) file, workingCommit, branchCommit);
            }
            if (bb == null && wb != null) {
                if (wb.equals(sb)) {
                    delete((String) file, splitPoint);
                } else {
                    conflicted = conflict((String) file, workingCommit, branchCommit);
                }
            }
        }
        if (conflicted) {
            System.out.println("Encountered a merge conflict");
        }
        commitMerge(branchName);
        clearStaging();
    }

    private void forDelete(Commit workingCommit, Commit branchCommit, String splitPoint) {
        Commit splitPointCommit = loadCommit(splitPoint);
        for (Object file : workingCommit.trackedFiles().keySet()) {
            String splitFileBlob = (String) splitPointCommit.trackedFiles().get(file);
            String branchFileBlob = (String) branchCommit.trackedFiles().get(file);
            if (splitFileBlob == null && branchFileBlob == null) {
                delete((String) file, splitPoint);
            }
        }
    }

    private void clearStaging() {
        stagedAddition.clear();
        stagedRemoval.clear();
        saveStagedAddition();
        saveStagedRemoval();
    }

    private void delete(String file, String splitPoint) {
        loadStagedRemoval();
        Commit splitPointCommit = loadCommit(splitPoint);
        String splitFileBlob = (String) splitPointCommit.trackedFiles().get(file);
        restrictedDelete(file);
        stagedRemoval.put(file, splitFileBlob);
    }

    private void commitMerge(String branchName) {
        commit("Merged " + branchName + " into " + workingBranch + ".");
        Commit newCommit = loadCommit(head);
        newCommit.changeSecondary(workingBranch);
        saveCommit(newCommit);
    }

    private ArrayList<String> parentsB(String branchName) {
        ArrayList<String> parentsB = new ArrayList<>();
        Commit branchCommit = loadCommit((String) branches.get(branchName));
        while (branchCommit.parent() != null) {
            parentsB.add(branchCommit.parent());
            branchCommit = loadCommit(branchCommit.parent());
        }
        return parentsB;
    }

    private String split(String branchName) {
        Commit workingCommit = loadCommit(head);
        Commit branchCommit = loadCommit((String) branches.get(branchName));
        ArrayList<String> parentsW = new ArrayList<>();
        while (workingCommit.parent() != null) {
            parentsW.add(workingCommit.parent());
            workingCommit = loadCommit(workingCommit.parent());
        }
        if (parentsW.contains((String) branches.get(branchName))) {
            return (String) branches.get(branchName);
        }
        while (branchCommit.parent() != null) {
            if (parentsW.contains(branchCommit.parent())) {
                break;
            }
            branchCommit = loadCommit(branchCommit.parent());
        }
        return branchCommit.parent();
    }

    private boolean untracked(String branch) {
        loadBranches();
        String headOfBranch = (String) branches.get(branch);

        Commit branchCommit = loadCommit(headOfBranch);
        Commit workingCommit = loadCommit(head);

        for (Object file : plainFilenamesIn(CWD)) {
            String fileName = (String) file;
            if (!workingCommit.trackedFiles().containsKey(fileName)) {
                if (branchCommit.trackedFiles().containsKey(fileName)) {
                    String workingBlob = sha1(readContentsAsString(join(CWD, fileName)));
                    String branchBlob = (String) branchCommit.trackedFiles().get(fileName);
                    if (!workingBlob.equals(branchBlob)) {
                        System.out.println("There is an untracked file in the way; "
                                + "delete it, or add and commit it first.");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean conflict(String file, Commit workingCommit, Commit branchCommit) {
        File conflict = join(CWD, file);
        String f = (String) workingCommit.trackedFiles().get(file);
        String s = (String) branchCommit.trackedFiles().get(file);
        String loadedF = "";
        String loadedS = "";
        if (f != null) {
            loadedF = loadBlob(f);
        }
        if (s != null) {
            loadedS = loadBlob(s);
        }
        writeContents(conflict, "<<<<<<< HEAD" + "\n", loadedF,
                "=======" + "\n", loadedS, ">>>>>>>" + "\n");
        stagedAddition.put(file, sha1(readContentsAsString(conflict)));
        //System.out.println(readContentsAsString(conflict));
        return true;
    }







    public void loadBranches() {
        File branchesFile = join(GITLET_DIR, "branches");
        branches = readObject(branchesFile, HashMap.class);
    }
    public void saveBranches() {
        File branchesFile = join(GITLET_DIR, "branches");
        writeObject(branchesFile, branches);
    }
    public void loadStagedAddition() {
        File stagedAdditionFile = join(GITLET_DIR, "stagedAddition");
        stagedAddition = readObject(stagedAdditionFile, HashMap.class);
    }
    public void saveStagedAddition() {
        File stagedAdditionFile = join(GITLET_DIR, "stagedAddition");
        writeObject(stagedAdditionFile, stagedAddition);
    }
    public void loadStagedRemoval() {
        File stagedRemovalFile = join(GITLET_DIR, "stagedRemoval");
        stagedRemoval = readObject(stagedRemovalFile, HashMap.class);
    }
    public void saveStagedRemoval() {
        File stagedRemovalFile = join(GITLET_DIR, "stagedRemoval");
        writeObject(stagedRemovalFile, stagedRemoval);
    }
    public static Commit loadCommit(String commitHash) {
        File commitFile = join(COMMITS_DIR, commitHash);
        Commit loadedCommit = readObject(commitFile, Commit.class);
        loadedCommit.loadTrackedFiles(hashCommit(loadedCommit));
        return loadedCommit;
    }
    public static String hashCommit(Commit commit) {
        byte[] serializedCommit = serialize(commit);
        return sha1(serializedCommit);
    }
    public static void saveCommit(Commit commit) {
        String commitHash = hashCommit(commit);
        File commitFile = join(COMMITS_DIR, commitHash);
        File tfFile = join(COMMITS_DIR, commitHash + "tf");
        writeObject(commitFile, commit);
        writeObject(tfFile, commit.trackedFiles());
    }
    public static void saveBlob(String blob) {
        String blobHash = sha1(blob);
        File blobFile = join(BLOBS_DIR, blobHash);
        if (blobFile.exists()) {
            return;
        }
        writeContents(blobFile, blob);
    }
    public static String loadBlob(String blobHash) {
        File blobFile = join(BLOBS_DIR, blobHash);
        return readContentsAsString(blobFile);
    }

    private void printCommit(Commit currentCommit) {
        System.out.println("===");
        System.out.println("commit " + hashCommit(currentCommit));
        System.out.println("Date: " + currentCommit.timeStamp());
        System.out.println(currentCommit.message() + "\n");
    }
}
