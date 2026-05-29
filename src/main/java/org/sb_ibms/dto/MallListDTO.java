package org.sb_ibms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MallListDTO {

    private Long id;
    private String name;
    private String areaName;
    private String address;
}
