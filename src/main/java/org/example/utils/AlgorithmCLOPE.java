package org.example.utils;

import lombok.RequiredArgsConstructor;
import org.example.dto.ClusterDTO;
import org.example.dto.ElementDTO;
import org.example.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AlgorithmCLOPE {

    public void putTransactionInCluster(Transaction transaction, Double repulsion) {
        ClusterDTO bestCluster = null;
        double bestDeltaAdd;
        bestDeltaAdd = - Double.MAX_VALUE;
        ClusterDTO.findById(transaction.getClusterId()).ifPresent(clusterDTO -> clusterDTO.removeTransaction(transaction));
        for (ClusterDTO clusterDTO : ClusterDTO.getClusterDTOS()) {
            double deltaAdd = deltaAdd(clusterDTO, transaction, repulsion);
            if (deltaAdd > 0 && deltaAdd > bestDeltaAdd && (!clusterDTO.equals(bestCluster))) {
                bestDeltaAdd = deltaAdd;
                bestCluster = clusterDTO;
            }
        }
        double newDeltaAdd = deltaAdd(transaction, repulsion);
        if (bestCluster == null || newDeltaAdd > bestDeltaAdd) {
            bestCluster = new ClusterDTO();
        }
        bestCluster.addTransaction(transaction);
        transaction.setClusterId(bestCluster.getClusterId());
    }

    public Double calculateGlobalProfit(Double repulsion) {
        Set<ClusterDTO> clusterDTOs = ClusterDTO.getClusterDTOS();
        if (clusterDTOs.isEmpty()) return 0.0;
        double d1 = 0.0;
        int d2 = 0;
        for (ClusterDTO clusterDTO : clusterDTOs) {
            int area = clusterDTO.getArea();
            int width = clusterDTO.getUniqueElementsCount();
            d1 += area / Math.pow(width, repulsion) * clusterDTO.getTransactionsCount();
            d2 += clusterDTO.getTransactionsCount();
        }
        return d1 / d2;
    }

    public Double deltaAdd(ClusterDTO clusterDTO, Transaction transaction, Double repulsion) {
        int transactionElementsCount = transaction.getElements().replaceAll(",", "").replaceAll("\\?", "").length();
        int newArea = clusterDTO.getArea() + transactionElementsCount;
        int newWidth = clusterDTO.getUniqueElementsCount();
        String[] elements = transaction.getElements().split(",");
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].equals("?")) {
                ElementDTO newElementDTO = ElementDTO.findElementDTO(i, elements[i]);
                if (!clusterDTO.getElementDTOsMap().containsKey(newElementDTO)) {
                    newWidth++;
                }
            }
        }
        double d1 = newArea * (clusterDTO.getTransactionsCount() + 1) / Math.pow(newWidth, repulsion);
        double d2 = clusterDTO.getArea() * clusterDTO.getTransactionsCount() / Math.pow(clusterDTO.getUniqueElementsCount(), repulsion);
        return d1 - d2;
    }

    public Double deltaAdd(Transaction transaction, Double repulsion) {
        int newArea = transaction.getElements().replaceAll(",", "").replaceAll("\\?", "").length();
        return newArea / Math.pow(newArea, repulsion);
    }
}