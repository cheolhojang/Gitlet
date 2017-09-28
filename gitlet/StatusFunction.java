package gitlet;


//import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import java.util.Map;

import static gitlet.Main.debugPrint;
import static gitlet.Main.userMessage;
import static gitlet.Utils.plainFilenamesIn;
//import static gitlet.Utils.readContents;
//import static gitlet.Utils.sha1;

/**
 * Part of project2
 * Created by Ray on 7/15/2017.
 */
public class StatusFunction extends Function {

    public StatusFunction(Repository repository, String[] args) {
        super(repository, args);
    }

    public boolean checkArgs() {
        return args.length == 1;
    }

    public void apply() {
        if (!this.checkArgs()) {
            this.badArgumentNumber();
            return;
        }

        ArrayList<String> stagedFiles = new ArrayList<>();
        ArrayList<String> removedFiles = new ArrayList<>();

        List<String> allFiles = plainFilenamesIn(".");

        Commit stagingCommit = this.repository.getStagingCommit(this.repository.getCurrentBranch());
        Commit headCommit = stagingCommit.getParentCommit();

        for (HashMap.Entry<String, String> entry : stagingCommit.blobs.entrySet()) {
            String fileName = entry.getKey();
            String fileSHA1 = entry.getValue();
            if (stagingCommit.blobs.containsKey(fileName)
                    && stagingCommit.blobs.get(fileName) == null) {
                debugPrint("it's null");
                removedFiles.add(fileName);
                debugPrint(stagingCommit.blobs.containsValue(fileSHA1)
                        + " " + !headCommit.blobs.containsValue(fileSHA1));
            } else if (stagingCommit.blobs.containsValue(fileSHA1)
                    && !headCommit.blobs.containsValue(fileSHA1)) {              // file is staged
                stagedFiles.add(fileName);
            }
        }


        userMessage("=== Branches ===");
        for (HashMap.Entry<String, String> entry : this.repository.getBranches().entrySet()) {
            String branchName = entry.getKey();
            if (branchName.equals(this.repository.getCurrentBranch())) {
                userMessage("*" + branchName);
            } else {
                continue; //userMessage(branchName);
            }
        }
        for (HashMap.Entry<String, String> entry : this.repository.getBranches().entrySet()) {
            String branchName = entry.getKey();
            if (!branchName.equals(this.repository.getCurrentBranch())) {
                userMessage(branchName);
            }
        }
        userMessage("");



        userMessage("=== Staged Files ===");
        for (String file : stagedFiles) {
            userMessage(file);
        }
        userMessage("");

        userMessage("=== Removed Files ===");
        for (String file : removedFiles) {
            userMessage(file);
        }
        userMessage("");

        userMessage("=== Modifications Not Staged For Commit ===");
        userMessage("");

        userMessage("=== Untracked Files ===");
        userMessage("");


    }
}
