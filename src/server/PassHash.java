package server;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PassHash
{
	public static String PassHash (String passwordToHash)
	{
			String generatedPassword = null;
	        try {

	            MessageDigest md = MessageDigest.getInstance("MD5");
	            md.update(passwordToHash.getBytes());
	            byte[] bytes = md.digest();
	            StringBuilder sb = new StringBuilder();
	            for(int i=0; i< bytes.length ;i++)
	            {
	                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
	            }
	            generatedPassword = sb.toString();
	           
	        }
	        
	        catch (NoSuchAlgorithmException e)
	        {
	            e.printStackTrace();
	        }
	        
	        PrintWriter writer;

	        return generatedPassword;
	        
	}
}
