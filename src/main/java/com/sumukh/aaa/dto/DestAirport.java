package com.sumukh.aaa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestAirport {
    private String iata;   // e.g. "DEL"
    private String name;   // e.g. "Indira Gandhi International Airport (New Delhi)"
}