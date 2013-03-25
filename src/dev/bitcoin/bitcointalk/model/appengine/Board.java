package dev.bitcoin.bitcointalk.model.appengine;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnSave;
import com.googlecode.objectify.annotation.Unindex;

import dev.bitcoin.bitcointalk.model.CategoryBoard;

@Entity
public class Board implements HasFreshness {

	@Id transient Long id;
	@Index public String boardId;
	@Unindex public String title;
	@Unindex boolean isBeingUpdated;
	@Unindex Date dateLastUpdated;
	static int freshness = 1000 * 60 * 5; // 5 minutes

	@Load
	transient List<Ref<Topic>> _topics;
	public Collection<Topic> topics;
	
	@Load
	transient List<Ref<Board>> _childBoards;
	public Collection<Board> childBoards;
	
	public Board() {
	}

	public Board(String title, String boardId) {
		this.title = title;
		this.boardId = boardId;
	}

	public void loadTopics() {
		this.topics = getTopics();
	}
	
	public void loadChildren() {
		this.childBoards = getChildren();
	}
	
	public Collection<Topic> getTopics() { 
		if (_topics == null) 
			return new ArrayList<Topic>(); 
		
		return ofy().load().refs(_topics).values();
	}
	
	public Collection<Board> getChildren() { 
		if (_childBoards == null) 
			return new ArrayList<Board>(); 
		
		return ofy().load().refs(_childBoards).values();
	}
	
	@OnSave
	void saveDate() {
		if(_topics != null)
			dateLastUpdated = new Date();
	}

	@Override
	public Date getLastUpated() {
		return dateLastUpdated;
	}

	@Override
	public int getFreshnessTime() {
		return freshness;
	}

	public void setTopics(List<Topic> topics) {
		ofy().save().entities(topics).now();
		List<Ref<Topic>> boardRefs = new ArrayList<Ref<Topic>>();
		for (Topic topic : topics) {
			boardRefs.add(Ref.create(topic));
		}
		this._topics = boardRefs;
	}
	
	public void setChildBoards(List<Board> childBoardsAppEngine) {
		ofy().save().entities(childBoardsAppEngine).now();
		List<Ref<Board>> boardRefs = new ArrayList<Ref<Board>>();
		for (Board childBoard : childBoardsAppEngine) {
			boardRefs.add(Ref.create(childBoard));
		}
		this._childBoards = boardRefs;
	}
	
	@Override
	public String getId() {
		return boardId;
	}

	@Override
	public boolean isBeingUpdated() {
		return isBeingUpdated;
	}

	@Override
	public void setBeingUpdated(boolean updated) {
		this.isBeingUpdated = updated;
	}

	public void save() {
		ofy().save().entities(this).now();
	}

	public boolean isUnripe() {
		return _topics == null || _topics.size() == 0;
	}

	
}
