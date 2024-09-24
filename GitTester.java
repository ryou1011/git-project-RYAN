import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Scanner;

public class GitTester{

    private static String originalIndex;
    private static File testFile;
    public static void main (String [] args) throws IOException {
        Git.setToggle(true);
        System.out.println(ifInitRepoWorks());
        testFile = getMadeFile("Brody is so darn cool!");
        ifMakeFileWorks(testFile);
        resetFiles();
    }
    public static String ifInitRepoWorks() throws IOException {
        if (new File ("git/objects/index").exists()) {
            return "it alr exists bud";
        }
        else {
            Git.initRepo();
            File temp = new File ("git/objects/index");
            if (temp.exists()) {
                temp.delete();
                return "yuh, w code";
            }
            else {
                return "L code, file wudn't made";
            }
        }
    }

    public static File getMadeFile (String input) throws IOException {
        File file = new File ("git/objects/newTestFile");
        file.createNewFile();
        file.mkdirs();
        BufferedWriter bw = new BufferedWriter (new FileWriter (file));
        bw.write(input);
        bw.close();
        File test = Git.makeFile(file);
        return test;
    }

    public static void ifMakeFileWorks(File test) throws IOException {
        //reads the contents of the index for later use
        File index = new File ("git/objects/index");
        Scanner sc2 = new Scanner (index);
        while (sc2.hasNextLine()) {
            originalIndex += sc2.nextLine();
        }
        sc2.close();
        //writes out the file name, contents, and hash
        Scanner sc = new Scanner (test);
        String contents = "";
        while (sc.hasNextLine()) {
            contents += sc.nextLine();
        }
        sc.close();
        System.out.println ("File name: " + test.getName() + "\n" + "File contents: " + contents);
        //makes sure that the new file is in the objects folder
        File obj = new File ("git/objects");
        obj.mkdirs();
        if (new File (obj, test.getName()).exists()) {
            System.out.println ("Huzzah, the file is in the objects folder");
        }
        else {
            System.out.println ("Oopsie daisy, the file is not in the objects folder");
        }
        //makes sure there is an entry in the index file
        File index2 = new File ("git/objects/index");
        Scanner sc3 = new Scanner (index2);
        String newIndex = originalIndex;
        while (sc3.hasNextLine()) {
            newIndex += sc3.nextLine();
        }
        sc3.close();
        if (newIndex.length() > originalIndex.length()) {
            System.out.println ("Nice! There is a new entry into the index file");
        }
        else {
            System.out.println ("Uh oh! No new entry :(");
        }
    }

    public static void resetFiles () throws IOException {
        //resets index file
        File index = new File ("git/objects/index");
        BufferedWriter bw = new BufferedWriter(new FileWriter (index));
        bw.write (originalIndex);
        bw.close();
        //deletes the file that was created
        testFile.delete();
    }
}