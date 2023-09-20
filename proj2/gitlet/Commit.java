package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  Keeps track of files commit objects tracks
 *
 *  @author alysa liu
 */
public class Commit implements Serializable {
    /** The message of this Commit. */
    private String message;
    /** The author of this Commit. */
    private String author;
    /** The timestamp of this Commit. */
    private String timeStamp;
    /** The files tracked by this Commit. */
    private HashMap trackedFiles;
    /** The parent of this Commit. */
    private String parent;
    /** The second parent of this Commit. */
    private String secondary;
    /** The current working directory. */
    private static final File CWD = new File(System.getProperty("user.dir"));
    /** The directory of commits. */
    private static final File COMMITS_DIR = join(CWD, ".gitlet", "commits");

    public Commit(Date date, String message, String parent, String secondary) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("E MMM d HH:mm:ss y Z");
        this.message = message;
        this.author = "WHOISTHEAUTHOR??";
        this.timeStamp = dateFormatter.format(date);
        trackedFiles = new HashMap<String, String>();
        this.parent = parent;
        this.secondary = secondary;
        if (!date.equals(new Date(0))) {
            Commit parentCommit = loadCommit(parent);
            for (Object entry : parentCommit.trackedFiles.entrySet()) {
                HashMap.Entry entryMap = (Map.Entry) entry;
                trackedFiles.put(entryMap.getKey(), entryMap.getValue());
            }
            if (secondary != null) {
                Commit secondaryCommit = loadCommit(secondary);
                for (Object entry : secondaryCommit.trackedFiles.entrySet()) {
                    HashMap.Entry entryMap = (Map.Entry) entry;
                    trackedFiles.put(entryMap.getKey(), entryMap.getValue());
                }
            }
        }
    }

    public String message() {
        return message;
    }

    public String author() {
        return author;
    }

    public String timeStamp() {
        return timeStamp;
    }

    public HashMap trackedFiles() {
        return trackedFiles;
    }

    public String parent() {
        return parent;
    }

    public void changeSecondary(String secondHash) {
        this.secondary = secondHash;
    }

    public Commit loadCommit(String hash) {
        File commitFile = join(COMMITS_DIR, hash);
        return readObject(commitFile, Commit.class);
    }

    public void loadTrackedFiles(String commitHash) {
        File tfFile = join(COMMITS_DIR, commitHash + "tf");
        trackedFiles = readObject(tfFile, HashMap.class);
    }

    public void addToStagingArea(HashMap stage) {
        for (Object entry : stage.entrySet()) {
            HashMap.Entry entryMap = (Map.Entry) entry;
            trackedFiles.put(entryMap.getKey(), entryMap.getValue());
        }
    }

    public void rmFromStagingArea(HashMap stage) {
        Iterator iter = stage.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry currElement = (Map.Entry) iter.next();
            trackedFiles.remove(currElement.getKey());
        }
    }
}
