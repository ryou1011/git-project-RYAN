
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitTester {

    public static void main(String[] args) {
        try {
            // Test 1: Initialize the repository
            testInitRepo();

            // Test 2: Add a file to the repository as a blob
            testAddFile();

            // Test 3: Add a directory to the repository as a tree
            testAddDirectory();

            // Test 4: Enable zip toggle and add a file (for compression)
            testCompressionToggle();

            // Test 5: Validate index format after adding blobs and trees
            validateIndexFile();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testInitRepo() throws IOException {
        System.out.println("Running testInitRepo...");
        Git.initRepo();
        File objectsDir = new File("git/objects");
        File indexFile = new File("git/index");
        if (objectsDir.exists() && indexFile.exists()) {
            System.out.println("Repository initialized successfully.");
        } else {
            System.out.println("Repository initialization failed.");
        }
        System.out.println();
    }

    public static void testAddFile() throws IOException {
        System.out.println("Running testAddFile...");

        // Create a test file
        File testFile = new File("testFile.txt");
        FileWriter writer = new FileWriter(testFile);
        writer.write("This is a test file for blob creation.");
        writer.close();

        // Add the file as a blob
        Git.makeFile(testFile, "testFile.txt");

        // Check if the file was added to git/objects
        String hash = Git.getHash("This is a test file for blob creation.");
        File blobFile = new File("git/objects/" + hash);
        if (blobFile.exists()) {
            System.out.println("File added successfully as a blob.");
        } else {
            System.out.println("Failed to add the file as a blob.");
        }
        System.out.println();
    }

    public static void testAddDirectory() throws IOException {
        System.out.println("Running testAddDirectory...");

        // Create a test directory with files
        File testDir = new File("testDir");
        testDir.mkdir();

        File file1 = new File("testDir/file1.txt");
        FileWriter writer1 = new FileWriter(file1);
        writer1.write("This is file 1 inside testDir.");
        writer1.close();

        File file2 = new File("testDir/file2.txt");
        FileWriter writer2 = new FileWriter(file2);
        writer2.write("This is file 2 inside testDir.");
        writer2.close();

        // Add the directory as a tree
        Git.addDirectory("testDir", "testDir");

        // Check if the directory tree was added
        File objectsDir = new File("git/objects");
        if (objectsDir.list().length > 0) {
            System.out.println("Directory added successfully as a tree.");
        } else {
            System.out.println("Failed to add the directory as a tree.");
        }
        System.out.println();
    }

    public static void testCompressionToggle() throws IOException {
        System.out.println("Running testCompressionToggle...");

        // Enable compression
        Git.setToggle(true);

        // Create another test file
        File compressedFile = new File("compressedTestFile.txt");
        FileWriter writer = new FileWriter(compressedFile);
        writer.write("This is a file to test compression.");
        writer.close();

        // Add the file with compression
        Git.makeFile(compressedFile, "compressedTestFile.txt");

        // Check if the compressed file exists
        String hash = Git.getHash("This is a file to test compression.");
        File zipFile = new File("git/objects/" + hash + ".zip");
        if (zipFile.exists()) {
            System.out.println("File compressed and added successfully.");
        } else {
            System.out.println("Failed to compress and add the file.");
        }
        System.out.println();
    }

    public static void validateIndexFile() throws IOException {
        System.out.println("Running validateIndexFile...");

        Path indexPath = Paths.get("git/index");
        if (Files.exists(indexPath)) {
            String content = Files.readString(indexPath);
            System.out.println("Index file content:");
            System.out.println(content);
        } else {
            System.out.println("Index file does not exist.");
        }
        System.out.println();
    }
}
