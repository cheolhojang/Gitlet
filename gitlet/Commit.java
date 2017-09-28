package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Main.debugPrint;
import static gitlet.Main.userMessage;
import static gitlet.Repository.deSerialize;
import static gitlet.Repository.serializeObject;
import static gitlet.Utils.readContents;
import static gitlet.Utils.sha1;

/**
 * Part of project2
 * Created by Ray on 7/12/2017.
 */
public class Commit implements Serializable {

    String sha1;

    /**
     * SHA-1 reference to parent
     **/
    String parent;

    /**
     * User-supplied log message
     **/
    String logMessage;

    /**
     * Date object to keep track of when it was created
     **/
    String timeStamp;

    /**
     * Maps filename to text String of file SHA-1
     **/
    HashMap<String, String> blobs;

    /**
     * General constructor
     **/

    String timeStampForStaging;
    public Commit(String parentSHA1, String log) {
        Commit parentCommit = (Commit) deSerialize(parentSHA1);

        if (parentCommit != null && parentCommit.blobs != null) {
            this.blobs = (HashMap<String, String>) parentCommit.blobs.clone();
        } else {
            this.blobs = new HashMap<>();
        }

        this.parent = parentSHA1;
        this.logMessage = log;
        this.timeStampForStaging = new Date().toString();
        this.sha1 = sha1(this.parent + this.logMessage + this.timeStampForStaging);
    }



    public Commit getParentCommit() {
        return (Commit) deSerialize(this.parent);
    }

    /**
     * Add filename to Commit (add command) and update SHA-1
     **/
    public void addFile(String filename) {
        File inFile = new File(filename);
        byte[] contents;
        String contentSHA1;
        try {
            contents = readContents(inFile);
            contentSHA1 = sha1(contents);
        } catch (IllegalArgumentException e) {
            userMessage("File does not exist.");
            return;
        }
        if (this.blobs.containsKey(filename)) {
            if (this.blobs.get(filename) == null) {
                Commit myParent = this.getParentCommit();
                String parentFileSHA1 = myParent.blobs.get(filename);
                this.blobs.put(filename, contentSHA1);
            } else if (this.blobs.get(filename).equals(contentSHA1)) {
                debugPrint("identical contents" + this.blobs.size());
                return;
            } else {
                this.blobs.put(filename, contentSHA1);
                debugPrint("same name different content");
            }
        } else {
            this.blobs.put(filename, contentSHA1);
            debugPrint("new content");
        }
        serializeObject(contents, contentSHA1);
    }

    public void removeFile(String filename) {
        if (this.blobs.containsKey(filename)) {
            this.blobs.put(filename, null);
            debugPrint("removed " + filename);
            Commit stagingCommit = this;
            if (stagingCommit.blobs.containsKey(filename)
                    && stagingCommit.blobs.get(filename) == null) {
                debugPrint("it got here");
            }
        }
    }

    public boolean hasFileName(String filename) {
        return this.blobs.containsKey(filename);
    }

    public boolean hasFileSHA1(String fileSHA1) {
        return this.blobs.containsValue(fileSHA1);
    }

    public void setTimeStamp() {
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public String getSHA1() {
        return this.sha1;
    }

    public Commit(String log) {
        this(null, log);
    }

}
