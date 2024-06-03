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
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class TransactionService {

    //TODO
    private static final int PAGE_SIZE = 10000;

    private final TransactionRepository transactionRepository;
    private final AlgorithmCLOPE algorithmCLOPE;
    @Setter
    private boolean stoppedByUser = false;
    private Transaction transaction;

    public void initialization(double repulsion) {
        if (transaction == null) {
            Optional<Transaction> optional = transactionRepository.findById(1L);
            optional.ifPresent(value -> transaction = value);
        }
        if (transaction.getClusterId() != null && ClusterDTO.getClustersCount() == 0) {
            recoveryData();
        }
        while (true) {
            transaction.setClusterId(algorithmCLOPE.putTransactionInCluster(mapToDTO(transaction), repulsion));
            transactionRepository.save(transaction);
            Optional<Transaction> optional = transactionRepository.findById(transaction.getId() + 1);
            if (optional.isPresent() && !stoppedByUser) {
                transaction = optional.get();
            } else {
                transaction = null;
                break;
            }
        }
    }

//    public void initialization(double repulsion) {
//        int pageNumber = 0;
//        while (true) {
//            Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE);
//            List<Transaction> transactionList = transactionRepository.findTransactions(pageable).getContent();
//            if (transactionList.isEmpty() || stoppedByUser) break;
//            for (Transaction transaction : transactionList) {
//                transaction.setClusterId(algorithmCLOPE.putTransactionInCluster(mapToDTO(transaction), repulsion));
//            }
//            transactionRepository.saveAll(transactionList);
//            pageNumber++;
//        }
//    }

    public void iteration(double repulsion, double calculationError) {
        double lastGlobalProfit;
        double newGlobalProfit;
        do {
            lastGlobalProfit = algorithmCLOPE.calculateGlobalProfit(repulsion);
            initialization(repulsion);
            newGlobalProfit = algorithmCLOPE.calculateGlobalProfit(repulsion);
            System.out.println("lastGlobalProfit " + lastGlobalProfit);
            System.out.println("newGlobalProfit " + newGlobalProfit);
        }
        while ((newGlobalProfit - lastGlobalProfit) > calculationError);
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
        ClusterDTO.setClusterDTOS(new HashSet<>());
    }

    public void printClusterInformation(int elementIndex) {
        if (ClusterDTO.getClustersCount() == 0) {
            recoveryData();
        }

        //TODO delete
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
        stringBuilder.append("Transactions count ").append(count.get());
        System.out.println(stringBuilder);
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
}