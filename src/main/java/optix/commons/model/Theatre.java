package optix.commons.model;

import java.util.ArrayList;

public class Theatre {
    //@SuppressWarnings("checkstyle:membername")
    private static final String SPACES = "  "; // CHECKSTYLE IGNORE THIS LINE
    private static final String STAGE = "                |STAGE|           \n"; // CHECKSTYLE IGNORE THIS LINE
    private static final String MESSAGE_TICKET_COST = "The total cost of the tickets are $%1$.2f\n";

    private Seat[][] seats = new Seat[6][10];
    private int tierOneSeats;
    private int tierTwoSeats;
    private int tierThreeSeats;
    private double seatBasePrice;

    private Show show;

    /**
     * instantiates Theatre Object. Used when loading save file data.
     *
     * @param showName      name of show
     * @param revenue       expected revenue, calculated from seat purchases - cost
     * @param seatBasePrice base price of seats
     */
    public Theatre(String showName, double revenue, double seatBasePrice) {
        show = new Show(showName, revenue);
        this.seatBasePrice = seatBasePrice;
        initializeLayout();
    }

    /**
     * Instantiates Theatre Object. Used when there is no revenue yet (fresh instance).
     *
     * @param showName      name of show
     * @param seatBasePrice base price of seats.
     */
    public Theatre(String showName, double seatBasePrice) {
        show = new Show(showName, 0);
        this.seatBasePrice = seatBasePrice;
        initializeLayout();
    }

    public Theatre(Show show) {
        this.show = show;
    }

    // can have multiple layouts to be added for future extensions.

    private void initializeLayout() {
        tierOneSeats = 0;
        tierTwoSeats = 0;
        tierThreeSeats = 0;
        for (int i = 0; i < seats.length; i++) {
            for (int j = 0; j < seats[i].length; j++) {
                switch (i) {
                case 0:
                case 1:
                    seats[i][j] = new Seat("3");
                    tierThreeSeats++;
                    break;
                case 2:
                case 3:
                    seats[i][j] = new Seat("2");
                    tierTwoSeats++;
                    break;
                case 4:
                case 5:
                    seats[i][j] = new Seat("1");
                    tierOneSeats++;
                    break;
                default:
                    assert i > seats.length;
                    break;
                }
            }
        }
    }

    public void setShowName(String showName) {
        show.setShowName(showName);
    }

    public String getShowName() {
        return show.getShowName();
    }

    public double getProfit() {
        return show.getProfit();
    }

    public Seat[][] getSeats() {
        return seats;
    }

    public String getTierOneSeats() {
        return Integer.toString(tierOneSeats);
    }

    public String getTierTwoSeats() {
        return Integer.toString(tierTwoSeats);
    }

    public String getTierThreeSeats() {
        return Integer.toString(tierThreeSeats);
    }

    public double getSeatBasePrice() {
        return seatBasePrice;
    }

    /**
     * function to set the status of a seat (change it to booked when a seat is bought).
     *
     * @param row desired seat row
     * @param col desired seat column
     */
    public void setSeat(int row, int col) {
        seats[row][col].setSold(true);
        switch (seats[row][col].getSeatTier()) {
        case "1":
            tierOneSeats--;
            break;
        case "2":
            tierTwoSeats--;
            break;
        case "3":
            tierThreeSeats--;
            break;
        default:
            System.out.println("Should have a Seat Tier!");
        }
    }

    /**
     * function to reset the status of a seat (change it to available when a seat is removed).
     *
     * @param row desired seat row
     * @param col desired seat column
     */
    public void resetSeat(int row, int col) {
        seats[row][col].setSold(false);
        switch (seats[row][col].getSeatTier()) {
        case "1":
            tierOneSeats++;
            break;
        case "2":
            tierTwoSeats++;
            break;
        case "3":
            tierThreeSeats++;
            break;
        default:
            System.out.println("Should have a Seat Tier!");
        }
    }

    /**
     * Get the seating arrangement of the Theatre.
     *
     * @return seating arrangement as a String.
     */
    public String getSeatingArrangement() {
        StringBuilder seatingArrangement = new StringBuilder(STAGE);

        for (int i = 0; i < seats.length; i++) {
            seatingArrangement.append(SPACES);
            for (int j = 0; j < seats[i].length; j++) {
                seatingArrangement.append(seats[i][j].getSeat());
            }
            seatingArrangement.append("\n");
        }
        seatingArrangement.append(getSeatsLeft());

        return seatingArrangement.toString();
    }

    private String getSeatsLeft() {
        return "\nTier 1 Seats (rows E and F): " + tierOneSeats + "\n"
                + "Tier 2 Seats (rows C and D): " + tierTwoSeats + "\n"
                + "Tier 3 Seats (rows A and B): " + tierThreeSeats + "\n";
    }


    /**
     * Sell seats to customers.
     *
     * @param seat desired seat
     * @return cost of seat.
     */
    public double sellSeats(String seat) {
        int row = getRow(seat.substring(0, 1));
        int col = getCol(seat.substring(1));

        double costOfSeat = 0;

        //This needs to be changed in the event that the theatre dont have fixed seats for each row
        if (row == -1 || col == -1) {
            costOfSeat = -1;
            return costOfSeat;
        }

        double revenue = show.getProfit();

        if (!seats[row][col].isSold()) {
            Seat soldSeat = seats[row][col];
            soldSeat.setSold(true);
            costOfSeat = soldSeat.getSeatPrice(seatBasePrice);
            revenue += costOfSeat;
            this.setSeat(row, col);
        }
        show.setProfit(revenue);
        return costOfSeat;
    }

    /**
     * Sell seats to customers. Used when customer wants to buy multiple seats.
     *
     * @param seats String array of desired seats
     * @return Message detailing status of desired seats (sold out or successfully purchased.)
     */
    public String sellSeats(String... seats) {
        double totalCost = 0;
        ArrayList<String> seatsSold = new ArrayList<>();
        ArrayList<String> seatsNotSold = new ArrayList<>();
        ArrayList<String> seatsNotExist = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        for (String seatNumber : seats) {
            double costOfSeat = sellSeats(seatNumber);

            if (costOfSeat > 0) {
                totalCost += costOfSeat;
                seatsSold.add(seatNumber);
            } else if (costOfSeat == 0) {
                seatsNotSold.add(seatNumber);
            } else {
                seatsNotExist.add(seatNumber);
            }
        }

        if (seatsSold.isEmpty()) { //all seats are unavailable or does not exist
            if (!seatsNotSold.isEmpty()) {
                message.append(String.format("☹ OOPS!!! All of the seats %s are unavailable.\n", seatsNotSold));
            }
            if (!seatsNotExist.isEmpty()) {
                message.append(String.format("☹ OOPS!!! All of the seats %s do not exist.\n", seatsNotExist));
            }
        } else if (seatsNotSold.isEmpty() && seatsNotExist.isEmpty()) { //all seats are valid
            message.append("You have successfully purchased the following seats: \n"
                    + seatsSold + "\n"
                    + String.format(MESSAGE_TICKET_COST, totalCost));
        } else { //combination of all
            message.append("You have successfully purchased the following seats: \n"
                    + seatsSold + "\n"
                    + String.format(MESSAGE_TICKET_COST, totalCost));
            if (!seatsNotSold.isEmpty()) {
                message.append("The following seats are unavailable: \n"
                        + seatsNotSold + "\n");
            }
            if (!seatsNotExist.isEmpty()) {
                message.append("The following seats do not exist: \n"
                        + seatsNotExist + "\n");
            }
        }
        return message.toString();
    }

    /**
     * Reassigns the seat of a customer.
     *
     * @param oldSeat The seat to be changed.
     * @param newSeat The seat to change to.
     * @return Message detailing the success of reassignment and the cost difference between the seats.
     */
    public String reassignSeat(String oldSeat, String newSeat) {
        StringBuilder message = new StringBuilder();

        int oldSeatRow = getRow(oldSeat.substring(0, 1));
        int oldSeatCol = getCol(oldSeat.substring(1));
        int newSeatRow = getRow(newSeat.substring(0, 1));
        int newSeatCol = getCol(newSeat.substring(1));

        //if seat number is invalid.
        if (oldSeatRow == -1 || oldSeatCol == -1 || newSeatRow == -1 || newSeatCol == -1) {
            message.append("☹ OOPS!!! Please enter valid seat numbers.\n");
            return message.toString();
        }

        if (oldSeat.equals(newSeat)) {
            message.append(String.format("Your current seat is already %1$s.\n", oldSeat));
            return message.toString();
        }

        if (!seats[oldSeatRow][oldSeatCol].isSold()) { //if the seat has not been booked yet.
            message.append(String.format("The seat %1$s is still available for booking.\n", oldSeat));
            return message.toString();
        }

        if (seats[newSeatRow][newSeatCol].isSold()) { // if the new seat has already been booked.
            message.append(String.format("☹ OOPS!!! Seat %1$s is unavailable. Use the View Command to"
                    + " view the available seats.\n", newSeat));
            return message.toString();
        }

        double costOfNewSeat = sellSeats(newSeat);
        double costOfOldSeat = removeSeat(oldSeat);

        message.append(String.format("Your seat has been successfully changed from %1$s to %2$s.\n", oldSeat,
                newSeat));

        if (costOfNewSeat > costOfOldSeat) {
            double extraCost = costOfNewSeat - costOfOldSeat;
            message.append(String.format("An extra cost of $%1$.2f is required.\n", extraCost));
        } else if (costOfOldSeat > costOfNewSeat) {
            double returnCost = costOfOldSeat - costOfNewSeat;
            message.append(String.format("$%1$.2f will be returned.\n", returnCost));
        }
        return message.toString();
    }

    /**
     * Removes a seat booking from the theatre.
     *
     * @param seatToRemove The seat to be removed.
     * @return The cost of the seat that has been removed.
     */
    public double removeSeat(String seatToRemove) {
        int row = getRow(seatToRemove.substring(0, 1));
        int col = getCol(seatToRemove.substring(1));
        double seatPrice = 0;

        if (row == -1 || col == -1) { //if seat number is invalid
            seatPrice = -1;
        } else if (!seats[row][col].isSold()) { //if the seat is not sold yet
            seatPrice = 0;
        } else {
            double currRevenue = show.getProfit();
            seatPrice = seats[row][col].getSeatPrice(seatBasePrice);
            show.setProfit(currRevenue - seatPrice);
            this.resetSeat(row, col);
        }
        return seatPrice;
    }

    /**
     * Remove seats for customers. Used when customer wants to remove multiple seats.
     *
     * @param seatsToRemove String array of seats to be removed
     * @return Message detailing status of the removal
     */
    public String removeSeat(String... seatsToRemove) {
        double totalRefund = 0;
        ArrayList<String> seatsRemoved = new ArrayList<>();
        ArrayList<String> seatsEmpty = new ArrayList<>();
        ArrayList<String> seatsNotExist = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        for (String seatNumber : seatsToRemove) {
            double costOfSeat = removeSeat(seatNumber);

            if (costOfSeat > 0) {
                totalRefund += costOfSeat;
                seatsRemoved.add(seatNumber);
            } else if (costOfSeat == 0) {
                seatsEmpty.add(seatNumber);
            } else {
                seatsNotExist.add(seatNumber);
            }
        }

        if (seatsRemoved.isEmpty()) { //all seats are not yet sold or does not exist
            if (!seatsEmpty.isEmpty()) {
                message.append(String.format("☹ OOPS!!! All of the seats %s are not yet sold.\n", seatsEmpty));
            }
            if (!seatsNotExist.isEmpty()) {
                message.append(String.format("☹ OOPS!!! All of the seats %s do not exist.\n", seatsNotExist));
            }
        } else if (seatsEmpty.isEmpty() && seatsNotExist.isEmpty()) { //all seats are valid
            message.append("You have successfully removed the following seats: \n"
                    + seatsRemoved + "\n"
                    + String.format(MESSAGE_TICKET_COST, totalRefund));
        } else { //combination of all
            message.append("You have successfully removed the following seats: \n"
                    + seatsRemoved + "\n"
                    + String.format(MESSAGE_TICKET_COST, totalRefund));
            if (!seatsEmpty.isEmpty()) {
                message.append("The following seats are not yet sold: \n"
                        + seatsEmpty + "\n");
            }
            if (!seatsNotExist.isEmpty()) {
                message.append("The following seats do not exist: \n"
                        + seatsNotExist + "\n");
            }
        }
        return message.toString();
    }

    /**
     * Refunds a seat booking from the theatre.
     *
     * @param seatToRefund The seat to be refunded
     * @return The amount of money refunded for the seat
     */
    public double refundSeat(String seatToRefund) {
        int row = getRow(seatToRefund.substring(0, 1));
        int col = getCol(seatToRefund.substring(1));
        double seatPrice = 0;

        if (row == -1 || col == -1) { //if seat number is invalid
            seatPrice = -1;
        } else if (!seats[row][col].isSold()) { //if the seat is not sold yet
            seatPrice = 0;
        } else {
            double currRevenue = show.getProfit();
            seatPrice = seats[row][col].getSeatPrice(seatBasePrice) * 0.5;
            show.setProfit(currRevenue - seatPrice);
            this.resetSeat(row, col);
        }
        return seatPrice;
    }

    /**
     * Refund seats for customers. Used when customer wants to refund multiple seats.
     *
     * @param seatsToRefund String array of seats to be refunded
     * @return Message detailing status of the refund
     */
    public String refundSeat(String... seatsToRefund) {
        double totalRefund = 0;
        ArrayList<String> seatsRefunded = new ArrayList<>();
        ArrayList<String> seatsEmpty = new ArrayList<>();
        ArrayList<String> seatsNotExist = new ArrayList<>();
        StringBuilder message = new StringBuilder();
        for (String seatNumber : seatsToRefund) {
            double costOfSeat = refundSeat(seatNumber);

            if (costOfSeat > 0) {
                totalRefund += costOfSeat;
                seatsRefunded.add(seatNumber);
            } else if (costOfSeat == 0) {
                seatsEmpty.add(seatNumber);
            } else {
                seatsNotExist.add(seatNumber);
            }
        }

        if (seatsRefunded.isEmpty()) { //all seats are not yet sold or does not exist
            if (!seatsEmpty.isEmpty()) {
                message.append(String.format("☹ OOPS!!! All of the seats %s are not yet sold.\n", seatsEmpty));
            }
            if (!seatsNotExist.isEmpty()) {
                message.append(String.format("☹ OOPS!!! All of the seats %s do not exist.\n", seatsNotExist));
            }
        } else if (seatsEmpty.isEmpty() && seatsNotExist.isEmpty()) { //all seats are valid
            message.append("You have successfully refunded the following seats: \n"
                    + seatsRefunded + "\n"
                    + String.format(MESSAGE_TICKET_COST, totalRefund));
        } else { //combination of all
            message.append("You have successfully refunded the following seats: \n"
                    + seatsRefunded + "\n"
                    + String.format(MESSAGE_TICKET_COST, totalRefund));
            if (!seatsEmpty.isEmpty()) {
                message.append("The following seats are not yet sold: \n"
                        + seatsEmpty + "\n");
            }
            if (!seatsNotExist.isEmpty()) {
                message.append("The following seats do not exist: \n"
                        + seatsNotExist + "\n");
            }
        }
        return message.toString();
    }

    private int getRow(String row) {
        switch (row.toUpperCase()) {
        case "A":
            return 0;
        case "B":
            return 1;
        case "C":
            return 2;
        case "D":
            return 3;
        case "E":
            return 4;
        case "F":
            return 5;
        default:
            return -1;
        }
    }

    private int getCol(String col) {
        switch (col) {
        case "1":
            return 0;
        case "2":
            return 1;
        case "3":
            return 2;
        case "4":
            return 3;
        case "5":
            return 4;
        case "6":
            return 5;
        case "7":
            return 6;
        case "8":
            return 7;
        case "9":
            return 8;
        case "10":
            return 9;
        default:
            return -1;
        }
    }


    public boolean hasSameName(String checkName) {
        return show.hasSameName(checkName);
    }

    public String writeToFile() {
        return String.format("%s | %f | %f\n", show.getShowName(), show.getProfit(), seatBasePrice);
    }
}