# Gitlet Design Document

**Name**: Alysa Liu

## Classes and Data Structures

### Main

#### Fields
1. public static final File CWD: The current working directory

2. public static final File GITLET_DIR: The .gitlet directory

3. public static final File REPO_OBJECT: File that stores the repository object

### Repository

#### Fields

1. public static final File CWD: The current working directory

2. public static final File GITLET_DIR: The .gitlet directory

3. public static final File COMMITS_DIR: The directory of commits

4. public static final File BLOBS_DIR: The directory of blobs

5. public String head: head pointer with hash of working commit

6. public HashMap branches: HashMap of all branches (keys are working branch, values are hash code of branch)

7. public String workingBranch: current working branch

8. public HashMap stagedAddition: HashMap of files to be added to staging area (file name as key, hash of file contents as value)

9. public HashMap stagedRemoval: HashMap of files to be removed from staging area (file name as key, hash of file contents as value)

### Commit

#### Fields

1. public String message: the message of the commit
   
2. public String author: the author of the commit
   
3. public String timeStamp: timestamp of the commit

4. public HashMap trackedFiles: HashMap of files tracked by the commit

5. public String parent: hashcode of parent of the commit

6. private static final File CWD: The current working directory

7. private static final File COMMITS_DIR: The directory of commits

## Algorithms

###Main

####public static void main(String[] args) 
Method that takes in commands entered by the user and calls the appropriate method in the Repository class.
If there were no commands entered or Gitlet has not been initialized and the first argument is not "init", exits system.
At the end, saves the state of the repository object in a file.

###Repository

####public void init()
Initializes the Gitlet version-control system by creating directories
for .gitlet, commits, and blobs. It also instantiates the HashMap for saving branches and files in the staging area.
The head points to the hash code of the initial commit.
If it has already been initialized, it only prints out a message.

####public void add(String fileName)
If the file is not already in the staging area, it adds the specified file to the staging area by saving it to the 
HashMap that keeps track of files staged for addition. Otherwise, it removes the file from the staging area. 
At the end, it saves the contents of the file (blob).
It then saves the state of the staging area to a file. 

####public void commit(String message)
Creates a new Commit object with the current time, the commit message, and parent as the current head.
Checks that there was a commit message entered.
Loads the files staged for addition and removal into their respective HashMaps, and checks that they are not empty. 
Then, it adds and removes the specified files to and from the new Commit object, and saves it.
Clears the staging areas and saves them. 

####public void log()
Uses instance variables of the Commit object corresponding to the head pointer 
to display information about the current commit and ones before it.

####public void checkout(String commitToBeLoaded, String fileName)
Checks out the version of the file that exists in the specified commit by joining a file
with the contents of the specified version of the file to the current working directory.

####public void checkoutFile(String fileName)
Checks out version of file at head by calling the checkout method.

####public void checkoutCommit(String commitID, String fileName)
Checks out version of file as it exists in the commit passed in by calling the checkout method.

####public void checkoutBranch(String fileName)
Checks out version of file at head by calling the checkout method.



####private void loadStagedAddition()
Helper method that loads the HashMap saved in a file that contains the
names of files that were staged for addition. It then saves the HashMap in that file 
as the current HashMap containing names of files staged for addition.

####private void saveStagedAddition()
Helper method that saves the HashMap containing names of files staged for addition
into a file.

####private void loadStagedRemoval()
Helper method that loads the HashMap saved in a file that contains the
names of files that were staged for removal. It then saves the HashMap in that file
as the current HashMap containing names of files staged for removal.

####private void saveStagedRemoval()
Helper method that saves the HashMap containing names of files staged for removal
into a file.

####private void loadBranches()
Helper method that loads the file containing a HashMap that kept track of all
previous branches into the current branches HashMap.

####private void saveBranches()
Helper method that saves the HashMap containing all the previous branches
into a file.

####private static Commit loadCommit(String hash)
Helper method that returns the Commit with the specified hashcode. 

####private static String hashCommit(Commit commit) 
Helper method that returns hashcode of the Commit object.

####private static void saveCommit(Commit commit)
Helper method that saves the specified commit to a file labeled
as its hashcode. It also saves the tracked files of the commit to a file labeled with
its hashcode followed by "tf."

####private static void saveBlob(String blob)
Helper method that saves the contents of the blob to a file labeled with the hashcode of its contents. 

####private static String loadBlob(String hash)
Helper method that returns the contents of the file specified by the hashcode as a String.

###Commit

####public Commit (Date date, String message, String parent)
Creates a Commit object with the date, message, and parent passed in. 
If it is not the initial commit, the tracked files of its parent get put into its tracked files HashMap.

####public Commit loadCommit(String commitHash)
Returns the commit of the specified hashcode by reading from a file with that hashcode containing a Commit object.

####public void loadTrackedFiles(String commitHash)
Loads file containing tracked files of commit with specified hashcode to current Commit object's HashMap containing its tracked files. 

####public void addToStagingArea(HashMap stage)
Goes through the HashMap containing files staged for addition and adds them to the Commit object's tracked files.

####public void rmFromStagingArea(HashMap stage)
Goes through the HashMap containing files staged for removal and removes them from the Commit object's tracked files.

## Persistence
The Repository class will set up all persistence. It will:
1. Create the .gitlet directory if it doesn’t already exist
2. Create the commits directory if it doesn’t already exist
3. Create the blobs directory if it doesn't already exist

Times we need to record the state of our files: 



