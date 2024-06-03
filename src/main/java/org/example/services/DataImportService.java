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

//@Service
//@RequiredArgsConstructor
//public class DataImportService {
//
//    private final TransactionRepository transactionRepository;
//    private static final String DEFAULT_FILE_PATH = "input/agaricus-lepiota.data";
//    private static final int BATCH_SIZE = 1000;
//    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
//
//    public void importData() {
//        importData(DEFAULT_FILE_PATH);
//    }
//
//    public void importData(String filePath) {
//        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
//        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
//        executor.submit(() -> {
//            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//                String line;
//                while ((line = br.readLine()) != null) {
//                    queue.put(line);
//                }
//            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
//            } finally {
//                for (int i = 0; i < THREAD_COUNT; i++) {
//                    try {
//                        queue.put("EOF");
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    }
//                }
//            }
//        });
//        for (int i = 0; i < THREAD_COUNT; i++) {
//            executor.submit(() -> {
//                List<Transaction> batch = new ArrayList<>(BATCH_SIZE);
//                try {
//                    while (true) {
//                        String line = queue.take();
//                        if ("EOF".equals(line)) {
//                            break;
//                        }
//                        Transaction transaction = new Transaction();
//                        transaction.setElements(line);
//                        batch.add(transaction);
//
//                        if (batch.size() >= BATCH_SIZE) {
//                            saveBatch(batch);
//                            batch.clear();
//                        }
//                    }
//                    if (!batch.isEmpty()) {
//                        saveBatch(batch);
//                    }
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
//            });
//        }
//
//        executor.shutdown();
//        try {
//            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    @Transactional
//    public void saveBatch(List<Transaction> batch) {
//        transactionRepository.saveAll(batch);
//    }
//}