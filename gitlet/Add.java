package gitlet;

//import java.io.File;

//import static gitlet.Main.userMessage;

/**
 * Part of project2
 * Created by Ray on 7/12/2017.
 */
public class Add extends Function {
    /** Process args in Repository 'gitlet' **/

    public Add(Repository repos, String[] args) {
        super(repos, args);
    }

    public void apply() {

        //PLACE THE COMMIT ONTO THE STAGE
        if (!this.checkArgs()) {
            this.badArgumentNumber();
            return;
        }
        String name = args[1];
        String branch = repository.getCurrentBranch();
        String stagingHead = repository.getStagingSHA1(branch);
        Commit stagelocation = repository.getCommit(stagingHead);
        stagelocation.addFile(name);

        repository.serializeCommit(stagelocation, stagelocation.getSHA1());


    }

    /** Assert we have good arguments **/
    public boolean checkArgs() {
        if (args.length != 2) {
            this.badArgumentNumber();
            return false;
        }
        return true;
    }

}
