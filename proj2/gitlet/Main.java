package gitlet;

import java.io.File;
import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author alysa liu
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    /**The current working directory.*/
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory.*/
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** File that stores the repository object */
    public static final File REPO_OBJECT = join(GITLET_DIR, "repository");

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        if (!GITLET_DIR.exists() && !firstArg.equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        Repository repo = new Repository();
        switch (firstArg) {
            case "init":
                repo.init();
                break;
            case "add":
                repo = readObject(REPO_OBJECT, Repository.class);
                String fileName = args[1];
                repo.add(fileName);
                break;
            case "rm":
                repo = readObject(REPO_OBJECT, Repository.class);
                repo.remove(args[1]);
                break;
            case "commit":
                repo = readObject(REPO_OBJECT, Repository.class);
                String message = args[1];
                repo.commit(message);
                break;
            case "log":
                repo = readObject(REPO_OBJECT, Repository.class);
                repo.log();
                break;
            case "global-log":
                repo = readObject(REPO_OBJECT, Repository.class);
                repo.globalLog();
                break;
            case "checkout":
                repo = readObject(REPO_OBJECT, Repository.class);
                if (args.length == 3) {
                    repo.checkoutFile(args[2]);
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    repo.checkoutCommit(args[1], args[3]);
                } else if (args.length == 2) {
                    repo.checkoutBranch(args[1]);
                }
                break;
            case "branch":
                repo = readObject(REPO_OBJECT, Repository.class);
                repo.branch(args[1]);
                break;
            case "find":
                repo = readObject(REPO_OBJECT, Repository.class);
                repo.find(args[1]);
                break;
            case "status":
                repo = readObject(REPO_OBJECT, Repository.class);
                repo.status();
                break;
            case "rm-branch":
                repo = readObject(REPO_OBJECT, Repository.class);
                repo.rmBranch(args[1]);
                break;
            case "reset":
                repo = readObject(REPO_OBJECT, Repository.class);
                repo.reset(args[1]);
                break;
            case "merge":
                repo = readObject(REPO_OBJECT, Repository.class);
                repo.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }

        writeObject(REPO_OBJECT, repo);
    }
}
