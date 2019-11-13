package com.upb.zadanie3.database.file.domain;

import com.upb.zadanie3.database.comment.domain.Comment;
import com.upb.zadanie3.database.user.domain.User;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "EncryptedFile")
public class EncryptedFile {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private String fileName;

    @OneToMany(mappedBy="encryptedFile")
    private List<Comment> coments;

    @ManyToOne
    private User recipientUser;

    public User getRecipientUser() {
        return recipientUser;
    }

    public void setRecipientUser(User recipientUser) {
        this.recipientUser = recipientUser;
    }

    public EncryptedFile() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<Comment> getComents() {
        return coments;
    }

    public void setComents(List<Comment> coments) {
        this.coments = coments;
    }
}
