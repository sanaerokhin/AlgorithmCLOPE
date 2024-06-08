package org.example.utils;

import lombok.RequiredArgsConstructor;
import org.example.dto.ClusterDTO;
import org.example.dto.ElementDTO;
import org.example.dto.TransactionDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AlgorithmCLOPE {

    public int putTransactionInCluster(TransactionDTO transactionDTO, Double repulsion) {
        ClusterDTO bestCluster = null;
        double bestDeltaAdd;
        bestDeltaAdd = -Double.MAX_VALUE;
        ClusterDTO.findById(transactionDTO.getClusterId()).ifPresent(clusterDTO -> clusterDTO.removeTransactionDTO(transactionDTO));
        for (ClusterDTO clusterDTO : ClusterDTO.getClusterDTOS()) {
            double deltaAdd = deltaAdd(clusterDTO, transactionDTO, repulsion);
            if (deltaAdd > bestDeltaAdd) {
                bestDeltaAdd = deltaAdd;
                bestCluster = clusterDTO;
            }
        }
        double newDeltaAdd = deltaAdd(transactionDTO, repulsion);
        if (bestCluster == null || newDeltaAdd > bestDeltaAdd) {
            bestCluster = new ClusterDTO();
        }
        bestCluster.addTransactionDTO(transactionDTO);
        return bestCluster.getClusterId();
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

    public Double deltaAdd(ClusterDTO clusterDTO, TransactionDTO transactionDTO, Double repulsion) {
        synchronized (clusterDTO) {
            List<ElementDTO> elementDTOList = transactionDTO.getElementDTOList();
            int newWidth = clusterDTO.getUniqueElementsCount();
            int transactionElementsCount = 0;
            for (ElementDTO elementDTO : elementDTOList) {
                if (elementDTO != null) {
                    transactionElementsCount++;
                    if (!clusterDTO.getElementDTOsMap().containsKey(elementDTO)) {
                        newWidth++;
                    }
                }
            }
            int newArea = clusterDTO.getArea() + transactionElementsCount;
            double d1 = newArea * (clusterDTO.getTransactionsCount() + 1) / Math.pow(newWidth, repulsion);
            double d2 = clusterDTO.getArea() * clusterDTO.getTransactionsCount() / Math.pow(clusterDTO.getUniqueElementsCount(), repulsion);
            return d1 - d2;
        }
    }

    public Double deltaAdd(TransactionDTO transactionDTO, Double repulsion) {
        long newArea = transactionDTO.getElementDTOList().stream().filter(Objects::nonNull).count();
        return newArea / Math.pow(newArea, repulsion);
    }
}