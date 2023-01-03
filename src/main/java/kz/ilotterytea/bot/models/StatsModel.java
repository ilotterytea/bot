package kz.ilotterytea.bot.models;

/**
 * Statistics model.
 * @author ilotterytea
 * @since 1.0
 */
public class StatsModel {
    /** The number of chat lines since joining the chat. */
    private int chatLines;
    /** The number of "successful tests". */
    private int successfulTests;
    /** The number of executed commands. */
    private int executedCommandsCount;

    public StatsModel(
            int chatLines,
            int successfulTests,
            int executedCommandsCount
    ) {
        this.chatLines = chatLines;
        this.successfulTests = successfulTests;
        this.executedCommandsCount = executedCommandsCount;
    }

    public int getChatLines() { return chatLines; }
    public void setChatLines(int chatLines) { this.chatLines = chatLines; }
    public int getSuccessfulTests() { return successfulTests; }
    public void setSuccessfulTests(int successfulTests) { this.successfulTests = successfulTests; }
    public int getExecutedCommandsCount() { return executedCommandsCount; }
    public void setExecutedCommandsCount(int executedCommandsCount) { this.executedCommandsCount = executedCommandsCount; }
}
