package com.cms.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Document(collection = "announcements")
public class Announcement extends BaseDocument{

    @Id
    private String id;

    @Field("title")
    private String title;

    @Field("content")
    private String content;

    @DBRef(lazy = true)
    @Field("bulletin")
    private Bulletin bulletin;

}
