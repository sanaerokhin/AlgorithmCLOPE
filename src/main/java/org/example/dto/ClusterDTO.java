package org.example.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter
@Setter
public class ClusterDTO implements Comparable<ClusterDTO> {

    @Setter
    @Getter
    private static Integer clustersCount = 0;
    @Getter
    @Setter
    private static Set<ClusterDTO> clusterDTOS = new ConcurrentSkipListSet<>();

    private Integer clusterId;
    private Integer transactionsCount;
    private final Map<ElementDTO, Integer> elementDTOsMap = new HashMap<>();

    public synchronized static Optional<ClusterDTO> findById(Integer id) {
        return clusterDTOS.stream().filter(clusterDTO -> clusterDTO.getClusterId().equals(id)).findFirst();
    }

    public static void remove(ClusterDTO clusterDTO) {
        clusterDTOS.remove(clusterDTO);
    }

    public ClusterDTO() {
        clustersCount++;
        clusterId = clustersCount;
        transactionsCount = 0;
        clusterDTOS.add(this);
    }

    public synchronized void addTransactionDTO(TransactionDTO transactionDTO) {
        transactionsCount++;
        transactionDTO.setClusterId(clusterId);
        transactionDTO.getElementDTOList()
                .stream()
                .filter(Objects::nonNull)
                .forEach(elementDTO -> elementDTOsMap.compute(elementDTO, (key, value) -> value == null ? 1 : value + 1));
    }

    public synchronized void removeTransactionDTO(TransactionDTO transactionDTO) {
        transactionsCount--;
        if (transactionsCount <= 0) {
            ClusterDTO.remove(this);
            return;
        }
        transactionDTO.getElementDTOList()
                .stream()
                .filter(Objects::nonNull)
                .forEach(elementDTO -> elementDTOsMap.compute(elementDTO, (key, value) -> (value == null || value == 1) ? null : value - 1));
    }

    public int getUniqueElementsCount() {
        return elementDTOsMap.keySet().size();
    }

    public int getArea() {
        int area = 0;
        for (Integer value : elementDTOsMap.values()) {
            area += value;
        }
        return area;
    }

    public Integer getOcc(ElementDTO elementDTO){
        return elementDTOsMap.get(elementDTO);
    }

    @Override
    public int compareTo(ClusterDTO o) {
        return clusterId.compareTo(o.getClusterId());
    }
}