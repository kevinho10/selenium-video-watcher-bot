# selenium-video-watcher-bot

## Purpose
### The goal of this project is to create a bot using Selenium and ChromeDriver to simulate a user watching videos. The reason this bot was created was to solve one of the biggest problems that viewers watching or listening to YouTube videos face while they are preoccupied doing something else. Because they are preoccupied, they would probably be leaving the browser to idly play, and they will not always get maximal video play-time. I.E. a lengthy ad might pop up in the middle of the video, or if they are idle for too long on the web browser, an idle dialog would pop up and the video would be paused. The role of this bot is to skip ads and close idle dialogs in the place of the viewer as soon as possible because they are not able to.

## How To use
### Once you run the bot, it asks for a url link to the video you would like to watch. Currently, the bot only accepts YouTube links. Once the user enters the url, the bot opens a Chrome web browser and starts playing the video in the link. After that, the user can start doing whatever else they would like to do while letting the bot run idly. The user only needs to enter 'done' when they would like to stop watching the videos, and the bot will terminate.
### Originally, this bot was made for the purpose of helping users listen to music idly while doing other tasks, but it can also be used to watch or listen to other kinds of videos. It is recommended though to provide a url that links to a video in a playlist, so that the videos that play will be more predictable. It is totally fine to enter an url link to a standalone video--however, it should be known that the bot relies on YouTube's auto play feature to navigate to the next video once the current one is done playing, so there would be less predictability of the videos that would play. 
