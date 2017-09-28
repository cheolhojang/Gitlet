package gitlet;

/**
 * Created by cjjang on 7/15/17.
 */
public class Rm extends Function {
    public Rm(Repository repos, String[] args) {
        super(repos, args);
    }

    public void apply() {

        String name = args[1];
        String branch = repository.getCurrentBranch();
        Commit stagelocation = repository.getStagingCommit(branch);


        Commit headPointer = stagelocation.getParentCommit();


        if (!stagelocation.hasFileName(name)) {
            if (headPointer == null) {
                System.out.println("No head and not on the stage");
            } else {
                if (headPointer.hasFileName(name)) {
                    System.out.println("Not in the Head Pointer and not in the Stage Commit");
                }
            }
        } else {
            if (stagelocation.hasFileName(name)) {

                stagelocation.removeFile(name);
                if (repository.serializeCommit(stagelocation, stagelocation.getSHA1())) {
                    repository.deSerialize(name);
                }
            }
        }
    }

    /** Assert we have good arguments **/
    public boolean checkArgs() {
        if (args[0].equals("rm")) {
            return true;
        } else if (args.length != 2) {
            this.badArgumentNumber();
            return false;
        }
        return true;
    }
}
