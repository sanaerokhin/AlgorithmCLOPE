package org.example.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.model.Transaction;

import java.util.*;

@Getter
@Setter
public class ClusterDTO {

    @Getter
    @Setter
    private static Integer clustersCount = 0;
    @Getter
    @Setter
    private static Set<ClusterDTO> clusterDTOS = new HashSet<>();

    private Integer clusterId;
    private Integer transactionsCount;
    private final Map<ElementDTO, Integer> elementDTOsMap = new HashMap<>();

    public static Optional<ClusterDTO> findById(Integer id) {
        return clusterDTOS.stream().filter(clusterDTO -> clusterDTO.getClusterId().equals(id)).findFirst();
    }

    public static void remove(ClusterDTO clusterDTO) {
        clusterDTOS.remove(clusterDTO);
    }

    public ClusterDTO() {
        clustersCount++;
        clusterDTOS.add(this);
        clusterId = clustersCount;
        transactionsCount = 0;
    }

    public void addTransaction(Transaction transaction) {
        transactionsCount++;
        transaction.setClusterId(clusterId);
        String[] elements = transaction.getElements().split(",");
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].equals("?")) {
                ElementDTO elementDTO = ElementDTO.findElementDTO(i, elements[i]);
                elementDTOsMap.compute(elementDTO, (key, value) -> value == null ? 1 : value + 1);
            }
        }
    }

    public void removeTransaction(Transaction transaction) {
        transactionsCount--;
        if (transactionsCount <= 0) {
            ClusterDTO.remove(this);
            return;
        }
        String[] elements = transaction.getElements().split(",");
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].equals("?")) {
                ElementDTO elementDTO = ElementDTO.findElementDTO(i, elements[i]);
                elementDTOsMap.compute(elementDTO, (key, value) -> (value == null || value == 1) ? null : value - 1);
            }
        }

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

}