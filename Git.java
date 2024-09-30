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

public class Git {

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
}
