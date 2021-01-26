package apicpn.models.DAO;

import java.io.File;
import java.io.IOException;

import org.springframework.util.ResourceUtils;

public class CPNDAO {
	
	private String fileCPN;
	
	public CPNDAO(String fileName) {
		configPathFileCPN(fileName);
	}
	
	public String getFileCPN() {
		return fileCPN;
	}
	
	private void configPathFileCPN(String fileName){
	    try {
	    	File file = ResourceUtils.getFile("classpath:"+fileName);
	        fileCPN = "" + file;
	    } catch (IOException e) {
	    	e.getMessage();
	    }
    }
}