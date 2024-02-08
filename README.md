> Kindly excuse the commit messages, I thank you.

# JosVoiceTweet
An JosVoiceTweet built in Java, allows you to post a Tweet using the Google Speech-To-Text API and the Twitter4J Library.


## SETTING THINGS UP
##### Make sure you have the following.
1. Java 8+ installed on you Machine.
2. [Maven](https://maven.apache.org/install.html) Installed (to build dependencies)
3. [Twitter Developer account](https://developer.twitter.com/en/apps)
4. [Google Cloud Speech-To-Text](https://console.cloud.google.com/apis/library/speech.googleapis.com)

### LET'S START
- Clone/Download this project and Locate to the projects folder
- Run this command:
    ``` 
    mvn install dependency:copy-dependencies 
    ```
- Download and Setup your Google Cloud Credentials, Please follow this tutorial [here](https://cloud.google.com/video-intelligence/docs/common/auth)
- Fetch your Twitter Developer app Keys and Tokens: 
    - make sure your Twiter app has Read and Write permissions.
    - Insert your Keys and Tokens in the `public void InitConfig()` method, in the `JosVoiceTweet.java` file.
- All should be ready now, run your app ;-)
> I will be updating this ReadMe to give more clarification on how to set this project up. Should you enconter problems, you can get in touch with me on Twitter: [@JosiahThobejane](https://twitter.com/josiahthobejane) or email JosiahThobejane@gmail.com
