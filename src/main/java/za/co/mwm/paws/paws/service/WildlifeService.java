package za.co.mwm.paws.paws.service;

import java.util.List;
import org.springframework.stereotype.Service;
import za.co.mwm.paws.paws.dto.WildlifeCategoryResponse;

@Service
public class WildlifeService {

    private static final String RISK_HIGH = "HIGH";
    private static final String RISK_MEDIUM = "MEDIUM";
    private static final String RISK_LOW = "LOW";

    public List<WildlifeCategoryResponse> getEndangeredCategories() {
        return List.of(
                WildlifeCategoryResponse.builder()
                        .category("Mega-Herbivores")
                        .species(List.of("African Elephant", "White Rhinoceros"))
                        .riskLevel(RISK_HIGH)
                        .build(),
                WildlifeCategoryResponse.builder()
                        .category("Carnivores at Risk")
                        .species(List.of("African Wild Dog", "Cheetah", "Leopard"))
                        .riskLevel(RISK_HIGH)
                        .build(),
                WildlifeCategoryResponse.builder()
                        .category("Pangolins")
                        .species(List.of("Ground Pangolin (Temminck's)"))
                        .riskLevel(RISK_HIGH)
                        .build(),
                WildlifeCategoryResponse.builder()
                        .category("Rare Antelopes")
                        .species(List.of("Sable Antelope", "Roan Antelope"))
                        .riskLevel(RISK_MEDIUM)
                        .build(),
                WildlifeCategoryResponse.builder()
                        .category("Rare Birds")
                        .species(
                                List.of(
                                        "Lappet-faced Vulture",
                                        "Martial Eagle",
                                        "Saddle-billed Stork",
                                        "Kori Bustard"))
                        .riskLevel(RISK_MEDIUM)
                        .build(),
                WildlifeCategoryResponse.builder()
                        .category("Reptiles")
                        .species(List.of("African Rock Python", "Nile Crocodile"))
                        .riskLevel(RISK_LOW)
                        .build());
    }
}

