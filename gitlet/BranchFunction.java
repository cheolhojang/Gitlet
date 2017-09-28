package gitlet;

import static gitlet.Main.debugPrint;
import static gitlet.Main.userMessage;

/**
 * Part of project2
 * Created by Ray on 7/16/2017.
 */
public class BranchFunction extends Function {

    public BranchFunction(Repository repository, String[] args) {
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
            String desiredBranch = this.args[1];

            if (this.repository == null) {
                debugPrint("repository is null");
            }

            debugPrint("new branch " + desiredBranch);

            String currentBranch = this.repository.getCurrentBranch();
            Commit currentStaging = this.repository.getStagingCommit(currentBranch);
            Commit currentHead = currentStaging.getParentCommit();

            debugPrint("    current branch " + currentBranch);
            debugPrint("        staged @ " + currentStaging.getSHA1());
            debugPrint("        head @ " + currentHead.getSHA1());

            if (this.repository.getBranches().containsKey(desiredBranch)) {
                userMessage("A branch with that name already exists.");
                return;
            }

            Commit newStaging = new Commit(currentHead.getSHA1(), "");

            debugPrint("    new branch " + desiredBranch);
            debugPrint("        staged @ " + newStaging.getSHA1());
            debugPrint("        head @ " + newStaging.getParentCommit().getSHA1());
            this.repository.serializeCommit(newStaging, newStaging.getSHA1());
            this.repository.addBranch(desiredBranch, newStaging.getSHA1());
            // this.repository.addBranch(desiredBranch, currentHead.getSHA1());
            // Points desiredBranch to HEAD instead of to the newStaging Commit
        }

    }
}
