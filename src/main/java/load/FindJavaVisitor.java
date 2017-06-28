package load;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * Created by jiangyuan5 on 2017/6/27.
 */
public class FindJavaVisitor extends SimpleFileVisitor<Path> {
    private String suffix;
    private List<Path> result;

    public FindJavaVisitor(List<Path> result, String suffix) {
        this.result = result;
        this.suffix = suffix;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (file.toString().endsWith(suffix)) {
            result.add(file.toAbsolutePath());
        }
        return FileVisitResult.CONTINUE;
    }
}
