package org.example.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TransactionDTO {

    private Long id;

    private Integer clusterId;

    private List<ElementDTO> elementDTOList;
}
