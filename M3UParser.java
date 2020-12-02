import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.util.ArrayList;

public class M3UParser {

	public M3UParser() throws Exception {

	}

	@SuppressWarnings("resource")
	public String convertStreamToString(java.io.InputStream is) {
		try {
			return new java.util.Scanner(is).useDelimiter("\\A").next();
		} catch (java.util.NoSuchElementException e) {
			return "";
		}
	}

	public ArrayList<M3UHolder> parseFile(File f) throws FileNotFoundException {
		
		ArrayList<M3UHolder> myArray = new ArrayList<M3UHolder>();
		
		if (f.exists()) {
			String stream = convertStreamToString(new FileInputStream(f));
			stream = stream.replaceAll("#EXTM3U", "").trim();
			String[] arr = stream.split("#EXTINF.*,");
			String lines[] = null;
			String name = "";
			String url = "";
			String code = "";
			
			{
				for (int n = 0; n < arr.length; n++) {
					
					lines = arr[n].split("\\r?\\n");
					
					if(lines!=null && lines.length==2) {
						
						if (endsWithDigits(lines[1])) {
					
							for (int i = 0; i<lines.length;i++) {
								if (i==0) {
									name = lines[i];
								}else {
									url = lines[i];
									code = lastBigInteger(lines[i]).toString();
									M3UHolder m3uHold = new M3UHolder();
									
									m3uHold.setName(name);
									m3uHold.setUrl(url);
									m3uHold.setCode(code);
									
									myArray.add(m3uHold);
								}
							}
						}
					}
				}
			}
		}
		return myArray;
	}
	
	private static boolean endsWithDigits(String s) {
		
		String shortString = s.substring(s.length() -3, s.length());
		
		    if (shortString == null) {
		        return false;
		    }
		    try {
		        @SuppressWarnings("unused")
				double d = Double.parseDouble(shortString);
		    } catch (NumberFormatException nfe) {
		        return false;
		    }
		    return true;
		
	}
	
	private static BigInteger lastBigInteger(String s) {
	    int i = s.length();
	    while (i > 0 && Character.isDigit(s.charAt(i - 1))) {
	        i--;
	    }
	    return new BigInteger(s.substring(i));
	}
}
