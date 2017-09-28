package gitlet;

/**
 * Part of project2
 * Created by Ray on 7/12/2017.
 */
public abstract class Function {

    Repository repository;

    String[] args;

    abstract void apply();

    abstract boolean checkArgs();

    void badArgumentNumber() {
        System.out.println("Incorrect operands.");
    }

    public Function(Repository gitlet, String[] args) {
        this.repository = gitlet;
        this.args = args;
    }

    public Function() {
        System.out.println("didn't pass func any args");
    }
}
