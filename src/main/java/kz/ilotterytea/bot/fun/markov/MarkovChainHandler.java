package kz.ilotterytea.bot.fun.markov;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Markov chain handler.
 * @author ilotterytea
 * @since 1.2
 */
public class MarkovChainHandler {
    private final ArrayList<ChatChain> chainRecords;
    private final File file;

    public MarkovChainHandler(File file) {
        this.chainRecords = new ArrayList<>();
        this.file = file;

        processFile();
    }

    public String generateText(String text) {
        ArrayList<String> s = new ArrayList<>(Arrays.asList(text.trim().split(" ")));
        ArrayList<Chain> chains = MarkovUtils.tokenizeText(text);
        StringBuilder message = new StringBuilder();

        for (String w : s) {
            Chain firstChain = chainRecords
                    .stream()
                    .filter(c -> Objects.equals(c.getFromWord(), w))
                    .findFirst().orElse(null);
            if (firstChain == null || Objects.equals(firstChain.getToWord(), "\\x03") || Objects.equals(firstChain.getToWord(), "")) {
                continue;
            }

            Chain nextChain = null;

            while (true) {
                Chain chain;

                if (nextChain == null) {
                    message.append(firstChain.getFromWord()).append(" ");

                    chain = chainRecords
                            .stream()
                            .filter(c -> Objects.equals(c.getFromWord(), firstChain.getToWord()))
                            .findFirst().orElse(null);

                    if (chain == null) {
                        break;
                    }

                    nextChain = chain;
                } else {
                    message.append(nextChain.getFromWord()).append(" ");

                    Chain finalNextChain = nextChain;
                    chain = chainRecords
                            .stream()
                            .filter(c -> Objects.equals(c.getFromWord(), finalNextChain.getToWord()))
                            .findFirst().orElse(null);

                    if (chain == null) {
                        break;
                    }

                    nextChain = chain;
                }
            }
        }

        return message.toString();
    }

    public void scanText(String text, String msgId, String channelId, String userId) {
        ArrayList<Chain> chains = MarkovUtils.tokenizeText(text);

        for (Chain chain : chains) {
            ChatChain record = chainRecords
                    .stream()
                    .filter(c -> Objects.equals(c.getFromWord(), chain.getFromWord()))
                    .findFirst().orElse(null);

            if (record != null) {
                record.setToWord(chain.getToWord());
                record.setToWordAuthor(new ChainSender(msgId, channelId, userId));
            } else {
                chainRecords.add(new ChatChain(
                        chain.getFromWord(),
                        chain.getToWord(),
                        new ChainSender(msgId, channelId, userId),
                        new ChainSender(msgId, channelId, userId)
                ));
            }
        }
    }

    private void processFile() {
        if (file.isDirectory()) return;
        if (!file.exists()) return;

        try (Reader reader = new FileReader(file)){
            ArrayList<ChatChain> records = new Gson().fromJson(reader, new TypeToken<ArrayList<ChatChain>>(){}.getType());

            if (records != null) {
                chainRecords.addAll(records);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        try (Writer writer = new FileWriter(file)){
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(chainRecords));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<ChatChain> getRecords() { return chainRecords; }
}
