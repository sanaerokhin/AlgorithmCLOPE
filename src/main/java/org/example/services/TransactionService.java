package org.example.services;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.dto.ClusterDTO;
import org.example.dto.ElementDTO;
import org.example.dto.TransactionDTO;
import org.example.model.Transaction;
import org.example.repositories.TransactionRepository;
import org.example.utils.AlgorithmCLOPE;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int PAGE_SIZE = 2000;

    private final TransactionRepository transactionRepository;
    private final AlgorithmCLOPE algorithmCLOPE;
    @Setter
    private boolean stoppedByUser = false;

    public void initialization(double repulsion) {
        initialization(repulsion, PAGE_SIZE);
    }

    public void initialization(double repulsion, double pageSize) {
        int pageNumber = 0;
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        while (true) {
            Pageable pageable = PageRequest.of(pageNumber, (int) pageSize);
            List<Transaction> transactionList = transactionRepository.findTransactions(pageable).getContent();
            if (transactionList.isEmpty() || stoppedByUser) break;
            forkJoinPool.execute(() -> {
                for (Transaction transaction : transactionList) {
                    transaction.setClusterId(algorithmCLOPE.putTransactionInCluster(mapToDTO(transaction), repulsion));
                }
                transactionRepository.saveAll(transactionList);
            });
            pageNumber++;
        }
        forkJoinPool.shutdown();
        try {
            forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void iteration(double repulsion, double calculationError) {
        double lastGlobalProfit;
        double newGlobalProfit;
        int i = 1;
        do {
            lastGlobalProfit = algorithmCLOPE.calculateGlobalProfit(repulsion);
            initialization(repulsion, ((double) PAGE_SIZE / i));
            newGlobalProfit = algorithmCLOPE.calculateGlobalProfit(repulsion);
            System.out.println("last global profit " + lastGlobalProfit);
            System.out.println("new global profit  " + newGlobalProfit);
            i++;
        }
        while ((newGlobalProfit - lastGlobalProfit) > calculationError);
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        List<ElementDTO> elementDTOList = new ArrayList<>();
        String[] elements = transaction.getElements().split(",");
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].equals("?")) {
                elementDTOList.add(ElementDTO.findElementDTO(i, elements[i]));
            } else {
                elementDTOList.add(null);
            }
        }
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setId(transaction.getId());
        transactionDTO.setClusterId(transaction.getClusterId());
        transactionDTO.setElementDTOList(elementDTOList);
        return transactionDTO;
    }

    public void recoveryData() {
        int i = 0;
        long transactionsCount = transactionRepository.countByClusterIdIsNotNull();
        do {
            List<Transaction> transactionList = transactionRepository.findAllByClusterId(i);
            if (!transactionList.isEmpty()) {
                ClusterDTO clusterDTO = new ClusterDTO();
                clusterDTO.setClusterId(i);
                transactionList.forEach(transaction1 -> clusterDTO.addTransactionDTO(mapToDTO(transaction1)));
                transactionsCount -= transactionList.size();
                ClusterDTO.setClustersCount(i);
            }
            i++;
        } while (transactionsCount > 0);
    }

    public void clear() {
        transactionRepository.clearClusterIdValues();
        ClusterDTO.setClustersCount(0);
        ClusterDTO.setClusterDTOS(new ConcurrentSkipListSet<>());
    }

    public void clearAll() {
        transactionRepository.deleteAll();
        ClusterDTO.setClustersCount(0);
        ClusterDTO.setClusterDTOS(new ConcurrentSkipListSet<>());
    }

    public void printClusterInformation(int elementIndex) {
        if (ClusterDTO.getClustersCount() == 0) {
            recoveryData();
        }
        AtomicInteger count = new AtomicInteger();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(System.lineSeparator());
        List<ClusterDTO> set = new ArrayList<>(ClusterDTO.getClusterDTOS().stream().toList());
        set.sort(Comparator.comparingInt(ClusterDTO::getClusterId));
        for (ClusterDTO clusterDTO : set) {
            List<ElementDTO> elementList = new ArrayList<>();
            clusterDTO.getElementDTOsMap().forEach((k, v) -> {
                if (k.getElementNumber().equals(elementIndex)) {
                    elementList.add(k);
                }
            });
            stringBuilder.append("cluster ");
            stringBuilder.append(clusterDTO.getClusterId());
            stringBuilder.append(": ");
            elementList.forEach(elementDTO -> {
                stringBuilder.append(elementDTO.getElementValue());
                stringBuilder.append(":");
                stringBuilder.append(clusterDTO.getOcc(elementDTO));
                stringBuilder.append(" ");
                count.addAndGet(clusterDTO.getOcc(elementDTO));
            });
            stringBuilder.append(System.lineSeparator());
        }
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("Element[").append(elementIndex + 1).append("] values: ");
        ElementDTO.getELEMENT_DTOS().forEach(elementDTO -> {
            if (elementDTO.getElementNumber().equals(elementIndex)) {
                stringBuilder.append(elementDTO.getElementValue());
            }
        });
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("Transactions count ").append(count.get()).append(System.lineSeparator());
        System.out.println(stringBuilder);
    }
}