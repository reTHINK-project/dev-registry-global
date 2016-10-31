package eu.rethink.globalregistry.certification.future;

import net.tomp2p.futures.BaseFutureAdapter;
import net.tomp2p.futures.FutureDirect;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FutureDirectListener extends BaseFutureAdapter<FutureDirect> {

    private final CountDownLatch latch;
    private String reply;

    public static final int NETWORK_OPERATION = 60000;

    public FutureDirectListener() {
        this.latch = new CountDownLatch(1);
        this.reply = null;
    }

    public String awaitReply() {

        try {
            latch.await(NETWORK_OPERATION * 3, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return "ERROR";
        }

        System.out.println("[FUTURE-DIRECT-LISTENER][REPLY] Result: " + reply);

        if(reply == null) {
            return "ERROR";
        }

        return reply;

    }

    @Override
    public void operationComplete(FutureDirect future) throws Exception {

        if(future.isSuccess()) {
            this.reply = (String)future.object();
        }

        latch.countDown();

    }
}
