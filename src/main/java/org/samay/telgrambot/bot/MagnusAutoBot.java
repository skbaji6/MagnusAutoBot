package org.samay.telgrambot.bot;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.samay.telgrambot.config.BotConfig;
import org.samay.telgrambot.watermark.ImageWaterMarker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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
	
	@Autowired
	private BotConfig botConfig;

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
					.setChatId(update.getMessage().getChatId())
					.setText("");
			try {
				execute(message); // Call method to send the message
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}
		}

		if (update.hasMessage() && update.getMessage().hasDocument()) {
			Document document = update.getMessage().getDocument();
			String caption="";
			String filename=document.getFileName();
			filename=filename.replaceAll("@movieztrends", "");
			filename=filename.replaceAll("@","");
			filename=filename.replaceAll("_"," ");
			
			String language="";
			String quality="";
			String year="";
			if(filename.toLowerCase().contains("tel")) {
				language += "-Telugu";
			}
			if(filename.toLowerCase().contains("hin")) {
				language += "-Hindi";
			}
			if(filename.toLowerCase().contains("eng")) {
				language += "-English";
			}
			
			
			
			if(filename.toLowerCase().contains("720")) {
				quality="720P";
			}
			if(filename.toLowerCase().contains("480")) {
				quality="480P";
			}
			if(filename.toLowerCase().contains("360")) {
				quality="360P";
			}
			if(filename.toLowerCase().contains("hd") && filename.toLowerCase().contains("cam")) {
				quality="HD-Cam";
			}else if(filename.toLowerCase().contains("hd")) {
				quality="Proper HD";
			}
			
			Pattern p = Pattern.compile("((19|20)\\d\\d)");
	        Matcher m = p.matcher(filename);
	        
	        while(m.find()) {
	           year=m.group();
	        }
	        
	        filename=getFileName(filename);
	        filename=filename.replaceAll("\\."," ");
			
	        caption+="🎬  Title: "+filename;
			if(!StringUtils.isEmpty(year)) {
					caption+="\n🎞  Year : " + year;
			}
			if(!StringUtils.isEmpty(language)) {
				caption+="\n🔊 Language : "+language.substring(1);
			}
			if(!StringUtils.isEmpty(quality)) {
				caption+="\n💿 Quality : "+quality;
			}
			caption+="\n📤 Uploaded : @movieztrends";
			
			caption+="\n\n Invite https://t.me/movieztrends";
			
			String fileType=document.getMimeType();
			Integer filesize=document.getFileSize();
			SendDocument sdocument = new SendDocument().setChatId(update.getMessage().getChatId()).setDocument(document.getFileId())
					.setCaption(caption);		
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
			String f_id = photos.stream().sorted(Comparator.comparing(PhotoSize::getFileSize).reversed()).findFirst()
					.orElse(null).getFileId();
			// Know photo width
			int f_width = photos.stream().sorted(Comparator.comparing(PhotoSize::getFileSize).reversed()).findFirst()
					.orElse(null).getWidth();
			// Know photo height
			int f_height = photos.stream().sorted(Comparator.comparing(PhotoSize::getFileSize).reversed()).findFirst()
					.orElse(null).getHeight();
			int watermarkScale = f_height / 4;

			TelegramFileDownloader filedownloader = new TelegramFileDownloader(() -> getBotToken());
			File downloadedfile;
			try {
				GetFile getFile = new GetFile().setFileId(f_id);
				org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
				System.out.println("FilePath: " + file.getFilePath());

				downloadedfile = filedownloader.downloadFile(file.getFilePath());
				String watermarkAppliedImageLocation = imageWaterMarker.applyWaterMark(watermarkScale, downloadedfile,
						f_id);

				// Set photo caption
				String caption = "file_id: " + f_id + "\nwidth: " + Integer.toString(f_width) + "\nheight: "
						+ Integer.toString(f_height);
				System.out.println(caption);
				SendPhoto msg = new SendPhoto().setChatId(chat_id)
						.setPhoto(new InputFile(new File(watermarkAppliedImageLocation), "@movieztrends_photo"));

				execute(msg); // Call method to send the photo with caption
				System.out.println("trying to delete Downloaded file Path : "+downloadedfile.toPath());
				Files.delete(Paths.get(watermarkAppliedImageLocation));
			} catch (TelegramApiException e) {
				e.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}

	@Override
	public String getBotUsername() {
		return botConfig.getUsername();
	}

	@Override
	public String getBotToken() {
		return botConfig.getToken();
	}
	
	private static String getFileName(String fileName) {
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(0,fileName.lastIndexOf("."));
        else return "";
    }

}
