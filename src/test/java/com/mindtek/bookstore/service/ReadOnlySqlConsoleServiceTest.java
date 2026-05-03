package com.mindtek.bookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.testng.annotations.Test;

public class ReadOnlySqlConsoleServiceTest {

    @Test
    public void validateAcceptsSelectAndStripsTrailingSemicolon() {
        assertThat(ReadOnlySqlConsoleService.validateAndNormalize("  SELECT 1 "))
                .isEqualTo("SELECT 1");
        assertThat(ReadOnlySqlConsoleService.validateAndNormalize("SELECT 1;")).isEqualTo("SELECT 1");
    }

    @Test
    public void validateRejectsInsertAndSemicolonChains() {
        assertThatThrownBy(() -> ReadOnlySqlConsoleService.validateAndNormalize("INSERT INTO books VALUES (1)"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ReadOnlySqlConsoleService.validateAndNormalize("SELECT 1; SELECT 2"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void semicolonInsideStringIgnored() {
        assertThat(ReadOnlySqlConsoleService.validateAndNormalize("SELECT ';' AS x")).isEqualTo("SELECT ';' AS x");
    }
}
