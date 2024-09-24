import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.FileOutputStream;
import java.util.zip.*;
import java.nio.file.*;

public class Git {

    public static boolean zipToggle;
    public static void main (String [] args) {

    }

    public static boolean getToggle() {
        return zipToggle;
    }

    public static void setToggle(boolean boo) {
        zipToggle = boo;
    }

    public static void initRepo() throws IOException {
        if (new File ("git/objects/index").exists()) {
            System.out.println ("Git Repository already exists”");
        }
        else {
            File index = new File ("git/objects/index");
            index.createNewFile();
            index.mkdirs();
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
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static File makeFile (File file) throws IOException {
        //gets the file's contents
        String text = "";
        Scanner sc = new Scanner (file);
        while (sc.hasNextLine()) {
            text += sc.nextLine();
        }
        sc.close();
        //checks if needs to compress
        if (zipToggle) {
            String filePath = "git/objects/" + file.getName();
            compressFile(filePath);
        }
        //generates new file name using the original file's contents
        String hash = getHash(text);
        File obj = new File ("git/objects");
        obj.mkdirs();
        File newFile = new File (obj, hash);
        newFile.createNewFile();
        newFile.mkdirs();
        //copies original contents into new file
        try(BufferedWriter bw = new BufferedWriter (new FileWriter(newFile))) {
            bw.write(text);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //writes the stuff into index
        File index = new File ("git/objects/index");
        try(BufferedWriter bw2 = new BufferedWriter (new FileWriter(index))) {
            bw2.newLine();
            String name = file.getName();
            bw2.write(name);
            bw2.write(" ");
            bw2.write(hash);
            bw2.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }

    public static void compressFile (String path) {
        try {
            File file = new File (path);
            String zipFileName = file.getName().concat(".zip");
            FileOutputStream out = new FileOutputStream(zipFileName);
            ZipOutputStream zip = new ZipOutputStream(out);
            zip.putNextEntry(new ZipEntry(file.getName()));
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            zip.write(bytes, 0, bytes.length);
            zip.closeEntry();
            zip.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}