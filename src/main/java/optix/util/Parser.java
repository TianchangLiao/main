package optix.util;

import optix.commands.ByeCommand;
import optix.commands.Command;
import optix.commands.TabCommand;
import optix.commands.finance.ViewMonthlyCommand;
import optix.commands.finance.ViewProfitCommand;
import optix.commands.parser.AddAliasCommand;
import optix.commands.parser.ListAliasCommand;
import optix.commands.parser.RemoveAliasCommand;
import optix.commands.parser.ResetAliasCommand;
import optix.commands.seats.ReassignSeatCommand;
import optix.commands.seats.RefundSeatCommand;
import optix.commands.seats.RemoveSeatCommand;
import optix.commands.seats.SellSeatCommand;
import optix.commands.seats.ViewSeatsCommand;
import optix.commands.shows.AddCommand;
import optix.commands.shows.DeleteCommand;
import optix.commands.shows.EditCommand;
import optix.commands.shows.ListCommand;
import optix.commands.shows.ListDateCommand;
import optix.commands.shows.ListShowCommand;
import optix.commands.shows.RescheduleCommand;
import optix.exceptions.OptixException;
import optix.exceptions.OptixInvalidCommandException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Parse input arguments and create a new Command Object.
 */
public class Parser {
    public static HashMap<String, String> commandAliasMap = new HashMap<>();
    private File preferenceFilePath; // the directory where the file is stored
    private File preferenceFile; // the path to the file itself
    // array of all possible command values
    private static String[] commandList = {"bye", "list", "help", "edit", "sell", "view",
        "reschedule", "add", "delete", "reassign-seat", "show", "archive", "finance",
        "view-profit", "view-monthly", "add-alias", "remove-alias", "reset-alias", "list-alias",
        "refund-seat", "remove-seat"};
    private static final Logger OPTIXLOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Set the path to directory containing the save file for preferences.
     * Set the path to the save file for preferences.
     *
     * @param filePath path to directory containing the save file for preferences.
     */
    public Parser(File filePath) {
        initLogger();
        OPTIXLOGGER.log(Level.INFO, "Parser initialization begin");
        this.preferenceFile = new File(filePath + "\\ParserPreferences.txt");
        this.preferenceFilePath = filePath;
        // load preferences from file
        if (commandAliasMap.isEmpty()) {
            try {
                loadPreferences();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                OPTIXLOGGER.log(Level.WARNING, "Error loading preferences.");
            }
        }
        OPTIXLOGGER.log(Level.INFO, "Parser initialization complete.");
    }

    /**
     * Parse input argument and create a new Command Object based on the first input word.
     *
     * @param fullCommand The entire input argument.
     * @return Command Object based on the first input word.
     * @throws OptixException if the Command word is not recognised by Optix.
     */
    public Command parse(String fullCommand) throws OptixException {
        // add exception for null pointer exception. e.g. reschedule
        OPTIXLOGGER.log(Level.INFO, "Parsing string: " + fullCommand);
        String[] splitStr = fullCommand.trim().split(" ", 2);
        String aliasName = splitStr[0];
        String commandName = commandAliasMap.getOrDefault(aliasName, aliasName);
        commandName = commandName.toLowerCase().trim();
        if (splitStr.length == 1) {
            switch (commandName) {
            case "bye":
                return new ByeCommand();
            case "list":
                return new ListCommand();
            case "reset-alias":
                return new ResetAliasCommand(this.preferenceFilePath);
            case "list-alias":
                return new ListAliasCommand();
            case "help":
            case "archive":
            case "finance":
                return new TabCommand(commandName);
            default:
                OPTIXLOGGER.log(Level.WARNING, "Error with command: " + commandName);
                throw new OptixInvalidCommandException();
            }
        } else if (splitStr.length == 2) {

            switch (commandName) {
            case "edit":
                return new EditCommand(splitStr[1]);
            case "sell":
                return new SellSeatCommand(splitStr[1]);
            case "view":
                return new ViewSeatsCommand(splitStr[1]);
            case "reschedule":
                return new RescheduleCommand(splitStr[1]);
            case "list":
                return parseList(splitStr[1]);
            case "bye":
                return new ByeCommand();
            case "add": // add poto|5/10/2020|20
                return new AddCommand(splitStr[1]);
            case "delete": // e.g. delete SHOW_NAME DATE_1|DATE_2|etc
                return new DeleteCommand(splitStr[1]);
            case "view-profit": //e.g. view-profit lion king|5/5/2020
                return new ViewProfitCommand(splitStr[1]);
            case "view-monthly": //e.g. view-monthly May 2020
                return new ViewMonthlyCommand(splitStr[1]);
            case "add-alias":
                return new AddAliasCommand(splitStr[1], this.preferenceFilePath);
            case "remove-alias":
                return new RemoveAliasCommand(splitStr[1], commandAliasMap);
            case "reassign-seat":
                return new ReassignSeatCommand(splitStr[1]);
            case "remove-seat":
                return new RemoveSeatCommand(splitStr[1]);
            case "refund-seat":
                return new RefundSeatCommand(splitStr[1]);
            default:
                OPTIXLOGGER.log(Level.WARNING, "Error with command: " + commandName);
                throw new OptixInvalidCommandException();
            }
        } else {
            OPTIXLOGGER.log(Level.WARNING, "Error with command: " + fullCommand);
            throw new OptixInvalidCommandException();
        }
    }

    //@@ OungKennedy
    /**
     * Adds a new alias-command pair to commandAliasMap.
     *
     * @param newAlias new alias to add
     * @param command  existing command to be paired to
     * @throws OptixException thrown when the alias-command pair is invalid (refer to below)
     *                        the alias must not be the name of a command.
     *                        the alias must not already be in use. use remove-alias to remove a pair to redirect existing aliases.
     *                        the command to be paired to must exist (refer to commandList for list of existing commands).
     *                        the pipe symbol is a special character- it cannot be used.
     */
    public void addAlias(String newAlias, String command) throws OptixException {
        OPTIXLOGGER.log(Level.INFO, "adding new alias");
        if (!newAlias.contains("|") // pipe symbol not in alias
                && Arrays.asList(commandList).contains(command) // command exists
                && !commandAliasMap.containsKey(newAlias) // new alias is not already in use
                && !Arrays.asList(commandList).contains(newAlias)) { // new alias is not the name of a command
            commandAliasMap.put(newAlias, command);
            OPTIXLOGGER.log(Level.INFO, "add alias successful");
        } else {
            OPTIXLOGGER.log(Level.INFO, "error adding alias.");
            throw new OptixException("Invalid alias-command input.\n Alias cannot be a command keyword.\n"
                    + "Alias cannot already be in use.");
        }
    }

    //@@ OungKennedy
    private void loadPreferences() throws IOException {
        OPTIXLOGGER.log(Level.INFO, "loading preferences");
        File filePath = this.preferenceFile;
        // if file does not exist, create a new file and write the default aliases
        if (filePath.createNewFile()) {
            OPTIXLOGGER.log(Level.INFO, "preference file not found. Creating new file.");
            resetPreferences();
            savePreferences();
        } else { // if file exists then load the preferences within
            OPTIXLOGGER.log(Level.INFO, "preference file found.");
            FileReader fr = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fr);
            String aliasPreference;
            while ((aliasPreference = br.readLine()) != null) {
                if (aliasPreference.length() == 0) { // handle empty line
                    continue;
                }
                String[] aliasDetails = aliasPreference.split("\\|");

                String alias = aliasDetails[0];
                String command = aliasDetails[1];
                try {
                    this.addAlias(alias, command);
                } catch (OptixException e) {
                    System.out.println(e.getMessage());
                }
            }
            br.close();
            fr.close();
        }
        OPTIXLOGGER.log(Level.INFO, "load preferences completed");
    }

    //@@ OungKennedy
    /**
     * Writes the contents of commandAliasMap to the file in preferenceFilePath.
     */
    public void savePreferences() throws IOException {
        OPTIXLOGGER.log(Level.INFO, "saving preferences");
        FileWriter writer = new FileWriter(this.preferenceFile, false);
        for (Map.Entry<String, String> entry : commandAliasMap.entrySet()) {
            String saveString = entry.getKey() + "|" + entry.getValue() + '\n'; // no need to escape. why?
            writer.write(saveString);
        }
        writer.close();
        OPTIXLOGGER.log(Level.INFO, "preferences saved");
    }

    //@@ OungKennedy
    /**
     * Method to reset preferences to default values.op
     */
    public static void resetPreferences() {
        OPTIXLOGGER.log(Level.INFO, "Saving preferences");
        commandAliasMap.clear();
        commandAliasMap.put("re", "reassign-seat");
        commandAliasMap.put("arc", "archive");
        commandAliasMap.put("shw", "show");
        commandAliasMap.put("fin", "finance");
        commandAliasMap.put("b", "bye");
        commandAliasMap.put("l", "list");
        commandAliasMap.put("h", "help");
        commandAliasMap.put("e", "edit");
        commandAliasMap.put("s", "sell");
        commandAliasMap.put("v", "view");
        commandAliasMap.put("rd", "reschedule");
        commandAliasMap.put("a", "add");
        commandAliasMap.put("d", "delete");
        commandAliasMap.put("vp", "view-profit");
        commandAliasMap.put("vm", "view-monthly");
        commandAliasMap.put("a-a", "add-alias");
        commandAliasMap.put("rm-a", "remove-alias");
        commandAliasMap.put("rst-a", "reset-alias");
        commandAliasMap.put("rf-s", "refund-seat");
        commandAliasMap.put("rm-s", "remove-seat");
        OPTIXLOGGER.log(Level.INFO, "preferences saved");
    }

    /**
     * Parse the remaining user input to its respective parameters for ListDateCommand or ListShowCommand.
     *
     * @param details The details to create a new ListDateCommand or ListShowCommand Object.
     * @return new ListDateCommand or ListShowCommand Object.
     */
    private static Command parseList(String details) {
        String[] splitStr = details.split(" ");

        if (splitStr.length == 2) {
            try {
                Integer.parseInt(splitStr[1]);
                return new ListDateCommand(details);
            } catch (NumberFormatException e) {
                return new ListShowCommand(details);
            }
        }

        return new ListShowCommand(details);
    }

    private void initLogger() {
        LogManager.getLogManager().reset();
        OPTIXLOGGER.setLevel(Level.ALL);
        try {
            // do not append here to avoid
            FileHandler fh = new FileHandler("OptixLogger.log",1024 * 1024,1, false);
            OPTIXLOGGER.addHandler(fh);
        } catch (IOException e) {
            OPTIXLOGGER.log(Level.SEVERE, "File logger not working", e);
        }
        OPTIXLOGGER.log(Level.FINEST, "Logging in " + this.getClass().getName());
    }

}
