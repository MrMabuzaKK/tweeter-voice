package JosVoiceTweet;

import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.protobuf.ByteString;
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.*;
import java.io.IOException;
import java.util.Scanner;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class JosVoiceTweet {

    Twitter twitter;
    public static void main(String[] args) {
                
        JosVoiceTweet JosVoiceTweet = new JosVoiceTweet();
        JosVoiceTweet.InitConfig();

        try {
            JosVoiceTweet.getSpeech();
        } catch (IOException e){            
            e.printStackTrace();
        }
    }
    
    //configure the auth for Twitter APIs access
    public void InitConfig()
    { 
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
        .setOAuthConsumerKey("5U2b3ex6tAU02bzEUsBf04A0P")
        .setOAuthConsumerSecret("lEYWpplstulnFOy4U4bROkEnZ0uCvXM96UqV4Zfa8i0AsHC2Gj")
        .setOAuthAccessToken("723164798834368512-E8oThYkfLE7iH9rtzSIG3jl0JCbs51W")
        .setOAuthAccessTokenSecret("1fhxGYdvOf4XUlTDjhv2LEZY8arWLcf6s99dSowwqX94H");
        TwitterFactory twitterFactory = new TwitterFactory(configurationBuilder.build());
        twitter = twitterFactory.getInstance();
    }

    public void getSpeech() throws IOException {
        //target dataline
        TargetDataLine line = null;
        AudioInputStream audio = null;

        try {
            int sampleRate = 16000;
            AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

            //check if mic is supported
            if(!AudioSystem.isLineSupported(info)){
                System.out.println("Mic is not supported on your pc");
                System.exit(0);
            }

            //get targetdata line
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
            line.start();

            //audio inputstream
            audio = new AudioInputStream(line);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try (SpeechClient client = SpeechClient.create()) {
            ResponseObserver<StreamingRecognizeResponse> responseObserver = new ResponseObserver<StreamingRecognizeResponse>() {
                public void onStart(StreamController controller) {
                    System.out.println("Start Saying your Tweet [LISTENING]\n");                     
                }
    
                public void onResponse(StreamingRecognizeResponse response) {                                        
                    //get the words only
                    String wordsToTweet = null;
                    boolean isFinal = false;
                    if (response.getResultsCount() > 0) {
                        final StreamingRecognitionResult result = response.getResults(0);
                        isFinal = result.getIsFinal();
                        if (result.getAlternativesCount() > 0) {
                            final SpeechRecognitionAlternative alternative = result.getAlternatives(0);
                            wordsToTweet = alternative.getTranscript(); 
                                   
                            System.out.println("YOU JUST SAID: " + wordsToTweet
                            + "\n\nDo you want to send this Tweet? Type 1 for YES, 2 for NO.");

                            Scanner scanner = new Scanner(System.in);
                            int answer = scanner.nextInt();
                            scanner.close();
                            //post our thing to Twitter
                            switch(answer){
                                case 1:
                                    postToTwitter(wordsToTweet);
                                break;
                                case 2:
                                    System.out.println("You chose not to send the Tweet");
                                    break;                                
                                default:
                                    System.out.println("Incorrect selection, Try again");
                                    //hopefuly this works, lol!
                                    onResponse(response);
                                    break;
                            }
                        }
                    }           
                    
                    onComplete();
                }
    
                public void onComplete() {
                    System.out.println("Tweet Sent");
                    System.exit(0);
                }
    
                public void onError(Throwable t) {
                System.out.println("ERROR OCURRED: " + t);
                }
            };
      
            ClientStream<StreamingRecognizeRequest> clientStream = client.streamingRecognizeCallable().splitCall(responseObserver);
      
            RecognitionConfig recConfig = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setLanguageCode("en-ZA")
                    .setSampleRateHertz(16000)
                    .build();
            StreamingRecognitionConfig config = StreamingRecognitionConfig.newBuilder()
                    .setConfig(recConfig).build();
      
            StreamingRecognizeRequest request = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(config)
                    .build(); // The first request in a streaming call has to be a config
      
            clientStream.send(request);
      
            while (true) {
              byte[] data = new byte[10];
              try {
                audio.read(data);
              } catch (IOException e) {
                System.out.println(e);
              }
              request = StreamingRecognizeRequest.newBuilder()
                      .setAudioContent(ByteString.copyFrom(data))
                      .build();
              clientStream.send(request);
            }
          } catch (Exception e) {
            System.out.println(e);
          }
    }

    public void postToTwitter(String tweet){
        try {
            twitter.updateStatus("[#JosSmartTweet]\n\n" + tweet + "\n\n\n- JosSpeechTweet CLI JosVoiceTweet");
            System.out.println("Tweet has been seent");
        } catch (TwitterException ex) {
           System.out.println(ex.getMessage());
        }

    }
}
