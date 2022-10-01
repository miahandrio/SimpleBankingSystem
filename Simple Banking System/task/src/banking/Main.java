package banking;

public class Main {
    public static void main(String[] args) {
        DBConnection dbConnection = new DBConnection(args);
        Menu menu = new Menu(dbConnection);
        while (menu.isSessionActive()) {
            menu.get();
        }
        System.out.println("\nBye!");
    }
}