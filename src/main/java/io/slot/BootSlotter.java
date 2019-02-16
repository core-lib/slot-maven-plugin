package io.slot;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;

/**
 * Spring Boot JAR 开槽机
 *
 * @author Payne 646742615@qq.com
 * 2019/2/16 10:46
 */
public class BootSlotter extends FileSlotter implements Slotter {

    @Override
    public void slot(InputStream in, OutputStream out) throws IOException {
        JarArchiveInputStream zis = null;
        JarArchiveOutputStream zos = null;
        try {
            zis = new JarArchiveInputStream(in);
            zos = new JarArchiveOutputStream(out);

            JarArchiveEntry entry;
            while ((entry = zis.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                } else if (entry.getName().endsWith(".jar")) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    CheckedOutputStream cos = new CheckedOutputStream(bos, new CRC32());
                    IOKit.transfer(zis, cos);
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setMethod(JarArchiveEntry.STORED);
                    jarArchiveEntry.setSize(bos.size());
                    jarArchiveEntry.setTime(entry.getTime());
                    jarArchiveEntry.setCrc(cos.getChecksum().getValue());
                    zos.putArchiveEntry(jarArchiveEntry);
                    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                    IOKit.transfer(bis, zos);
                } else if (entry.getName().equals("META-INF/MANIFEST.MF")) {
                    Manifest manifest = new Manifest(zis);
                    Attributes attributes = manifest.getMainAttributes();
                    attributes.putValue("Main-Class", "io.slot.core.BootLauncher");
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                    manifest.write(zos);
                } else {
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                    IOKit.transfer(zis, zos);
                }
                zos.closeArchiveEntry();
            }

            IOKit.embed("io/slot/**", zos);
            IOKit.embed("io/loadkit/**", zos);

            zos.finish();
        } finally {
            IOKit.close(zis);
            IOKit.close(zos);
        }
    }
}
