import java.util.*;
import java.io.*;
import java.net.*;

class Testing5{
    static final String ADDRESS="http://localhost:9999/Test1/Servlet1";

    public static void main(String[] args) throws Exception{
        Scanner in2=new Scanner(System.in);
        // System.out.println("Enter your nickname : ");
        ClientComm clientComm=ClientComm.getInstance();
        clientComm.registerClientComm(args[0], args[1], args[2]);

        URL url=new URL(ADDRESS);

        HttpURLConnection conn3=(HttpURLConnection)url.openConnection();
        conn3.setRequestMethod("POST");
        conn3.setDoInput(true);
        conn3.setDoOutput(true);
        DataOutputStream dos=new DataOutputStream(conn3.getOutputStream());
        clientComm.registerClient(dos);
        dos.flush();
        dos.close();
        InputStream in=conn3.getInputStream();
        int c;
        while((c=in.read()) != -1){
            System.out.print((char)c);
        }
        in.close();
        conn3.disconnect();

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
                        clientComm.receiveMessage(dos);

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
                }
            }
        });

        outputThread.start();
        inputThread.start();
    }

    static class Client{
        String nick;
        String password;

        Client(String nick, String password){
            this.nick=nick;
            this.password=password;
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

        private void registerClientComm(String senderNick, String password, String rcvrNick){
            rcvr=new Client(rcvrNick, null);
            sender=new Client(senderNick, password);
        }

        public void sendMessage(String message, DataOutputStream dos) throws Exception{
            // message+="\n";
            dos.writeUTF(sender+":SEND:"+rcvr+":"+message+":"+sender.password);
            dos.flush();
            dos.close();
        }

        public void registerClient(DataOutputStream dos) throws Exception{
            dos.writeUTF(sender+":SIGN_UP:::"+sender.password);
            dos.flush();
            dos.close();
        }

        public void receiveMessage(DataOutputStream dos) throws Exception{
            dos.writeUTF(sender+":RECEIVE:"+rcvr.nick+":"+sender.password+":");
            dos.flush();
            dos.close();
        }
    }
}