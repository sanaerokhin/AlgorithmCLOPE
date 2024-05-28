package org.example;

import lombok.RequiredArgsConstructor;
import org.example.services.DataImportService;
import org.example.services.TransactionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Scanner;
import java.util.concurrent.*;

@SpringBootApplication
@RequiredArgsConstructor
public class Application implements CommandLineRunner {

    private final TransactionService transactionService;
    private final DataImportService dataImportService;
    private final ApplicationContext context;
    private CompletableFuture<Void> future = CompletableFuture.runAsync(System.out::println);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (!future.isDone()) {
                System.out.println("You can stop calculation by enter command stop");
            } else {
                System.out.println("Enter command: (import data / initialization / iteration / print / clear / stop / exit)");
            }
            String command = scanner.nextLine().trim();
            switch (command) {
                case "import data":
                    handleImportData(scanner);
                    break;
                case "initialization":
                    handleInitialization(scanner);
                    break;
                case "iteration":
                    handleIteration(scanner);
                    break;
                case "print":
                    handlePrint(scanner);
                    break;
                case "clear":
                    handleClear();
                    break;
                case "stop":
                    handleStopByUser();
                    break;
                case "exit":
                    handleExit();
                    return;
                default:
                    System.out.println("Unknown command. Try again.");
            }
        }
    }

    private void handleImportData(Scanner scanner) {
        if (!future.isDone()) {
            System.out.println("Calculation is running");
        } else {
            System.out.println("Enter absolute file path or press enter to use default:");
            String filePath = scanner.nextLine().trim();
            System.out.println("importing running");
            if (filePath.isEmpty()) {
                dataImportService.importData();
            } else {
                dataImportService.importData(filePath);
            }
            System.out.println("importing complete");
        }
    }

    private void handleInitialization(Scanner scanner) {
        if (!future.isDone()) {
            System.out.println("Calculation is running");
        } else {
            transactionService.setStoppedByUser(false);
            System.out.println("Enter repulsion parameter (r):");
            double r = Double.parseDouble(scanner.nextLine().trim());
            System.out.println("initialization running");
            future = CompletableFuture.runAsync(() -> {
                transactionService.initialization(r);
                System.out.println("initialization complete");
            });
        }
    }

    private void handleIteration(Scanner scanner) {
        if (!future.isDone()) {
            System.out.println("Calculation is running");
        } else {
            transactionService.setStoppedByUser(false);
            System.out.println("Enter repulsion parameter (r):");
            double r = Double.parseDouble(scanner.nextLine().trim());
            System.out.println("Enter calculation error parameter:");
            double c = Double.parseDouble(scanner.nextLine().trim());
            System.out.println("iteration running");
            future = CompletableFuture.runAsync(() -> {
                transactionService.iteration(r, c);
                System.out.println("iteration complete");
            });
        }
    }

    private void handlePrint(Scanner scanner) {
        if (!future.isDone()) {
            System.out.println("Calculation is running");
        } else {
            try {
                System.out.println("Enter element index:");
                int elementIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                transactionService.printClusterInformation(elementIndex);
                System.out.println("printing complete");
            } catch (NumberFormatException e) {
                System.out.println("Invalid index. Please enter a valid number.");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Index out of bounds. Please enter a valid index.");
            }
        }
    }

    private void handleClear() {
        if (!future.isDone()) {
            System.out.println("Calculation is running");
        } else {
            transactionService.clear();
            System.out.println("clearing complete");
        }
    }

    private void handleStopByUser() {
        if (future.isDone()) {
            System.out.println("Calculation is not running");
        } else {
            transactionService.setStoppedByUser(true);
            System.out.println("calculation stopped");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleExit() {
        transactionService.setStoppedByUser(true);
        System.out.println("exiting...");
        SpringApplication.exit(context, () -> 0);
        System.exit(0);
    }
}