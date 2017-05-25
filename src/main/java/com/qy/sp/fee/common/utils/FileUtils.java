package com.qy.sp.fee.common.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {

	
	/**
	 * 将流转换为string
	 * @param iStream
	 * @return
	 * @throws Exception
	 */
	public static String reader(InputStream iStream) throws Exception{
		BufferedReader br = null;
		InputStreamReader reader = null;
		StringBuffer buff = new StringBuffer();
		try {
			reader = new InputStreamReader(iStream);
			br = new BufferedReader(reader);
			String line = null;
			while ((line = br.readLine()) != null) {
				buff.append(line);
			}
		}finally{
			if(null!=iStream){
				iStream.close();
			}
			if(null!=reader){
				reader.close();
			}
			if(null!=br){
				br.close();
			}
		}
		return buff.toString();
	}
	public static byte[] InputStreamToByte(InputStream is) throws IOException {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		int ch;
		while ((ch = is.read()) != -1) {
			bytestream.write(ch);
		}
		byte imgdata[] = bytestream.toByteArray();
		bytestream.close();
		return imgdata;
	}
	//获得指定文件的byte数组 
    public static byte[] getBytes(String filePath){  
        try {  
            return InputStreamToByte(new FileInputStream(new File(filePath)));
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return null;  
    }  
  
    //根据byte数组，生成文件 
    public static File getFile(byte[] bfile,String fileName) {  
        BufferedOutputStream bos = null;  
        FileOutputStream fos = null;  
        File file = null;  
        try {  
            file = new File(fileName);
            if(!file.exists()){
            	file.createNewFile();
            }
            fos = new FileOutputStream(file);  
            bos = new BufferedOutputStream(fos);  
            bos.write(bfile); 
            return file;
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (bos != null) {  
                try {  
                    bos.close();  
                } catch (IOException e1) {  
                   e1.printStackTrace();  
                }  
            }  
            if (fos != null) {  
                try {  
                    fos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            }  
        } 
        return null;
    }
}
