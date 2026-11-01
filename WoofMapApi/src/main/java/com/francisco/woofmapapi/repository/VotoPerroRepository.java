package com.francisco.woofmapapi.repository;

import com.francisco.woofmapapi.entity.VotoPerro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VotoPerroRepository extends JpaRepository<VotoPerro, Long> {

    boolean existsByPerroIdAndUsuarioId(Long perroId, Long usuarioId);
}
