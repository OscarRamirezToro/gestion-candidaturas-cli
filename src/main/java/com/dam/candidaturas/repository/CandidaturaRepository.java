package com.dam.candidaturas.repository;

import com.dam.candidaturas.model.Candidatura;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CandidaturaRepository {
    List<Candidatura> findAll() throws IOException;
    Optional<Candidatura> findById(int id) throws IOException;
    void save(Candidatura candidatura) throws IOException;
    boolean deleteById(int id) throws IOException;
    int getNextId() throws IOException;
}