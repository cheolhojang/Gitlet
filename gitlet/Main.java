package gitlet;


import static gitlet.Repository.*;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void userMessage(String message) {
        System.out.println(message);
    }
    public static void debugPrint(String message) {
        if (debugging) {
            System.out.println(message);
        }
    }


    static boolean debugging = false;

    public static void main(String... args) {
        if (args.length < 1) {
            userMessage("Please enter a command.");
            return;
        }
        String function = args[0];
        Repository repository;
        if (!function.equals("init") && !isInitialized()) {
            userMessage("Not in an initialized gitlet directory.");
            return;
        }
        if (function.equals("init")) {
            if (args.length != 1) {
                userMessage("Incorrect operands.");
                return;
            }
            if (isInitialized()) {
                userMessage("A gitlet version-control system "
                        + "already exists in the current directory.");
                return;
            } else {
                Commit initialCommit = new Commit("", "initial commit");
                initialCommit.setTimeStamp();
                Commit staging = new Commit(initialCommit.getSHA1(), "");
                repository = new Repository(staging);
                repository.serializeCommit(initialCommit, initialCommit.getSHA1());
                repository.serializeRepository();
                return;
            }
        }
        repository = (Repository) deSerialize("repository");
        if (function.equals("add")) {
            Add add = new Add(repository, args);
            add.apply();
        } else if (function.equals("rm")) {
            RMFunction rmFN = new RMFunction(repository, args);
            rmFN.apply();
        } else if (function.equals("commit")) {
            CommitFunction commitCall = new CommitFunction(repository, args);
            commitCall.apply();
        } else if (function.equals("log")) {
            LogFN logFN = new LogFN(repository, args);
            logFN.apply();
        } else if (function.equals("checkout")) {
            CheckoutFunction checkoutFN = new CheckoutFunction(repository, args);
            checkoutFN.apply();
        } else if (function.equals("status")) {
            StatusFunction statusFN = new StatusFunction(repository, args);
            statusFN.apply();
        } else if (function.equals("branch")) {
            BranchFunction branchFN = new BranchFunction(repository, args);
            branchFN.apply();
        } else if (function.equals("global-log")) {
            GlobalLogFunction globalLogFN = new GlobalLogFunction(repository, args);
            globalLogFN.apply();
        } else if (function.equals("reset")) {
            ResetFunction resetFN = new ResetFunction(repository, args);
            resetFN.apply();
        } else if (function.equals("find")) {
            FindFunction findFN = new FindFunction(repository, args);
            findFN.apply();
        } else if (function.equals("rm-branch")) {
            RMBranchFunction rmBranchFN = new RMBranchFunction(repository, args);
            rmBranchFN.apply();
        } else if (function.equals("merge")) {
            MergeFunction mergeFN = new MergeFunction(repository, args);
            if (mergeFN.checkArgs()) {
                mergeFN.apply();
            }
        } else {
            userMessage("No command with that name exists.");
            return;
        }
        repository.serializeRepository();
    }
}
