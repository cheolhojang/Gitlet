package gitlet;

//import java.io.File;

/**
 * Created by cjjang on 7/14/17.
 */
public class Remove extends Function {

    public Remove(Repository repos, String[] args) {
        super(repos, args);
    }

    public void apply() {
        String name = args[1];
        String branch = repository.getCurrentBranch();
        Commit stagelocation = repository.getCommit(branch);
        if (stagelocation.hasFileName(name)) {
            stagelocation.removeFile(name);
            if (repository.serializeCommit(stagelocation, stagelocation.getSHA1())) {
                repository.deSerialize(name);
            }
        } else {
            System.out.println("Can't Remove");
        }
    }

    /** Assert we have good arguments **/
    public boolean checkArgs() {
        if (args[0].equals("Remove")) {
            return true;
        } else if (args.length != 2) {
            this.badArgumentNumber();
            return false;
        }
        return true;
    }

}
