package com.belatrixsf.technicalexercise.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JobLoggerRewrite {
    private final boolean logToFile;
    private final boolean logToConsole;
    private final boolean logMessage;
    private final boolean logWarning;
    private final boolean logError;
    private final boolean logToDatabase;
    private final Map dbParams;

    private static final Logger logger = Logger.getLogger("MyLog");

    public JobLoggerRewrite(boolean logToFileParam, boolean logToConsoleParam, boolean logToDatabaseParam,
                     boolean logMessageParam, boolean logWarningParam, boolean logErrorParam, Map dbParamsMap) {
        logError = logErrorParam;
        logMessage = logMessageParam;
        logWarning = logWarningParam;
        logToDatabase = logToDatabaseParam;
        logToFile = logToFileParam;
        logToConsole = logToConsoleParam;
        dbParams = dbParamsMap;
    }

    public void LogMessage(String messageText, boolean message, boolean warning, boolean error) throws Exception {

        if (messageText == null){
            return;
        }
        messageText = messageText.trim();

        if (messageText.isEmpty()) {
            return;
        }

        if (!logToConsole && !logToFile && !logToDatabase) {
            throw new Exception("Invalid configuration");
        }
        if ((!logError && !logMessage && !logWarning) || (!message && !warning && !error)) {
            throw new Exception("Error or Warning or Message must be specified");
        }

        Properties connectionProps = new Properties();
        connectionProps.put("user", dbParams.get("userName"));
        connectionProps.put("password", dbParams.get("password"));

        Connection connection = DriverManager.getConnection("jdbc:" + dbParams.get("dbms") + "://" + dbParams.get("serverName")
                + ":" + dbParams.get("portNumber") + "/", connectionProps);

        int logTimes = 0;
        if (message && logMessage) {
            logTimes = 1;
        }

        if (error && logError) {
            logTimes = 2;
        }

        if (warning && logWarning) {
            logTimes = 3;
        }

        Statement statement = connection.createStatement();

        String l = null;
        File logFile = new File(dbParams.get("logFileFolder") + "/logFile.txt");

        if (!logFile.exists()) {
            logFile.createNewFile();
        }

        FileHandler fileHandler = new FileHandler(dbParams.get("logFileFolder") + "/logFile.txt");
        ConsoleHandler consoleHandler = new ConsoleHandler();

        if (error && logError) {
            l = l + "error " + DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
        }

        if (warning && logWarning) {
            l = l + "warning " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
        }

        if (message && logMessage) {
            l = l + "message " +DateFormat.getDateInstance(DateFormat.LONG).format(new Date()) + messageText;
        }

        if(logToFile) {
            logger.addHandler(fileHandler);
            logger.log(Level.INFO, messageText);
        }

        if(logToConsole) {
            logger.addHandler(consoleHandler);
            logger.log(Level.INFO, messageText);
        }

        if(logToDatabase) {
            statement.executeUpdate("insert into Log_Values('" + message + "', " + logTimes + ")");
        }
    }
}

