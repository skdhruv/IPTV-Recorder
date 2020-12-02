import java.io.File;
import java.util.ArrayList;
import java.util.Properties;


public class StartRecorder {
	
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_RESET = "\u001B[0m";
	
	private static String fileSeparator = File.separator;
	private static String m3uFile = "";
	private static String destinationPath = "";
	private static String url = "";
	private static String m3uName = "";
	private static boolean useFFMPEG = false;
	private static boolean useM3UFile = false;

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		
		try{
			 
			 RecorderHelper rH = new RecorderHelper();
			
			 Properties prop = new Properties();
			 
			 ArrayList<M3UHolder> myChannels = new ArrayList<M3UHolder>();
			
			 prop = rH.readPropertiesFile(rH.getRunningDir("config.properties") + fileSeparator + "config.properties");
			 destinationPath = prop.getProperty("destinationPath");
			 useFFMPEG = Boolean.valueOf(prop.getProperty("useFFMPEG"));
			 useM3UFile = Boolean.valueOf(prop.getProperty("useM3UFile"));
			 
			 if (useM3UFile) {
				 m3uFile = prop.getProperty("m3uFile");
				 M3UParser m3u = new M3UParser();
				 myChannels = m3u.parseFile(new File(m3uFile));
			 }else {
				 M3UParser m3u = new M3UParser();
				 url = prop.getProperty("url");
				 m3uName = prop.getProperty("m3uName");
				 rH.getM3UFile(url,destinationPath, m3uName);
				 myChannels = m3u.parseFile(new File((destinationPath + fileSeparator + "NordicStream.m3u")));
			 }
			 
			 //Visa kanaler
			 while(true) {
				 if (rH.loadChannels(myChannels, false, "")){
					 break;
				 } 
			 }
			
			 //Hanterar starttid
			 rH.waitForTimeInput("\nPlease choose a starting time for the recording: \n", true);
			 
			 //Hanterar sluttid
			 rH.waitForTimeInput("\nPlease choose a stop time for the recording: \n", false );
			 
			 //Hanterar klocka och när inspelning startar
			 rH.startCounter();
			 
			 if (useFFMPEG) {
				 
				 //Startar inspelning med ffmpeg
				 rH.startRecFFMPEG(destinationPath);
				 
				 //Hanterar klocka och när inspelningen avslutas
				 rH.endCounter();
				 
				 //Avslutar inspelning
				 rH.stopRecording();
				 
			 }else {
				 //Startar och stoppar inspelning med inputstream/outputstream
				 rH.startRecRegular(destinationPath);
				 //Visar meddelande om när inspelningen slutar
				 rH.endCounter();
			 }
			 
			 //Avslutsmeddelande
			 rH.endMessage();
			 
			 //Vänta några extra sekunder så att övriga inspelningstrådar stängs. 
			 Thread.sleep(10000);
				 
			}catch(Exception e){
				e.printStackTrace();
			}
	}	
}
