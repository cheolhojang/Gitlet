package gitlet;

import java.io.File;
import java.util.HashMap;
import java.util.List;

//import static gitlet.Main.debugPrint;
import static gitlet.Main.userMessage;
import static gitlet.Utils.*;

/**
 * Part of project2
 * Created by Ray on 7/15/2017.
 */
public class CheckoutFunction extends Function {

    public  CheckoutFunction(Repository repository, String[] args) {
        super(repository, args);
    }

    public boolean checkArgs() {
        return this.args.length < 5 && this.args.length > 1;
    }

    public void apply() {
        if (!this.checkArgs()) {
            this.badArgumentNumber();
            return;
        } else if (this.args.length == 3 && this.args[1].equals("--")) {
            overwritePreviousFile(args[2]);
        } else if (this.args.length == 4 && this.args[2].equals("--")) {
            revertFile(this.repository, this.args[1], this.args[3]);
        } else if (this.args.length == 2) {
            String givenBranch = this.args[1];
            if (!this.repository.getBranches().containsKey(givenBranch)) {
                userMessage("No such branch exists.");
                return;
            } else if (this.repository.getCurrentBranch().equals(givenBranch)) {
                userMessage("No need to checkout the current branch.");
                return;
            } else {
                String currentBranch = this.repository.getCurrentBranch();
                String givenStagingSHA1 = this.repository.getStagingSHA1(givenBranch);
                String currentStagingSHA1 = this.repository
                        .getStagingSHA1(currentBranch);
                Commit givenStagingCommit = this.repository.getCommit(givenStagingSHA1);
                Commit currentStagingCommit = this.repository.getCommit(currentStagingSHA1);
                Commit givenHeadCommit = givenStagingCommit.getParentCommit();
                Commit currentHeadCommit = currentStagingCommit.getParentCommit();
                List<String> workingFiles = plainFilenamesIn(".");
                for (HashMap.Entry<String, String> entry : currentStagingCommit.blobs.entrySet()) {
                    if (!currentHeadCommit.blobs.containsKey(entry.getKey())
                            && !(currentHeadCommit.blobs.get(entry.getKey()) == null)) {
                        userMessage("You have uncommitted changes.");
                        return;
                    }
                }
                for (String fileName : workingFiles) {
                    String fileSHA1 = sha1(readContents(new File(fileName)));
                    if (givenHeadCommit.blobs.containsKey(fileName)) {
                        if (!currentHeadCommit.blobs.containsKey(fileName)) {
                            userMessage("There is an untracked file "
                                    + "in the way; "
                                    + "delete it or add it first.");
                            return;
                        }
                    }
                }

                for (String fileName : workingFiles) {
                    if (givenHeadCommit.blobs.containsKey(fileName)) {
                        revertFile(this.repository, givenHeadCommit.getSHA1(), fileName);
                    }
                }

                for (HashMap.Entry<String, String> entry : givenHeadCommit.blobs.entrySet()) {
                    revertFile(this.repository, givenHeadCommit.getSHA1(), entry.getKey());
                }

                for (HashMap.Entry<String, String> entry : currentHeadCommit.blobs.entrySet()) {
                    if (!givenHeadCommit.blobs.containsKey(entry.getKey())) {
                        restrictedDelete(entry.getKey());
                    }
                }
// hey git i'm making a change bitch
                repository.setCurrentBranch(givenBranch);

            }
        } else {
            this.badArgumentNumber();
            return;
        }
    }

    /** Revert a file in current directory to its snapshot in the previous commit
     *
     * @param fileName name of a file in most recent commit directory
     */
    public void overwritePreviousFile(String fileName) {
        Commit stagingCommit = repository.getStagingCommit(repository.getCurrentBranch());
        Commit headCommit = stagingCommit.getParentCommit();
        String newFileContentSHA1 = headCommit.blobs.get(fileName);
        if (!headCommit.hasFileName(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            repository.updateFileInDirectory(fileName, newFileContentSHA1);
        }
    }

    /** Reverts file in current directory to its state in given commit
     *
     * @param commitSHA1 SHA-1 ID of commit to revert to
     * @param fileName Name of file in Commit
     */
    public static void revertFile(Repository repository, String commitSHA1, String fileName) {
        Commit foundCommit;

        if (commitSHA1.length() < 40) {

            commitSHA1 = findCommitFromShorthand(commitSHA1);
        }
        if (commitSHA1 != null) {
            foundCommit = repository.getCommit(commitSHA1);
        } else {
            userMessage("No commit with that id exists.");
            return;
        }
        if (foundCommit == null) {
            userMessage("No commit with that id exists.");
            return;
        } else {
            if (!foundCommit.hasFileName(fileName)) {
                userMessage("File does not exist in that commit.");
            } else {
                String desiredContent = foundCommit.blobs.get(fileName);
                repository.updateFileInDirectory(fileName, desiredContent);
            }
        }
    }

    /** Searches .gitlet for a SHA-1 ID matching the given shorthand
     *
     * @param shortHand SHA-1 ID to find match for
     * @return SHA-1 of serialized object present in .gitlet
     */
    public static String findCommitFromShorthand(String shortHand) {
        List<String> fileNames = plainFilenamesIn(".gitlet");
        for (String file : fileNames) {
            if (!file.equals("repository")
                && file.substring(0, shortHand.length()).equals(shortHand)) {
                return file;
            }
        }
        return null;
    }
}
