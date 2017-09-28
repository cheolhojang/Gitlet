package gitlet;

// import com.sun.org.apache.regexp.internal.RE;

import static gitlet.Main.userMessage;

/**
 * Part of project2
 * Created by Ray on 7/15/2017.
 */
public class LogFN extends Function {

    public LogFN(Repository repository, String[] args) {
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
        Commit stagingCommit = repository.getStagingCommit(repository.getCurrentBranch());
        if (stagingCommit != null) {
            Commit headComit = repository.getCommit(stagingCommit.parent);
            recursivePrinter(headComit);
        }
    }

    private void recursivePrinter(Commit head) {
        if (head == null) {
            return;
        } else {
            userMessage("===");
            userMessage("Commit " + head.getSHA1());
            userMessage(head.timeStamp);
            userMessage(head.logMessage);
            if (head.getParentCommit() == null) {
                return;
            }
            userMessage("");
            recursivePrinter(head.getParentCommit());
        }

    }
}
