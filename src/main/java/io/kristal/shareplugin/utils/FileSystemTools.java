package io.kristal.shareplugin.utils;

import android.os.Environment;
import android.util.Log;

import org.cobaltians.cobalt.Cobalt;

import java.io.File;

/**
 * Created by Roxane P. on 5/2/16.
 * FileSystemTools
 * Some tools about android internal / external storage
 */
public class FileSystemTools {

    private static final String TAG = "FileSystemTools";

    private FileSystemTools() {
        throw new AssertionError();
    }

    /**
     * makeDirs
     * make a directory at path
     * @param filePath path of the directory
     */
    public static boolean makeDirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (stringIsBlank(filePath)) return false;
        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) || folder.mkdirs();
    }

    /* Adapt the string argument to be use as file name in a filesystem */
    public static String fileNameForFileSystem(String fileName) {
        String ret = fileName;
        ret = ret.replace(' ', '.');
        ret = ret.replace('%', '.');
        ret = ret.replace('/', '.');
        ret = ret.replace('\\', '.');
        return ret;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * deleteFile
     * delete file or directory
     * <ul>
     * <li>if path is null or empty, return true</li>
     * <li>if path not exist, return true</li>
     * <li>if path exist, delete recursion. return true</li>
     * <ul>
     * @param path path of the file to delete
     * @return true if the file not exist anymore, false instead
     */
    public static boolean deleteFile(String path) {
        if (stringIsBlank(path)) return true;
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    /**
     * get folder name from path
     *
     * <pre>
     *      getFolderName(null)               =   null
     *      getFolderName("")                 =   ""
     *      getFolderName("   ")              =   ""
     *      getFolderName("a.mp3")            =   ""
     *      getFolderName("a.b.rmvb")         =   ""
     *      getFolderName("abc")              =   ""
     *      getFolderName("c:\\")              =   "c:"
     *      getFolderName("c:\\a")             =   "c:"
     *      getFolderName("c:\\a.b")           =   "c:"
     *      getFolderName("c:a.txt\\a")        =   "c:a.txt"
     *      getFolderName("c:a\\b\\c\\d.txt")    =   "c:a\\b\\c"
     *      getFolderName("/home/admin")      =   "/home"
     *      getFolderName("/home/admin/a.txt/b.mp3")  =   "/home/admin/a.txt"
     * </pre>
     *
     * @param filePath the path of file
     * @return
     */
    public static String getFolderName(String filePath) {
        if (stringIsBlank(filePath)) return filePath;
        int filePosition = filePath.lastIndexOf(File.separator);
        return (filePosition == -1) ? "" : filePath.substring(0, filePosition);
    }

    /**
     * return true is str is empty or null
     */
    public static boolean stringIsBlank(String str) {
        return (str == null || str.trim().length() == 0);
    }
}
