package optix.commands.finance;

import optix.commands.Command;
import optix.commons.Model;
import optix.commons.Storage;
import optix.exceptions.OptixException;
import optix.exceptions.OptixInvalidCommandException;
import optix.exceptions.OptixInvalidDateException;
import optix.ui.Ui;
import optix.util.OptixDateFormatter;

import java.util.logging.Level;

//@@author NicholasLiu97
public class ViewMonthlyCommand extends Command {
    private String details;
    private OptixDateFormatter formatter = new OptixDateFormatter();

    public ViewMonthlyCommand(String details) {
        this.details = details;
        initLogger();
    }

    @Override
    public String execute(Model model, Ui ui, Storage storage) {
        OPTIXLOGGER.log(Level.INFO, "executing command");
        StringBuilder message = new StringBuilder();
        int mth;
        int yr;
        try {
            String[] detailsArray = parseDetails(this.details);
            mth = formatter.getMonth(detailsArray[0].trim().toLowerCase());
            yr = formatter.getYear(detailsArray[1].trim());

            if (mth == 0 || yr == 0) {
                OPTIXLOGGER.log(Level.WARNING, "month is 0 or year is 0");
                throw new OptixInvalidDateException();
            }
            if (yr < storage.getToday().getYear()) {
                message.append(model.findMonthly(mth, yr, model.getShowsHistory()));
            } else if (yr > storage.getToday().getYear()) {
                message.append(model.findMonthly(mth, yr, model.getShows()));
            } else { // year is the current year or later
                if (mth < storage.getToday().getMonthValue()) {
                    message.append(model.findMonthly(mth, yr, model.getShowsHistory()));
                } else if (mth == storage.getToday().getMonthValue()) {
                    message.append(model.findMonthly(mth, yr, model.getShowsHistory(), model.getShows()));
                } else {
                    message.append(model.findMonthly(mth, yr, model.getShows()));
                }
            }
        } catch (OptixException e) {
            message.append(e.getMessage());
            ui.setMessage(message.toString());
            return "";
        }
        ui.setMessage(message.toString());
        return "finance";
    }

    @Override
    public String[] parseDetails(String details) throws OptixInvalidCommandException {
        String[] detailsArray = details.trim().split(" ");
        if (detailsArray.length != 2) {
            OPTIXLOGGER.log(Level.WARNING, "full command has too many spaces. Expected detailsArray length: 2");
            throw new OptixInvalidCommandException();
        }
        return detailsArray;
    }
}