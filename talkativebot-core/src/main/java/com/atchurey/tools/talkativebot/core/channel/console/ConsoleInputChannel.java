package com.atchurey.tools.talkativebot.core.channel.console;

import com.atchurey.tools.talkativebot.core.channel.ConversationAddress;
import com.atchurey.tools.talkativebot.core.channel.IncomingMessage;
import com.atchurey.tools.talkativebot.core.channel.InputChannel;
import com.atchurey.tools.talkativebot.core.channel.InputMessageHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConsoleInputChannel implements InputChannel {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleInputChannel.class);

    private final ConversationAddress address;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean running;

    public ConsoleInputChannel(ConversationAddress address) {
        this.address = address;
    }

    @Override
    public String name() {
        return "console";
    }

    @Override
    public void start(InputMessageHandler handler) {
        running = true;

        executorService.submit(() -> {
            logger.info("Console input channel started for {}", address.persistenceKey());

            Scanner scanner = new Scanner(System.in);

            while (running) {
                try {
                    if (!scanner.hasNextLine()) {
                        logger.debug("Console input stream has no next line");
                        continue;
                    }

                    String input = scanner.nextLine();
                    logger.debug("Console input received: {}", input);


                    handler.handle(new IncomingMessage(address, input))
                            .whenComplete((ignored, exception) -> {
                                if (exception != null) {
                                    logger.error("Could not handle console input: {}", input, exception);
                                }
                            });
                } catch (Exception exception) {
                    if (running) {
                        logger.error("Console input channel failed", exception);
                    }
                }
            }
            logger.info("Console input channel stopped for {}", address.persistenceKey());
        });
    }

    @Override
    public void stop() {
        running = false;
        executorService.shutdownNow();
    }
}
