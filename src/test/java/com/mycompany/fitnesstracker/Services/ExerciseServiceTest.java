package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.ExerciseMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.Exercise;
import com.mycompany.fitnesstracker.Models.WorkoutEntities.ExerciseDTO;
import com.mycompany.fitnesstracker.Repositories.ExerciseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExerciseServiceTest {

    private ExerciseRepository exerciseRepository;
    private ExerciseMapper exerciseMapper;
    private FileStorageService fileStorageService;
    private UserService userService;
    private ExerciseService service;

    @BeforeEach
    void setUp() {
        exerciseRepository = mock(ExerciseRepository.class);
        exerciseMapper = mock(ExerciseMapper.class);
        fileStorageService = mock(FileStorageService.class);
        userService = mock(UserService.class);
        service = new ExerciseService(exerciseRepository, exerciseMapper, fileStorageService, userService);
    }

    private void mockAdmin() {
        when(userService.getUserByJWt())
                .thenReturn(User.builder().id(1L).email("admin@test.com").role(Role.ROLE_ADMIN).build());
    }

    private void mockUser() {
        when(userService.getUserByJWt())
                .thenReturn(User.builder().id(2L).email("user@test.com").role(Role.ROLE_USER).build());
    }

    /* ── Admin can write ── */

    @Test
    void createExercise_admin_savesAndReturnsDTO() {
        mockAdmin();
        Exercise saved = Exercise.builder().name("Bench").build();
        when(exerciseRepository.save(any(Exercise.class))).thenReturn(saved);
        when(exerciseMapper.toDTO(saved)).thenReturn(ExerciseDTO.builder().name("Bench").build());

        ExerciseDTO result = service.createExercise(ExerciseDTO.builder().name("Bench").build(), null);

        assertEquals("Bench", result.getName());
        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    void updateExercise_admin_savesAndReturnsDTO() {
        mockAdmin();
        Exercise existing = Exercise.builder().id(5L).name("Old").build();
        when(exerciseRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(exerciseRepository.save(existing)).thenReturn(existing);
        when(exerciseMapper.toDTO(existing)).thenReturn(ExerciseDTO.builder().id(5L).name("Updated").build());

        ExerciseDTO result = service.updateExercise(ExerciseDTO.builder().id(5L).name("Updated").build(), null);

        assertEquals("Updated", result.getName());
        verify(exerciseRepository).save(existing);
    }

    @Test
    void deleteExercise_admin_deletes() {
        mockAdmin();

        service.deleteExerciseById(9L);

        verify(exerciseRepository).deleteById(9L);
    }

    /* ── Non-admin cannot write (FORBIDDEN, not 500) ── */

    @Test
    void createExercise_nonAdmin_throwsForbidden_andDoesNotSave() {
        mockUser();

        BaseException ex = assertThrows(BaseException.class,
                () -> service.createExercise(ExerciseDTO.builder().name("Bench").build(), null));
        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, ex.getStatus());
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void updateExercise_nonAdmin_throwsForbidden_andDoesNotTouchRepo() {
        mockUser();

        BaseException ex = assertThrows(BaseException.class,
                () -> service.updateExercise(ExerciseDTO.builder().id(5L).name("X").build(), null));
        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, ex.getStatus());
        verify(exerciseRepository, never()).findById(any());
        verify(exerciseRepository, never()).save(any());
    }

    @Test
    void deleteExercise_nonAdmin_throwsForbidden_andDoesNotDelete() {
        mockUser();

        BaseException ex = assertThrows(BaseException.class, () -> service.deleteExerciseById(9L));
        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, ex.getStatus());
        verify(exerciseRepository, never()).deleteById(any());
    }

    /* ── Read stays open ── */

    @Test
    void getAllExercises_doesNotRequireAdmin() {
        when(exerciseRepository.findAll()).thenReturn(List.of(Exercise.builder().name("Squat").build()));
        when(exerciseMapper.toDTO(any(Exercise.class))).thenReturn(ExerciseDTO.builder().name("Squat").build());

        List<ExerciseDTO> result = service.getAllExercises();

        assertEquals(1, result.size());
        verify(userService, never()).getUserByJWt();
    }
}
