

package keeno.usap.util;

import keeno.usap.util.collection.Lists;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Utility class for extracting names of all classes inside
 * given JAR files or directories.
 */
public class ClassNameExtractor {

    private static final String JAR = ".jar";

    private static final String CLASS = ".class";

    private static final String JAVA = ".java";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Options: <output-path> <path> <path> ...");
            System.out.println("<path> can be a path to a JAR file or" +
                    " a directory containing classes");
            return;
        }
        File outFile = new File(args[0]);
        System.out.printf("Dumping extracted class names to %s%n",
                outFile.getAbsolutePath());
        String[] jars = Arrays.copyOfRange(args, 1, args.length);
        try (PrintStream out = new PrintStream(new FileOutputStream(outFile))) {
            for (String arg : jars) {
                extract(arg).forEach(out::println);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts names of all classes in given path.
     */
    public static List<String> extract(String path) {
        return path.endsWith(JAR) ? extractJar(path) : extractDir(path);
    }

    private static List<String> extractJar(String jarPath) {
        File file = new File(jarPath);
        try (JarFile jar = new JarFile(file)) {
            System.out.printf("Scanning %s ... ", file.getAbsolutePath());
            List<String> classNames = jar.stream()
                    .filter(e -> !e.getName().startsWith("META-INF"))
                    .filter(e -> e.getName().endsWith(CLASS))
                    .map(e -> {
                        String name = e.getName();
                        return name.replaceAll("/", ".")
                                .substring(0, name.length() - CLASS.length());
                    })
                    .toList();
            System.out.printf("%d classes%n", classNames.size());
            return classNames;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read jar file: " +
                    file.getAbsolutePath(), e);
        }
    }

    private static List<String> extractDir(String dirPath) {
        Path dir = Path.of(dirPath);
        if (!dir.toFile().isDirectory()) {
            throw new RuntimeException(dir + " is not a directory");
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            System.out.printf("Scanning %s ... ", dir.toAbsolutePath());
            List<String> classNames = new ArrayList<>();
            paths.map(dir::relativize).forEach(path -> {
                String fileName = path.getFileName().toString();
                int suffix;
                if (fileName.endsWith(CLASS)) {
                    suffix = CLASS.length();
                } else if (fileName.endsWith(JAVA)) {
                    suffix = JAVA.length();
                } else {
                    return;
                }
                String name = String.join(".",
                        Lists.map(Lists.asList(path), Path::toString));
                String className = name.substring(0, name.length() - suffix);
                classNames.add(className);
            });
            System.out.printf("%d classes%n", classNames.size());
            return classNames;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read directory: " + dirPath, e);
        }
    }
}
