package org.samay.telgrambot.watermark;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;

@Component
public class ImageWaterMarker {

	public String applyWaterMark(int watermarkScale, File downloadedfile,String fileId) {
		String localImagePath = fileId+"."+getFileExtension(downloadedfile);
		try {			
			
			BufferedImage image = ImageIO.read(downloadedfile);
			BufferedImage overlay = ImageIO.read(new File("src/main/java/movieztrends.png"));

			// create the new image, canvas size is the max. of both image sizes
			BufferedImage combined = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			
			// paint both images, preserving the alpha channels
			Graphics g = combined.getGraphics();
			g.drawImage(image, 0, 0, null);
			g.drawImage(overlay, 20, 20,watermarkScale,watermarkScale, null);
			//g.setFont(new Font("Verdana", Font.BOLD, 10));
			//g.drawChars("@movieztrends".toCharArray(), 0, 13, 20, 100);

			ImageIO.write(combined, "PNG", new File(localImagePath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return localImagePath;
	}
	
	private String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}
