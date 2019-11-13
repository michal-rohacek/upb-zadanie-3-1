package com.upb.zadanie3.database.file.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IFileRepository extends JpaRepository<EncryptedFile, Integer> {
}
