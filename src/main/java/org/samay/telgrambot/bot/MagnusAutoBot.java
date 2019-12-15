package org.samay.telgrambot.bot;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.samay.telgrambot.config.BotConfig;
import org.samay.telgrambot.utils.TelegramUtil;
import org.samay.telgrambot.watermark.ImageWaterMarker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MagnusAutoBot extends TelegramLongPollingBot {
	@Autowired
	private ImageWaterMarker imageWaterMarker;

	@Autowired
	private BotConfig botConfig;

	@Autowired
	private TelegramUtil telegramUtil;

	@Autowired
	private Environment env;

	@Override
	public void onUpdateReceived(Update update) {
		// for text messages
		if (update.hasMessage() && update.getMessage().hasText()) {
			String caption = "";
			if (update.getMessage().getText().startsWith("/extract")) {
				caption = telegramUtil.extractDetails(update.getMessage().getText().replace("/extract", ""));
			} else if (update.getMessage().getText().startsWith("http")) {
				try {
					String[] text = update.getMessage().getText().split("\\|");
					String fileName = null;
					URL url = new URL(text[0]);
					if (text.length == 2) {
						fileName = text[1].trim();
					} else {
						fileName = FilenameUtils.getName(update.getMessage().getText());
					}

					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setRequestMethod("HEAD");
					urlConnection.connect();
					SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
							.setChatId(update.getMessage().getChatId())
							.setText("File Size detected as " + urlConnection.getContentLength()
									+ "\nFile will be named : " + fileName
									+ "\n downloading Started be patience to complete the request");
					execute(message);
					File file = new File(fileName);
					FileUtils.copyURLToFile(url, file);
					System.out.println("File is Downloaded at " + file.getAbsolutePath());
					message = new SendMessage() // Create a SendMessage object with mandatory fields
							.setChatId(update.getMessage().getChatId())
							.setText("Uploading started");
					execute(message);
					
					SendDocument msg = new SendDocument().setChatId(update.getMessage().getChatId())
							.setDocument(new InputFile(file,
									"@movieztrends " + fileName
											.replaceAll("@", "").replaceAll("movieztrends", "").trim()));
					execute(msg);
					if (Arrays.asList(env.getActiveProfiles()).contains("prod")) {
						Files.delete(Paths.get(file.getAbsolutePath()));
					} else {
						System.out.println("File Downloaded Location:" + file.getAbsolutePath());
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else if (update.getMessage().getText().startsWith("/upload")) {

				if (update.getMessage().getReplyToMessage() != null) {
					Message messagetoUpload = update.getMessage().getReplyToMessage();
					if (messagetoUpload.hasDocument()) {
						try {
							File downloadedFile = telegramUtil.downloadFile(this,
									messagetoUpload.getDocument().getFileId());
							SendDocument msg = new SendDocument().setChatId(update.getMessage().getChatId())
									.setDocument(new InputFile(downloadedFile,
											"@movieztrends" + messagetoUpload.getDocument().getFileName()
													.replaceAll("@", "").replaceAll("movieztrends", "").trim()));
							execute(msg);
							System.out.println(
									"trying to delete Downloaded file Path : " + downloadedFile.getAbsolutePath());
							if (Arrays.asList(env.getActiveProfiles()).contains("prod")) {
								Files.delete(Paths.get(downloadedFile.getAbsolutePath()));
							} else {
								System.out.println("File Downloaded Location:" + downloadedFile.getAbsolutePath());
							}
						} catch (TelegramApiException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				;
			}
			if (!StringUtils.isEmpty(caption)) {
				SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
						.setChatId(update.getMessage().getChatId()).setText(caption);

				try {
					execute(message); // Call method to send the message
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}
			}
		}

		// for Documents
		if (update.hasMessage() && update.getMessage().hasDocument()) {
			Document document = update.getMessage().getDocument();

			String fileType = document.getMimeType();
			Integer filesize = document.getFileSize();
			String fileExtension = telegramUtil.getFileExtension(document.getFileName());
			if ("mkv".equalsIgnoreCase(fileExtension) || "mp4".equalsIgnoreCase(fileExtension)) {

				SendDocument sdocument = new SendDocument().setChatId(update.getMessage().getChatId())
						.setDocument(document.getFileId())
						.setCaption(telegramUtil.extractDetails(document.getFileName()));
				try {
					execute(sdocument);
				} catch (TelegramApiException e) {
					e.printStackTrace();
				}
			}
		}

		if (update.hasMessage() && update.getMessage().hasVideo()) {
			Video document = update.getMessage().getVideo();

			SendDocument sdocument = new SendDocument().setChatId(update.getMessage().getChatId())
					.setDocument(document.getFileId()).setCaption(telegramUtil.extractDetails(document.toString()));
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
				System.out.println("trying to delete Downloaded file Path : " + downloadedfile.toPath());
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

}
