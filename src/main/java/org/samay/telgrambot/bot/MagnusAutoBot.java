package org.samay.telgrambot.bot;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import org.samay.telgrambot.watermark.ImageWaterMarker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.facilities.filedownloader.TelegramFileDownloader;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MagnusAutoBot extends TelegramLongPollingBot {
	@Autowired
	private ImageWaterMarker imageWaterMarker;

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
					.setChatId(update.getMessage().getChatId())
					.setText(update.getMessage().getText() + "\nJoin and support:\nChannel : @movieztrends");
			try {
				execute(message); // Call method to send the message
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}

		if (update.hasMessage() && update.getMessage().hasDocument()) {
			Document document = update.getMessage().getDocument();
			SendDocument sdocument = new SendDocument().setChatId("@megadlbot").setDocument(document.getFileId());
			try {
				
				execute(sdocument);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}
		
	 if (update.hasMessage() && update.getMessage().hasPhoto()) {
		    // Message contains photo
		    // Set variables
		    long chat_id = update.getMessage().getChatId();

		    // Array with photo objects with different sizes
		    // We will get the biggest photo from that array
		    List<PhotoSize> photos = update.getMessage().getPhoto();
		    // Know file_id
		    String f_id = photos.stream()
		                    .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
		                    .findFirst()
		                    .orElse(null).getFileId();
		    // Know photo width
		    int f_width = photos.stream()
		                    .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
		                    .findFirst()
		                    .orElse(null).getWidth();
		    // Know photo height
		    int f_height = photos.stream()
		                    .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
		                    .findFirst()
		                    .orElse(null).getHeight();
		    int watermarkScale=f_height/4;
		    
		    
		    TelegramFileDownloader filedownloader=new TelegramFileDownloader(()->getBotToken());
		    File downloadedfile;
			try {
				GetFile getFile= new GetFile().setFileId(f_id);
				org.telegram.telegrambots.meta.api.objects.File file=execute(getFile);
				System.out.println("FilePath: "+file.getFilePath());
				
				
				downloadedfile = filedownloader.downloadFile(file.getFilePath());
				String watermarkAppliedImageLocation=imageWaterMarker.applyWaterMark(watermarkScale,downloadedfile,f_id);
			
		    // Set photo caption
		    String caption = "file_id: " + f_id + "\nwidth: " + Integer.toString(f_width) + "\nheight: " + Integer.toString(f_height);
		    System.out.println(caption);
		    SendPhoto msg = new SendPhoto()
		                    .setChatId(chat_id)
		                    .setPhoto(new InputFile(new File(watermarkAppliedImageLocation), "@movieztrends_photo"))
		                    .setCaption("@movieztrends");
		   
		        execute(msg); // Call method to send the photo with caption
		    } catch (TelegramApiException e) {
		        e.printStackTrace();
		    }
		}
	}

	@Override
	public String getBotUsername() {
		return "MagnusAutoBot";
	}

	@Override
	public String getBotToken() {
		return "1006535286:AAHHbNhIsH3Y0qjxzarfidq4dbN17qesSNI";
	}

}
