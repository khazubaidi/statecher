package io.github.khazubaidi.repositories;

import io.github.khazubaidi.markers.Statechable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@Deprecated
@NoRepositoryBean
public interface StatecherRepository<T extends Statechable, ID>
        extends JpaRepository<T, ID> {
}