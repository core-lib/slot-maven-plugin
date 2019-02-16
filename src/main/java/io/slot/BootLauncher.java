package io.slot;

import io.loadkit.Loaders;
import io.loadkit.Resource;
import org.springframework.boot.loader.JarLauncher;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * Spring Boot JAR 启动器
 *
 * @author Payne 646742615@qq.com
 * 2019/2/16 10:49
 */
public class BootLauncher extends JarLauncher {
    private static final String SLOT_ROOT = "--slot.root=";
    private static final String SLOT_PATH = "--slot.path=";

    private final String root;
    private final List<String> paths;

    public BootLauncher(String root, List<String> paths) {
        if (root == null) {
            throw new NullPointerException("root must not be null");
        }
        if (paths == null) {
            paths = Collections.emptyList();
        }
        this.root = root;
        this.paths = paths;
    }

    public static void main(String[] args) throws Exception {
        String root = System.getProperty("user.dir");
        List<String> paths = new ArrayList<>();
        List<String> arguments = new ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith(SLOT_ROOT)) {
                root = arg.substring(SLOT_ROOT.length());
            } else if (arg.startsWith(SLOT_PATH)) {
                String path = arg.substring(SLOT_PATH.length());
                paths.add(path);
            } else {
                arguments.add(arg);
            }
        }
        new BootLauncher(root, paths).launch(arguments.toArray(new String[0]));
    }

    @Override
    protected ClassLoader createClassLoader(URL[] urls) throws Exception {
        Set<URL> slots = new LinkedHashSet<>(Arrays.asList(urls));
        for (String path : paths) {
            Enumeration<Resource> resources = Loaders.ant(Loaders.file(new File(root))).load(path);
            while (resources.hasMoreElements()) {
                Resource resource = resources.nextElement();
                slots.add(resource.getUrl());
            }
        }
        return super.createClassLoader(slots.toArray(new URL[0]));
    }

}
