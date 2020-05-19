# GitHub Tutorial

This tutorial assumes:
- You have a [github](https://github.com/) account 
- (recommended) You have installed and signed in to [github desktop](https://desktop.github.com/)

## 1. Fork the repository
1. Head to [the project repository](https://github.com/dkantereivin/-P.P.-Java-Bot) (you may already be here)
2. Click fork in the top right
3. After a waiting screen you will be redirected to your own repository, forked from the original.

__What does fork mean?__
A fork is a second version of the code, it will contain all the code within the main repository at time of forking, it will not update as other people update the main repository so you will have to merge the project frequently.

__How do I view the repositories I have forked?__
If you open [github](https://github.com/) your project repositories will appear on the left, your repos will have a book next to them, forked repos will have a Y shape next to them.

## 2. Download the code
In github desktop:
1. Click add in top left, select Clone repository
2. Select your forked version of -P.P.-Java-Bot
3. Choose a local path (the place it will be downloaded to on your computer)
4. Hit clone
5. After the project has been downloaded you will then be able to view and edit the code within your favourite IDE

__Do I have to use github desktop?__
No, but it is easier than learning the CLI and faster than uploading directly to github.

## 3. Editing the code
__In eclipse:__
1. Launch Eclipse
2. As your workspace, select the directory/folder where you downloaded the project
3. On the left, in the Package Explorer, right click and select import
4. Select General > Existing Projects into Workspace
5. The root directory is -P.P.-Java-Bot which should already be located within your workspace
6. Click finish to add the project to your package explorer

If you use a different IDE and know how to import a project, feel free to use this guide to download and edit the README.md file to include your IDE.

__Where should I add my code?__ src/main/java/com.programmerspalace is the main project package, all files should be within this package, feel free to make new sub-packages for different commands or groups of classes

__What is App.java?__ src/main/java/com.programmerspalace.App.java is the main class, this is the class to launch if you are testing the bot

__Where do I put my discord token?__ -P.P.-Java-Bot/config.conf is where all of the bot settings are stored. You will need to create a config.conf file (inside the -P.P.-Java-Bot directory/folder) that will store your configuration settings, currently the only two are the bot token, and the prefix (i like to use ! as a prefix) the file should look like:
	<br>\<discord token\>
    <br>!
    
__Will others be able to see the token?__ No, he github file has been set up to automatically ignore all .conf files so your discord token isn't published.

## 4. Running the code
__How do I check my progress?__ To run the code you must create your own discord bot client and discord server

__How do I set up my own discord bot client?__ [How to set up your own bot](https://discordpy.readthedocs.io/en/latest/discord.html)

__How do I know if it has been set up correctly?__ First launch the project from App.java, then try using the !ping command to check you have everything working

__Why is my bot still appearing online?__ Once you stop your code from running it will take a few minutes for discord to update the bots status to offline, your bot should no longer be running and you can test this with !ping

## 5. Commit your code to github
In github desktop:
1. Select your project in the top left
2. A list of files edited will appear on the left, you can select these files to view how the file has changed.
3. In the bottom left give your commit a name (description is optional, but recommended)
4. Select Commit to master
5. Push your changes to master

__What is the difference between commit and push?__ Commit saves those changes to the local git repository on your computer, it allows you to name and describe the changes you have made. Push uploads those changes to github where they can be viewed online. With one push you can push many commits, this may help if you are making multiple changes.

## 6. Merge your code with the main repository
In your forked repository:
1. Head to pull requests
2. Click new pull request
3. Give your request a name and comment
4. Click Create pull request
5. Your code will be reviewed then added to the main repository

__What if multiple people are working on the same code?__ This is why we are using git, if they are working on different parts of the code there will not be a problem. If two people change the same code, it may need to be decided which code is being used.
