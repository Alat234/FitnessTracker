package com.mycompany.fitnesstracker.Services;

import com.mycompany.fitnesstracker.Mappers.UserMapper;
import com.mycompany.fitnesstracker.Models.BaseException;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverGymCardDTO;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverGymDetailsDTO;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverTrainerCardDTO;
import com.mycompany.fitnesstracker.Models.Discover.DiscoverTrainerDetailsDTO;
import com.mycompany.fitnesstracker.Models.Enums.Role;
import com.mycompany.fitnesstracker.Models.Gym;
import com.mycompany.fitnesstracker.Models.User;
import com.mycompany.fitnesstracker.Models.UserDTO;
import com.mycompany.fitnesstracker.Models.UserInfo;
import com.mycompany.fitnesstracker.Repositories.GymRepository;
import com.mycompany.fitnesstracker.Repositories.GymTrainerRepository;
import com.mycompany.fitnesstracker.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DiscoverServiceTest {

    private GymRepository gymRepository;
    private GymTrainerRepository gymTrainerRepository;
    private UserRepository userRepository;
    private UserService userService;
    private UserMapper userMapper;
    private ConnectionService connectionService;
    private DiscoverService service;

    @BeforeEach
    void setUp() {
        gymRepository = mock(GymRepository.class);
        gymTrainerRepository = mock(GymTrainerRepository.class);
        userRepository = mock(UserRepository.class);
        userService = mock(UserService.class);
        userMapper = mock(UserMapper.class);
        connectionService = mock(ConnectionService.class);
        service = new DiscoverService(gymRepository, gymTrainerRepository,
                userRepository, userService, userMapper, connectionService);
    }

    private User trainer(long id, String first) {
        User u = User.builder().id(id).email("t@test.com").role(Role.ROLE_TRAINER).build();
        UserInfo info = UserInfo.builder().userIdentity(u).firstName(first).specialization("Strength").build();
        u.setUserInfo(info);
        return u;
    }

    private Gym gym(long id, String name, Boolean isPublic) {
        Gym g = new Gym();
        g.setId(id);
        g.setName(name);
        g.setCity("Kyiv");
        g.setIsPublic(isPublic);
        return g;
    }

    private User userWithInfo() {
        User user = User.builder().id(7L).email("u@test.com").build();
        UserInfo info = UserInfo.builder().userIdentity(user).build();
        user.setUserInfo(info);
        return user;
    }

    @Test
    void getGyms_returnsOnlyPublicGymsFromRepository() {
        when(gymRepository.findPublicGyms()).thenReturn(List.of(
                gym(1, "Alpha", true), gym(2, "Beta", null)));
        when(gymTrainerRepository.countByGym(any(Gym.class))).thenReturn(0L);

        List<DiscoverGymCardDTO> cards = service.getGyms();

        assertEquals(2, cards.size());
        assertEquals("Alpha", cards.get(0).name());
        verify(gymRepository).findPublicGyms();
    }

    @Test
    void selectGym_setsUserInfoGym_andSaves() {
        Gym g = gym(3, "Gamma", true);
        User user = userWithInfo();
        when(gymRepository.findById(3L)).thenReturn(Optional.of(g));
        when(userService.getUserByJWt()).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(UserDTO.builder().email("u@test.com").build());

        service.selectGym(3L);

        assertSame(g, user.getUserInfo().getGym());
        verify(userRepository).save(user);
    }

    @Test
    void leaveGym_clearsUserInfoGym() {
        User user = userWithInfo();
        user.getUserInfo().setGym(gym(3, "Gamma", true));
        when(userService.getUserByJWt()).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(UserDTO.builder().email("u@test.com").build());

        service.leaveGym();

        assertNull(user.getUserInfo().getGym());
        verify(userRepository).save(user);
    }

    @Test
    void selectGym_privateGym_isHiddenAs404_andNotSaved() {
        when(gymRepository.findById(9L)).thenReturn(Optional.of(gym(9, "Private", false)));

        assertThrows(BaseException.class, () -> service.selectGym(9L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void selectGym_missingGym_fails() {
        when(gymRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(BaseException.class, () -> service.selectGym(404L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void discoverDtos_doNotExposeOwner() {
        assertFalse(hasComponentContaining(DiscoverGymCardDTO.class, "owner"));
        assertFalse(hasComponentContaining(DiscoverGymDetailsDTO.class, "owner"));
    }

    @Test
    void getTrainers_returnsOnlyRoleTrainer() {
        when(userRepository.findAllByRole(Role.ROLE_TRAINER)).thenReturn(List.of(trainer(5, "Max")));
        when(gymTrainerRepository.findFirstByTrainer(any(User.class))).thenReturn(Optional.empty());

        List<DiscoverTrainerCardDTO> cards = service.getTrainers();

        assertEquals(1, cards.size());
        assertEquals("Max", cards.get(0).firstName());
        assertEquals("Strength", cards.get(0).specialization());
        verify(userRepository).findAllByRole(Role.ROLE_TRAINER);
    }

    @Test
    void getTrainer_nonTrainerId_isHiddenAs404() {
        User normal = User.builder().id(6L).email("n@test.com").role(Role.ROLE_USER).build();
        when(userRepository.findUserById(6L)).thenReturn(Optional.of(normal));

        assertThrows(BaseException.class, () -> service.getTrainer(6L));
    }

    @Test
    void connectTrainer_delegatesToConnectionService_byId() {
        User me = User.builder().id(1L).email("me@test.com").build();
        when(userService.getUserByJWt()).thenReturn(me);

        service.connectTrainer(5L);

        verify(connectionService).requestTrainerConnection(5L, "me@test.com");
    }

    @Test
    void discoverTrainerDtos_doNotExposeEmailOrPhone() {
        for (Class<?> dto : List.of(DiscoverTrainerCardDTO.class, DiscoverTrainerDetailsDTO.class)) {
            assertFalse(hasComponentContaining(dto, "email"));
            assertFalse(hasComponentContaining(dto, "phone"));
            assertFalse(hasComponentContaining(dto, "password"));
        }
    }

    private boolean hasComponentContaining(Class<?> recordClass, String needle) {
        return Arrays.stream(recordClass.getRecordComponents())
                .map(RecordComponent::getName)
                .anyMatch(n -> n.toLowerCase().contains(needle));
    }
}
