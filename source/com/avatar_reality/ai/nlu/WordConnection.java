package com.avatar_reality.ai.nlu;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="word_connection")
public class WordConnection
{
    @Id
    @GeneratedValue
    @Column(name="id")
	String id;
    @Column(name="id1")
	String id1;
    @Column(name="id2")
	String id2;
    @Column(name="relation")
	String relation;
    @Column(name="occurences")
	int occurences = 1;
    
    public String toString()
    {
    	return relation+"("+id1+","+id2+")";
    }
}
