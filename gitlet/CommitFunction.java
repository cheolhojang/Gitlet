package gitlet;

// import java.util.Date;
import java.util.HashMap;

import static gitlet.Main.userMessage;

/**
 * Part of project2
 * Created by Ray on 7/15/2017.
 */
public class CommitFunction extends Function {

    public CommitFunction(Repository repository, String[] args) {
        super(repository, args);
    }

    public boolean checkArgs() {
        if (this.args.length != 2) {
            this.badArgumentNumber();
            return false;
        } else if (this.args[1].equals("")) {
            userMessage("Please enter a commit message.");
            return false;
        }
        return true;
    }

    /** 1) deserialize current staging commit
     *  2) create new staging commit (old as parent, copy old's content)
     *  3)
     */
    public void apply() {
        if (!this.checkArgs()) {
            return;
        }
        String logMessage = args[1];
        String currentBranch = repository.getCurrentBranch();
        String currentStagingSHA1 = repository.getStagingSHA1(currentBranch);
        Commit currentStagingCommit = repository.getStagingCommit(currentBranch);
        currentStagingCommit.setTimeStamp();
        currentStagingCommit.logMessage = logMessage;
        Commit newStaging = new Commit(currentStagingSHA1, "");
        HashMap<String, String> clonedHashMap = new HashMap<>();

        for (HashMap.Entry<String, String> entry : currentStagingCommit.blobs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null) {
                clonedHashMap.put(key, value);
            }
        }

        if (!checkForChangedFiles(currentStagingCommit)) {              // No changes
            userMessage("No changes added to the commit.");
            return;
        }

        newStaging.blobs = clonedHashMap;
        String newStagingSHA1 = newStaging.getSHA1();

        repository.updateBranchSHA1(currentBranch, newStagingSHA1);
        repository.serializeCommit(newStaging, newStagingSHA1);
        repository.serializeCommit(currentStagingCommit, currentStagingSHA1);
    }

    public boolean checkForChangedFiles(Commit currentStage) {
        String lastCommitSHA1 = currentStage.parent;
        Commit lastCommit = repository.getCommit(lastCommitSHA1);
        HashMap<String, String> stagingBlobs = currentStage.blobs;
        HashMap<String, String> committedBlobs = lastCommit.blobs;
        return !stagingBlobs.equals(committedBlobs);
    }
}
