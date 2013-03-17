package dev.bitcoin.bitcointalk.model.appengine;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Transient;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;

@Entity
public class TopicPage {
	
	@Id transient Long id;
	
	@Transient
	public String pageId;
	
	@Load transient List<Ref<Post>> _posts;
	public Collection<Post> posts;

	public void loadPosts() {
		posts = getPosts();
	}
	
	public Collection<Post> getPosts() { 
		if(_posts == null)
			return new ArrayList<Post>();
		
		return ofy().load().refs(_posts).values();
	}
	
	public void setPosts(Collection<Post> appenginePosts) {
		ofy().save().entities(appenginePosts).now();
		List<Ref<Post>> boardRefs = new ArrayList<Ref<Post>>();
		for (Post post : appenginePosts) {
			boardRefs.add(Ref.create(post));
		}
		this._posts = boardRefs;
	}

	public void save() {
		ofy().save().entities(this);
	}
	
	public void delete() {
		ofy().delete().entities(_posts);
		ofy().delete().entity(this);
	}
	
}

