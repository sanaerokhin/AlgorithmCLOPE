package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.model.Transaction;
import org.example.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DataImportService {

    private final TransactionRepository transactionRepository;
    private static final String DEFAULT_FILE_PATH = "input/agaricus-lepiota.data";

    public void importData() {
        importData(DEFAULT_FILE_PATH);
    }

    public void importData(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Transaction transaction = new Transaction();
                transaction.setElements(line);
                transactionRepository.save(transaction);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
