package com.neobank.core.branches;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for BranchService using JUnit 5 and Mockito.
 * Tests branch management operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BranchService Unit Tests")
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    private BranchService branchService;

    @BeforeEach
    void setUp() {
        branchService = new BranchService(branchRepository);
    }

    @Nested
    @DisplayName("Branch Retrieval")
    class BranchRetrievalTests {

        @Test
        @DisplayName("Should get branch by ID successfully")
        void shouldGetBranchByIdSuccessfully() {
            // Given
            UUID branchId = UUID.randomUUID();
            BranchEntity branch = new BranchEntity();
            branch.setId(branchId);
            branch.setCode("BR001");
            branch.setName("Test Branch");

            given(branchRepository.findById(branchId)).willReturn(Optional.of(branch));

            // When
            Optional<BranchEntity> result = branchService.getBranchById(branchId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("BR001");
        }

        @Test
        @DisplayName("Should return empty when branch not found by ID")
        void shouldReturnEmptyWhenBranchNotFoundById() {
            // Given
            UUID branchId = UUID.randomUUID();
            given(branchRepository.findById(branchId)).willReturn(Optional.empty());

            // When
            Optional<BranchEntity> result = branchService.getBranchById(branchId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should get branch by code successfully")
        void shouldGetBranchByCodeSuccessfully() {
            // Given
            String code = "BR001";
            BranchEntity branch = new BranchEntity();
            branch.setId(UUID.randomUUID());
            branch.setCode(code);
            branch.setName("Test Branch");

            given(branchRepository.findByCode(code)).willReturn(Optional.of(branch));

            // When
            Optional<BranchEntity> result = branchService.getBranchByCode(code);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo(code);
        }

        @Test
        @DisplayName("Should return empty when branch not found by code")
        void shouldReturnEmptyWhenBranchNotFoundByCode() {
            // Given
            String code = "NONEXISTENT";
            given(branchRepository.findByCode(code)).willReturn(Optional.empty());

            // When
            Optional<BranchEntity> result = branchService.getBranchByCode(code);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should get Head Office branch successfully")
        void shouldGetHeadOfficeBranchSuccessfully() {
            // Given
            BranchEntity headOffice = new BranchEntity();
            headOffice.setId(UUID.randomUUID());
            headOffice.setCode("HO-001");
            headOffice.setName("Head Office");

            given(branchRepository.findByCode("HO-001")).willReturn(Optional.of(headOffice));

            // When
            BranchEntity result = branchService.getHeadOffice();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("HO-001");
            assertThat(result.getName()).isEqualTo("Head Office");
        }

        @Test
        @DisplayName("Should throw exception when Head Office not found")
        void shouldThrowExceptionWhenHeadOfficeNotFound() {
            // Given
            given(branchRepository.findByCode("HO-001")).willReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> branchService.getHeadOffice())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Head Office branch not found");
        }
    }

    @Nested
    @DisplayName("Branch Creation")
    class BranchCreationTests {

        @Test
        @DisplayName("Should create branch successfully")
        void shouldCreateBranchSuccessfully() {
            // Given
            String code = "BR001";
            String name = "Test Branch";

            given(branchRepository.existsByCode(code)).willReturn(false);
            given(branchRepository.save(any(BranchEntity.class))).willReturn(new BranchEntity());

            // When
            BranchEntity result = branchService.createBranch(code, name);

            // Then
            assertThat(result).isNotNull();

            ArgumentCaptor<BranchEntity> branchCaptor = ArgumentCaptor.forClass(BranchEntity.class);
            verify(branchRepository).save(branchCaptor.capture());

            BranchEntity saved = branchCaptor.getValue();
            assertThat(saved.getCode()).isEqualTo(code);
            assertThat(saved.getName()).isEqualTo(name);
            assertThat(saved.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when branch code already exists")
        void shouldThrowExceptionWhenBranchCodeAlreadyExists() {
            // Given
            String code = "BR001";
            String name = "Test Branch";

            given(branchRepository.existsByCode(code)).willReturn(true);

            // When/Then
            assertThatThrownBy(() -> branchService.createBranch(code, name))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Branch code already exists");
        }

        @Test
        @DisplayName("Should get or create branch when it doesn't exist")
        void shouldGetOrCreateBranchWhenItDoesNotExist() {
            // Given
            String code = "BR001";
            String name = "Test Branch";

            given(branchRepository.findByCode(code)).willReturn(Optional.empty());
            given(branchRepository.existsByCode(code)).willReturn(false);
            given(branchRepository.save(any(BranchEntity.class))).willReturn(new BranchEntity());

            // When
            BranchEntity result = branchService.getOrCreateBranch(code, name);

            // Then
            assertThat(result).isNotNull();
            verify(branchRepository).save(any(BranchEntity.class));
        }

        @Test
        @DisplayName("Should get existing branch when it exists")
        void shouldGetExistingBranchWhenItExists() {
            // Given
            String code = "BR001";
            BranchEntity existingBranch = new BranchEntity();
            existingBranch.setId(UUID.randomUUID());
            existingBranch.setCode(code);
            existingBranch.setName("Existing Branch");

            given(branchRepository.findByCode(code)).willReturn(Optional.of(existingBranch));

            // When
            BranchEntity result = branchService.getOrCreateBranch(code, "New Branch");

            // Then
            assertThat(result).isEqualTo(existingBranch);
            verify(branchRepository, never()).save(any(BranchEntity.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle branch code with special characters")
        void shouldHandleBranchCodeWithSpecialCharacters() {
            // Given
            String code = "BR-001_A";
            String name = "Test Branch";

            given(branchRepository.existsByCode(code)).willReturn(false);
            given(branchRepository.save(any(BranchEntity.class))).willReturn(new BranchEntity());

            // When
            BranchEntity result = branchService.createBranch(code, name);

            // Then
            assertThat(result).isNotNull();

            ArgumentCaptor<BranchEntity> branchCaptor = ArgumentCaptor.forClass(BranchEntity.class);
            verify(branchRepository).save(branchCaptor.capture());

            assertThat(branchCaptor.getValue().getCode()).isEqualTo(code);
        }

        @Test
        @DisplayName("Should handle branch name with special characters")
        void shouldHandleBranchNameWithSpecialCharacters() {
            // Given
            String code = "BR001";
            String name = "Branch O'Brien-Smith";

            given(branchRepository.existsByCode(code)).willReturn(false);
            given(branchRepository.save(any(BranchEntity.class))).willReturn(new BranchEntity());

            // When
            BranchEntity result = branchService.createBranch(code, name);

            // Then
            assertThat(result).isNotNull();

            ArgumentCaptor<BranchEntity> branchCaptor = ArgumentCaptor.forClass(BranchEntity.class);
            verify(branchRepository).save(branchCaptor.capture());

            assertThat(branchCaptor.getValue().getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("Should handle branch name with unicode")
        void shouldHandleBranchNameWithUnicode() {
            // Given
            String code = "BR001";
            String name = "分行 测试";

            given(branchRepository.existsByCode(code)).willReturn(false);
            given(branchRepository.save(any(BranchEntity.class))).willReturn(new BranchEntity());

            // When
            BranchEntity result = branchService.createBranch(code, name);

            // Then
            assertThat(result).isNotNull();

            ArgumentCaptor<BranchEntity> branchCaptor = ArgumentCaptor.forClass(BranchEntity.class);
            verify(branchRepository).save(branchCaptor.capture());

            assertThat(branchCaptor.getValue().getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("Should handle long branch name")
        void shouldHandleLongBranchName() {
            // Given
            String code = "BR001";
            String name = "a".repeat(200);

            given(branchRepository.existsByCode(code)).willReturn(false);
            given(branchRepository.save(any(BranchEntity.class))).willReturn(new BranchEntity());

            // When
            BranchEntity result = branchService.createBranch(code, name);

            // Then
            assertThat(result).isNotNull();

            ArgumentCaptor<BranchEntity> branchCaptor = ArgumentCaptor.forClass(BranchEntity.class);
            verify(branchRepository).save(branchCaptor.capture());

            assertThat(branchCaptor.getValue().getName()).hasSize(200);
        }
    }
}
