package zerox06.dslp.network;

import net.minecraft.util.logging.UncaughtExceptionLogger;
import zerox06.dslp.DSLPMod;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class BetterLanPinger extends Thread {
    public static final String PING_ADDRESS = "224.0.2.60";
    public static final int PING_PORT = 4445;
    private static final long PING_INTERVAL_MS = 1500L;
    private final Supplier<String> MOTDSupplier;
    private final DatagramSocket socket;
    private boolean running = true;
    private final String serverPort;

    public BetterLanPinger(Supplier<String> MOTDSupplier, String serverPort) throws IOException {
        super("Lan Pinger Thread");
        this.MOTDSupplier = MOTDSupplier;
        this.serverPort = serverPort;
        setDaemon(true);
        setUncaughtExceptionHandler(new UncaughtExceptionLogger(DSLPMod.LOGGER));
        this.socket = new DatagramSocket();
    }

    public void run() {
        while(!this.isInterrupted() && this.running) {
            try {
                byte[] announcement = createAnnouncement();
                InetAddress inetAddress = InetAddress.getByName(PING_ADDRESS);
                DatagramPacket packet = new DatagramPacket(announcement, announcement.length, inetAddress, PING_PORT);
                socket.send(packet);
            } catch (IOException e) {
                DSLPMod.LOGGER.warn("Exception in Lan Pinger Thread: {}", e.getMessage());
                break;
            }

            try {
                sleep(PING_INTERVAL_MS);
            }
            catch (InterruptedException ignored) {}
        }

    }

    public void interrupt() {
        super.interrupt();
        this.running = false;
    }

    public byte[] createAnnouncement() {
        return ("[MOTD]" + MOTDSupplier.get() + "[/MOTD][AD]" + serverPort + "[/AD]").getBytes(StandardCharsets.UTF_8);
    }
}
