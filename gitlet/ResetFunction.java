package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static gitlet.CheckoutFunction.findCommitFromShorthand;
import static gitlet.Main.userMessage;
import static gitlet.Repository.deSerialize;
import static gitlet.Utils.*;

/**
 * Part of project2
 * Created by Ray on 7/16/2017.
 */
public class ResetFunction extends Function {

    public ResetFunction(Repository repository, String[] args) {
        super(repository, args);
    }

    public boolean checkArgs() {
        return this.args.length == 2;
    }

    public void apply() {
        if (!this.checkArgs()) {
            this.badArgumentNumber();
            return;
        } else {
            String commitID = findCommitFromShorthand(args[1]);
            Commit givenCommit = repository.getCommit(commitID);
            Commit headCommit = repository
                    .getStagingCommit(repository
                            .getCurrentBranch())
                    .getParentCommit();         // current head commit;
            if (givenCommit == null) {
                userMessage("No commit with that id exists.");
                return;
            }

            List<String> workingFiles = plainFilenamesIn(".");
            for (String directoryFile : workingFiles) {
                if (!headCommit.blobs.containsKey(directoryFile)) {
                    String givenFilenameSHA1 = givenCommit.blobs.get(directoryFile);
                    File direcFile = new File(directoryFile);
                    String direcFilenameSHA1 = sha1(readContents(direcFile));
                    if (givenFilenameSHA1 != null
                            && !givenFilenameSHA1.equals(direcFilenameSHA1)) {
                        userMessage("There is an untracked file in the way;"
                                + "delete it or add it first.");
                        return;
                    }
                }
            }

            for (String workingFile : workingFiles) {
                restrictedDelete(workingFile);
            }

            for (HashMap.Entry<String, String> entry : givenCommit.blobs.entrySet()) {
                if (entry.getValue() != null) {
                    File outFile = new File(entry.getKey());
                    byte[] fileContent = (byte[]) deSerialize(entry.getValue());
                    writeContents(outFile, fileContent);
                }
            }

            Commit newStagingCommit = new Commit(commitID, "");
            newStagingCommit.blobs = (HashMap) givenCommit.blobs.clone();
            this.repository.serializeCommit(newStagingCommit, newStagingCommit.getSHA1());

            this.repository.getBranches()
                    .put(this.repository.getCurrentBranch(), newStagingCommit.getSHA1());

        }
    }
}
