package org.example.services;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.dto.ClusterDTO;
import org.example.dto.ElementDTO;
import org.example.model.Transaction;
import org.example.repositories.TransactionRepository;
import org.example.utils.AlgorithmCLOPE;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final DataImportService dataImportService;
    private final TransactionRepository transactionRepository;
    private final AlgorithmCLOPE algorithmCLOPE;
    @Setter
    private boolean stoppedByUser = false;
    private Transaction transaction;

    public void initialization(double repulsion) {
        if (transaction == null) {
            Optional<Transaction> optional = transactionRepository.findById(1L);
            if (optional.isPresent()) {
                transaction = optional.get();
            } else {
                dataImportService.importData();
                initialization(repulsion);
                return;
            }
        }
        if (transaction.getClusterId() != null && ClusterDTO.getClustersCount() == 0) {
            recoveryData();
        }
        while (true) {
            algorithmCLOPE.putTransactionInCluster(transaction, repulsion);
            transactionRepository.save(transaction);
            Optional<Transaction> optional = transactionRepository.findById(transaction.getId() + 1);
            if (optional.isPresent() && !stoppedByUser) {
                transaction = optional.get();
            } else {
                break;
            }
        }
    }

    public void iteration(double repulsion, double calculationError) {
        double lastGlobalProfit = algorithmCLOPE.calculateGlobalProfit(repulsion);
        double newGlobalProfit = Double.MAX_VALUE;
        while ((newGlobalProfit - lastGlobalProfit) > calculationError) {
            lastGlobalProfit = algorithmCLOPE.calculateGlobalProfit(repulsion);
            initialization(repulsion);
            newGlobalProfit = algorithmCLOPE.calculateGlobalProfit(repulsion);
        }
    }

    private void recoveryData() {
        int i = 0;
        long transactionsCount = transactionRepository.countByClusterIdIsNotNull();
        do {
            List<Transaction> transactionList = transactionRepository.findAllByClusterId(i);
            if (!transactionList.isEmpty()) {
                ClusterDTO clusterDTO = new ClusterDTO();
                clusterDTO.setClusterId(i);
                transactionList.forEach(clusterDTO::addTransaction);
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
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(System.lineSeparator());
        for (int i = 0; i <= ClusterDTO.getClusterDTOS().size(); i++) {
            Optional<ClusterDTO> optional = ClusterDTO.findById(i);
            if (optional.isPresent()) {
                ClusterDTO clusterDTO = optional.get();
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
                });
                stringBuilder.append(System.lineSeparator());
            }
        }
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("Element[").append(elementIndex + 1).append("] values: ");
        ElementDTO.getELEMENT_DTOS().forEach(elementDTO -> {
            if (elementDTO.getElementNumber().equals(elementIndex)) {
                stringBuilder.append(elementDTO.getElementValue());
            }
        });
        stringBuilder.append(System.lineSeparator());

        System.out.println(stringBuilder);
    }
}