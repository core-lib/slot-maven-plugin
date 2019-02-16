package io.slot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 开槽机
 *
 * @author Payne 646742615@qq.com
 * 2019/2/16 10:41
 */
public interface Slotter {

    /**
     * 开槽，将目标文件开槽输出至目标文件。
     *
     * @param src  源文件
     * @param dest 目标文件
     * @throws IOException I/O 异常
     */
    void slot(String src, String dest) throws IOException;

    /**
     * 开槽，将目标文件开槽输出至目标文件。
     *
     * @param src  源文件
     * @param dest 目标文件
     * @throws IOException I/O 异常
     */
    void slot(File src, File dest) throws IOException;

    /**
     * 开槽，将输入流开槽输出至输出流。
     *
     * @param in  输入流
     * @param out 输出流
     * @throws IOException I/O 异常
     */
    void slot(InputStream in, OutputStream out) throws IOException;

}
