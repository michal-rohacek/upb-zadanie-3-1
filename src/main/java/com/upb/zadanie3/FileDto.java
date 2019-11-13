package com.upb.zadanie3;

import com.upb.zadanie3.database.comment.domain.Comment;

import java.util.ArrayList;
import java.util.List;

public class FileDto {
    public Integer id;
    public String fileLink;
    public List<Comment> comments = new ArrayList<>();
}
