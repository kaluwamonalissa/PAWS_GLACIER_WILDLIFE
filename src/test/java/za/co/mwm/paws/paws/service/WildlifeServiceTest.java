package za.co.mwm.paws.paws.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import za.co.mwm.paws.paws.dto.WildlifeCategoryResponse;

class WildlifeServiceTest {

    private WildlifeService wildlifeService;

    @BeforeEach
    void setUp() {
        wildlifeService = new WildlifeService();
    }

    @Test
    void givenNoInput_whenGetEndangeredCategories_shouldReturnSixCategories() {
        final List<WildlifeCategoryResponse> categories =
                wildlifeService.getEndangeredCategories();
        assertThat(categories).hasSize(6);
    }

    @Test
    void givenNoInput_whenGetEndangeredCategories_shouldIncludeHighRiskCategory() {
        final List<WildlifeCategoryResponse> categories =
                wildlifeService.getEndangeredCategories();
        assertThat(categories).anyMatch(c -> "HIGH".equals(c.getRiskLevel()));
    }

    @Test
    void givenNoInput_whenGetEndangeredCategories_shouldIncludeMegaHerbivoresCategory() {
        final List<WildlifeCategoryResponse> categories =
                wildlifeService.getEndangeredCategories();
        assertThat(categories)
                .anyMatch(c -> "Mega-Herbivores".equals(c.getCategory()));
    }

    @Test
    void givenNoInput_whenGetEndangeredCategories_shouldIncludeElephantInMegaHerbivores() {
        final List<WildlifeCategoryResponse> categories =
                wildlifeService.getEndangeredCategories();
        final WildlifeCategoryResponse megaHerbivores =
                categories.stream()
                        .filter(c -> "Mega-Herbivores".equals(c.getCategory()))
                        .findFirst()
                        .orElseThrow();
        assertThat(megaHerbivores.getSpecies()).contains("African Elephant");
    }

    @Test
    void givenNoInput_whenGetEndangeredCategories_shouldHaveNonEmptySpeciesLists() {
        final List<WildlifeCategoryResponse> categories =
                wildlifeService.getEndangeredCategories();
        assertThat(categories).allMatch(c -> !c.getSpecies().isEmpty());
    }
}

