package com.mycompany.fitnesstracker.Repositories;

import com.mycompany.fitnesstracker.Models.Connection.UserConnection;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionStatus;
import com.mycompany.fitnesstracker.Models.Enums.ConnectionType;
import com.mycompany.fitnesstracker.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {

    /* Існуючий зв'язок цієї пари і типу — для перевірки дублікатів/повторного запрошення */
    Optional<UserConnection> findByOwnerAndViewerAndType(User owner, User viewer, ConnectionType type);

    /* Вихідні запрошення власника (усі статуси) */
    List<UserConnection> findAllByOwnerOrderByCreatedAtDesc(User owner);

    /* Вхідні запрошення для viewer-а у вибраних статусах (PENDING + ACCEPTED) */
    List<UserConnection> findAllByViewerAndStatusInOrderByCreatedAtDesc(User viewer, Collection<ConnectionStatus> statuses);

    /* Клієнти тренера: ACCEPTED TRAINER-зв'язки, де viewer = тренер */
    List<UserConnection> findAllByViewerAndTypeAndStatus(User viewer, ConnectionType type, ConnectionStatus status);

    /* Перевірка прийнятого доступу viewer-а до даних власника (для share-ендпоінтів наступної фази) */
    List<UserConnection> findAllByOwnerIdAndViewerAndStatus(Long ownerId, User viewer, ConnectionStatus status);
}
