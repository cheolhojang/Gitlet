package gitlet;


import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static gitlet.Main.userMessage;
import static gitlet.Repository.deSerialize;
import static gitlet.Utils.*;

/**
 * Part of project2
 * Created by Ray on 7/19/2017.
 */
public class MergeFunction extends Function {

    public static void userMsg(String message) {
        userMessage(message);
    }

    public MergeFunction(Repository repository, String[] args) {
        super(repository, args);
    }

    public boolean checkArgs() {
        if (!this.repository.existsBranch(args[1])) {
            userMessage("A branch with that name does not exist.");
            return false;
        }
        if (this.repository.getCurrentBranch().equals(args[1])) {
            userMessage("Cannot merge a branch with itself.");
            return false;
        }
        return this.args.length == 2;
    }
    static boolean conflict = false;
    public void apply() {
        String desiredBranch = args[1]; String currentBranch = this.repository.getCurrentBranch();
        Commit desiredStagingCommit = this.repository.getStagingCommit(desiredBranch);
        Commit givenHead = desiredStagingCommit.getParentCommit();
        Commit currentStagingCommit = this.repository.getStagingCommit(currentBranch);
        Commit currentHead = currentStagingCommit.getParentCommit();
        Commit splitHead = findSplitPoint(currentHead, givenHead);
        if (givenHead == splitHead) {
            userMsg("Given branch is an ancestor of the current branch."); return;
        }
        if (splitHead.getSHA1().equals(currentHead.getSHA1())) {
            userMessage("Current branch fast-forwarded.");
            return;
        }
        for (HashMap.Entry<String, String> entry : currentStagingCommit.blobs.entrySet()) {
            if (!currentHead.blobs.containsKey(entry.getKey())
                    && !(givenHead.blobs.get(entry.getKey()) == null)) {
                userMessage("You have uncommitted changes."); return;
            }
        }
        List<String> directoryFiles = plainFilenamesIn(".");
        for (String directoryFile : directoryFiles) {
            if (!currentHead.blobs.containsKey(directoryFile)) { // not tracked
                String dirFileSHA1 = sha1(readContents(new File(directoryFile)));
                if (!givenHead.blobs.containsKey(directoryFile)
                        || !dirFileSHA1.equals(givenHead.blobs.get(directoryFile))) {
                    userMsg("There is an untracked file in the way; delete it or add it first.");
                    return;
                }
            }
        }
        deleteAll(directoryFiles); HashMap<String, String> staging = new HashMap<>();
        LinkedList<String> conflicted = new LinkedList<>();
        HashMap<String, String> givenBlobs = (HashMap<String, String>) givenHead.blobs.clone();
        for (HashMap.Entry<String, String> entry : givenBlobs.entrySet()) {
            if (conflicted.contains(entry.getKey())) {
                continue;
            }
            String fileName = entry.getKey(); String fileSHA1 = entry.getValue();
            if (fileSHA1 == null) {
                continue;
            }
            if (fileSHA1 != null
                    && !containsFile(splitHead, fileName, fileSHA1) // not in split, but in given
                    && (!containsFile(currentHead, fileName, fileSHA1))) {  // not in current
                if (hasDifferentContent(currentHead, splitHead, fileName)
                        && currentHead.blobs.containsKey(fileName)) {
                    conflict = true; conflicted.add(fileName);
                    byte[] contents = makeConflictedFile(currentHead, givenHead, fileName);
                } else {
                    setDirectoryFile(fileName, fileSHA1); staging.put(fileName, fileSHA1);
                }
            } else if (fileSHA1 != null
                    && hasDifferentContent(givenHead, splitHead, fileName) // mod in given
                    && !hasDifferentContent(currentHead, splitHead, fileName)) {  // same in curr
                setDirectoryFile(fileName, fileSHA1); staging.put(fileName, fileSHA1);
            } else if (fileSHA1 != null && currentHead.blobs.get(fileName) != null
                    && hasDifferentContent(givenHead, currentHead, fileName)) {
                conflict = true; conflicted.add(fileName);
                byte[] contents = makeConflictedFile(currentHead, givenHead, fileName);
            }
        }
        HashMap<String, String> current = (HashMap<String, String>) currentHead.blobs.clone();
        for (HashMap.Entry<String, String> entry : current.entrySet()) {
            String fileName = entry.getKey(); String fileSHA1 = entry.getValue();
            if (fileSHA1 == null) {
                continue;
            }
            if (conflicted.contains(fileName)) {
                continue;
            }
            if (fileSHA1 != null
                    && hasDifferentContent(currentHead, splitHead, fileName)
                    && !hasDifferentContent(givenHead, splitHead, fileName)) {
                setDirectoryFile(fileName, fileSHA1); currentHead.blobs.put(fileName, fileSHA1);
            } else if (fileSHA1 != null
                    && !containsFile(splitHead, fileName, fileSHA1)) {
                setDirectoryFile(fileName, fileSHA1); currentHead.blobs.put(fileName, fileSHA1);
            } else if (fileSHA1 != null && containsFile(splitHead, fileName, fileSHA1)
                    && (containsFile(givenHead, fileName, fileSHA1))) {
                currentHead.blobs.remove(fileName);
            }
        }
        for (HashMap.Entry<String, String> entry : staging.entrySet()) {
            currentStagingCommit.blobs.put(entry.getKey(), entry.getValue());
        }
        if (!conflict) {
            Commit newHead = new Commit(currentStagingCommit.getSHA1(), "");
            currentStagingCommit.setTimeStamp();
            currentStagingCommit.logMessage = "Merged "
                    + currentBranch + " with " + desiredBranch + ".";
            this.repository.serializeCommit(newHead, newHead.getSHA1());
            this.repository.serializeCommit(currentStagingCommit, currentStagingCommit.getSHA1());
            this.repository.getBranches().put(currentBranch, newHead.getSHA1());
        } else {
            userMessage("Encountered a merge conflict.");
            this.repository.serializeCommit(currentStagingCommit, currentStagingCommit.getSHA1());
        }
    }
    public static Commit findSplitPoint(Commit head1, Commit head2) {
        HashMap<String, Commit> head1chain = new HashMap<>();
        buildHeadChain(head1, head1chain); return traceHeadChain(head2, head1chain);
    }

    public static void buildHeadChain(Commit head, HashMap<String, Commit> chain) {
        if (head == null) {
            return;
        } else {
            chain.put(head.getSHA1(), head); buildHeadChain(head.getParentCommit(), chain);
        }
    }
    public static Commit traceHeadChain(Commit head, HashMap<String, Commit> head1chain) {
        if (head.getParentCommit() == null) {
            return head;
        } else if (head1chain.containsKey(head.getSHA1())) {
            return head;
        } else {
            return traceHeadChain(head.getParentCommit(), head1chain);
        }
    }
    public static boolean hasDifferentContent(Commit c1, Commit c2, String fileName) {
        if (c1.blobs.get(fileName) == null || c2.blobs.get(fileName) == null) {
            return true;
        }
        return !c1.blobs.get(fileName).equals(c2.blobs.get(fileName));
    }

    public static void setDirectoryFile(String fileName, String serializedSHA1) {
        byte[] content = (byte[]) deSerialize(serializedSHA1);
        writeContents(new File(fileName), content);
    }

    public static boolean containsFile(Commit c, String fileName, String fileSHA1) {
        if (c.blobs.containsKey(fileName) && c.blobs.get(fileName) != null) {
            return c.blobs.get(fileName).equals(fileSHA1);
        }
        return false;
    }

    public byte[] makeConflictedFile(Commit c1, Commit c2, String fileName) {
        String contentC1 = new String((byte[]) deSerialize(c1.blobs.get(fileName)));
        String contentC2 = new String((byte[]) deSerialize(c2.blobs.get(fileName)));



        String start = "<<<<<<< HEAD\n";
        String mid = "=======\n";
        String end = ">>>>>>>";

        String combined = (start + contentC1 + mid + contentC2 + end + "\n");
        byte[] combinedBytes = combined.getBytes();
        writeContents(new File(fileName), combinedBytes);
        return combinedBytes;

    }
    public void deleteAll(List<String> directoryFiles) {
        for (String fileName : directoryFiles) {
            restrictedDelete(fileName);
        }
    }

}