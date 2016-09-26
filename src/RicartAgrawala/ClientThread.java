package RicartAgrawala;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClientThread implements Runnable {

    private int myId;
    public static int N;
    private static File outFile;
    Hello serverReference;

    ClientThread(String[] args) {
        take_input(args);
    }

    private void take_input(String[] S) {
        this.myId = Integer.parseInt(S[1]);
        ClientThread.N = Integer.parseInt(S[3]);
        ClientThread.outFile = new File(S[5]);
    }

    @Override
    @SuppressWarnings("null")
    public void run() {
        try {
            getReferenceToOwnServer();
        } catch (RemoteException | NotBoundException | InterruptedException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        int counterValue = 0;
        while (counterValue <= 20) {
            try {
                sleep(generateRandomNumber());
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                counterValue = generateLockRequest();
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.printf("Counter Has Reached 100, exit the program\n");
    }

    /**
     * This method gets an object reference to its own server
     */
    private void getReferenceToOwnServer() throws RemoteException, NotBoundException, InterruptedException {
        Boolean b = false;
        while (b == false) {
            sleep(1000);
            try {
                Registry registry = LocateRegistry.getRegistry(null);
                String s = "myId" + this.myId;
                serverReference = (Hello) registry.lookup(s);
                b = true;
                System.out.printf("Successfull got reference to %d\n", this.myId);
            } catch (NotBoundException e) {
                System.out.printf("Server with id %d still not bound\n", this.myId);
            } catch (RemoteException e) {
                System.err.println("Server Exception: " + e.toString());
            }
        }
    }

    /**
     * This method sends a message to its own server telling it that it is
     * interested in the lock
     */
    private int generateLockRequest() throws FileNotFoundException, IOException, InterruptedException {
        sleep(10000);
        int[] ClockValue;
        ClockValue = serverReference.lockRequest();
        System.out.printf("Client with id %d has the lock\n", this.myId);
        int returnval = writeToFile(ClockValue);
        serverReference.unlockRequest();
        System.out.printf("Client with id %d released the lock\n", this.myId);
        return returnval;
    }

    /**
     * This function writes to the file and returns the current value of the
     * counter
     */
    @SuppressWarnings("null")
    private int writeToFile(int[] ClockValue) throws FileNotFoundException, IOException {
        System.out.printf("Currently Write to file by Client %d\n", this.myId);
        int counter;
        if (ClientThread.outFile.exists() == false) {
            counter = 0;
        } else {
            String last = null, line;
            try (BufferedReader reader = new BufferedReader(new FileReader(outFile))) {
                while ((line = reader.readLine()) != null) {
                    last = line;
                }
            }
            String[] split;
            split = last.split(":");
            counter = Integer.parseInt(split[0]);
        }
        counter++;
        String towrite;
        towrite = counter + ":" + this.myId + ":" + ClockValue.toString();
        System.out.printf("%s\n", towrite);
        BufferedWriter writer;
        writer = new BufferedWriter(new FileWriter(outFile, true));
        try (PrintWriter out = new PrintWriter(writer)) {
            out.println(towrite);
        }
        System.out.printf("Write to File Complete by Client %d\n", this.myId);
        return counter;
    }

    /**
     * generate a random number in the specified range
     *
     * @return
     */
    @SuppressWarnings("null")
    private long generateRandomNumber() {
        Random rand;
        rand = new Random();
        int randNum;
        randNum = rand.nextInt(((ClientThread.N) * 2 - 1) + 1) + 1;
        return randNum;
    }
}
