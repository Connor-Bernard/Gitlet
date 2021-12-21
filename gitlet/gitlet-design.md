# Gitlet Design Document

**Name**: Connor Bernard

## Classes and Data Structures

###Repo class
* Contains methods for data structure management and modulation
* instance variables:
  * HEAD pointer --> a commit (or a hash of a commit)
  * current branch --> string of current branch
  * linked list hash map for **branches** <"name", "hash of head commit">
* methods:
  * commit
  * init
  * add (refers to staging area)
  * log
  * global-log
  * rm (refers to staging are)
  * clear (refers to staging area)
  * find
  * status
  * checkout
  * branch
  * rm-branch
  * reset
  * merge

###Commit class
* copies the parent commit to this commit
* instance variables:
  * timestamp
  * commit message
  * parent SHA-1 hash (stored as a string)
  * LinkedListHashMap mapping names of files to hashes for the blobs
* methods:
  * updateContents = modifies the LinkedListHashMap
  * remove = removes the file marked for removal

###Staging Area class
* add blobs of files that we need to commit when we commit something
* methods:
  * add
    * adds file to staging area
    * updates file if already in staging area
  * rm
    * marks a file for removal (does not remove until next commit)
  * clear
    * clears the current staging area
* instance variables:
  * Linked list hash map of <"file name", "hash">
  * LinkedListHashSet toRemove
    * need to check to make sure that a file is not in both of the above names
  * directory to staging area

###Main class
* driver class that calls methods from repo
* instance variables:
  * current repo
* methods:
  * check to see if .gitlet exists; if it does, then deserialize the current repo

## Algorithms

### Methods
* init (in repo)
  * creates a new repo
  * creates .gitlet folder
  * creates a commits folder
    * creates a file for CommitMessages (detailed in find method below)
    * creates initial commit
  * creates a blobs folder
* add (in staging area)
  * checks to see if hashed name already exists in the current head's blobs
  * if it does, do not add the file, otherwise, add the file and update private variables in respective classes
* commit (in repo)
  * creates a new commit
  * adds the new changes that are currently staged in the staging area to the current commit
* rm (in staging area)
  * stages a file for removal
* log (in repo)
  * looks through the list of previous commits and prints their data
* global-log (in repo)
  * * looks through the list of previous commits and prints their data
* find (in repo)
  * look through the messages file in the commit folder and find all instances where the first argument in the reader for each line matches the commit message you are trying to find
* status (in repo)
  * looks through all of the files in the current folder and checks to see if their blob allready exists
    * if the blob does exist, do not mention it
    * if it doesnt exist, mark it as either staged or unstaged depending on whether it is in the staging area
* checkout (in repo)
  * sets the HEAD pointer to the branch being checked out
* branch (in repo)
  * creates a new branch from the current HEAD pointer with the given name
* rm-branch (in repo)
  * removes the pointer corresponding from the commit of the branch head (from the repo data structures)
* reset (in repo)
  * remove all files from the curr directory
  * add all files from specified commit
* merge (in repo)
  * print out the contents of both files
  * if conflict, prompt user to choose one of the two options and override the earlier file with the new file (chosen file)
* clear (in staging area)
* checkRepo (in main)
  * checks to see if file has allready been initialized
  * if the folder has been initialized, we set our repo to the deserialized value of repo file in .gitlet folder  
    
## Persistence

### Staging area folder
* a folder of all of the blobs to add

### Commits folder
* should contain a folder for each commit
    * contains a file with the name of the commit SHA-1 hash
    * the contents of the file of the commit contain the hash of each blob in that commit
        * NOTE: the actual blob is stored in a seperate file
    * have a new file named commitMessages that maps the commit messages to the commit hashes

### Blobs Folder
* a folder of all of the blobs that have been committed

