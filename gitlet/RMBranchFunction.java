package gitlet;

import static gitlet.Main.userMessage;

/**
 * Part of project2
 * Created by Ray on 7/16/2017.
 */
public class RMBranchFunction extends Function {

    public RMBranchFunction(Repository repository, String[] args) {
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
            String branchName = this.args[1];
            if (!this.repository.getBranches().containsKey(branchName)) {
                userMessage("A branch with that name does not exist.");
                return;
            } else if (this.repository.getCurrentBranch().equals(branchName)) {
                userMessage("Cannot remove the current branch.");
                return;
            } else {
                this.repository.getBranches().remove(branchName);
            }
        }
    }
}
