package channel;

import java.io.IOException;

// reference http://www.careerbless.com/samplecodes/java/beginners/socket/SocketBasic1.php

// test test test 

public class Client {
	public static void main(String[] args) throws IOException, InterruptedException {
        String serverAddress1 = "0.0.0.0";
        String serverPort1 = "9090";
        String serverAddress2 = "0.0.0.0";
        String serverPort2 = "9090";
        String serverAddress3 = "0.0.0.0";
        String serverPort3 = "9090";
        int max = 10;
        
        DelayedChannel channel1 = new DelayedChannel(serverAddress1, serverPort1, max);
        DelayedChannel channel2 = new DelayedChannel(serverAddress2, serverPort2, max);
        DelayedChannel channel3 = new DelayedChannel(serverAddress3, serverPort3, max);
        
        channel1.start();
        channel2.start();
        channel3.start();
        
        channel1.putContents("test1");
        channel2.putContents("test2");
        channel3.putContents("test3");
        
        channel1.join();
        channel2.join();
        channel3.join();
        System.exit(0);
    }
}
