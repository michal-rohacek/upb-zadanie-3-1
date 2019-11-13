package com.upb.zadanie3.database.comment.domain;

import com.upb.zadanie3.database.file.domain.EncryptedFile;
import com.upb.zadanie3.database.user.domain.User;

import javax.persistence.*;

@Entity
@Table(name = "COMMENT")
public class Comment {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    @Column(length = 2000)
    private String comment;

    @ManyToOne
    private EncryptedFile encryptedFile;

    @ManyToOne
    private User userCreator;


    public Comment() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public EncryptedFile getEncryptedFile() {
        return encryptedFile;
    }

    public void setEncryptedFile(EncryptedFile encryptedFile) {
        this.encryptedFile = encryptedFile;
    }

    public User getUserCreator() {
        return userCreator;
    }

    public void setUserCreator(User userCreator) {
        this.userCreator = userCreator;
    }
}
