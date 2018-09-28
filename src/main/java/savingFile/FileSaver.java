package savingFile;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class FileSaver {
	
	public static void saveFile(StringWriter sw, String fileType) {
		JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		fileChooser.setAcceptAllFileFilterUsed(false); 
		fileChooser.setDialogTitle("Select a " + fileType +" file"); 

        FileNameExtensionFilter restrict = new FileNameExtensionFilter(fileType + " files", fileType); 
        fileChooser.addChoosableFileFilter(restrict); 
        
        fileChooser.setSelectedFile(new File("registration." + fileType));
        
		int returnValue = fileChooser.showSaveDialog(null);
		
		File selectedFile = null;
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			try {
				FileWriter fw = new FileWriter(selectedFile);
				fw.write(sw.toString());
				fw.close();
				
			} catch (IOException exception) {
				exception.printStackTrace();
			}
			
			System.out.println(selectedFile.getAbsolutePath());
		}
		
		switch (fileType) {
        case "HTML":  openHTMLFile(selectedFile.getAbsolutePath());
                 break;
		}
	}
	
	public static void openHTMLFile(String filePath) {
		try {
			  Desktop desktop = java.awt.Desktop.getDesktop();
			  URI URL = new URI(filePath);
			  desktop.browse(URL);
			} catch (Exception e) {
			  e.printStackTrace();
			}
	}
}
