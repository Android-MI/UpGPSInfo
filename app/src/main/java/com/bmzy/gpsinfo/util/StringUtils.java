package com.bmzy.gpsinfo.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StringUtils {
    public static String stringFromStream(InputStream is) {
        try {
            int readlen = 0;
            byte[] buff = new byte[20480];

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();

            while ((readlen = is.read(buff)) != -1) {
                outStream.write(buff, 0, readlen);
            }
            byte[] buf = outStream.toByteArray();
            outStream.close();
            return new String(buf, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String stringFromFile(String filepath) {
        try {
            FileInputStream fis = new FileInputStream(filepath);
            String string = stringFromStream(fis);
            fis.close();
            return string;
        } catch (Exception e) {
            return null;
        }
    }

    public static String stringFromFile(String filepath, String charset) {
        try {

            FileInputStream fis = new FileInputStream(filepath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, charset));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

}
