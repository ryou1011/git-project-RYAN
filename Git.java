import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.zip.*;
import java.time.*;
import java.time.format.*;

public class Git implements GitInterface{

    public static boolean zipToggle;

    public static void main(String[] args) {
        // Main method for testing
    }

    public static boolean getToggle() {
        return zipToggle;
    }

    public static void setToggle(boolean boo) {
        zipToggle = boo;
    }

    public static void initRepo() throws IOException {
        if (new File ("git").exists()) {
            File head = new File ("git/HEAD");
            head.createNewFile();
        } else {
            System.out.println("yo git don't exist for sum reason bro");
        }
        if (new File("git/objects").exists()) {
            System.out.println("Git Repository already exists");
        } else {
            File objects = new File("git/objects");
            objects.mkdirs();
        }

        if (new File("git/index").exists()) {
            System.out.println("Git Repository already exists");
        } else {
            File index = new File("git/index");
            index.createNewFile();
        }
    }

    public static String getHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hash = no.toString(16);
            while (hash.length() < 40) {
                hash = "0" + hash;
            }
            return hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static File makeFile(File file, String relativePath) throws IOException {
        // Reads the file content
        StringBuilder text = new StringBuilder();
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            text.append(sc.nextLine()).append("\n");
        }
        sc.close();

        // Check if compression is required
        if (zipToggle) {
            String filePath = "git/objects/" + file.getName();
            compressFile(filePath);
        }

        // Generate the SHA1 hash of the file contents
        String hash = getHash(text.toString());
        File objDir = new File("git/objects");
        objDir.mkdirs();
        File newFile = new File(objDir, hash);
        newFile.createNewFile();

        // Write the original content into the new blob file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(newFile))) {
            bw.write(text.toString());
        }

        // Write the entry into the index with 'blob' type
        File index = new File("git/index");
        try (BufferedWriter bw2 = new BufferedWriter(new FileWriter(index, true))) {
            bw2.write("blob " + hash + " " + relativePath);
            bw2.newLine();
        }

        return newFile;
    }

    public static String addDirectory(String dirPath, String relativePath) throws IOException {
        File directory = new File(dirPath);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Directory not found or is not accessible: " + dirPath);
        }
        
        StringBuilder treeContent = new StringBuilder();
        File[] files = directory.listFiles();
        if (files == null) {
            throw new IOException("Failed to read contents of directory: " + dirPath);
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Recursively handle subdirectories (trees)
                String subDirHash = addDirectory(file.getPath(), relativePath + "/" + file.getName());
                treeContent.append("tree ").append(subDirHash).append(" ").append(relativePath).append("/").append(file.getName()).append("\n");
            } else {
                // Handle files (blobs)
                makeFile(file, relativePath + "/" + file.getName());
                String fileHash = getHash(new String(Files.readAllBytes(file.toPath())));
                treeContent.append("blob ").append(fileHash).append(" ").append(relativePath).append("/").append(file.getName()).append("\n");
            }
        }

        // Create a tree file in the objects directory
        String treeHash = getHash(treeContent.toString());
        File treeFile = new File("git/objects/" + treeHash);
        try (BufferedWriter treeWriter = new BufferedWriter(new FileWriter(treeFile))) {
            treeWriter.write(treeContent.toString());
        }

        // Add the tree to the index
        File index = new File("git/index");
        try (BufferedWriter indexWriter = new BufferedWriter(new FileWriter(index, true))) {
            indexWriter.write("tree " + treeHash + " " + relativePath);
            indexWriter.newLine();
        }

        return treeHash;
    }

    public static void compressFile(String path) {
        try {
            File file = new File(path);
            String zipFileName = file.getName().concat(".zip");
            FileOutputStream out = new FileOutputStream(zipFileName);
            ZipOutputStream zip = new ZipOutputStream(out);
            zip.putNextEntry(new ZipEntry(file.getName()));
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            zip.write(bytes, 0, bytes.length);
            zip.closeEntry();
            zip.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String commit (String author, String message) throws IOException {
        File HEAD = new File ("git/HEAD");

        //gets parent line
        String parentLine = "";
        try {
            Scanner sc = new Scanner (HEAD);
            String parent = "";
            if (sc.hasNextLine()) {
                parent += sc.nextLine();
            }
            parentLine = "parent: " + parent + "\n";
            sc.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("issue with HEAD file");
            return null;
        }
        
        //gets tree line
        String treeLine = "";
        try {
            File index = new File ("git/index");
            Scanner sc2 = new Scanner (index);
            String rooty = "";
            while (sc2.hasNextLine()) {
                rooty = sc2.nextLine();
            }
            sc2.close();
            treeLine = "tree: " + rooty.substring(5, 45) + "\n";
        }
        catch (IOException e) {
            e.printStackTrace();
            System.out.println("issue with index file");
            return null;
        }


        //gets date line
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate date = LocalDate.now();
        String dateLine = format.format(date);

        //writes to commit file
        
        String temp = treeLine + parentLine + "author: " + author + "\n" + 
        "date: " + dateLine + "\n" + "message: " + message + "\n";
        String hash = getHash(temp);
        File commit = new File ("git/objects/" + hash);
        commit.createNewFile();

        BufferedWriter bw = new BufferedWriter (new FileWriter (commit));
        bw.write(temp);
        bw.close();

        //updates HEAD
        BufferedWriter bw2 = new BufferedWriter (new FileWriter(HEAD));
        bw2.write(hash);
        bw2.close();

        return hash; 
    }

    public void stage (String path) throws IOException {
       addDirectory(path, path);
    }

    public void checkout(String hash) throws IOException {
        //checks if file exists
        File file = new File ("git/objects/" + hash);
        if (!file.exists()) {
            throw new FileNotFoundException("File doesn't exist bro");
        }

        //gets the name of the root file
        File index = new File ("git/index");
        String temp = "";
        try {
            Scanner sc = new Scanner (index);
            while (sc.hasNextLine()) {
                temp = sc.nextLine();
            }
            sc.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String rootName = temp.substring(46);

        //deletes the directory
        File toReset = new File (rootName);
        resetFile(toReset);

        //recreate directory
        File root = new File (rootName);
        root.createNewFile();
        recreateDirectory(hash, root);

    }

    private void resetFile (File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File temp = files[i];
                resetFile (temp);
            }
            file.delete();
        }
    }

    private void recreateDirectory (String hash, File root) throws IOException {
        //recreate root file using commit
        File file = new File ("git/objects/" + hash);
        if (!file.exists()) {
            throw new FileNotFoundException("uh oh");
        }
        
        Scanner sc = new Scanner (root);
        while (sc.hasNextLine()) {
            String nextLine = sc.nextLine();
            String type = nextLine.substring(0, 4);
            String fileHash = nextLine.substring(5, 45);
            String path = nextLine.substring(46);
            File blob = new File (path);
            if (type.equals("blob")) {
                blob.createNewFile();
            }
            else {
                blob.mkdir();
                File subDirectory = new File ("git/objects/" + fileHash);
                recreateDirectory (fileHash, subDirectory);
            }
        }
        sc.close();
    }

}
