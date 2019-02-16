package io.slot;

import java.io.*;

/**
 * 文件开槽机
 *
 * @author Payne 646742615@qq.com
 * 2019/2/16 10:43
 */
public abstract class FileSlotter implements Slotter {

    @Override
    public void slot(String src, String dest) throws IOException {
        slot(new File(src), new File(dest));
    }

    @Override
    public void slot(File src, File dest) throws IOException {
        try (
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)
        ) {
            slot(in, out);
        }
    }

}
