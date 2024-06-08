package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.model.Transaction;
import org.example.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataImportService {

    private final TransactionRepository transactionRepository;
    private static final String DEFAULT_FILE_PATH = "input/agaricus-lepiota.data";
    private static final Integer MAX_BATCH_SIZE = 2_000;

    public void importData() {
        importData(DEFAULT_FILE_PATH);
    }

    public void importData(String filePath) {
        List<Transaction> transactionList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Transaction transaction = new Transaction();
                transaction.setElements(line);
                transactionList.add(transaction);
                if (transactionList.size() > MAX_BATCH_SIZE) {
                    transactionRepository.saveAll(transactionList);
                    transactionList.clear();
                }
            }
            transactionRepository.saveAll(transactionList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}