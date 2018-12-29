package com.icecold.sleepbandtest.utils;


import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.icecold.sleepbandtest.entity.EegInformation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * Created by jiangLiu on 2016/11/2.
 */
public class FileUtil {

    public final static String DOWNLOAD_ZIP = "downloadData.zip";
    public final static String DOWNLOAD_FOLDER = "SleepData";
    public final static String DOWNLOAD_FILE_NAME = "data.txt";
    public final static String DOWNLOAD_CSV_FILE_NAME = "test_file.csv";
    private static final int BUFFER = 8192;

    /**
     * 此方法为android程序写入sd文件文件，用到了android-annotation的支持库@
     *
     * @param content   写入文件的内容
     * @param folder   保存文件的文件夹名称,如log；可为null，默认保存在sd卡根目录
     * @param fileName 文件名称，默认app_log.txt
     * @param append   是否追加写入，true为追加写入，false为重写文件
     * @param autoLine 针对追加模式，true为增加时换行，false为增加时不换行
     */
    public synchronized static void writeFileToSDCard(@NonNull final ArrayList<String> content, @Nullable final String folder,
                                                      @Nullable final String fileName, final boolean append, final boolean autoLine) {
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        String folderPath;
        if (sdCardExist) {
            //TextUtils为android自带的帮助类
            if (TextUtils.isEmpty(folder)) {
                //如果folder为空，则直接保存在sd卡的根目录
                folderPath = Environment.getExternalStorageDirectory()
                        + File.separator;
            } else {
                folderPath = Environment.getExternalStorageDirectory()
                        + File.separator + folder + File.separator;
            }
        } else {
            return;
        }

        File fileDir = new File(folderPath);
        if (!fileDir.exists()) {
            if (!fileDir.mkdirs()) {
                return;
            }
        }
        File file;
        //判断文件名是否为空
        if (TextUtils.isEmpty(fileName)) {
            file = new File(folderPath + "app_log.txt");
        } else {
            file = new File(folderPath + fileName);
        }
        RandomAccessFile raf = null;
        FileOutputStream out = null;
        try {
            if (append) {
                if (content.size()>0){
                    //如果为追加则在原来的基础上继续写文件
                    raf = new RandomAccessFile(file, "rw");
                    for (String buffer : content) {
                        raf.seek(file.length());
                        raf.writeBytes(buffer);
                        if (autoLine) {
                            raf.writeBytes("\n");
                        }
                    }
                }
            } else {
                if (content.size()>0){
                    //重写文件，覆盖掉原来的数据
                    for (String buffer : content) {
                        out = new FileOutputStream(file);
                        out.write(buffer.getBytes());
                        out.flush();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) {
                    raf.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //压缩成zip文件
        String filePath = Environment.getExternalStorageDirectory()
                + File.separator + FileUtil.DOWNLOAD_FOLDER + File.separator + FileUtil.DOWNLOAD_FILE_NAME;
        String zipPath = Environment.getExternalStorageDirectory()
                + File.separator + FileUtil.DOWNLOAD_FOLDER + File.separator + FileUtil.DOWNLOAD_ZIP;
        try {
            compress(filePath,zipPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeFileEmpty(String fileName) {
        File file = new File(fileName);
        FileWriter writer = null;
        if (file.exists()){
            try {
                // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件,false表示覆盖的方式写入
                writer = new FileWriter(file, false);
                writer.write("");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static void compress(String srcPath, String dstPath) throws IOException {
        File srcFile = new File(srcPath);
        File dstFile = new File(dstPath);
        if (!srcFile.exists()) {
            throw new FileNotFoundException(srcPath + "不存在！");
        }
        FileOutputStream out = null;
        ZipOutputStream zipOut = null;
        try {
            if (!dstFile.exists()){
                out = new FileOutputStream(dstFile);
                CheckedOutputStream cos = new CheckedOutputStream(out,new CRC32());
                zipOut = new ZipOutputStream(cos);
                String baseDir = "";
                compress(srcFile, zipOut, baseDir);
            }
        }
        finally {
            if(null != zipOut){
                zipOut.close();
            }
            if(null != out){
                out.close();
            }
        }
    }
    private static void compress(File file, ZipOutputStream zipOut, String baseDir) throws IOException {
        if (file.isDirectory()) {
            compressDirectory(file, zipOut, baseDir);
        } else {
            compressFile(file, zipOut, baseDir);
        }
    }
    /** 压缩一个目录 */
    private static void compressDirectory(File dir, ZipOutputStream zipOut, String baseDir) throws IOException {
        File[] files = dir.listFiles();
        for (File file : files) {
            compress(file, zipOut, baseDir + dir.getName() + "/");
        }
    }
    /** 压缩一个文件 */
    private static void compressFile(File file, ZipOutputStream zipOut, String baseDir)  throws IOException {
        if (!file.exists()){
            return;
        }
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            ZipEntry entry = new ZipEntry(baseDir + file.getName());
            zipOut.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                zipOut.write(data, 0, count);
            }
        }finally {
            if(null != bis){
                bis.close();
            }
        }
    }

    public static boolean deleteFile(File file){
        boolean delete = false;
        //判断文件或文件夹是否存在
        if (file.exists()){
            //判断是否是文件
            if (file.isFile()){
                delete = file.delete();
            }
//            else if (file.isDirectory()){
//                //是文件夹
//                File files[] = file.listFiles();
//                if (files!=null && files.length>0){
//                    for (File file1 : files) {
//                        this.deleteFile(file1);
//                    }
//                }
//            }
//            file.delete();
        }
        return delete;
    }
    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param filePath The path of file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean createFileByDeleteOldFile(final String filePath) {
        return createFileByDeleteOldFile(getFileByPath(filePath));
    }
    /**
     * Return the file by path.
     *
     * @param filePath The path of file.
     * @return the file
     */
    public static File getFileByPath(final String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }
    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param file The file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean createFileByDeleteOldFile(final File file) {
        if (file == null) return false;
        // file exists and unsuccessfully delete then return false
        if (file.exists() && !file.delete()) return false;
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * Return whether the file exists.
     *
     * @param filePath The path of file.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isFileExists(final String filePath) {
        return isFileExists(getFileByPath(filePath));
    }

    /**
     * Return whether the file exists.
     *
     * @param file The file.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isFileExists(final File file) {
        return file != null && file.exists();
    }

    /**
     * 获取当前app缓存的文件根目录
     * @param context 上下文
     * @return
     */
    public static String getDiskCachePath(Context context) {
        String cachePath;
        if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable())
                && context.getExternalCacheDir() != null) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    /**
     * 读取文件的内容以string方式返回
     * @param path 文件路径
     * @return 文件内容
     */
    public static String readFileContent(String path){

        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            fileInputStream = new FileInputStream(path);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            StringBuffer stringBuffer = new StringBuffer();
            byte[] data = new byte[1024];
            int number;
            while ((number = bufferedInputStream.read(data)) > 0){
                String hexStr = new String(data);

                stringBuffer.append(hexStr,0,number);
            }
            bufferedInputStream.close();
            return stringBuffer.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     * 写入 .csv 文件
     * @param mList 需要写入的数据
     * @param path 需要写入文件的path
     */
    public static void writeCsv(ArrayList<EegInformation> mList,String path) {
        try {
            File file = new File(path);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));  // 防止出现乱码
            // 添加头部
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("time_stamp", "attention", "meditation"));
            // 添加内容
            for (int i = 0; i < mList.size(); i++) {
                csvPrinter.printRecord(
                        mList.get(i).getTimeStamp(),
                        mList.get(i).getAttention(),
                        mList.get(i).getMeditation());
            }
            csvPrinter.printRecord();
            csvPrinter.flush();
            mList.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
