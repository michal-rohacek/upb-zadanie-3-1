package com.upb.zadanie3.database.comment.domain;

import com.upb.zadanie3.database.file.domain.EncryptedFile;
import com.upb.zadanie3.database.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

}
