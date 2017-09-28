package gitlet;

import java.io.Serializable;
import java.util.HashMap;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


// import static gitlet.Main.debugPrint;
import static gitlet.Utils.sha1;
import static gitlet.Utils.writeContents;

/**
 * Part of project2
 * Created by Ray on 7/12/2017.
 */
public class Repository implements Serializable {


    /** Takes a filename and a String's SHA1-ID  and writes the string's content
     * into fileName
     * @param fileName name of a file in directory
     * @param newContentSHA1 SHA-1 of a String to write to it
     */
    public void updateFileInDirectory(String fileName, String newContentSHA1) {
        try {
            File outFile = new File(fileName);
            if (outFile == null) {
                return;
            }
            byte[] content = (byte[]) deSerialize(newContentSHA1);
            if (content == null) {
                return;
            }

            writeContents(outFile, content);
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public static final String PATH = ".gitlet/";

    /** Name of current branch, can use branches map to find pointer **/
    private String currentBranch;

    /** Maps branch name to Commit SHA-1 ID **/
    private HashMap<String, String> branches;

    public HashMap<String, String> getBranches() {
        return this.branches;
    }

    public String getCurrentBranch() {
        return this.currentBranch;
    }

    public void setCurrentBranch(String newBranchName) {
        this.currentBranch = newBranchName;
    }

    public Commit getStagingCommit(String branchName) {
        if (!this.existsBranch(branchName)) {
            return null;
        } else {
            String stagingSHA1 = this.getStagingSHA1(branchName);
            Commit stagingCommit = this.getCommit(stagingSHA1);
            return stagingCommit;
        }
    }

    public boolean updateBranchSHA1(String branchName, String newSHA1) {
        if (!this.existsBranch(branchName)) {
            return false;
        } else {
            this.branches.put(branchName, newSHA1);
            return true;
        }
    }

    public Repository(Commit initialCommit) {
        File f = new File(PATH);
        try {
            f.mkdir();
        } catch (SecurityException e) {
            return;
        }
        this.branches = new HashMap<String, String>();
        this.addBranch("master", initialCommit.getSHA1());
        this.currentBranch = "master";
        this.serializeCommit(initialCommit, initialCommit.getSHA1());
    }


    public void addBranch(String branchName, String headSHA1) {
        assert branchName != null;
        assert headSHA1 != null;
        this.branches.put(branchName, headSHA1);
    }

    /** Get SHA-1 of staging Commit given branch name **/
    public String getStagingSHA1(String branchName) {
        return branches.get(branchName);
    }

    /** True iff there's a branch with given name **/
    public boolean existsBranch(String branchName) {
        return this.branches.containsKey(branchName);
    }

    /** True iff there's an existant .gitlet directory else False **/
    public static boolean isInitialized() {
        File f = new File(PATH);
        return (f.exists() && f.isDirectory());

    }














    /** !!! DON'T USE !!! USE getCommit(sha1) OR getText(sha1) INSTEAD !!! **/
    private Object deSerializeObject(String sha1FileName) {
        return deSerialize(sha1FileName);
    }

    /** Deserialize a Commit : Get a Commit given its SHA-1 ID (NULL if not a Commit) **/
    public Commit getCommit(String commitSHA1) {
        try {
            return (Commit) this.deSerializeObject(commitSHA1);
        } catch (ClassCastException e) {
            return null;
        }
    }

    /** Serialize a Commit : True if successful **/
    public boolean serializeCommit(Commit commit, String commitSHA1) {
        return serializeObject(commit, commitSHA1);
    }

    /** Serialize this Repository instance as 'GITLET_REPOSITORY' **/
    public boolean serializeRepository() {
        return serializeObject(this, "repository");
    }

    /** !!! DON'T USE !!! USE serializeString OR serializeObject INSTEAD !!!**/
    public static boolean serializeObject(Object object, String sha1) {
        if (object == null) {
            return false;
        } else {
            try {
                File outFile = new File(".gitlet/" + sha1);
                ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outFile));
                out.writeObject(object);
                //debugPrint("Serialized: " + object.toString() + " as " + sha1);
                out.close();
            } catch (IOException e) {
                return false;
            }
            return true;
        }
    }


    public static Object deSerialize(String sha1FileName) {
        Object object;
        File inFile = new File(".gitlet/" + sha1FileName);
        try {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(inFile));
            object = input.readObject();
            input.close();
            //debugPrint("DeSerialized: " + sha1FileName + " to " + object.toString());
        } catch (IOException | ClassNotFoundException excp) {
            object = null;
        }
        return object;
    }
}
