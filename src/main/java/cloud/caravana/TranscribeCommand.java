package cloud.caravana;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@TopCommand
@Command(name = "transcribe", mixinStandardHelpOptions = true)
public class TranscribeCommand implements Runnable {
    static class TranscribeVisitor extends SimpleFileVisitor<Path> {
        
        @Override
        public FileVisitResult visitFile(Path path, 
            BasicFileAttributes attrs) throws IOException {
            System.out.println("Visitng "+path);
            var hasAudio = path.toString().endsWith(".mp4");
            if(hasAudio){
                new TranscribeFlow(path).run();
            }
            return FileVisitResult.CONTINUE;
        }
    }

    @Parameters(paramLabel = "<name>", defaultValue = "picocli",
        description = "Your name.")
    String name;

    @Override
    public void run() {
        System.out.printf("Hello %s, transcribe me!\n", name);
        var homeDir = System.getProperty("user.home");
        var inputDir = Paths.get(homeDir, "ttmx");
        var visitor = new TranscribeVisitor();
        try {
            Files.walkFileTree(inputDir, visitor);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
