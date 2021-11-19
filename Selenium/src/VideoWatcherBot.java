import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * A class to represent a bot that can keep YouTube videos playing with autoplay enabled
 * The bot uses Selenium with ChromeDriver to keep playing YouTube videos in a Chrome Webpage
 * The bot will try skipping ads and closing YouTube's idle dialog popup
 */
public class VideoWatcherBot implements Runnable {
	
	private boolean done = false;
	private String videoUrl = "";
	private final static int BUFFER = 1;

	/**
	 * Listens for user input to start watching a video or stop watching videos
	 * Starts a new thread for the bot or terminates it accordingly
	 */
	public static void main(String args[]) {
		
		//Initialize scanner to get input
		Scanner input = new Scanner(System.in);
		
		//Starts an instance of the bot
		VideoWatcherBot videoWatcher = new VideoWatcherBot();
		
		//Input validation
		while(videoWatcher.getVideoUrl().equals("")) {
			
			//Prompt user for the video to begin playing
			System.out.println("Please enter the url of video or playlist you would like to start watching.");
			
			//Retrieves the video link
			String url = input.next();
			
			//Accepts the link if it directs to YouTube
			if(url.startsWith("https://www.youtube.com")) {
				
				videoWatcher.setVideoUrl(url);
			}
		}
		
		//Creates a thread for the bot
		Thread thread = new Thread(videoWatcher);
		thread.start();
		
		System.out.println("When you would like to stop, please enter 'done'.");
		
		//Actively waits for the command to stop the bot
		while(videoWatcher.getDone() == false) {
			
			String done = input.next(); 
			
			if(done.equals("done")) {
				
				input.close();
				videoWatcher.setDone(true); 
				
				System.out.println("All Done!");
				break;
			}
			
			System.out.println("When you would like to stop, please enter 'done'.");
		}
	}

	/**
	 * Will try to skip ads, or will wait until they are over if it cannot
	 * @param driver - The WebDriver that is being used
	 */
	private static void handleAd(WebDriver driver) {
		
		int ads = 1;					   //There will always be at least one ad 
		final int TIME_UNTIL_CAN_SKIP = 5; //This is the default amount of seconds viewers have to wait before they can skip YouTube ads
		
		//Check if there are multiple consecutive ads that will be playing in a row
		try {
			
			//Locates the ad banner for information
			String text = driver.findElement(By.className("ytp-ad-simple-ad-badge")).getAttribute("innerText"); //"video-ads" or "ytp-ad-player-overlay"
			
			//Records the number of ads
			if(text.length() > 4) {

				ads = Integer.parseInt(text.substring(text.indexOf("of") + 3,  text.indexOf("of") + 4));
			}
		}
		catch(NoSuchElementException adBadge) {

			System.out.println("No ad badge");
		}
		
		//Will try to skip the first ad, if possible
		//If cannot, then it will try to skip the next ad, and so on
		for(int i = 0; i < ads; i++) {
			
			try {
				
				//Locates the skip button
				WebElement skip = driver.findElement(By.className("ytp-ad-skip-button-text")); 
				
				//Waits the default time until the skip button can be skipped, plus a buffer
				TimeUnit.SECONDS.sleep(TIME_UNTIL_CAN_SKIP + BUFFER);
				skip.click();
				
				//If an ad is skipped, even if there are more ads scheduled, the rest should also be skipped, so stop loop
				i = ads;
			}
			//There is no skip button, so will have to wait for it to be over
			catch(NoSuchElementException skipButton) {
				
				//Checks the duration of the ad
				int adTime = checkVideoDuration(driver, true);
				
				//Waits for the duration of the ad to be passed
				try {
					
					TimeUnit.SECONDS.sleep(adTime + BUFFER);
				} 
				catch (InterruptedException interrupted) {
					
					interrupted.printStackTrace();
				}
			}
			catch(InterruptedException interrupted) {
				
				interrupted.printStackTrace();
			}
			catch(ElementNotInteractableException notInteractable) {

				System.out.println("Cannot click skip ad");
			}
		}
	}
	
	/**
	 * Checks the duration of an ad or a video
	 * @param driver - The WebDriver being used
	 * @param isAd whether we are checking the duration of an ad or not
	 * @return the duration of the ad or video in seconds
	 */
	private static int checkVideoDuration(WebDriver driver, Boolean isAd) {
		
		//Locates the necessary element and retrieves the duration of the ad or video
		String videoTime = driver.findElement(By.className("ytp-bound-time-right")).getAttribute("innerText");
		
		//Gets the colon index in the string to parse it
		int colonSeparator = videoTime.indexOf(":");
		
		String timeInHours = "";
		String timeInMins = "";
		String timeInSecs = "";
		int timeToWaitInSecs = 0;

		
		//Video less than a hour
		if(videoTime.length() < 6) {
			
			//Records the minutes and seconds that the duration of the ad or video will last for
			timeInMins = videoTime.substring(0, colonSeparator);
			timeInSecs = videoTime.substring(colonSeparator + 1);
			
			
			//Calculates the duration in seconds
			timeToWaitInSecs = Integer.parseInt(timeInSecs) + Integer.parseInt(timeInMins) * 60;
		}
		//Video is a hour or more
		else {
			//Gets the second colon to parse the longer string
			int secondColonSeparator = videoTime.substring(colonSeparator + 1).indexOf(":");
			
			
			//Records the hours, minutes and seconds that the duration of the ad or video will last for
			timeInHours = videoTime.substring(0, colonSeparator);
			timeInMins = videoTime.substring(colonSeparator + 1, colonSeparator + secondColonSeparator + 1);
			timeInSecs = videoTime.substring(colonSeparator + secondColonSeparator + 2);
			
			
			//Calculates the duration in seconds
			timeToWaitInSecs = Integer.parseInt(timeInSecs) + Integer.parseInt(timeInMins) * 60 + Integer.parseInt(timeInHours) * 3600;
		}
		
		return timeToWaitInSecs;
	}

	/**
	 * Starts up the video playing bot
	 */
	public void run() {
		
		System.setProperty("webdriver.chrome.driver", "C:/SeleniumDriver/chromedriver.exe");
		
		//Opens a chrome web browser
		WebDriver driver = new ChromeDriver();
		
		//Navigates to the url
		driver.get(getVideoUrl());
		
		//Starts playing the video
		WebElement play = driver.findElement(By.className("ytp-play-button"));
		play.click();

		//Records the title of the current video
		String title = driver.getTitle();
		
		
		while(true) {
			
			//Continually checks if the user entered the terminate command
			boolean done = getDone();
			
			
			//Stops the bot if commanded to terminate
			if(done) {
				
				driver.quit();
				break;
			}
			
			//Continually checks the current title to see if it has changed
			String currentTitle = driver.getTitle();
			
			
			//Updates the title if it has changed
			if(!title.equals(currentTitle)) {
				
				title = currentTitle;
			}
			
			//Routinely checks if there is an ad
			try {
				
				driver.findElement(By.className("ytp-ad-player-overlay"));
				
				//If the ad element is found, there is an ad, and code execution will continue so that we can handle it
				handleAd(driver);
			}
			catch(NoSuchElementException overlay) {
				
				//The ad element is not found because there is no ad, so we do not need to do anything
			} 

			try {
				
				//Every second, check if the page has changed
				new WebDriverWait(driver, BUFFER).until(ExpectedConditions.not(ExpectedConditions.titleIs(title)));
			}
			//Video hasn't finished yet, or it has, but autoplay has not yet directed the webpage to the next video
			catch(TimeoutException e) {
				
				//Check the aria-label attribute of the play button
				String videoStatus = play.getAttribute("aria-label");
				
				
				//If the label is "play", then that means the video was paused for some reason
				if(videoStatus != null && videoStatus.startsWith("Play")) {
					
					//Check if user has been idle for too long and an idle dialog popped up
					try {
						
						//Locate the dialog
						WebElement confirm = driver.findElement(By.id("confirm-button"));
						
						
						//If the dialog is found, click continue to keep playing videos
						WebElement button = confirm.findElement(By.id("button"));
						button.click();
					}
					catch(NoSuchElementException dialog) {
						
					}
					catch(ElementNotInteractableException notInteractable) {

						System.out.println("Cannot click on dialog.");
					}
				}
			}
		}
	}


	/**
	 * Sets the url to begin watching videos
	 * @param url - The url of the first video to watch
	 */
	private void setVideoUrl(String url) {
		
		videoUrl = url;
	}
	
	/**
	 * Gets the url to start watching videos
	 * @return the url of the video to start watching 
	 */
	private String getVideoUrl() {
		
		return videoUrl;
	}
	
	/**
	 * Sets the variable that determines whether the user wants to stop watching videos
	 * @param bool - The boolean determining whether the user wants to stop or not
	 */
	private void setDone(boolean bool) {
		
		done = bool;
	}
	
	/**
	 * Retrieves the variable the determines whether the user wants to stop watching videos
	 * @return the boolean that tells if the user is done watching videos
	 */
	private boolean getDone() {
		
		return done;
	}
}