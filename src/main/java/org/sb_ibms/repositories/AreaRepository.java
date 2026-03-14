package org.sb_ibms.repositories;

import org.sb_ibms.models.Area;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<Area, Long> {

    Area findByName(String name);
}
