package gitlet;

import java.util.ArrayList;

import static gitlet.Main.userMessage;

/**
 * Created by Sean on 16/07/2017.
 */

/**
 * This function only take one-branch situation into account for now
 */
public class FindFunction1 extends Function {

    public FindFunction1(Repository repository, String[] args) {
        super(repository, args);
    }

    public boolean checkArgs() {
        if (this.args.length != 2) {
            this.badArgumentNumber();
            return false;
        } else if (this.args[1].equals("")) { // change this into when commit message is not found
            userMessage("Found no commit with that message.");
            return false;
        }
        return true;
    }

    public void apply() {
        if (!this.checkArgs()) {
            return;
        }
        String givenCommitMessage = args[1];
        boolean matchFound = false;
        for (String branchName : repository
                .getBranches().keySet()) {    //iterate through all existing branches
            String stagingSha1 = repository.getBranches().get(branchName);
            Commit assessingCommit = repository.getCommit(stagingSha1);
            ArrayList<String> matchingCommits = new ArrayList<String>();
            while (assessingCommit.parent != null) { // ALERT: off by one
                if (givenCommitMessage.equals(assessingCommit.logMessage)) {
                    matchingCommits.add(assessingCommit.getSHA1());
                    matchFound = true;
                }
                assessingCommit = repository.getCommit(assessingCommit.parent);
            }
            //compensate for the last commit in branch
            if (givenCommitMessage.equals(assessingCommit.logMessage)) {
                matchingCommits.add(assessingCommit.getSHA1());
                matchFound = true;
            }
            //Deal with output
            if (matchFound) {
                userMessage("Commit with following ID have inquired log message:");
                for (int i = 0; i < matchingCommits.size(); i++) {
                    userMessage(matchingCommits.get(i));
                }
            } else {
                userMessage("Found no commit with that message.");
            }
        }
    }
}
