package gitlet;


import static gitlet.Main.debugPrint;
import static gitlet.Utils.restrictedDelete;

/**
 * Part of project2
 * Created by Ray on 7/15/2017.
 */
public class RMFunction extends Function {

    public RMFunction(Repository repository, String[] args) {
        super(repository, args);
    }

    public boolean checkArgs() {
        return args.length == 2;
    }

    public void apply() {
        // System.out.println("in RMFunction.apply");
        String name = args[1];
        String branch = this.repository.getCurrentBranch();
        Commit stagingCommit = this.repository.getStagingCommit(branch);
        Commit headCommit = stagingCommit.getParentCommit();



        if (headCommit.blobs.containsKey(name)) {               // tracked
            restrictedDelete(name);
            debugPrint("it's tracked");
            stagingCommit.removeFile(name);
        } else if (stagingCommit.hasFileName(name)) {           // staged
            stagingCommit.blobs.remove(name);
        } else {

            System.out.println("No reason to remove the file.");
        }
        this.repository.serializeCommit(stagingCommit, stagingCommit.getSHA1());
    }
}
