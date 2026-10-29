package com.atm;

import java.util.EnumMap;
import java.util.Map;
import java.util.Scanner;

/** Console entry point. This is a simulation, not a connection to a real banking system. */
public class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Map<Denomination, Integer> initialNotes = new EnumMap<>(Denomination.class);
        initialNotes.put(Denomination.N500, 10);
        initialNotes.put(Denomination.N200, 10);
        initialNotes.put(Denomination.N100, 20);
        initialNotes.put(Denomination.N50, 20);
        initialNotes.put(Denomination.N20, 20);
        initialNotes.put(Denomination.N10, 20);

        Account account = new Account("1234567890123456", 1234, 20_000, 20_000);
        ATM atm = new ATM(new CashInventory(initialNotes), new GreedyDispenser());

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("============================================");
            System.out.println(" ATM CASH DISPENSER WITH DENOMINATION LOGIC ");
            System.out.println("============================================");
            System.out.println("Demo card number: 1234567890123456");
            System.out.println("Demo PIN: 1234");
            System.out.println("ATM cash available: Rs." + atm.getCashAvailable());

            if (!login(scanner, account)) {
                return;
            }

            boolean running = true;
            while (running) {
                printMenu(atm);
                System.out.print("Choose an option: ");
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1" -> withdraw(scanner, atm, account);
                    case "2" -> showStatement(account);
                    case "3" -> System.out.println("Available balance: Rs." + account.getBalance());
                    case "4" -> chooseStrategy(scanner, atm);
                    case "0" -> running = false;
                    default -> System.out.println("Invalid option. Please choose 0–4.");
                }
                System.out.println();
            }
            System.out.println("Thank you for using the ATM simulation.");
        }
    }

    private static boolean login(Scanner scanner, Account account) {
        while (true) {
            System.out.print("Enter card number: ");
            String cardNumber = scanner.nextLine().trim();
            System.out.print("Enter PIN: ");
            String pinText = scanner.nextLine().trim();
            try {
                int pin = Integer.parseInt(pinText);
                account.authenticate(cardNumber, pin);
                System.out.println("Authentication successful.\n");
                return true;
            } catch (NumberFormatException exception) {
                System.out.println("PIN must contain digits only.");
            } catch (AuthenticationException exception) {
                System.out.println(exception.getMessage());
                if (account.isLocked()) {
                    return false;
                }
            }
        }
    }

    private static void printMenu(ATM atm) {
        System.out.println("------------- ATM MENU -------------");
        System.out.println("1. Withdraw cash");
        System.out.println("2. Mini statement");
        System.out.println("3. Check balance");
        System.out.println("4. Change dispensing strategy (current: " + atm.getStrategyName() + ")");
        System.out.println("0. Exit");
    }

    private static void withdraw(Scanner scanner, ATM atm, Account account) {
        System.out.print("Enter withdrawal amount (Rs.100–Rs.20,000): ");
        String amountText = scanner.nextLine().trim();
        try {
            int amount = Integer.parseInt(amountText);
            Map<Denomination, Integer> notes = atm.withdraw(account, amount);
            System.out.println("Withdrawal successful using " + atm.getStrategyName() + " strategy.");
            System.out.println("Notes dispensed:");
            for (Denomination denomination : Denomination.values()) {
                int count = notes.getOrDefault(denomination, 0);
                if (count > 0) {
                    System.out.printf("  %-8s x %d%n", denomination, count);
                }
            }
            System.out.println("Remaining balance: Rs." + account.getBalance());
            System.out.println("Cash remaining in ATM: Rs." + atm.getCashAvailable());
        } catch (NumberFormatException exception) {
            System.out.println("Please enter a whole-number amount.");
        } catch (ATMException exception) {
            System.out.println("Withdrawal failed: " + exception.getMessage());
        }
    }

    private static void chooseStrategy(Scanner scanner, ATM atm) {
        System.out.println("1. Greedy (largest notes first)");
        System.out.println("2. Optimal (fewest notes, dynamic programming)");
        System.out.print("Choose strategy: ");
        switch (scanner.nextLine().trim()) {
            case "1" -> {
                atm.setStrategy(new GreedyDispenser());
                System.out.println("Greedy strategy selected.");
            }
            case "2" -> {
                atm.setStrategy(new OptimalDispenser());
                System.out.println("Optimal strategy selected.");
            }
            default -> System.out.println("Invalid strategy; current strategy unchanged.");
        }
    }

    private static void showStatement(Account account) {
        var transactions = account.miniStatement();
        if (transactions.isEmpty()) {
            System.out.println("No transactions recorded yet.");
            return;
        }
        System.out.println("---------------- MINI STATEMENT ----------------");
        for (Transaction transaction : transactions) {
            System.out.printf("%s | %-8s | Rs.%d | %s%n",
                    transaction.timestamp(), transaction.type(), transaction.amount(), transaction.status());
        }
    }
}
