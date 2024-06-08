package org.example.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter
@Setter
public class ElementDTO implements Comparable<ElementDTO>{

    private Integer elementNumber;
    private String elementValue;

    @Getter
    private static final Set<ElementDTO> ELEMENT_DTOS = new ConcurrentSkipListSet<>();

    public ElementDTO(Integer elementNumber, String elementValue) {
        this.elementNumber = elementNumber;
        this.elementValue = elementValue;
        ELEMENT_DTOS.add(this);
    }

    public static ElementDTO findElementDTO(Integer elementNumber, String elementValue) {
        for (ElementDTO elementDTO : ELEMENT_DTOS) {
            if (elementDTO.getElementNumber().equals(elementNumber) && elementDTO.getElementValue().equals(elementValue)) {
                return elementDTO;
            }
        }
        return new ElementDTO(elementNumber, elementValue);
    }

    @Override
    public int compareTo(ElementDTO o) {
        if (elementNumber.equals(o.elementNumber)) {
            if (elementValue.equals(o.elementValue)) {
                return 0;
            }
            return elementValue.compareTo(o.elementValue);
        }
        return elementNumber.compareTo(o.elementNumber);
    }
}
