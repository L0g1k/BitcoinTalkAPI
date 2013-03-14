package dev.bitcoin.bitcointalk.model.appengine;

import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Unindex;

@Entity
public class Post {

	public Post() {} 
	
	public Post(String poster, String content) {
		this.poster = poster;
		this.content = content;
	}
	
	@Id Long id;
	String poster;
	String content;
	@Unindex Date dateLastUpdated;
}
