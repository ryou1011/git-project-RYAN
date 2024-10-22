import java.io.*;
import java.nio.file.*;

public class CommitTester {

    public static void main (String [] args) throws IOException {
        Git git = new Git();
        Git.initRepo();
        File test = new File ("root/testFile");
        test.mkdirs();
        git.stage("root/testFile");
        String hash = git.commit("ms ann", "say yo last name for me again");
        File commit = new File ("git/objects/" + hash);
        
        //tests file creation
        if (commit.exists()) {
            System.out.println("chat is this rizz");
        } else {
            System.out.println("minus 1000 aura slime");
        }

        //tests contents
        String text = new String(Files.readAllBytes(commit.toPath()));
        if (text.contains("author: ms ann") && text.contains("message: say yo last name for me again")) {
            System.out.println("on skibidi that was sigma");
        }
        else {
            System.out.println("yo bro that was mad beta of you bro");
        }

        //tests after adding new file
        File newNew = new File ("root/newNew");
        File newFile = new File("root/newFile");
        File newFile2 = new File("root/newFile2");
        newNew.mkdirs();
        newFile.mkdirs();
        newFile2.mkdirs();
        git.stage("root/newNew");
        git.stage("root/newFile");
        git.stage("root/newFile2");
        String newHash = git.commit("brotizzle", "I'm really him University");
        File newCommit = new File ("git/objects/" + newHash);
        if (newCommit.exists()) {
            System.out.println("yo gng we lookin good in the hood");
        }
        else {
            System.out.println("we are not good in the hood");
        }

        String newText = new String(Files.readAllBytes(newCommit.toPath()));
        if (newText.contains ("author: brotizzle") && newText.contains("message: I'm really him University")) {
            System.out.println("its lookin saucy frfr");
        }
        else {
            System.out.println("whoops!");
        }

        //tests checkout
        // git.checkout(hash);
        // if (test.exists() && !newFile.exists()) {
        //     System.out.println("checkout works I think");
        // }
        // else {
        //     System.out.println("its no good");
        // }

    }
}