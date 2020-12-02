import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;

public class RecorderHelper {
	
	private static String fileSeparator = File.separator;
	
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_RESET = "\u001B[0m";
	
	private static String timeFrom = "";
	private static String timeTo = "";
	private static String name = "";
	private static String url = "";
	private static String code = "";
	private static int lengthAccepted = 160;
	private static String jarDir = "";
	private static String errorMessage = "";
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static String getRunningDir(String myFileName){
		
		CodeSource codeSource = StartRecorder.class.getProtectionDomain().getCodeSource();
		
		try {
			File jarFile = new File(codeSource.getLocation().toURI().getPath());
			jarDir = jarFile.getParentFile().getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		File f = new File(jarDir + fileSeparator + myFileName);
		
		if (!f.exists()) {
			System.out.println("Could not find config.properties, this program tried to find the file in this location:");
			System.out.println(f.getPath());
			System.out.println("Closing the application!");
			System.exit(1);
		}
		
		return jarDir;
	}
	
	//Laddar properties från config.properties
	public Properties readPropertiesFile(String fileName) throws IOException {
	      FileInputStream fis = null;
	      Properties prop = null;
	      try {
	         fis = new FileInputStream(fileName);
	         prop = new Properties();
	         prop.load(fis);
	      } catch(FileNotFoundException fnfe) {
	         fnfe.printStackTrace();
	      } catch(IOException ioe) {
	         ioe.printStackTrace();
	      } finally {
	         fis.close();
	      }
	      return prop;
	}
	
	public boolean loadChannels(ArrayList<M3UHolder> myChannels, boolean restarted, String text){
		
		 clearScreen();
		
		 Iterator<M3UHolder> itr = myChannels.iterator();
		 
		 int counter = 0;
		 
		 while(itr.hasNext()) {
			 
			 M3UHolder mH = new M3UHolder();
			 
			 mH = itr.next();
			
			 name = "Channel name: " + mH.getName().trim();
			 code = "Channel code: " + mH.getCode().trim();
			 
			 int totalOfBoth = name.length() + code.length();
			 
			 int lengthOfSpaces = lengthAccepted - totalOfBoth;
			 
			 if (code.length() == 18) {
				 lengthOfSpaces = lengthOfSpaces -1;
			 }
			 
			 String space = createSpaces(lengthOfSpaces);
			 
			 int lengthTotal = name.length() + space.length() + code.length();
			 
			 String underLine = createUnderLine(lengthTotal);
			 
			 System.out.println(name + space + code);
			 System.out.println(underLine);
			 
			 counter++;
			 
			 if (counter==20) {
				 
				 if (waitForChannelInput(myChannels, restarted, text)) {
					 break;
				 }else {
					 if (getErrorMessage()!="") {
						 return false;
					 }
				 }
				 
				 counter=0;
				 
				 clearScreen();
			 } 
		 }
		 
		 return true;
	}
	
	public boolean waitForChannelInput(ArrayList<M3UHolder> myChannels, boolean restarted, String text) {
		
		String input = "";
		
		if (getErrorMessage()!="") {
			 printErrorMessage(getErrorMessage());
			 setErrorMessage("");
		 }
		
		System.out.println("\nPlease choose a channel code you want to record from the list or \"ENTER\" to continue: ");
		
		boolean done = false;
		
		while(true) {
		        
			input = readInput();
			
			if (input == "" || input.length()==0) {
				return false;
			}
			
		    if(isNumeric(input)) {
		    	
		    	 Iterator<M3UHolder> itr = myChannels.iterator();
					
				 while(itr.hasNext()) {
					 
					 M3UHolder mH = new M3UHolder();
					 
					 mH = itr.next();
					 
					 if (mH.getCode().equalsIgnoreCase(input)) {
						 
						 url = mH.getUrl();
						 code = mH.getCode();
						 name = mH.getName();
						 
						 done = true; 
					 }
				 }		
		    }else {
		    	setErrorMessage( "\nYou have entered a channel code that is not numeric, reloading channel list!");
		    	return false;
		    }
		    
		    if(done) {
		    	break;
		    }else {
	    		setErrorMessage("\nThe channel that you have entered does not exist, reloading channel list");
	    		return false;
		    }
		}
		
		return true;
	}
	
	public String waitForTimeInput(String message, boolean startTime) {
		
		clearScreen();
		
		String input = "";
		
		System.out.println(message);
		
		while(true) {
			
			input = readInput();
			
			if (correctTimeSyntax(input, startTime)) {
				return input;
			}else {
				clearScreen();
				printErrorMessage("You have entered wrong time format, should be HH:MM, please try again: \n");
			}
		}
	}
	
	public boolean correctTimeSyntax(String time, boolean startTime) {
		    try {
		        LocalTime.parse(time);
		        if (startTime) {
		        	timeFrom = time;
		        }else {
		        	timeTo = time;
		        }
		        return true;
		    } catch (DateTimeParseException | NullPointerException e) {
		    	return false;
		    }
	}
	
	public boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false;
	    }
	    try {
	        @SuppressWarnings("unused")
			double d = Double.parseDouble(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
	public static void clearScreen(){
		
		if (OS.contains("win")) {
			myRunnable("cls", OS);
        }else {
        	System.out.print("\033\143");
        }
	}
	
	private static String createSpaces( int spaces ) {
		  return CharBuffer.allocate( spaces ).toString().replace( '\0', ' ' );
	}
	
	private static String createUnderLine( int lines ) {
		  return CharBuffer.allocate( lines ).toString().replace( '\0', '_' );
	}
	
	public void startCounter() {
		
		String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
		 
		while(true) {
			 
			if (currentTime.trim().equalsIgnoreCase(timeFrom.trim())){
				break;
			}
			
			waitingForStartDisplay("Waiting for recording to start ... ");
 
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//
			}
			
			currentTime = new SimpleDateFormat("HH:mm").format(new Date());
		}
	}
	
	public void endCounter() {
		
		String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
		 
		while(true) {
			 
			if (currentTime.trim().equalsIgnoreCase(timeTo.trim())){
				break;
			}
			
			waitingForEndDisplay("Recording has started, waiting for recording to end ... ");
 
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//
			}
			
			currentTime = new SimpleDateFormat("HH:mm").format(new Date());
		}
	}
	
	public void waitingForStartDisplay(String text) {
		
		clearScreen();
		
		String displayTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		
		displayTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		System.out.println("\nRecording channel:  " + name.trim());
		System.out.println("Recording code:     " + code.trim());
		System.out.println("Recording startime: " + timeFrom.trim());
		System.out.println("Recording stoptime: " + timeTo.trim());
		System.out.println("URL of channel:     " + url.trim() + "\n");
		printWaitingMessage("\n" + text + displayTime);
			
	}
	
	public void waitingForEndDisplay(String text) {
		
		clearScreen();
		
		String displayTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		
		displayTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
		System.out.println("\nRecording channel:  " + name.trim());
		System.out.println("Recording code:     " + code.trim());
		System.out.println("Recording startime: " + timeFrom.trim());
		System.out.println("Recording stoptime: " + timeTo.trim());
		System.out.println("URL of channel:     " + url.trim() + "\n");
		printOkMessage("\n" + text + displayTime);
			
	}
	
	public void startRecFFMPEG(String filePath) throws InterruptedException {
		
		String startString = "ffmpeg -hide_banner -loglevel panic -i " + url + " -c copy " + createFileName(filePath);
		
		new Thread(new Runnable() {
            @Override
            public void run() {
            	myRunnable(startString, OS);
            }
        }).start();
				
	}
	
	public void startRecRegular(String filePath) {
		
		new Thread(new Runnable() {
            @Override
            public void run() {
            	try {
        			
        			InputStream input = new URL(url).openStream();
        			FileOutputStream outputStream = new FileOutputStream(new File(createFileName(filePath)));
        			int read;
        			byte[] bytes = new byte[8096];
        			
        	        while ((read = input.read(bytes)) != -1) {
        	            
        	        	outputStream.write(bytes, 0, read);
        	            
        	            String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
        	    			 
            			if (currentTime.trim().equalsIgnoreCase(timeTo.trim())){
            				outputStream.close();
            				input.close();
            				break;
            			}
        	        }
        			        
        		} catch (MalformedURLException e) {
        			e.printStackTrace();
        		} catch (IOException e) {
        			printErrorMessage("Could not open the stream, and therefor an FileNotFoundException occured, exiting program!");
        			System.exit(1);
        		}	
            }
        }).start();
	}
	
	public static String createFileName(String filePath) {
		
		String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());
		
		LocalDateTime now = LocalDateTime.now();
		int hour = now.getHour();
		int minute = now.getMinute();
		
		String h = "";
		String m = "";
		
		if (hour<10) {
			h = "0" + hour;
		}else {
			h = String.valueOf(hour);
		}
		
		if (minute < 10) {
			m = "0" + m;
			
			if (m.equalsIgnoreCase("0")) {
				m = "00";
			}
		}else {
			m = String.valueOf(minute);
		}
		
		String displayTime = date + "-" + h + m;
		
		String fileName = filePath + fileSeparator + "MyRecording-" + displayTime + ".ts";
		
		return fileName;
	}
	
	public void stopRecording() {
		String stopString = "kill -9 $(ps -ef | grep -v grep | grep ffmpeg | awk '{print $2}')";
		
		new Thread(new Runnable() {
            @Override
            public void run() {
            	myRunnable(stopString, OS);
            }
        }).start();
	}
	
	private static void myRunnable(String command, String OS) {
		
		

		if (OS.contains("win")) {
			 try {
				new ProcessBuilder("cmd", "/c", command).inheritIO().start().waitFor();
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
        }else {
        	
        	ProcessBuilder processBuilder = new ProcessBuilder();
        	processBuilder.command("bash", "-c", command);
        	
        	try {

    	        Process process = processBuilder.start();

    	        process.waitFor();

    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    } catch (InterruptedException e) {
    	        e.printStackTrace();
    	    }
        } 
	}
	
	public static void getM3UFile(String myUrl, String path, String m3uName) {
		
		try {
			
			InputStream input = new URL(myUrl).openStream();
			FileOutputStream outputStream = new FileOutputStream(new File(path + fileSeparator + m3uName));
			int read;
			byte[] bytes = new byte[8096];
			
	        while ((read = input.read(bytes)) != -1) {
	        	outputStream.write(bytes, 0, read);
	        }
	        
	        outputStream.close();
			input.close();
			        
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public String readInput() {
		
		String input = "";
		
		@SuppressWarnings("resource")
		Scanner userInput = new Scanner(System.in);
		
		input = userInput.nextLine();
		
		if (input=="" || input.length()==0) {
			return "";
		}else {
			return input.trim();
		}	
	}
	
	public void endMessage() {
		
		String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
		 
		printOkMessage("\nRecording ended successfully: " + currentTime);
	}

	public static String getErrorMessage() {
		return errorMessage;
	}

	public static void setErrorMessage(String errorMessage) {
		RecorderHelper.errorMessage = errorMessage;
	}
	
	public static void printErrorMessage(String message) {
		System.out.println(ANSI_RED + message + ANSI_RESET);
	}
	
	public static void printOkMessage(String message) {
		System.out.println(ANSI_GREEN + message + ANSI_RESET);
	}
	
	public static void printWaitingMessage(String message) {
		System.out.println(ANSI_YELLOW + message + ANSI_RESET);
	}
}
