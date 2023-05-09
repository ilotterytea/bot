package kz.ilotterytea.bot.utils;

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Storage utilities.
 * @author ilotterytea
 * @since 1.0
 */
public class StorageUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(StorageUtils.class);

    public static List<String> getFilepathsFromResource(String folder_path) {
        List<String> paths = new ArrayList<>();
        ClassLoader classLoader = StorageUtils.class.getClassLoader();
        URL resource = classLoader.getResource(folder_path.substring(1));

        // run in ide
        if(resource != null && Objects.equals(resource.getProtocol(), "file")){
            try {
                paths.addAll(
                        Arrays.stream(Objects.requireNonNull(new File(resource.toURI()).list())).map(p->folder_path + "/" + p)
                                .collect(Collectors.toList())
                );
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            return paths;
        }

        try {
            URI uri = StorageUtils.class.getResource(folder_path).toURI();

            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                Path folder = fs.getPath(folder_path);
                List<Path> pathz = Files.walk(folder, 1).collect(Collectors.toList());
                pathz.remove(0);
                pathz.forEach(p -> {
                    paths.add(folder_path + "/" + p.getFileName().toString());
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return paths;
    }

    public static String readFileFromResources(String filepath) {
        try {
            return Resources.toString(Resources.getResource(filepath), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
