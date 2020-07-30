package org.samay.telgrambot.utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.facilities.filedownloader.TelegramFileDownloader;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramUtil {
	
	@Autowired
	private TelegramFileDownloader telegramFileDownloader;
	
	public String extractDetails(String filename) {
		String caption = "";
		filename = filename.replaceAll("@movieztrends", "");
		filename = filename.replaceAll("@", "");
		filename = filename.replaceAll("_", " ");

		String language = "";
		String quality = "";
		String year = "";
		if (filename.toLowerCase().contains("tel") && !filename.toLowerCase().contains("telly")) {
			language += "-Telugu";
		}
		if (filename.toLowerCase().contains("tam")) {
			language += "-Tamil";
		}
		if (filename.toLowerCase().contains("hin")) {
			language += "-Hindi";
		}
		if (filename.toLowerCase().contains("eng")) {
			language += "-English";
		}
		if (filename.toLowerCase().contains("malay")) {
			language += "-Malayalam";
		}

		if (filename.toLowerCase().contains("1080")) {
			quality = "1080P";
		}
		if (filename.toLowerCase().contains("720")) {
			quality = "720P";
		}
		if (filename.toLowerCase().contains("480")) {
			quality = "480P";
		}
		if (filename.toLowerCase().contains("360")) {
			quality = "360P";
		}
		if (filename.toLowerCase().contains("hd") && filename.toLowerCase().contains("cam")) {
			quality = "HD-Cam";
		} else if (filename.toLowerCase().contains("hd")) {
			quality = "Proper HD";
		} else if (filename.toLowerCase().contains("dvd") && filename.toLowerCase().contains("scr")) {
			quality = "DVDScr";
		} else if (filename.toLowerCase().contains("dvd")) {
			quality = "DVD";
		}

		if (filename.toLowerCase().contains("blu") || filename.toLowerCase().contains("brrip")) {
			quality += "-BluRayRip";
		}

		Pattern p = Pattern.compile("((19|20)\\d\\d)");
		Matcher m = p.matcher(filename);

		while (m.find()) {
			year = m.group();
		}

		filename = getFileName(filename);
		filename = filename.replaceAll("\\.", " ");

		caption += "🎬  Title: " + filename;
		if (!StringUtils.isEmpty(year)) {
			caption += "\n🎞  Year : " + year;
		}
		if (!StringUtils.isEmpty(language)) {
			caption += "\n🔊 Language : " + language.substring(1);
		}
		if (!StringUtils.isEmpty(quality)) {
			caption += "\n💿 Quality : " + quality;
		}

		caption += "\n\n Invite https://t.me/joinchat/AAAAAElErExrsYLj2BvzKw";

		return caption;
	}

	public File downloadFile(TelegramLongPollingBot bot,String fileId) throws TelegramApiException {
		GetFile getFile = new GetFile().setFileId(fileId);org.telegram.telegrambots.meta.api.objects.File file = bot.execute(getFile);
		System.out.println("FilePath: " + file.getFilePath());
		return telegramFileDownloader.downloadFile(file.getFilePath());
	}
	
	private String getFileName(String fileName) {
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(0, fileName.lastIndexOf("."));
		else
			return "";
	}
	
	public String getFileExtension(String fileName) {
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
			return fileName.substring(fileName.lastIndexOf(".")+1);
		else
			return "";
	}
}
