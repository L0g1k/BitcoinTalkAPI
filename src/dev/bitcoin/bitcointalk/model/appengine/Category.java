package dev.bitcoin.bitcointalk.model.appengine;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;

@Entity
public class Category {

	@Id Long id;
	String categoryId;
	String title;
	@Load transient List<Ref<Board>> _boards;
	public Collection<Board> boards;
	public Category() {}
	
	public Category(String title) {
		this.title = title;
	}

	public void loadBoards() {
		this.boards = getBoards();
	}
	public Collection<Board> getBoards() { 
		if(_boards == null)
			return new ArrayList<Board>();
		
		return ofy().load().refs(_boards).values();
	}
	
	public void setBoards(List<Board> boards) { 
		ofy().save().entities(boards).now();
		List<Ref<Board>> boardRefs = new ArrayList<Ref<Board>>();
		for (Board board : boards) {
			boardRefs.add(Ref.create(board));
		}
		this._boards = boardRefs;
	}
}
