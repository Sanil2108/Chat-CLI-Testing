import java.util.*;
import java.io.*;
import java.net.*;

class Testing4{
    static final String ADDRESS="http://localhost:9999/Test1/Servlet1";

    static boolean checking=false;
    static String[] checkingArgs;

    public static void main(String[] args) throws Exception{
        Scanner in2=new Scanner(System.in);
        // System.out.println("Enter your nickname : ");
        ClientComm clientComm=ClientComm.getInstance();
        if(args.length==2){
        	clientComm.registerClientComm(args[0], args[1]);
    	}

    	if(args.length>2){
    		checking=true;
    		checkingArgs=args;
    	}

        URL url=new URL(ADDRESS);

        //Outputs to the server
        Thread outputThread=new Thread(new Runnable(){
            @Override
            public void run(){
                while(true){
                    try{

                        HttpURLConnection conn=(HttpURLConnection)url.openConnection();

                        conn.setRequestMethod("POST");
                        conn.setDoOutput(true);
                        conn.setDoInput(true);

                        //Writing to the servlet
                        DataOutputStream dos=new DataOutputStream(conn.getOutputStream());

                        String msg=in2.nextLine();
                        clientComm.sendMessage(msg, dos);

                        dos.flush();
                        dos.close();
                        
                        InputStream in=conn.getInputStream();
                        int c;
                        while((c=in.read())!=-1){
                            System.out.print((char)c);
                        }
                        in.close();

                        conn.disconnect();

                    }catch(Exception e){
                        System.out.println("Exception in outputThread : ");
                        e.printStackTrace();
                    }
                }
            }
        });

        //Inputs from the server
        Thread inputThread=new Thread(new Runnable(){
            @Override
            public void run(){
                long milliseconds=1000;
                while(true){
                    try{
                        
                        Thread.sleep(milliseconds);

                        HttpURLConnection conn2=(HttpURLConnection)url.openConnection();
                        conn2.setRequestMethod("POST");
                        conn2.setDoOutput(true);
                        conn2.setDoInput(true);

                        //Reading from the Servlet
                        DataOutputStream dos=new DataOutputStream(conn2.getOutputStream());
                        if(checking){
                        	clientComm.checkMessages(dos, checkingArgs);
                        }else{
                        	clientComm.receiveMessage(dos);
                    	}

                        InputStream in=conn2.getInputStream();
                        int c;
                        while((c=in.read())!=-1){
                            System.out.print((char)c);
                        }

                        dos.flush();
                        dos.close();
                        in.close();

                        conn2.disconnect();

                    }catch(Exception e){
                        System.out.println("Exception in inputThread");
                        e.printStackTrace();
                    }
                    break;
                }
            }
        });

        outputThread.start();
        inputThread.start();
    }

    static class Client{
        String nick;

        Client(String nick){
            this.nick=nick;
        }
        
        @Override
        public String toString(){
            return nick;
        }
    }

    //Singleton
    static class ClientComm{
        private static Client rcvr;
        private static Client sender;

        static ClientComm clientComm=new ClientComm();

        public static ClientComm getInstance(){
            return clientComm;
        }

        private ClientComm(){

        }

        private void registerClientComm(String senderNick, String rcvrNick){
            rcvr=new Client(rcvrNick);
            sender=new Client(senderNick);
        }

        public void sendMessage(String message, DataOutputStream dos) throws Exception{
            dos.writeUTF(sender+":SEND:"+rcvr+":"+message);
            dos.flush();
            dos.close();
        }

        public void receiveMessage(DataOutputStream dos) throws Exception{
            dos.writeUTF(sender+":RECEIVE:"+rcvr);
            dos.flush();
            dos.close();
        }

        public void checkMessages(DataOutputStream dos, String[] senders) throws Exception{
        	String allReceivers="";
        	//Skipping the first argument as it is the name of the client logged in
        	for(int i=1;i<senders.length;i++){
        		allReceivers+=senders[i]+";";
        	}
        	System.out.println(senders[0]+":CHECK::"+allReceivers);
        	dos.writeUTF(senders[0]+":CHECK::"+allReceivers);
        }
    }
}