package com.upb.zadanie3.database.user.domain;


import com.upb.zadanie3.database.comment.domain.Comment;
import com.upb.zadanie3.database.file.domain.EncryptedFile;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private String username;

    @Column(length = 2000)
    private String passwordHash;

    @Column(length = 5000)
    private String publicKey;

    @Column(length = 5000)
    private String privateKey;

    @OneToMany(mappedBy = "recipientUser")
    private List<EncryptedFile> encryptedFile;

    @OneToMany(mappedBy = "userCreator")
    private List<Comment> comments;

    public User() {
    }

    public User(String username, String passwordHash, String publicKey, String privateKey) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<EncryptedFile> getEncryptedFile() {
        return encryptedFile;
    }

    public void setEncryptedFile(List<EncryptedFile> encryptedFile) {
        this.encryptedFile = encryptedFile;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", passwordHash='" + passwordHash + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", privateKey='" + privateKey + '\'' +
                '}';
    }
}
