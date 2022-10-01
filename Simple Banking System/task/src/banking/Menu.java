package banking;

import java.util.*;


public class Menu {
    private boolean sessionActive;
    private boolean loggedIn;

    private String loggedCard;
    Scanner scanner;
    DBConnection dbConnection;

    public Menu(DBConnection dbConnection) {
        sessionActive = true;
        scanner = new Scanner(System.in);
        this.dbConnection = dbConnection;
    }

    public void get() {
        if (!loggedIn) {
            System.out.println("\n1. Create an account\n" +
                    "2. Log into account\n" +
                    "0. Exit");
            System.out.print(">");
            switch (scanner.nextInt()) {
                case(1) :
                    System.out.println();
                    createAccount();
                    break;
                case(2) :
                    System.out.println();
                    logIn();
                    break;
                case(0) :
                    sessionActive = false;
                    break;
            }
        } else {
            System.out.println("\n1. Balance\n" +
                    "2. Add income\n" +
                    "3. Do transfer\n" +
                    "4. Close account\n" +
                    "5. Log out\n" +
                    "0. Exit");
            System.out.print(">");
            switch (scanner.nextInt()) {
                case(1) :
                    System.out.println("Balance: " + getBalance());
                    break;

                case(2) :
                    addIncome();
                    break;

                case(3) :
                    doTransfer();
                    break;

                case(4) :
                    dbConnection.deleteCard(loggedCard);
                    loggedIn = false;
                    System.out.println("This account has been closed!");
                    break;

                case(5) :
                    loggedIn = false;
                    System.out.println("You have successfully logged out!");
                    break;

                case(0) :
                    sessionActive = false;
                    break;
            }
        }
    }



    private void createAccount() {
        Random random = new Random();
        String cardNumber = generateCardNumber();
        String pin = String.valueOf(random.nextInt(8999) + 1000);

        dbConnection.insertNewCard(cardNumber, pin);

        System.out.println("Your card has been created ");
        System.out.println("Your card number :");
        System.out.println(cardNumber);
        System.out.println("Your card PIN :");
        System.out.println(pin);

    }

    private String generateCardNumber() {
        Random random = new Random();
        String cardNumber = "400000" + (random.nextInt(899999999) + 100000000) ;

        int checkSum = generateCheckNumber(cardNumber);
        cardNumber += checkSum;

        return cardNumber;
    }



    private void logIn() {
        System.out.println("\nEnter your card number:");
        System.out.print(">");
        String cardNumber = scanner.next();

        if (cardNumber.length() != 16) {
            System.out.println("Wrong card number length");
        } else {
            System.out.println("Enter your PIN:");
            System.out.print(">");
            String pin = scanner.next();
            if (dbConnection.matchCardWithPin(cardNumber, pin)) {
                loggedCard = cardNumber;
                loggedIn = true;
                System.out.println("\nYou have successfully logged in!");
            } else {
                System.out.println("\nWrong card number or PIN!");
            }
        }
    }



    private void addIncome() {
        try {
            System.out.println("\nEnter income:");
            System.out.print(">");
            int income = scanner.nextInt();
            System.out.println("Income was added!");
            dbConnection.addBalance(income, loggedCard);
        } catch(InputMismatchException e) {
            System.out.println("Only integer numbers are accepted!");
            get();
        }
    }

    private boolean doTransfer() {
        System.out.println("\nEnter card number:");
        System.out.print(">");
        String receiverCard = scanner.next();

        if (loggedCard.equals(receiverCard)) {
            System.out.println("You can't transfer money to the same account!");
            return false;
        }

        int checkNumber = generateCheckNumber(receiverCard.substring(0, 15));
        int cardCheckNumber = Integer.parseInt(receiverCard.substring(15));
        if (checkNumber != Integer.parseInt(receiverCard.substring(15))) {
            System.out.println("Probably you made mistake in the card number. Please try again!");
            return false;
        }


        if (!dbConnection.isCardExist(receiverCard)) {
            System.out.println("Such a card does not exist.");
            return false;
        }

        System.out.println("Enter how much money you want to transfer:");
        System.out.print(">");
        int transferAmount = scanner.nextInt();
        if (dbConnection.getBalance(loggedCard) < transferAmount) {
            System.out.println("Not enough money!");
            return false;
        }
        dbConnection.doTransfer(transferAmount, loggedCard, receiverCard);
        System.out.println("Success!");
        return false;
    }


    private int generateCheckNumber(String cardNumber) {
        char[] cardNumberArray = cardNumber.toCharArray();

        int numberSum = 0;
        for (int i = 0; i < cardNumberArray.length; i++) {
            int number = Integer.parseInt(String.valueOf(cardNumberArray[i]));
            if (i % 2 == 0) {
                number *= 2;
                if (number > 9) {
                    number -= 9;
                }
            }
            numberSum += number;
        }

        int checkSum = 10 - (numberSum % 10);
        if (checkSum == 10) {
            checkSum = 0;
        }
        return checkSum;
    }


    private int getBalance() {
        return dbConnection.getBalance(loggedCard);
    }



    public boolean isSessionActive() {
        return sessionActive;
    }
}