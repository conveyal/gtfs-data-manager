package utils;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class HashUtils {
	
	public static String hashString(String input)  {
		
		try {
		
			byte[] bytesOfMessage = input.getBytes("UTF-8");	
			
			return DigestUtils.md5Hex(bytesOfMessage);
			
		}
		catch(Exception e) {
			
			return "";
		}
	}

	
	public static String hashFile(File file)  {
		
		try {
			
			return "" + FileUtils.checksumCRC32(file);
			
		}
		catch(Exception e) {
			
			return "";
		}
	}

}

