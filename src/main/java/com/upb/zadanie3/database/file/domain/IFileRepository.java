package com.upb.zadanie3.database.file.domain;

import com.upb.zadanie3.database.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IFileRepository extends JpaRepository<EncryptedFile, Integer> {

    EncryptedFile findEncryptedFileById(Integer id);

    List<EncryptedFile> findAll();

    List<EncryptedFile> getAllByRecipientUser(User recipientUser);

    EncryptedFile findEncryptedFilesByFileName(String fileName);

}
