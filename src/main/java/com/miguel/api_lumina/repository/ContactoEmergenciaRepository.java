package com.miguel.api_lumina.repository;

import com.miguel.api_lumina.entity.ContactoEmergenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactoEmergenciaRepository extends JpaRepository<ContactoEmergenciaEntity, Long> {

    List<ContactoEmergenciaEntity> findByPacienteIdPacienteOrderByEsPrincipalDescIdContactoEmergenciaAsc(Long idPaciente);

    Optional<ContactoEmergenciaEntity> findByIdContactoEmergenciaAndPacienteIdPaciente(Long idContactoEmergencia, Long idPaciente);

    Optional<ContactoEmergenciaEntity> findFirstByPacienteIdPacienteAndEsPrincipalTrue(Long idPaciente);

    Optional<ContactoEmergenciaEntity> findFirstByPacienteIdPacienteOrderByEsPrincipalDescIdContactoEmergenciaAsc(Long idPaciente);

    long countByPacienteIdPaciente(Long idPaciente);

    boolean existsByPacienteIdPacienteAndTelefono(Long idPaciente, String telefono);

    @Modifying
    @Query("UPDATE ContactoEmergenciaEntity c SET c.esPrincipal = false WHERE c.paciente.idPaciente = :idPaciente")
    void resetAllPrincipalesByPacienteId(@Param("idPaciente") Long idPaciente);
}
