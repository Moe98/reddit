package org.sab.notification;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        FirebaseMessagingConnector a = null;
        try {
            a = FirebaseMessagingConnector.getInstance();
        } catch (GoogleCredentialsLoadingFailedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(a);
    }
}
