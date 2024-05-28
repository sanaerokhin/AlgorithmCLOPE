package org.example.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Optional;

@Getter
@Setter
public class ElementDTO {

    private Integer elementNumber;
    private String elementValue;

    @Getter
    private static final HashSet<ElementDTO> ELEMENT_DTOS = new HashSet<>();

    public ElementDTO(Integer elementNumber, String elementValue) {
        this.elementNumber = elementNumber;
        this.elementValue = elementValue;
        ELEMENT_DTOS.add(this);
    }

    public static ElementDTO findElementDTO(Integer elementNumber, String elementValue) {
        Optional<ElementDTO> optional = ELEMENT_DTOS
                .stream()
                .filter(el -> el.elementNumber.equals(elementNumber) && el.elementValue.equals(elementValue))
                .findFirst();
        return optional.orElseGet(() -> new ElementDTO(elementNumber, elementValue));
    }

    public boolean equals(ElementDTO el2) {
        return this.elementNumber.equals(el2.elementNumber)
                && this.elementValue.equals(el2.elementValue) ;
    }
}
