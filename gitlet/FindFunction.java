package gitlet;

import java.util.List;

import static gitlet.Main.userMessage;
import static gitlet.Repository.deSerialize;
import static gitlet.Utils.plainFilenamesIn;

/**
 * Part of project2
 * Created by Ray on 7/16/2017.
 */
public class FindFunction extends Function {

    public FindFunction(Repository repository, String[] args) {
        super(repository, args);
    }
    private static boolean foundMatch = false;
    public boolean checkArgs() {
        return this.args.length == 2;
    }

    public void apply() {
        if (!this.checkArgs()) {
            this.badArgumentNumber();
            return;
        } else {
            String searchFor = args[1];
            if (searchFor.equals("")) {
                return;
            }
            List<String> objectFiles = plainFilenamesIn(".gitlet");
            for (String objectSHA1 : objectFiles) {
                try {
                    Commit foundCommit = (Commit) deSerialize(objectSHA1);
                    tryPrintCommit(foundCommit, searchFor);
                } catch (ClassCastException e) {
                    continue;
                }
            }
            if (!foundMatch) {
                userMessage("Found no commit with that message.");
            }
        }
    }

    public static void tryPrintCommit(Commit tryCommit, String searchFor) {
        if (tryCommit.logMessage.equals(searchFor)) {
            userMessage(tryCommit.sha1);
            foundMatch = true;
        }
    }
}
