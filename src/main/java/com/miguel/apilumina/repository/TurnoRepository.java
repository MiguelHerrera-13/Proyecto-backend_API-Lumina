package com.miguel.apilumina.repository;

import com.miguel.apilumina.entity.TurnoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurnoRepository extends JpaRepository<TurnoEntity, Long> {
}
