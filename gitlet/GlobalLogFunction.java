package gitlet;

import java.util.HashMap;
import java.util.List;

import static gitlet.Main.userMessage;
import static gitlet.Utils.plainFilenamesIn;
import static gitlet.Repository.deSerialize;

/**
 * Part of project2
 * Created by Ray on 7/16/2017.
 */
public class GlobalLogFunction extends Function {

    public GlobalLogFunction(Repository repository, String[] args) {
        super(repository, args);
    }

    public boolean checkArgs() {
        return this.args.length == 1;
    }

    private HashMap<String, String> alreadySeen = new HashMap<>();

    public void apply() {
        List<String> objectSHA1s = plainFilenamesIn(".gitlet");
        for (String serializedFile : objectSHA1s) {
            try {
                Commit deserialized = (Commit) deSerialize(serializedFile);
                this.commitPrint(deserialized);
            } catch (ClassCastException e) {
                continue;
            }
        }
    }

    public void commitPrint(Commit commit) {
        String commitSHA1 = commit.getSHA1();
        if (commit.logMessage.equals("")) {
            return;
        }
        userMessage("===");
        userMessage("Commit " + commitSHA1);
        userMessage(commit.timeStamp);
        userMessage(commit.logMessage);
        userMessage("");
    }
}
